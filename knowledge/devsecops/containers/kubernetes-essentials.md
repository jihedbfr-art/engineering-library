# Kubernetes — Essentials & Security

## Mental model

```
Cluster
└── Node(s)                    (machines)
    └── Pod(s)                 (1+ containers, shared network/volumes)
        └── Container(s)

Deployment  → manages ReplicaSets → manage Pods (self-healing, rolling updates)
Service     → stable virtual IP / DNS in front of pods
Ingress     → HTTP(S) routing from outside
ConfigMap   → non-secret config     Secret → sensitive config (base64, not encrypted by default!)
Namespace   → logical isolation
```

## A production-shaped Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: notes-api
spec:
  replicas: 3
  selector: { matchLabels: { app: notes-api } }
  template:
    metadata:
      labels: { app: notes-api }
    spec:
      securityContext:
        runAsNonRoot: true
        seccompProfile: { type: RuntimeDefault }
      containers:
        - name: api
          image: ghcr.io/acme/notes-api:1.4.2   # never :latest
          securityContext:
            allowPrivilegeEscalation: false
            readOnlyRootFilesystem: true
            capabilities: { drop: ["ALL"] }
          resources:
            requests: { cpu: 100m, memory: 256Mi }
            limits:   { cpu: 500m, memory: 512Mi }
          readinessProbe:
            httpGet: { path: /actuator/health/readiness, port: 8080 }
          livenessProbe:
            httpGet: { path: /actuator/health/liveness, port: 8080 }
```

## Security non-negotiables

1. **RBAC least privilege** — no `cluster-admin` for apps, one ServiceAccount per workload.
2. **NetworkPolicies** — default-deny, then allow explicitly (a flat network is one compromised pod away from everything).
3. **Secrets**: enable encryption at rest, or better use External Secrets Operator + a real vault.
4. **Pod Security Standards** — enforce `restricted` profile per namespace.
5. **Admission control** — Kyverno/OPA Gatekeeper: block `latest` tags, require probes and resource limits.
6. **Scan manifests** — `kubesec`, `kube-score`, `trivy config`.

## kubectl survival kit

```bash
kubectl get pods -A -o wide                  # everything, everywhere
kubectl describe pod <p>                     # events = 90% of debugging
kubectl logs <p> --previous                  # logs of the crashed container
kubectl exec -it <p> -- sh
kubectl port-forward svc/notes-api 8080:80
kubectl rollout status deploy/notes-api
kubectl rollout undo deploy/notes-api        # instant rollback
kubectl top pods                             # resource usage
kubectl get events --sort-by=.lastTimestamp
```

## Debugging a CrashLoopBackOff

1. `kubectl describe pod` → check Events (image pull? OOMKilled? probe failing?)
2. `kubectl logs --previous` → the dying container's last words
3. OOMKilled → raise memory limit or fix the leak
4. Probe failures → is the path right? is startup slower than `initialDelaySeconds`?
