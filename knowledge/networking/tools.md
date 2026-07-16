# Network Troubleshooting Tools

A layered toolkit — work up from "is the host there?" to "what's the app saying?"

## Is the host reachable? (IP layer)

```bash
ping example.com               # round-trip, packet loss (ICMP)
traceroute example.com         # every hop on the way (mtr = live version)
```
No ping? Could be down, or just blocking ICMP — not conclusive alone.

## Is the name resolving? (DNS)

```bash
dig example.com +short         # the IP, cleanly
dig example.com MX             # specific record type
nslookup example.com           # simpler, cross-platform
dig @8.8.8.8 example.com       # ask a specific resolver (bypass local cache)
```

## Is the port open? (Transport layer)

```bash
nc -zv example.com 443         # can I open a TCP connection to :443?
ss -tulpn                      # what's listening locally + which process
telnet example.com 80          # old-school port probe
nmap -p 1-1000 host            # scan ports (only hosts you're authorized to)
```

## What is the app actually saying? (Application layer)

```bash
# HTTP with full detail: DNS, TLS, headers, timing
curl -v https://example.com

# Just the status + timing breakdown
curl -s -o /dev/null -w "code=%{http_code} dns=%{time_namelookup} tls=%{time_appconnect} total=%{time_total}\n" https://example.com

# Follow redirects, send headers/data
curl -L -H "Authorization: Bearer $TOKEN" -d '{"x":1}' https://api/x

# Inspect a TLS certificate
openssl s_client -connect example.com:443 -servername example.com </dev/null 2>/dev/null | openssl x509 -noout -dates -subject
```

## Watching traffic (deep debugging)

```bash
tcpdump -i any port 443 -n            # capture packets (needs root)
tcpdump -i any host 10.0.0.5 -w cap.pcap   # save for Wireshark
```
Wireshark (GUI) for reading captures — see the actual bytes on the wire.

## The debugging ladder (follow it in order)

1. **DNS** — does the name resolve? (`dig`)
2. **Reachability** — can I reach the IP? (`ping`, `traceroute`)
3. **Port** — is the service's port open? (`nc -zv`, `ss`)
4. **TLS** — is the certificate valid / not expired? (`openssl s_client`)
5. **App** — what status/body does it return? (`curl -v`)
6. **Logs** — what does the server say it did? → [observability](../devsecops/monitoring/observability.md)

Each rung isolates a layer. Nine times out of ten the failure is DNS, a firewall/closed port, or an expired certificate.
