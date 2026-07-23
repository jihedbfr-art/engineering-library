# Cybersecurity Roadmap

Detailed version with resources: [cybersecurity/learning-path.md](../../cybersecurity/learning-path.md)

```
Stage 0            Stage 1           Stage 2          Stage 3            Stage 4
PREREQUISITES  →   FOUNDATIONS   →   WEB SECURITY  →  SPECIALIZE     →   PROVE IT
networking,        CIA, crypto,      OWASP, labs,     offensive /        CTFs, certs,
Linux, Python      threat models     Burp/ZAP         defensive /        write-ups,
                                                      cloud / appsec     bounties
```

## The four career paths after foundations

| Path | Day job | Core skills | First cert |
|---|---|---|---|
| **AppSec / DevSecOps** | Secure the SDLC: code review, pipelines, threat modeling | Dev background, SAST/DAST, cloud | portfolio > certs |
| **Offensive (pentest)** | Authorized attacks, reports | Web/network/AD exploitation, scripting | eJPT → OSCP |
| **Defensive (SOC/blue)** | Detect & respond | SIEM, log analysis, forensics, ATT&CK | Security+ → BTL1 |
| **Cloud security** | Secure AWS/Azure/GCP estates | IAM, network, IaC scanning, CSPM | provider security certs |

## Practice platforms (all legal)

- **TryHackMe** — guided, best for starting
- **PortSwigger Academy** — web security, free, exceptional
- **HackTheBox** — harder, closer to real
- **picoCTF / CTFtime** — competitions
- **OWASP Juice Shop** — run it yourself, attack it yourself

## Milestones checklist

- [ ] Home lab running (hypervisor + Kali + vulnerable VMs, isolated network)
- [ ] 25 TryHackMe rooms completed
- [ ] PortSwigger: all apprentice + half of practitioner labs
- [ ] First CTF participated (score irrelevant)
- [ ] 5 public write-ups on GitHub
- [ ] Path chosen + first cert scheduled

## Two truths people learn too late

1. **Defense pays as well as offense** and hires far more. "Pentester" is the famous job, not the common one.
2. Communication is half the job: a vulnerability you can't explain to a developer or a manager doesn't get fixed.
