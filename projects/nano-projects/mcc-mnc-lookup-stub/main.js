const OPERATORS = {
  "605-01": "Tunisie Telecom",
  "605-02": "Ooredoo Tunisie",
  "605-03": "Orange Tunisie",
  "208-01": "Orange France",
  "234-30": "EE (UK)",
};

const code = process.argv[2] || "605-02";
console.log(OPERATORS[code] || "Operateur inconnu dans ce mini-annuaire");
