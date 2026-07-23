# Blue Team — Detection, Hardening, Response

## Hardening quick wins (any system)

1. **Patch cadence** — automate updates; the average time-to-exploit of a public CVE keeps shrinking.
2. **Disable what you don't use** — services, ports, accounts, features. Attack surface is a budget.
3. **MFA everywhere admin** — one setting kills most account-takeover paths.
4. **Backups: 3-2-1 rule** — 3 copies, 2 media, 1 offline/immutable. Test restores; an untested backup is a hope.
5. **Egress filtering** — servers rarely need to reach the whole internet; malware does.

## Linux hardening starter

```bash
# Who can log in?
sudo grep -E 'PermitRootLogin|PasswordAuthentication' /etc/ssh/sshd_config
# → PermitRootLogin no, PasswordAuthentication no (keys only)

# What's listening?
ss -tulpn

# Who has sudo?
getent group sudo

# Unattended security updates (Debian/Ubuntu)
sudo apt install unattended-upgrades
```

## Detection — what to actually watch

| Signal | Why it matters |
|---|---|
| Failed logins spike, then one success | Brute force that worked |
| New user / new sudo grant | Persistence |
| Process spawned by a web server (`www-data` running `bash`) | Webshell |
| Outbound connections to new IPs at odd hours | C2 / exfiltration |
| Massive reads from a DB by one account | Data theft in progress |
| Logs suddenly stop | Attacker cleaning up |

Centralize logs off-host (attacker on the box can't erase what already left).

## Incident response — PICERL

1. **P**reparation — runbooks, contacts, tooling *before* the incident
2. **I**dentification — is it real? scope? severity?
3. **C**ontainment — isolate (don't power off: you lose RAM evidence)
4. **E**radication — remove access, patch the entry point, rotate credentials
5. **R**ecovery — restore from clean state, monitor closely
6. **L**essons learned — blameless post-mortem, fix the class of problem

During an incident: keep a timestamped log of every action taken. Future-you and any forensics team will be grateful.

## Know your enemy — frameworks

- **MITRE ATT&CK** — the encyclopedia of real attacker techniques (attack.mitre.org). Map your detections against it.
- **Cyber Kill Chain** — recon → weaponize → deliver → exploit → install → C2 → actions. Break any link, stop the attack.
