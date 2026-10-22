# Signature and issuer

[Version française](README.fr.md) · [Course](../README.md)

A readable JWT is still untrusted input. This chapter uses a random RSA key and a loopback JWKS endpoint; the API validates a signature before its role rules can matter. The test issuer uses an `.invalid` domain and is compared as a claim, without contacting that domain.

`wrongIssuerWithSameSigningKeyIsRejected` changes only issuer on a token signed by the trusted key. `untrustedSignatureIsRejected` keeps the expected issuer and roles but signs with another key under the same key ID. Both requests return 401. A token carrying `reader` cannot repair either validation failure.

The JWK URL is a trusted application setting, not a URL taken from incoming token data. Inspect the sample's decoder configuration before reasoning about an issuer mismatch. The local key server establishes a validation boundary; it does not implement OIDC discovery or a key-rotation service.

## Replay

From the lab checkout at the course revision, after locally installing the toolkit:

```bash
mvn -f examples/resource-server/pom.xml '-Dtest=JwtBoundaryTest#wrongIssuerWithSameSigningKeyIsRejected+untrustedSignatureIsRejected' test
```

## Worked exercises

1. Does decoding the payload prove the signature?

<details>
<summary>Answer</summary>

No. The request must pass signature validation with a trusted public key; reading claims is only inspection.

</details>

2. Why keep the signing key unchanged in the issuer test?

<details>
<summary>Answer</summary>

To avoid explaining the 401 by a different key. This test isolates the issuer mismatch.

</details>

3. Does this prove JWKS rotation?

<details>
<summary>Answer</summary>

No. The published key set stays fixed. Rotation and unknown-key recovery need separate tests.

</details>

## Sources and evidence

- [Tests at this revision](https://github.com/jihedbfr-art/spring-keycloak-toolkit/blob/dc2e74e12dafbc337ea94848694fec1bbf1631b6/examples/resource-server/src/test/java/lab/JwtBoundaryTest.java).
- [Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).

---

<div align="center">
  <img src="../../../../assets/brand/jihedailabs-logo.svg" alt="JihedAiLabs" width="120">
  <br>
  <sub>A <a href="https://github.com/jihedbfr-art"><b>JihedAiLabs</b></a> project</sub>
</div>
