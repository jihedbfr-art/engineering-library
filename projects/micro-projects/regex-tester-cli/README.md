# regex-tester-cli

Teste un pattern regex (syntaxe Python `re`) contre un texte ou un fichier, et affiche
tous les matches trouvés avec leurs positions et groupes capturés.

## Lancer

```bash
python regex_tester.py "\d+" -t "j'ai 3 chats et 12 chiens"
python regex_tester.py "(\w+)@(\w+\.\w+)" -f contacts.txt
```

## Exemple d'usage

```bash
$ python regex_tester.py "(\w+)@(\w+\.\w+)" -t "contact: jihed@example.com ou test@mail.fr"
2 match(es) trouvé(s) :

Match 1: 'jihed@example.com' [10:28]
  Groupe 1: 'jihed'
  Groupe 2: 'example.com'
Match 2: 'test@mail.fr' [32:44]
  Groupe 1: 'test'
  Groupe 2: 'mail.fr'
```

Options : `-i/--ignore-case`, `-m/--multiline`, source `-t/--text` ou `-f/--file`.
