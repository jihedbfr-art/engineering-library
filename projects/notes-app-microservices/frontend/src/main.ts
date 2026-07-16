import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withInterceptors, HttpInterceptorFn } from '@angular/common/http';
import { APP_INITIALIZER } from '@angular/core';
import { AppComponent } from './app/app.component';
import { KeycloakService } from './app/services/keycloak.service';
import { from, switchMap } from 'rxjs';

const authInterceptor: HttpInterceptorFn = (req, next) => {
  const keycloak = KeycloakService.instance;
  if (!keycloak || !keycloak.token) {
    return next(req);
  }
  return from(keycloak.updateTokenIfNeeded()).pipe(
    switchMap(() => next(req.clone({
      setHeaders: { Authorization: `Bearer ${keycloak.token}` }
    })))
  );
};

function initKeycloak(keycloak: KeycloakService) {
  return () => keycloak.init();
}

bootstrapApplication(AppComponent, {
  providers: [
    provideHttpClient(withInterceptors([authInterceptor])),
    KeycloakService,
    {
      provide: APP_INITIALIZER,
      useFactory: initKeycloak,
      deps: [KeycloakService],
      multi: true
    }
  ]
}).catch(err => console.error(err));
