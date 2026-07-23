# Graph Databases — Neo4j & the Graph Model

[NoSQL overview](nosql.md) lists graph as one of the four families. It deserves its own page because the *reason* to reach for one is specific: relationships-as-first-class-citizens, not just "another way to store JSON."

## The core idea: relationships are stored, not computed

```
SQL:    friendships table (user_id_a, user_id_b) → JOIN at query time to traverse
Graph:  (Alice)-[:FRIENDS_WITH]->(Bob) → the relationship IS a stored, indexed edge
```
In SQL, "find friends of friends of friends" means **three JOINs**, and each JOIN gets more expensive as the tables grow — join cost scales with the size of the *whole dataset*, not the size of the actual path you're traversing. In a graph database, traversing that same path means following pointers directly from node to node — cost scales with the size of **the subgraph you actually visit**, regardless of how big the overall database is.

## Nodes, relationships, properties — the whole model

```cypher
CREATE (alice:Person {name: 'Alice', joined: date('2024-01-15')})
CREATE (bob:Person {name: 'Bob'})
CREATE (acme:Company {name: 'Acme'})
CREATE (alice)-[:FRIENDS_WITH {since: 2023}]->(bob)
CREATE (alice)-[:WORKS_AT {role: 'Engineer'}]->(acme)
```
Both nodes *and* relationships can carry properties (`since: 2023`) — the relationship itself is data, not just a foreign key.

## Cypher — querying by drawing the pattern

```cypher
// Find Alice's friends-of-friends who aren't already her friend
MATCH (alice:Person {name: 'Alice'})-[:FRIENDS_WITH]->()-[:FRIENDS_WITH]->(fof)
WHERE NOT (alice)-[:FRIENDS_WITH]->(fof) AND fof <> alice
RETURN DISTINCT fof.name
```
Read the query almost literally: "Alice, friends-with, someone, friends-with, fof." The query *looks like* the pattern you're searching for in the graph — this readability is a real, practical advantage once your relationships get more than one or two hops deep.

## The use cases that actually justify a graph database

| Use case | Why graph wins |
|---|---|
| **Fraud detection** | Find rings of accounts sharing devices/addresses/payment methods — deep multi-hop patterns that degrade badly as SQL joins |
| **Recommendation engines** | "People who bought X also bought Y, bought by people similar to you" — naturally a graph traversal |
| **Social networks** | Friends-of-friends, shortest path, community detection — the textbook case |
| **Knowledge graphs / RAG** | Entities and their relationships for retrieval — an emerging pairing with [RAG](../ai/02-rag-architectures/rag-concepts.md) systems |
| **Network/IT topology** | "What breaks if this server goes down?" — literally a graph traversal problem |
| **Access control (complex permissions)** | "Can user X reach resource Y through any group/role chain?" |

## When NOT to reach for graph

- Your queries are mostly simple lookups or one-hop relationships — a regular foreign key and a JOIN is simpler, better understood by more engineers, and has a bigger hiring pool.
- You need heavy aggregate analytics over large volumes (sums, counts across millions of rows) — that's a [warehouse's](../data-engineering/data-warehouses.md) job, graphs aren't optimized for bulk scanning.
- Your team has zero graph-database experience and the relationships in your data aren't actually that deep — the learning curve isn't free, and "we might need deep traversals someday" isn't a reason to pay it now.

## Property graph vs RDF/triple stores (the other graph model)

Neo4j's **property graph** model (nodes/relationships/properties, queried with Cypher) is the practical mainstream choice for applications. **RDF triple stores** (subject-predicate-object, queried with SPARQL) come from the semantic-web/linked-data world and show up more in academic/government knowledge-graph contexts. Unless you have a specific RDF/ontology requirement, property graphs are the more approachable starting point.

## Scaling reality check

Graph databases are excellent at deep traversal, less naturally horizontally scalable than wide-column stores like [Cassandra](wide-column-cassandra.md) — sharding a graph without cutting through relationships that need to be traversed together is a genuinely hard problem. Neo4j and similar systems handle large single-instance graphs well; massive distributed graph workloads are a more specialized (and more painful) territory. Know your scale before committing.
