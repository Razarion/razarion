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

### TeaVM WASM-Angular Bridge
The frontend integrates TeaVM-compiled Java WASM with Angular through `razarion-frontend/src/app/gwtangular/`:
- `GwtAngularFacade.ts` - Entry point for WASM integration
- Java compiles to WASM via TeaVM in `razarion-client-teavm`
- **📖 See [docs/architecture/teavm-angular-bridge.md](docs/architecture/teavm-angular-bridge.md)** for detailed documentation on the bidirectional bridge architecture

### TypeScript Code Generation
Java DTOs from `razarion-share` are automatically transpiled to TypeScript interfaces in `razarion-frontend/src/app/generated/razarion-share.ts` via `typescript-generator-maven-plugin`.

## Documentation

Comprehensive documentation is available in the [`docs/`](docs/) directory:
- **[Architecture](docs/architecture/)** - Technical architecture and design patterns
- **[Development](docs/development/)** - Development guides and setup (TODO)
- **[Deployment](docs/deployment/)** - Deployment guides and operations

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

## Deployment

For production deployment to Google Kubernetes Engine (GKE), see the **[Kubernetes Deployment Guide](docs/deployment/kubernetes.md)**.
