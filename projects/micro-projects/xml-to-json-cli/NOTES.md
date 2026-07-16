# xml-to-json-cli

Convertisseur XML simple vers JSON.

- **Stack** : Python 3 stdlib (`xml.etree.ElementTree`, `json`, `argparse`). Aucune dépendance.
- **Lancer** : `python xml_to_json.py data.xml -o data.json`
- **Tester rapidement** : `<a>hi</a>` doit donner `{"a": "hi"}`.
- **Fichier clé** : `xml_to_json.py`, fonction récursive `element_to_dict`.
- **Points d'attention** : attributs -> clé `@attributes` ; texte mêlé à attributs/enfants -> clé
  `#text` ; balises enfants répétées au même niveau -> liste JSON automatiquement.
