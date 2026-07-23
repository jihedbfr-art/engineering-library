# Keycloak — Advanced Patterns

[backend/microservices](../microservices/spring-microservices.md) covers basic OIDC resource-server integration. This is what building a **real production identity layer** on Keycloak actually involves — the parts that only show up once you need custom login behavior, not just "protect this endpoint."

## Custom SPI authenticators — extending the login flow itself

Keycloak's authentication flow is a pipeline of **authenticators**; you can insert your own Java SPI implementation as a step — e.g. a custom risk check, an external identity verification, or integration with a legacy system that still needs to validate something during login.

```java
public class CustomBruteForceAuthenticator implements Authenticator {
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        UserModel user = context.getUser();
        if (isRateLimited(user)) {
            context.getEvent().error(Errors.USER_TEMPORARILY_DISABLED);
            Response challenge = context.form()
                .setError("tooManyAttempts")
                .createErrorPage(Response.Status.TOO_MANY_REQUESTS);
            context.failure(AuthenticationFlowError.ACCESS_DENIED, challenge);
            return;
        }
        context.success();     // proceed to the next authenticator in the flow
    }
    // action(), requiresUser(), configuredFor(), setRequiredActions() — the rest of the SPI contract
}
```
```java
public class CustomAuthenticatorFactory implements AuthenticatorFactory {
    public static final String PROVIDER_ID = "custom-bruteforce-authenticator";
    public Authenticator create(KeycloakSession session) { return new CustomBruteForceAuthenticator(); }
    public String getId() { return PROVIDER_ID; }
    // ...
}
```
Packaged as a JAR dropped into `providers/`, registered via `META-INF/services`, then wired into a custom **authentication flow** in the Keycloak admin console — you compose it alongside Keycloak's built-in steps (username/password, OTP) rather than replacing the whole login system.

## Anti-bruteforce — built-in vs custom

Keycloak ships basic brute-force detection (failed attempt count → temporary lockout) — fine as a baseline, but it's per-realm and blunt. A senior-level setup usually adds:
- **Custom SPI logic** (above) for smarter detection — IP-based rate limiting, device fingerprint correlation, integration with an external fraud signal ([SIM-Swap/Number-Verification APIs](../../telecom/camara-network-apis.md) are a genuinely relevant signal here if your users are mobile subscribers).
- **Distinguishing "wrong password" from "account doesn't exist"** in responses — never leak which one it was; that distinction is exactly what credential-stuffing tools use to build valid username lists ([OWASP A07](../../devsecops/security/owasp-top10.md)).

## Passwordless authentication flows

```
Option A: WebAuthn/FIDO2 (passkeys)
  Registration: browser generates a key pair, public key stored on the user's Keycloak account
  Login: browser signs a challenge with the private key — phishing-resistant by construction,
         the private key never leaves the device and can't be typed into a fake login page

Option B: Magic link / OTP-based
  User requests login → Keycloak sends a one-time link/code → custom SPI validates it →
  session issued without a password ever existing
```
WebAuthn is the stronger option where device support allows it — it eliminates the entire class of phishing and credential-stuffing attacks that plague password-based login, because there's no shared secret to steal or trick someone into typing.

## Custom themes — FreeMarker, and why the login page matters more than it looks

```
themes/custom-theme/login/
├── theme.properties
├── login.ftl              # the actual login form template (FreeMarker)
├── template.ftl            # shared page shell
└── resources/css/…
```
```html
<!-- login.ftl fragment -->
<form id="kc-form-login" action="${url.loginAction}" method="post">
    <input type="text" id="username" name="username" autofocus>
    <input type="password" id="password" name="password">
    <#if auth.showTryAnotherWayLink()>
        <a href="${url.loginAction}">Try another way</a>   <!-- e.g. passwordless fallback -->
    </#if>
</form>
```
Beyond branding, the login theme is where you surface custom flow steps (like the "try another way" link exposing your passwordless option) and where localized, non-leaky error messages live. This is genuinely part of the security posture, not just design — a login page that reveals too much in its error states undoes backend hardening.

## Multi-realm / multi-client architecture — the decision that's hard to undo later

```
Realm: "notesapp"
├── Client: "web-frontend"     (public, PKCE)
├── Client: "backend-service"  (confidential, service accounts)
└── Client: "mobile-app"       (public, PKCE, different redirect URIs)
```
- **One realm per genuinely separate tenant/product** — realms don't share users or roles by default; that isolation is the point.
- **One client per application type**, not one client shared across everything — different redirect URI rules, different token lifetimes, and a compromised mobile client's credentials shouldn't grant backend-service-level access.
- Get this topology wrong early in a multi-service platform and retrofitting it later means migrating live users across realms — plan the realm/client boundary at design time, not after three services already assume a single shared client.

## Token lifetime tuning — the tradeoff nobody sets thoughtfully by default

| Token | Too long | Too short |
|---|---|---|
| Access token | Stolen token useful for longer | Constant refresh traffic, more failure surface |
| Refresh token | Larger stolen-session blast radius | Users logged out annoyingly often |
| SSO session | Convenient, but a compromised browser session lives longer | Users re-authenticate constantly |

There's no universal right answer — a banking app and an internal admin tool should not share the same defaults. Set these deliberately per client based on what the client actually is, not on whatever Keycloak ships with out of the box.
