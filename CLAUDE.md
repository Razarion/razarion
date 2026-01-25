# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Razarion is a browser-based multiplayer Real-Time Strategy (RTS) game with a persistent shared world. It combines a Spring Boot 4 backend with an Angular 21 frontend that integrates GWT-compiled game logic and Babylon.js 3D rendering.

## Build Commands

### Full Build
```bash
mvn clean install
```

### Frontend Only
```bash
cd razarion-frontend
npm start          # Dev server with hot reload (http://localhost:4200)
npm run build      # Production build
npm test           # Run tests
```

### Backend Only
```bash
cd razarion-server
mvn spring-boot:run   # Runs on http://localhost:8080
```

### Database Setup
```bash
cd razarion-server/docker
docker-compose up -d
# MariaDB: localhost:32788, MongoDB: localhost:27017
```

## Architecture

```
razarion/
├── razarion-share/          # Shared Java DTOs and entities (GWT + server)
├── razarion-ui-service/     # UI service layer (Java, Dagger DI)
├── razarion-client/         # GWT main game client → compiles to JS
├── razarion-client-worker/  # GWT Web Worker for parallel processing
├── razarion-client-common/  # Shared GWT client utilities
├── razarion-frontend/       # Angular 21 web UI
└── razarion-server/         # Spring Boot 4 backend
```

### Key Technology Stack
- **Backend**: Spring Boot 4.0.1, Java 21, MariaDB + MongoDB
- **Frontend**: Angular 21, TypeScript, PrimeNG, Tailwind CSS
- **3D Engine**: Babylon.js 8.x
- **GWT**: 2.12.2 (Java→JavaScript compilation)

### GWT-Angular Bridge
The frontend integrates GWT-compiled Java with Angular through `razarion-frontend/src/app/gwtangular/`:
- `GwtAngularFacade.ts` - Entry point for GWT integration
- GWT compiles to `razarion-server/src/main/resources/generated/`

### TypeScript Code Generation
Java DTOs from `razarion-share` are automatically transpiled to TypeScript interfaces in `razarion-frontend/src/app/generated/razarion-share.ts` via `typescript-generator-maven-plugin`.

## Local Development Setup

```bash
# 1. Start databases
cd razarion-server/docker && docker-compose up -d

# 2. Terminal 1: Backend
cd razarion-server && mvn spring-boot:run

# 3. Terminal 2: Frontend
cd razarion-frontend && npm start

# Open http://localhost:4200
```

## Key Configuration Files
- `razarion-server/src/main/resources/application-local.properties` - Local dev config
- `razarion-frontend/src/proxy.conf.js` - API proxy for dev server
- `razarion-server/docker/docker-compose.yml` - Database credentials and ports

## Important Notes
- Always run Maven from root directory (multi-module project)
- GWT compilation is slow on first build (cached in `.gwt-unitCache/`)
- After changes to `razarion-share`, run `mvn clean install` to regenerate TypeScript
- Angular uses standalone components (no NgModules)

## Kubernetes Deployment (GKE)

### Cluster
- **Provider**: Google Kubernetes Engine (GKE)
- **Region**: `us-central1`
- **Cluster Name**: `razarion-cluster`
- **Container Registry**: `us-central1-docker.pkg.dev/neural-passkey-426618-j3/razarion-repo/`

### Komponenten

**MariaDB (StatefulSet):**
- Image: `mariadb:10.5`
- Service: `mariadb-service:3306` (ClusterIP: None)
- Datenbank: `razarion`, User: `raz_user`
- Persistent Storage: 5Gi
- Custom Config: `max_allowed_packet=128M`, Timeouts 300s

**Razarion-Server (Deployment):**
- Image: `razarion-server:latest` (via Jib-Plugin gebaut)
- Spring Profile: `prod`
- Service: LoadBalancer Port 80 → Container 8080
- Secrets benötigt:
  - `keystore-secret` (JWT-Keystore `jwt-keystore.p12`)
  - `grafana-loki-secrets` (Loki URL, Username, Password)

### Deployment-Befehle
```bash
# Cluster verbinden
gcloud container clusters get-credentials razarion-cluster --zone=us-central1

# ConfigMap für MariaDB erstellen
kubectl create configmap mariadb-custom-config --from-file=k8s/my-custom.cnf

# Container bauen und pushen
cd razarion-server && mvn compile jib:build

# Deployments anwenden
kubectl apply -f k8s/

# DB-Dump importieren
kubectl cp k8s/db_2025_11_23.sql razarion-mariadb-0:/tmp/import.sql
kubectl exec -it razarion-mariadb-0 -- bash -c "mysql -u root -pEinSicheresPasswort razarion < /tmp/import.sql"
```

### K8s Konfigurationsdateien
- `k8s/mariadb-statefulset.yaml` - MariaDB StatefulSet
- `k8s/mariadb-service.yaml` - MariaDB Headless Service
- `k8s/my-custom.cnf` - MariaDB Custom Config
- `k8s/razarion-server-deployment.yaml` - Server Deployment
- `k8s/razarion-server-service.yaml` - LoadBalancer Service
