# Node.js Backend Essentials

## The mental model: one thread, non-blocking I/O

Node runs your JavaScript on a **single thread** with an **event loop**. It doesn't wait on I/O (DB, network, files) — it registers a callback and moves on. This makes it excellent for **I/O-bound, high-concurrency** workloads (APIs, real-time), and poor for **CPU-bound** work (image processing, crypto) which blocks that one thread.

```
Request ──► [event loop] ──► start async I/O ──► (free to handle other requests)
                                   │
                             I/O done → callback/promise resolves → respond
```

**Golden rule**: never block the event loop. Offload CPU-heavy work to worker threads, a queue, or another service.

## Async: the only way to write it in 2020s

```js
// Always async/await over callbacks. Always handle errors.
async function getUser(id) {
  try {
    const user = await db.users.findById(id);
    if (!user) throw new NotFoundError(`user ${id}`);
    return user;
  } catch (err) {
    logger.error({ err, id }, 'getUser failed');
    throw err;                  // let the central error handler format the response
  }
}
```
- `Promise.all([...])` for independent async work in parallel — don't `await` in a loop when calls are independent.
- One unhandled rejection can crash the process; wire `process.on('unhandledRejection')` and fix the root cause.

## Express — the minimal, ubiquitous framework

```js
import express from 'express';
const app = express();
app.use(express.json());                        // parse JSON bodies

app.get('/notes/:id', async (req, res, next) => {
  try {
    const note = await noteService.get(req.params.id);
    res.json(note);
  } catch (err) { next(err); }                  // forward to error middleware
});

// Central error handler (last middleware)
app.use((err, req, res, next) => {
  const status = err.status ?? 500;
  logger.error({ err }, 'request failed');
  res.status(status).json({ error: err.publicMessage ?? 'Internal error' });
});

app.listen(3000);
```
**Fastify** is the modern high-performance alternative (schema-based validation, faster). **NestJS** adds structure (DI, modules, TypeScript-first) for larger apps — Angular-like architecture on the backend.

## Project structure that scales

```
src/
  routes/         thin — parse request, call service, format response
  services/       business logic (no HTTP here → testable, reusable)
  repositories/   data access
  models/         domain types
  middleware/     auth, validation, error handling
  config/         env-driven configuration
```
Keep HTTP concerns out of services. A service you can call from a route, a job, or a test is a service done right.

## The essentials checklist

- [ ] **TypeScript** — types catch a huge class of JS bugs; use it for any real backend.
- [ ] **Validate input** at the edge (zod / Joi / class-validator) — never trust the body.
- [ ] **Config from env** (dotenv locally, real secrets in prod) → [secrets](../../devsecops/security/secrets-management.md).
- [ ] **Structured logging** (pino/winston) with request ids → [observability](../../devsecops/monitoring/observability.md).
- [ ] **Graceful shutdown**: on SIGTERM, stop accepting, finish in-flight, close DB pool.
- [ ] **Never block the loop**: heavy CPU → worker_threads or a job queue (BullMQ).
- [ ] **Connection pooling** for the DB; don't open a connection per request.

## Security must-dos (Node-specific)

- `helmet` for security headers, `express-rate-limit` for abuse ([why](../../cybersecurity/web-security.md)).
- **Parameterized queries / ORM** — no string-built SQL ([injection](../../devsecops/security/owasp-top10.md)).
- Audit dependencies (`npm audit`, [SCA in CI](../../devsecops/security/sast-dast.md)) — the npm ecosystem is a supply-chain surface.
- Pin versions with a lockfile; be wary of postinstall scripts.
- Keep secrets out of the repo and out of client bundles.

## When Node is the right tool

- ✅ APIs, BFFs, real-time (WebSockets), I/O-heavy glue, serverless functions, tooling/CLIs.
- ⚠️ Reconsider for CPU-bound compute, heavy multithreading, hard-real-time — other runtimes fit better.
