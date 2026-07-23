# What Happens When You Type a URL

The classic interview question — and genuinely useful to understand. Follow one request end to end.

## The journey of `https://example.com`

```
1. URL parse      → scheme https, host example.com, path /
2. DNS lookup     → example.com → 93.184.216.34   (cache → resolver → root → TLD → authoritative)
3. TCP handshake  → SYN / SYN-ACK / ACK with the server
4. TLS handshake  → agree cipher, verify certificate, derive session key (now encrypted)
5. HTTP request   → GET / HTTP/2, headers (Host, User-Agent, Cookie...)
6. Server work    → app logic, DB queries, renders/returns response
7. HTTP response  → status 200, headers, HTML body
8. Browser render → parse HTML → fetch CSS/JS/images → build DOM → paint
9. Follow-ups     → JS runs, XHR/fetch calls, more assets
```

## 1–2. DNS: the internet's phone book

Your browser needs an IP. It checks caches (browser → OS → router → ISP resolver). On a miss, the resolver walks: **root** servers → **TLD** server (`.com`) → **authoritative** server for `example.com` → returns the IP. Results are cached with a TTL. This is why a new domain takes time to "propagate."

## 3–4. TCP + TLS: the connection

- **TCP** establishes a reliable ordered channel (the 3-way handshake).
- **TLS** then secures it: the server presents a certificate (proving identity, signed by a CA your browser trusts), both sides derive a shared secret, and everything after is encrypted. This is the "https" and the padlock.

## 5–7. HTTP: request & response

A text-based (HTTP/2+ binary) request/response protocol. The server may hit load balancers, caches (CDN), app servers, and databases before answering → see [system design](../computer-science/system-design.md). Details in [http.md](http.md).

## 8. Rendering

The browser parses HTML into the **DOM**, CSS into the **CSSOM**, combines them into a render tree, computes layout, and paints pixels. JavaScript can block and modify all of this. Details in [browser-rendering.md](browser-rendering.md).

## Why every layer matters to you

- Slow first byte? → DNS, TLS, or server. 
- Slow page after load? → assets, JS, rendering.
- "Works on my machine but not for users"? → caching, CDN, DNS propagation.

Understanding the pipeline turns "the site is slow" from a mystery into a checklist.
