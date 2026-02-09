# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Razarion is a browser-based multiplayer Real-Time Strategy (RTS) game with a persistent shared world. It combines a Spring Boot 4 backend with an Angular 21 frontend that integrates TeaVM-compiled Java game logic (WASM) and Babylon.js 3D rendering.

## Java & Maven Environment (Windows)

Before running `mvn` or `java`, set the environment from `C:\dev\scripts\jdk21.ps1`:
```bash
export JAVA_HOME="C:\dev\tech\Java\java-21-openjdk-21.0.1.0.12-3.win.jdk.x86_64"
export PATH="/c/dev/tech/apache-maven-3.9.6/bin:/c/dev/tech/Java/java-21-openjdk-21.0.1.0.12-3.win.jdk.x86_64/bin:$PATH"
```

## Build Commands

### Full Build
```bash
mvn clean install -DskipTests
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
mvn spring-boot:run -DskipTests   # Runs on http://localhost:8080
```

### Database Setup
```bash
cd razarion-server/docker
docker-compose up -d
# MariaDB: localhost:32788, MongoDB: localhost:27017, Mailhog: localhost:8025
```

## Architecture

```
razarion/
├── razarion-share/              # Shared Java DTOs, entities, game engine (server + WASM client)
├── razarion-ui-service/         # UI service layer (Java, Dagger 2 DI)
├── razarion-client-teavm/       # TeaVM WASM main client (replaces old GWT client)
├── razarion-client-worker-teavm/# TeaVM WASM Web Worker for parallel processing
├── razarion-frontend/           # Angular 21 web UI
├── razarion-server/             # Spring Boot 4 backend
└── razarion-test-share/         # Shared test utilities
```

### Key Technology Stack
- **Backend**: Spring Boot 4.0.1, Java 21, MariaDB + MongoDB
- **Frontend**: Angular 21, TypeScript 5.9, PrimeNG 21, Tailwind CSS 4
- **3D Engine**: Babylon.js 8.45
- **Game Client**: TeaVM 0.11.0 (Java compiled to WebAssembly via WASM-GC)
- **DI (client)**: Dagger 2.55

### TeaVM WASM-Angular Bridge

The game logic runs as WebAssembly compiled from Java via TeaVM. The bridge between WASM and Angular is bidirectional:

- **TypeScript side**: `razarion-frontend/src/app/gwtangular/GwtAngularFacade.ts` defines the contract
- **Java side**: `razarion-client-teavm/src/main/java/com/btxtech/client/`
  - `bridge/DtoConverter.java` - Java to JS DTO conversions (JsObject/JsArray wrappers)
  - `bridge/AngularProxyFactory.java` - Service proxy creation (methods callable from JS)
  - `jso/facade/JsGwtAngularFacade.java` - Main facade bridge
  - `jso/facade/Js*.java` - Individual facade adapters (JS to Java direction)
  - `jso/JsObject.java` / `jso/JsArray.java` - Plain JS object/array wrappers

**Critical**: With TeaVM WASM-GC, Java objects passed via `@JSBody` are NOT usable as plain JS objects. They must be converted to JS proxy objects using `DtoConverter` or `AngularProxyFactory`.

See [docs/architecture/teavm-angular-bridge.md](docs/architecture/teavm-angular-bridge.md) for detailed documentation.

### TypeScript Code Generation
Java DTOs from `razarion-share` are automatically transpiled to TypeScript interfaces in `razarion-frontend/src/app/generated/razarion-share.ts` via `typescript-generator-maven-plugin`.

### Dev Server Proxy
`razarion-frontend/src/proxy.conf.js` proxies to `http://127.0.0.1:8080`:
- `/teavm-client` - Main WASM client
- `/teavm-worker` - Web Worker WASM
- `/rest` - REST API
- `/systemconnection`, `/gameconnection` - WebSocket connections

## Documentation

Comprehensive documentation is available in the [`docs/`](docs/) directory:
- **[TeaVM-Angular Bridge](docs/architecture/teavm-angular-bridge.md)** - Bidirectional bridge architecture
- **[Multiplayer Sync](docs/architecture/multiplayer-sync.md)** - Command forwarding and sync patterns
- **[Kubernetes Deployment](docs/deployment/kubernetes.md)** - GKE deployment guide

## Local Development Setup

```bash
# 1. Start databases
cd razarion-server/docker && docker-compose up -d

# 2. Terminal 1: Backend
cd razarion-server && mvn spring-boot:run -DskipTests

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
- Always use `-DskipTests` when running Maven
- After changes to `razarion-share`, run `mvn clean install -DskipTests` to regenerate TypeScript
- Angular uses standalone components (no NgModules)
- Frontend build output goes to `razarion-server/src/main/resources/generated/game/`

## Deployment

For production deployment to Google Kubernetes Engine (GKE), see the **[Kubernetes Deployment Guide](docs/deployment/kubernetes.md)**.
