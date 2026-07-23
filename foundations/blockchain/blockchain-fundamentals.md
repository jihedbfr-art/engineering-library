# Blockchain Fundamentals

## What problem this actually solves

Every payment system, every database, every ledger has traditionally needed a **trusted central party** — a bank, a company, a government — to say "this is the real, current state of the truth." A blockchain's actual innovation is enabling **mutually distrusting parties to agree on shared state without any single party being trusted** — that's the whole point, and it's a genuinely narrow, specific problem. If your system already has a trusted central party you're fine trusting (your own company's database, an internal system), you don't have this problem, and a blockchain solves nothing a normal database doesn't already solve better and cheaper.

## The data structure — actually simple, once you see it

```
Block N-1                    Block N                      Block N+1
┌─────────────────┐        ┌─────────────────┐        ┌─────────────────┐
│ Previous hash    │◄───────│ Previous hash    │◄───────│ Previous hash    │
│ Transactions      │        │ Transactions      │        │ Transactions      │
│ Timestamp         │        │ Timestamp         │        │ Timestamp         │
│ Hash of THIS block│───────►│ Hash of THIS block│───────►│ Hash of THIS block│
└─────────────────┘        └─────────────────┘        └─────────────────┘
```
Each block contains a cryptographic hash of the *previous* block. Change anything in an old block — even one bit — and its hash changes, which breaks the "previous hash" reference stored in every block after it, all the way to the current tip of the chain. **This is what makes tampering detectable, not impossible** — an attacker with enough computing power *can* rewrite history, but doing so requires redoing the proof-of-work (or equivalent) for every subsequent block faster than the rest of the honest network extends the real chain — a cost that becomes astronomically, deliberately expensive as the chain grows longer past the point you'd want to tamper with.

## Consensus mechanisms — how a distributed set of strangers agrees on ONE truth

The actual hard problem: thousands of independent, mutually-distrusting nodes need to agree on exactly one valid next block, with no central coordinator, while some participants may be actively malicious. Two dominant answers:

```
Proof of Work (PoW) — Bitcoin's original mechanism
  Miners compete to solve a computationally expensive puzzle
  (find a value that makes the block's hash meet a difficulty target).
  Winner proposes the next block; everyone else verifies it (verification
  is cheap even though finding the solution was expensive) and moves on.
  Security comes from the sheer cost of the computation itself —
  attacking the chain requires out-computing the entire honest network.
  Cost: massive, genuinely controversial real-world energy consumption.

Proof of Stake (PoS) — Ethereum's mechanism since "the Merge" (2022)
  Validators lock up ("stake") real cryptocurrency as collateral.
  The protocol selects a validator to propose the next block, roughly
  weighted by stake. Provably dishonest behavior gets that validator's
  stake destroyed ("slashed") — the economic penalty replaces the
  computational cost as the security mechanism.
  Dramatically lower energy use; the actual reason Ethereum's switch
  was such a significant, closely-watched event in the space.
```
Neither is universally "better" — PoW's security model is battle-tested over more than a decade at massive real-world scale; PoS is more energy-efficient and enables different scalability approaches, at the cost of a comparatively newer, less battle-tested security track record. This tradeoff — computational cost vs economic stake as the anti-cheating mechanism — is the actual technical core the whole industry's endless debates keep circling back to.

## Public vs private/permissioned blockchains — a distinction the hype usually erases

```
Public (Bitcoin, Ethereum):
  Anyone can join, read, and (via consensus) write. Fully decentralized,
  fully trustless — but genuinely slow and expensive per transaction,
  by design, as the direct cost of that decentralization.

Permissioned (Hyperledger Fabric, R3 Corda, and similar):
  Only known, vetted, pre-approved organizations can participate.
  Much faster, much cheaper per transaction, but reintroduces a
  meaningful degree of trust in "who's allowed to participate" —
  which is a real, legitimate design choice for consortium use cases
  (e.g. banks jointly maintaining a shared trade-settlement ledger),
  but it is NOT the same trustless model public chains offer, and
  should never be marketed or understood as though it were.
```
A huge share of real, deployed "enterprise blockchain" projects are actually **permissioned** — which is a legitimate, sensible design choice for a genuine consortium problem, but it's worth being precise about the fact that it trades away a meaningful piece of what makes public blockchains conceptually interesting in the first place. Calling a permissioned ledger "blockchain" isn't wrong, but conflating it with Bitcoin's trust model in a pitch deck is a common, deliberate bit of hype-inflation worth being able to spot.

## What a blockchain is genuinely good at

- Multiple mutually-distrusting parties need a shared, tamper-evident record with no single trusted intermediary (the core case above, stated plainly).
- Provable scarcity of a digital asset (a token genuinely can't be double-spent, verifiably, without a central authority policing it).
- Censorship resistance — no single party can unilaterally block or reverse a valid transaction once it's confirmed.

## What it's genuinely bad at, honestly

- **Throughput**: Bitcoin does roughly 7 transactions/second; Ethereum's base layer isn't dramatically higher. A normal relational database does orders of magnitude more, trivially. Any blockchain application claiming "web-scale" throughput on-chain, without a genuine, credible layer-2 or sidechain solution behind that claim, deserves real skepticism.
- **Storage cost**: writing data to a public chain, permanently, replicated across every single node, is expensive by design — it's not "a database," and treating it like one is a common, expensive mistake.
- **Mutability**: the same tamper-resistance that's the whole point also means a genuine, legitimate mistake (a bug, a typo, a hack) can be extraordinarily hard or outright impossible to correct after the fact — a real cost the immutability tradeoff carries with it.
- **Anything requiring a trusted party anyway**: if your actual business model already requires trusting a company (a bank, an exchange, a marketplace operator) for something else entirely, a blockchain underneath it doesn't remove that trust requirement — it just adds a second, largely redundant layer of complexity on top of a trust dependency you already have.

## Where this connects

[Smart contracts](smart-contracts.md) is what happens when you put actual programmable logic on top of this shared-state ledger — and where a genuinely large share of the real, historical, very expensive security disasters in this space have actually happened.
