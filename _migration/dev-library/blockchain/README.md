# ⛓️ Blockchain & Web3

The most hyped and most misunderstood technology in this library. Stripped of the speculation, it's a genuinely specific tool solving a genuinely specific problem — knowing exactly what that problem is (and how narrow it actually is) matters more here than in almost any other section.

- [blockchain-fundamentals.md](blockchain-fundamentals.md) — what a blockchain actually is, consensus mechanisms, the trust model it replaces
- [smart-contracts.md](smart-contracts.md) — Solidity basics, the security mindset this domain demands, common exploit patterns

## The one question to ask before reaching for any of this

**"Do multiple parties who don't trust each other need to agree on shared state, without a trusted intermediary?"** If yes, a blockchain might be the right tool. If the honest answer is "no, we could just use a normal database with normal access control," a blockchain adds real complexity, real cost, and real new failure modes for no corresponding benefit — a shockingly large share of real-world "blockchain projects" fail exactly this test and would have been a better, cheaper, more maintainable product as a normal database with proper access control. Read this section with that filter on, not blockchain-as-buzzword.
