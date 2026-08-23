# Babylon.js Community – Razarion introduction

Material for introducing Razarion to the Babylon.js community.

- Forum, Demos category: https://forum.babylonjs.com/c/demos/9
- Community showcase page: https://www.babylonjs.com/community/

---

## 1. Forum post (Demos category)

**Title suggestion:**

> Razarion – open-source multiplayer browser RTS on one persistent shared world (Babylon.js + Java/WASM)

Alternatives, if the above runs too long in the topic list:

> Razarion – a multiplayer browser RTS where everyone shares one persistent world (Babylon.js + WASM)

> Razarion – open-source multiplayer RTS in the browser: one shared planet, Babylon.js + Java/WASM

**Body:**

Hi everyone :wave:

I would like to show you **Razarion**, a browser-based real-time strategy game I have been
building as a non-commercial hobby project. Babylon.js does all the rendering.

:point_right: **Play:** https://www.razarion.com — no download, no registration, just open the page.
Best on a desktop browser; touch controls for phones landed recently and are still rough.
:point_right: **Source (LGPL):** https://github.com/Razarion/razarion

### What it is

All players share **one persistent planet**. There are no matches and no lobbies — you land
somewhere on the map, build a base, harvest, fight, and the world keeps running when you close
the tab. It is in alpha, so expect rough edges and frequent changes.

### The unusual part of the stack

The game engine is **Java compiled to WebAssembly with TeaVM (WASM-GC)** — the exact same
simulation code runs on the Spring Boot server and in the browser, which is what keeps client
and server in sync. Around it:

- **Angular + PrimeNG** for all 2D UI (HUD, dialogs, editors)
- **Babylon.js 9** for the 3D scene
- a second TeaVM WASM module in a **Web Worker** for the parallel simulation work

Babylon.js is therefore driven entirely from TypeScript, while the game logic pushes state over
a bridge from WASM. Getting Java objects across the WASM-GC boundary into plain JS objects was
the single hardest part of the port (documented here:
https://github.com/Razarion/razarion/blob/master/docs/architecture/teavm-angular-bridge.md).

### Where Babylon.js does the heavy lifting

**Terrain.** The world is streamed as tiles. Each tile is a ground mesh with a `MultiMaterial`
(ground / asphalt submeshes) plus a separate water mesh at y=0. The interesting bit: land, beach
and underwater are **one single `NodeMaterial`**, blended by vertex height with `smoothstep`
instead of separate materials, so the shoreline transition is free of seams.

The grass/sand split uses a **procedurally generated splatter mask** (domain-warped tileable
Perlin noise, 512×512, generated at runtime) and derives its normal from screen-space
derivatives of the mask — no extra normal texture needed. At the transition band the splatter
normal is blended into the ground normal with `borderIntensity = h × (1 − h) × 4`, which gives a
raised-grass-over-sand look purely through lighting. Mountains blend in over a noise texture so
the rock/grass border is not a straight line.

**Water.** Water surface as a `NodeMaterial` with a reflection `CubeTexture` per tile, plus a
separate whitecap mesh whose material is built **programmatically from node blocks**
(`TextureBlock`, `SmoothStepBlock`, `VectorMerger`, …) against a generated whitecap texture.
Ships get ribbon wakes — two merged port/starboard ribbon meshes with shared vertices between
segments so the ribbon stays smooth — plus a bow-wave foam halo under the hull.

**Everything else that moves:** glTF models loaded through `AssetContainer`, `AnimationGroup`s
for unit animation, instanced meshes for the repeated stuff, shadow generators, and a pile of
small effect classes — muzzle flashes, explosions, sprite-sheet fireballs, building debris,
harvesting and energy beams, lightning, buildup effects when a structure is being constructed.
Almost all of them are `NodeMaterial`s (~120 usages in the codebase by now).

**Tooling.** The game ships with an in-browser editor for the content team: terrain height-map
brushes (raise, smooth, erosion, terrace, coast, mountain stamp), object scatter brushes, and
CRUD editors for models and particles. Those editors lazily `import("@babylonjs/node-editor")`
and hand you the **Node Material Editor / Node Particle Editor and the Inspector on the live
scene**, so a material can be tweaked in the running game and saved back to the server. That
workflow has been by far the biggest productivity win for us — thanks for building it.

There is also a small perf overlay (rolling 4s history of frame time and fps against 60/30 fps
thresholds) that we keep on during development.

### Feedback I would love

- Performance on your machine — an RTS wants many units on screen; I have not pushed
  thin instances nearly as far as I probably should.
- The shoreline / water transition: does it hold up when you zoom in?
- Anything that looks obviously wrong to someone who knows Babylon better than I do — the
  renderer code is here:
  https://github.com/Razarion/razarion/tree/master/razarion-frontend/src/app/game/renderer

The whole thing is LGPL, so feel free to dig around. Happy to answer anything about the
TeaVM/WASM ↔ Babylon bridge, it is a slightly odd setup and I had to figure most of it out the
hard way.

Thanks for a great engine! :heart:

---

### Before posting — checklist

- [ ] Add 3–5 screenshots and ideally a short GIF/video (Discourse embeds images inline; a demo
      post without pictures gets far less traction). Good candidates: shoreline close-up,
      a battle, the terrain editor with the Node Material Editor open, the minimap/world view.
- [ ] Verify the game runs on a fresh browser profile (no account, no cache) before linking.
- [ ] Check the Babylon.js version claim matches `razarion-frontend/package.json` at post time.
- [ ] Consider adding a Playground link if any isolated effect (whitecap material, splatter
      blending) can be reproduced standalone — the forum loves those.

---

## 2. Short blurb (community showcase / listing)

**Razarion — open-source multiplayer browser RTS on a persistent shared world**

Razarion is a multiplayer real-time strategy game that runs entirely in the browser: all players
share one persistent planet, with no download and no registration. Babylon.js renders the world —
node-material terrain that blends land, beach and underwater in a single shader, reflective
water with whitecaps and ship wakes, and glTF units — while the game engine is Java compiled to
WebAssembly via TeaVM, so client and server run identical simulation code. LGPL, alpha, feedback
very welcome. https://www.razarion.com

**One-liner:** Multiplayer browser RTS on one shared persistent planet — Babylon.js rendering, Java/WASM
game engine, open source.
