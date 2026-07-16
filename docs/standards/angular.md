# Standard — Angular

Stack : **Angular 17.3**, **TypeScript 5.4**, build via Angular CLI, servi derrière nginx (prod) avec `proxy.conf.json` (dev).

## Règles
- **Standalone components** (Angular 17) par défaut, pas de NgModule sauf raison précise.
- **Signals** pour l'état local de composant ; services pour l'état partagé.
- Appels HTTP dans des **services** typés, jamais directement dans un composant.
- **JWT** ajouté par un `HttpInterceptor` (`Authorization: Bearer`), pas à la main par requête.
- Typage strict : pas de `any` non justifié ; interfaces pour les réponses API.
- Un dossier `feature/` par domaine fonctionnel (routing lazy).

## Commandes
```bash
npm install
npm start          # dev server + proxy
npm run build      # build prod
npm test           # tests unitaires
```

## Ce qu'on évite
- Logique métier dans le template.
- Souscriptions non désabonnées → préférer `async` pipe ou `takeUntilDestroyed`.
- URLs d'API en dur dans les composants → `environment.ts` / proxy.
