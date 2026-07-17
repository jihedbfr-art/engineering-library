# invoice-generator-app

Genere une facture texte formatee (lignes, TVA 19%, total) a partir d'un client et d'une liste
d'articles en JSON.

## Lancer
```bash
python invoice_generator.py "Acme Corp" '[{"name":"Conseil","qty":5,"price":100},{"name":"Licence","qty":1,"price":500}]'
```
