<!-- GitAds-Verify: 9OIQBOZC2N87GYYK16MSJT88ZD9Q31GY -->
# Razarion – Open-World RTS in the Browser, powered by WebAssembly

**Razarion** is a browser-based, open-source real-time strategy (RTS) game powered by WebAssembly where **all players share one persistent planet**.
No registration. No installation. Just play.

👉 [https://www.razarion.com](https://www.razarion.com)

> Best experienced on desktop browsers (Chrome, Firefox)

---

## 🌍 Features

- **Multiplayer on one shared world** – Everyone plays together on a single persistent planet
- **Runs entirely in the browser via WebAssembly** – No login, no download, no ads
- **Fully open-source** – LGPL-licensed hobby project
- **In active alpha** – Under development, feedback welcome

---

## 🧪 Current State (Alpha)

- RTS gameplay with buildings, units, and real-time combat
- Shared game world for all users
- No account required – sessions are temporary
- Frequent updates and feature experiments
- Expect bugs and work-in-progress systems


---

## 🛠️ Technology

### 🔗 Server
- Java with Spring Boot
- Multiplayer coordination and game state

### 🔗 Client
- **Angular** and **PrimeNG** for UI
- **Babylon.js** for WebGL 3D rendering
- **TeaVM** (WASM) for the game engine

### 📖 Documentation

Detailed technical documentation is available in the [`docs/README.md`](docs/README.md):
- [Architecture](docs/architecture/) – TeaVM-Angular Bridge, Multiplayer Sync, Quest Tip System
- [Terrain System](docs/terrain/terrain-system.md) – Heightmap format, terrain classification, REST endpoints
- [Game Design](docs/game-design/progression.md) – Game progression, phases, units, quests
- [Deployment](docs/deployment/kubernetes.md) – Kubernetes / GKE deployment

---

## 💻 Source Code

Everything is open-source under the LGPL license:  
🔗 [GitHub Repository](https://github.com/Razarion/razarion)

---

## 🤝 Contributing

Razarion is a non-commercial, no-profit hobby project.  
Suggestions, issue reports, and pull requests are warmly welcome!

---

## 📜 License

This project is licensed under the **GNU Lesser General Public License (LGPL)**.

---

## 📢 Community

Feedback? Ideas? Bugs?  
Reach out on [Twitter/X](https://x.com/razariongame) or open an [Issue](https://github.com/Razarion/razarion/issues).

