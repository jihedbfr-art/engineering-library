import { Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';

@Injectable()
export class KeycloakService {

  static instance: KeycloakService | null = null;

  private keycloak = new Keycloak({
    url: 'http://localhost:8090',
    realm: 'notesapp',
    clientId: 'notesapp-frontend'
  });

  constructor() {
    KeycloakService.instance = this;
  }

  async init(): Promise<void> {
    await this.keycloak.init({
      onLoad: 'login-required',
      pkceMethod: 'S256',
      checkLoginIframe: false
    });
  }

  get token(): string | undefined {
    return this.keycloak.token;
  }

  get username(): string {
    return (this.keycloak.tokenParsed?.['preferred_username'] as string) ?? '';
  }

  get fullName(): string {
    const parsed = this.keycloak.tokenParsed;
    const name = `${parsed?.['given_name'] ?? ''} ${parsed?.['family_name'] ?? ''}`.trim();
    return name || this.username;
  }

  async updateTokenIfNeeded(): Promise<void> {
    try {
      await this.keycloak.updateToken(30);
    } catch {
      await this.keycloak.login();
    }
  }

  logout(): void {
    this.keycloak.logout({ redirectUri: window.location.origin });
  }
}
