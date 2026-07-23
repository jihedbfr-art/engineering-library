# foundations/

The "what is this and how does it work" layer — generalist computing knowledge that doesn't
assume you're working in this repo's specific Java/Spring/telecom stack. It answers a different
question than [`knowledge/`](../knowledge/README.md), which is about choosing, implementing, and
debugging things in production here.

This tree came from `dev-library`, a repo that started as a general dev encyclopedia and ended up
duplicating half of what `knowledge/` already covered. The parts that didn't overlap — languages,
web fundamentals, cloud, mobile, and a long tail of narrower topics — moved here with their commit
history intact instead of getting rewritten from scratch.

| Domain | Covers | Entries |
|---|---|---|
| [computer-science](computer-science/) | Big-O, data structures, algorithms, design patterns, system design | 5 |
| [programming](programming/) | Go, Java, JS/TS, Python, Rust, clean code, per-language concurrency | 11 |
| [databases](databases/) | SQL essentials, indexing, transactions, NoSQL, modeling — engine-agnostic | 7 |
| [web](web/) | How the web works, HTTP, REST vs GraphQL, browser rendering | 4 |
| [cloud](cloud/) | AWS/Azure/GCP essentials, managed containers comparison | 5 |
| [mobile](mobile/) | Android, iOS, Flutter, React Native | 5 |
| [frontend](frontend/) | React, Angular, web fundamentals | 3 |
| [data-engineering](data-engineering/) | ETL/ELT pipelines, streaming, warehouses, data quality | 4 |
| [legacy-modernization](legacy-modernization/) | Strangler fig, migration playbooks, Oracle ADF | 4 |
| [sre](sre/) | SLI/SLO, incident management, capacity planning | 3 |
| [game-dev](game-dev/) | Engine architecture, graphics, multiplayer networking | 3 |
| [embedded-iot](embedded-iot/) | Microcontrollers, RTOS, MQTT/CoAP | 3 |
| [networking](networking/) | TCP/IP, DNS, diagnostic tools | 2 |
| [blockchain](blockchain/) | Consensus, smart contracts, real exploits (reentrancy, The DAO) | 2 |
| [compilers](compilers/) | Lexing/parsing/IR/JIT, static vs dynamic typing | 2 |
| [resources](resources/) | Curated roadmaps, cheatsheets, book list — external links, not original content | 11 |

For the engine-specific and practice-oriented counterparts (Postgres internals, Java/Spring
patterns, telecom domain work), see [`knowledge/`](../knowledge/README.md) instead. If you're
about to add a page here, check `knowledge/` first — a concept that's really about *running this
stack in production* belongs there, not here.
