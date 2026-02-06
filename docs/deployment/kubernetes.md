# Kubernetes Deployment (GKE)

This guide covers deploying Razarion to Google Kubernetes Engine (GKE).

## Cluster Configuration

- **Provider**: Google Kubernetes Engine (GKE)
- **Region**: `us-central1`
- **Cluster Name**: `razarion-cluster`
- **Container Registry**: `us-central1-docker.pkg.dev/neural-passkey-426618-j3/razarion-repo/`

## Components

### MariaDB (StatefulSet)

- **Image**: `mariadb:10.5`
- **Service**: `mariadb-service:3306` (ClusterIP: None, headless service)
- **Database**: `razarion`
- **User**: `raz_user`
- **Persistent Storage**: 5Gi
- **Custom Configuration**:
  - `max_allowed_packet=128M`
  - Connection timeouts: 300s

### Razarion-Server (Deployment)

- **Image**: `razarion-server:latest` (built via Jib Maven plugin)
- **Spring Profile**: `prod`
- **Service**: LoadBalancer (Port 80 → Container 8080)
- **Required Secrets**:
  - `keystore-secret` - JWT keystore file (`jwt-keystore.p12`)
  - `grafana-loki-secrets` - Loki logging credentials (URL, username, password)

## Deployment Commands

### Connect to Cluster

```bash
gcloud container clusters get-credentials razarion-cluster --zone=us-central1
```

### Create MariaDB ConfigMap

```bash
kubectl create configmap mariadb-custom-config --from-file=k8s/my-custom.cnf
```

### Build and Push Container

```bash
cd razarion-server
mvn compile jib:build
```

### Deploy to Kubernetes

```bash
kubectl apply -f k8s/
```

### Import Database Dump

```bash
# Copy SQL dump to pod
kubectl cp k8s/db_2025_11_23.sql razarion-mariadb-0:/tmp/import.sql

# Import into database
kubectl exec -it razarion-mariadb-0 -- bash -c "mysql -u root -pEinSicheresPasswort razarion < /tmp/import.sql"
```

## Configuration Files

| File | Purpose |
|------|---------|
| `k8s/mariadb-statefulset.yaml` | MariaDB StatefulSet definition |
| `k8s/mariadb-service.yaml` | MariaDB headless service |
| `k8s/my-custom.cnf` | MariaDB custom configuration |
| `k8s/razarion-server-deployment.yaml` | Server deployment definition |
| `k8s/razarion-server-service.yaml` | LoadBalancer service |

## Common Operations

### View Logs

```bash
# Server logs
kubectl logs -f deployment/razarion-server

# MariaDB logs
kubectl logs -f razarion-mariadb-0
```

### Scale Deployment

```bash
kubectl scale deployment razarion-server --replicas=3
```

### Update Deployment

```bash
# After pushing new image
kubectl rollout restart deployment/razarion-server

# Check rollout status
kubectl rollout status deployment/razarion-server
```

### Access Database

```bash
# Port-forward MariaDB
kubectl port-forward razarion-mariadb-0 3306:3306

# Connect from local machine
mysql -h 127.0.0.1 -P 3306 -u raz_user -p razarion
```

## Monitoring

### Check Pod Status

```bash
kubectl get pods
kubectl describe pod <pod-name>
```

### View Events

```bash
kubectl get events --sort-by='.lastTimestamp'
```

### Resource Usage

```bash
kubectl top pods
kubectl top nodes
```

## Troubleshooting

### Pod Not Starting

```bash
# Check pod events
kubectl describe pod <pod-name>

# View logs
kubectl logs <pod-name>

# Interactive shell
kubectl exec -it <pod-name> -- /bin/bash
```

### Database Connection Issues

```bash
# Verify MariaDB is running
kubectl get pods -l app=mariadb

# Test connection from server pod
kubectl exec -it <server-pod> -- mysql -h mariadb-service -u raz_user -p
```

### Secret Issues

```bash
# List secrets
kubectl get secrets

# Verify secret content (base64 decoded)
kubectl get secret keystore-secret -o jsonpath='{.data.jwt-keystore\.p12}' | base64 -d > test.p12
```

## Security

### Create Secrets

```bash
# JWT Keystore
kubectl create secret generic keystore-secret \
  --from-file=jwt-keystore.p12=path/to/jwt-keystore.p12

# Loki credentials
kubectl create secret generic grafana-loki-secrets \
  --from-literal=loki-url=https://logs.example.com \
  --from-literal=loki-username=user \
  --from-literal=loki-password=pass
```

### Update Secrets

```bash
# Delete old secret
kubectl delete secret <secret-name>

# Create new secret
kubectl create secret generic <secret-name> --from-file=...

# Restart deployment to pick up new secret
kubectl rollout restart deployment/razarion-server
```

## Backup and Recovery

### Database Backup

```bash
# Create backup
kubectl exec razarion-mariadb-0 -- mysqldump -u root -pEinSicheresPasswort razarion > backup.sql

# Copy to local machine
kubectl cp razarion-mariadb-0:/tmp/backup.sql ./backup-$(date +%Y%m%d).sql
```

### Restore from Backup

```bash
# Copy backup to pod
kubectl cp backup.sql razarion-mariadb-0:/tmp/restore.sql

# Restore
kubectl exec -it razarion-mariadb-0 -- mysql -u root -pEinSicheresPasswort razarion < /tmp/restore.sql
```

## Health Checks

The server deployment includes:
- **Liveness probe**: HTTP GET `/actuator/health/liveness` every 10s
- **Readiness probe**: HTTP GET `/actuator/health/readiness` every 10s
- **Startup probe**: HTTP GET `/actuator/health/liveness` up to 60s

## Next Steps

- [ ] Set up automated backups
- [ ] Configure horizontal pod autoscaling
- [ ] Set up monitoring with Prometheus/Grafana
- [ ] Implement blue-green deployments
- [ ] Configure CDN for static assets
