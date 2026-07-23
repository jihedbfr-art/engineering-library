# Kubernetes Cheatsheet

Concepts & security: [devsecops/containers/kubernetes-essentials.md](../../devsecops/containers/kubernetes-essentials.md)

## Context & namespace

```bash
kubectl config get-contexts
kubectl config use-context <ctx>
kubectl config set-context --current --namespace=myns
kubectl get ns
```

## Inspect (90% of daily use)

```bash
kubectl get pods -A -o wide            # all namespaces, node/IP
kubectl get pods -w                    # watch live
kubectl describe pod <p>               # events at the bottom = gold
kubectl logs <p> -f                    # follow
kubectl logs <p> -c <container>        # multi-container pod
kubectl logs <p> --previous            # the crashed instance
kubectl get events --sort-by=.lastTimestamp
kubectl get all                        # pods, svc, deploy, rs in ns
```

## Interact

```bash
kubectl exec -it <p> -- sh
kubectl port-forward svc/api 8080:80
kubectl cp <p>:/path/file ./file
kubectl apply -f manifest.yaml
kubectl delete -f manifest.yaml
kubectl edit deploy/api               # live edit (careful)
```

## Deployments & rollout

```bash
kubectl rollout status deploy/api
kubectl rollout history deploy/api
kubectl rollout undo deploy/api                 # instant rollback
kubectl rollout restart deploy/api              # rolling restart (pick up new config/secret)
kubectl scale deploy/api --replicas=5
kubectl set image deploy/api api=myimg:1.4.3
```

## Config & secrets

```bash
kubectl create configmap app-cfg --from-literal=LOG_LEVEL=info
kubectl create secret generic db --from-literal=PASSWORD=... --dry-run=client -o yaml
kubectl get secret db -o jsonpath='{.data.PASSWORD}' | base64 -d
```

## Debug decision tree

| Status | Meaning | First command |
|---|---|---|
| `Pending` | Can't schedule (resources? node selector?) | `kubectl describe pod` → Events |
| `ImagePullBackOff` | Bad image / no registry auth | check image name + imagePullSecrets |
| `CrashLoopBackOff` | App keeps dying | `kubectl logs --previous` |
| `OOMKilled` | Over memory limit | raise limit or fix leak |
| `0/1 Ready` | Readiness probe failing | check probe path/port/delay |

```bash
# Ephemeral debug container into a running pod (k8s 1.25+)
kubectl debug -it <p> --image=busybox --target=<container>
# Throwaway pod for network tests
kubectl run tmp --rm -it --image=nicolaka/netshoot -- bash
```

## Resource & cluster health

```bash
kubectl top nodes
kubectl top pods
kubectl get nodes
kubectl describe node <n>              # allocatable vs used, pressure conditions
kubectl api-resources                  # what kinds exist here
```

## Handy aliases

```bash
alias k=kubectl
alias kgp='kubectl get pods'
alias kd='kubectl describe'
alias kl='kubectl logs -f'
# tab-completion:
source <(kubectl completion bash)
```
