# Engineering Library

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
![Knowledge domains](https://img.shields.io/badge/knowledge%20domains-8-informational)
![Stack](https://img.shields.io/badge/stack-Java%20%2F%20Spring%20%2F%20Angular-brightgreen)
[![Build](https://github.com/jihedbfr-art/engineering-library/actions/workflows/build.yml/badge.svg)](https://github.com/jihedbfr-art/engineering-library/actions/workflows/build.yml)
[![CodeQL](https://github.com/jihedbfr-art/engineering-library/actions/workflows/codeql.yml/badge.svg)](https://github.com/jihedbfr-art/engineering-library/actions/workflows/codeql.yml)

Java/Spring engineering notes built on top of 10+ years in telecom BSS and network provisioning,
before I moved into microservices architecture. This isn't a general-purpose dev encyclopedia —
it used to try to be one and it was worse for it. What's left is what I can actually back up in
a conversation: architecture decisions, failures, debugging recipes, and a telecom domain guide
built from things like number portability and a Nokia-to-Huawei core migration, not from reading
Wikipedia.

## What's in here

```
engineering-library/
├── projects/                  nano → micro → mini → standard → macro Java/Spring/Angular projects
├── knowledge/
│   ├── telecom/                BSS/OSS, provisioning, 5G core, number portability, roaming
│   ├── backend/                Java/Spring, Node.js, microservices, APIs
│   ├── architecture-library/   architecture patterns and ADRs
│   ├── engineering-failures/   postmortems and things that broke, written up honestly
│   ├── debugging-recipes/      how I actually tracked down specific bugs
│   ├── database-engineering/   Postgres/Oracle, schema and query design
│   ├── security-patterns/      auth, SSO, API security patterns
│   └── devsecops/              CI/CD, pipeline security
└── docs/standards/             Java/Spring, Angular, SQL, security conventions I hold projects to
```

Each `knowledge/` folder has its own README and a `_TEMPLATE.md` so new entries stay consistent.
Entries get added as I actually hit the thing, not in batches — some folders are thinner than
others and that's honest, not an oversight.

## Why telecom is the interesting part

Most Java/Spring content online comes from people who've only ever worked in web/e-commerce
backends. `knowledge/telecom/` is the opposite: number portability modeled as a multi-operator
state machine, a real 5G core migration, provisioning architecture that has to survive a partner
network being down. It's the one section of this repo nobody else can really write.

## License

MIT — see [LICENSE](LICENSE).
