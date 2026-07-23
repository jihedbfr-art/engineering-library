# Security Learning Path (Legal & Practical)

## Stage 0 — Prerequisites
Networking (TCP/IP, DNS, HTTP), Linux command line, one scripting language (Python or Bash). Security without these is memorizing magic words.

## Stage 1 — Foundations (1–3 months)
- [fundamentals.md](fundamentals.md) here
- TryHackMe "Pre Security" + "Cyber Security 101" paths
- Set up a home lab: VirtualBox/VMware + a Kali VM + intentionally vulnerable VMs (Metasploitable, DVWA) on an **isolated host-only network**

## Stage 2 — Web security (2–4 months)
- PortSwigger Web Security Academy (free, the best there is)
- OWASP Juice Shop — try to solve, then read the solutions
- Read real bug bounty write-ups (HackerOne hacktivity) — how pros think

## Stage 3 — Choose a direction

| Path | Focus | Entry certification |
|---|---|---|
| **AppSec / DevSecOps** | Secure code, pipelines, cloud | (portfolio > certs here) |
| **Pentest / offensive** | Authorized attack simulation | eJPT → OSCP |
| **Blue team / SOC** | Detection & response | CompTIA Security+ → BTL1/CySA+ |
| **Cloud security** | AWS/Azure/GCP hardening | Cloud provider security certs |

## Stage 4 — Prove it
- CTFs (picoCTF to start, then HackTheBox)
- Contribute security fixes to open source
- Publish write-ups of labs you solved — your GitHub *is* your CV
- Bug bounty **within program scope only** — scope is a legal boundary, not a suggestion

## The legal line (non-negotiable)

Testing a system without **explicit written authorization** is a crime in most jurisdictions, even "just to check". Labs, CTFs, your own machines, and in-scope bounty programs give you unlimited legal practice — there's no reason to cross the line.
