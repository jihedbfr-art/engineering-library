# Networking Fundamentals

## The layered model (TCP/IP, the practical one)

```
Application   HTTP, DNS, SSH, SMTP      ← what your app speaks
Transport     TCP, UDP                  ← ports, reliability
Internet      IP, ICMP                  ← addressing, routing between networks
Link          Ethernet, Wi-Fi          ← the physical hop
```
Data goes down the stack on send (each layer wraps the previous), up on receive. You mostly live at Application + Transport.

## IP addresses & subnets

- **IPv4**: `192.168.1.10` (32-bit, running out). **IPv6**: `2001:db8::1` (128-bit, the future).
- **Private ranges** (not routable on the internet): `10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`. Your home/office/cloud VPC uses these.
- **CIDR** `/24` = how many bits are the network. `192.168.1.0/24` = 256 addresses (`.0`–`.255`). Smaller number = bigger network.
- **Special**: `127.0.0.1` (localhost/loopback), `0.0.0.0` (all interfaces).

## Ports

An IP gets you to the machine; the **port** gets you to the service.
```
22 SSH   ·  53 DNS  ·  80 HTTP  ·  443 HTTPS
5432 Postgres · 3306 MySQL · 6379 Redis · 9092 Kafka · 27017 MongoDB
```
Ports 0–1023 are "well-known" (need privilege to bind). Your apps usually use higher ports.

## TCP vs UDP

| | TCP | UDP |
|---|---|---|
| Connection | handshake, reliable, ordered | fire-and-forget |
| Guarantees | delivery, order, retransmit | none |
| Overhead | higher | minimal |
| Use | web, APIs, databases, SSH | video/voice, gaming, DNS, metrics |

TCP: "did you get that? resending." UDP: "sent it, don't care." Choose UDP only when speed beats losing a packet.

## DNS — names → addresses

Translates `example.com` → `93.184.216.34`. Record types you'll meet:
```
A      name → IPv4
AAAA   name → IPv6
CNAME  alias → another name
MX     mail servers
TXT    verification, SPF/DKIM (email security)
NS     which servers are authoritative
```
DNS is cached with a TTL — why changes take time to propagate. See [how the web works](../web/how-the-web-works.md).

## NAT, firewalls, load balancers (the middle boxes)

- **NAT** — many private IPs share one public IP (your whole home network → one address).
- **Firewall** — allow/deny traffic by IP/port/direction. Default-deny inbound is the safe posture.
- **Load balancer** — one entry point spreading traffic across many servers → [system design](../computer-science/system-design.md).
- **Proxy / reverse proxy** — sits in front (nginx): TLS termination, routing, caching, rate limiting.

## Mental model for debugging

"Can A reach B?" walks the stack: is the **name** resolving (DNS)? the **route** reachable (IP/ping)? the **port** open (firewall/service up)? the **TLS** valid? the **app** responding? Tools for each in [tools.md](tools.md).
