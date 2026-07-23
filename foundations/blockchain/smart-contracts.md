# Smart Contracts

Programmable logic that runs *on* a blockchain — code whose execution and outcome are enforced by the same consensus mechanism securing the ledger itself, not by a company's servers you have to trust. Also the single biggest source of catastrophic, publicly documented losses in the entire blockchain space — the security mindset this page pushes isn't paranoia, it's a direct response to billions of dollars of real, historical exploits.

## What makes this different from normal application code

```
Normal backend bug:      you find it, you deploy a fix, maybe some
                          downtime, mostly recoverable.

Smart contract bug:      the contract is often IMMUTABLE once deployed.
                          Funds can be drained in minutes, publicly,
                          irreversibly, by anyone who finds the bug first
                          — and the blockchain's public nature means your
                          contract's exact code is available for ANYONE
                          to audit for exploitable bugs, including
                          attackers, before you even notice something's wrong.
```
This is the single fact that should reframe how you think about this domain entirely: you are writing code that manages real, often substantial value, that you frequently cannot patch after deployment, in full public view of every attacker who wants to look. The [security mindset from cybersecurity fundamentals](../../cybersecurity/fundamentals.md) — assume attackers, defense in depth, least privilege — applies here with dramatically less room for error than typical web development, because there's frequently no post-deployment fix available at all.

## A minimal Solidity contract

```solidity
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

contract SimpleEscrow {
    address public buyer;
    address public seller;
    uint public amount;
    bool public released;

    constructor(address _seller) payable {
        buyer = msg.sender;
        seller = _seller;
        amount = msg.value;
    }

    function release() external {
        require(msg.sender == buyer, "Only buyer can release funds");
        require(!released, "Already released");
        released = true;                           // state change BEFORE the transfer — see reentrancy below
        payable(seller).transfer(amount);
    }
}
```
Even this tiny example already demonstrates the pattern most real exploits target: **state changes happen before external calls** (`released = true` before `.transfer(...)`) — that ordering is not a style preference, it's a direct, deliberate defense against the single most infamous class of smart contract exploit.

## The reentrancy attack — the exploit that defined the field's paranoia

```solidity
// VULNERABLE — the classic mistake
function withdraw() external {
    uint amount = balances[msg.sender];
    require(amount > 0);
    (bool success, ) = msg.sender.call{value: amount}("");  // external call FIRST
    require(success);
    balances[msg.sender] = 0;                                // state updated AFTER — too late
}
```
If `msg.sender` is itself a malicious contract, its fallback function can **call `withdraw()` again, recursively, before the first call ever reaches the line that zeroes the balance** — draining far more than the attacker's actual balance, repeatedly, in a single transaction. This exact bug, at scale, is what caused **The DAO hack in 2016** — roughly $60 million drained, an event so significant it caused Ethereum to controversially hard-fork the entire chain to reverse it, a decision still debated in the community today precisely because it violated the "immutable, no one can reverse it" principle the technology is built on. The fix (shown in the escrow example above, and formalized as the **checks-effects-interactions pattern**): update all internal state *before* making any external call, every time, no exceptions.

## Integer overflow/underflow — mostly solved, but know why

```solidity
// Pre-0.8.0 Solidity — this was a REAL, exploited vulnerability class
uint8 balance = 0;
balance -= 1;   // underflows silently to 255 — before Solidity 0.8, no revert, no warning
```
Solidity 0.8.0+ added automatic overflow/underflow checks by default (previously you needed an external library, most commonly OpenZeppelin's SafeMath, on every arithmetic operation). Worth knowing this history specifically because a real amount of legacy/older deployed contract code still lacks these checks, and it remains a real audit checklist item for exactly that reason — "which Solidity version, and does it use SafeMath" is a genuine first question when reviewing older contract code.

## Access control — the other dominant real-world exploit category

```solidity
// VULNERABLE — no access restriction on a function that should have one
function setOwner(address newOwner) external {
    owner = newOwner;              // ANYONE can call this and become the owner
}

// FIXED
function setOwner(address newOwner) external {
    require(msg.sender == owner, "Not authorized");
    owner = newOwner;
}
```
A shocking share of real, historical exploits are this genuinely simple: a sensitive function with a missing or incorrectly implemented access check — the smart-contract-world equivalent of [OWASP's broken access control](../../devsecops/security/owasp-top10.md), which tops that list precisely because it's this common in traditional web applications too. The stakes and irreversibility are simply higher here.

## Auditing — a real, distinct professional discipline in this space

Given the immutability and public-attack-surface reality above, serious projects get contracts **formally audited** by specialized security firms before mainnet deployment — a genuinely distinct professional discipline from general application security review, precisely because the failure mode (funds gone, publicly, irreversibly, in minutes) is categorically more severe than a typical web app's failure modes. Real practice, beyond a single audit: bug bounty programs, formal verification tooling for high-value contracts, and a strong industry norm of extended, adversarial testing on testnets before any real value ever touches the contract.

## Gas — the cost model that also shapes security decisions

Every operation on Ethereum (and similar chains) costs **gas**, paid in real cryptocurrency, which means a poorly optimized contract isn't just slow — it's directly, measurably expensive for every single user who ever calls it. This cost pressure sometimes creates a genuine, uncomfortable tension with security best practices, which frequently require *more* checks, *more* operations, *more* gas — a smart contract engineer has to actively balance thoroughness against real, ongoing cost to users, in a way that has no direct equivalent in typical backend development where compute cost is usually the company's problem, not charged per-operation directly to the end user.

## Where this connects

This page is the sharpest, most consequential illustration in the entire library of the [OWASP-style security discipline](../../devsecops/security/owasp-top10.md) applied under a genuinely less forgiving constraint: no easy post-deployment patch, full public code visibility to attackers, and real money on the line the moment a contract goes live. If [access control and injection-class bugs](../../devsecops/security/owasp-top10.md) matter in a normal web app, they matter categorically more here, for exactly the reasons this page lays out.
