# Contact Manager CRUD

## But
CRUD de contacts (nom, email, téléphone, notes), recherche par nom en temps réel.

## Mini-architecture
- `index.html` : formulaire (ajout + édition via le même formulaire) + liste.
- `style.css` : cartes de contact, formulaire.
- `app.js` : tableau `contacts` persisté dans `localStorage`
  (clé `contact-manager-crud:contacts`). Le formulaire bascule entre mode "Ajouter"
  et mode "Enregistrer" via un champ caché `editId`.

## Lancer
Ouvrir `index.html` dans un navigateur, ou `npx serve .`.

## Choix technique
Vanilla JS/HTML/CSS + `localStorage` comme imposé — pas de backend. Un seul
formulaire sert à la fois pour la création et l'édition (pattern courant pour
limiter le code dans un mini-projet).
