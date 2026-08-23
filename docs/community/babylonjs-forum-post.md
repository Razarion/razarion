# Babylon.js Community – Razarion introduction

Material for introducing Razarion to the Babylon.js community.

- Forum, Demos category: https://forum.babylonjs.com/c/demos/9
- Community showcase page: https://www.babylonjs.com/community/

---

## 1. Forum post (Demos category)

**Title:**

> Razarion – open-source multiplayer browser RTS on one persistent shared world (Babylon.js + Java/WASM)

**Body:**

**Razarion** is an open-source multiplayer RTS that runs in the browser. All players share one
persistent planet — no matches, no lobbies, no download, no registration. It is in alpha, so
expect rough edges.

:video_game: **Play:** https://www.razarion.com (desktop browsers)
:movie_camera: **Gameplay video:** <YOUTUBE-LINK>
:computer: **Source (LGPL):** https://github.com/Razarion/razarion

Babylon.js does all the rendering. The game engine is Java compiled to WebAssembly with TeaVM
(WASM-GC), so client and server run the identical simulation code; Angular drives the 2D UI.

A few Babylon.js details:

- **Terrain** tiles blend land, beach and underwater in a *single* `NodeMaterial`, by vertex
  height with `smoothstep` — no seam at the shoreline. The grass/sand splatter mask is
  procedural (domain-warped Perlin, 512×512) and its normal comes from screen-space derivatives
  of the mask, so no extra normal texture is needed.
- **Water** is a `NodeMaterial` with a per-tile reflection `CubeTexture`, plus a whitecap
  material assembled programmatically from node blocks, ribbon wakes and a bow-wave halo per
  ship.
- **Units** are glTF through `AssetContainer` and `AnimationGroup`s; explosions, beams and muzzle
  flashes are node materials too — roughly 120 across the codebase.
- The **in-game content editor** opens the Node Material Editor and the Inspector on the live
  scene, so a material can be tuned in the running game and saved back to the server. Easily the
  biggest productivity win of the whole project.

Feedback on performance with many units on screen, and on whether the shoreline holds up at
close zoom, is very welcome.

---

### Before posting — checklist

- [ ] Replace `<YOUTUBE-LINK>` with the gameplay video URL.
- [ ] Add 3–5 screenshots (Discourse embeds them inline; a demo post without pictures gets far
      less traction). Good candidates: shoreline close-up, a battle, the terrain editor with the
      Node Material Editor open.
- [ ] Add an `open-source` tag instead of putting it in the title.
- [ ] Verify the game runs on a fresh browser profile (no account, no cache) before linking.
- [ ] Check the Babylon.js version claim matches `razarion-frontend/package.json` at post time.

---

## 2. Short blurb (community showcase / listing)

**Razarion — open-source multiplayer browser RTS on a persistent shared world**

Razarion is a multiplayer real-time strategy game that runs entirely in the browser: all players
share one persistent planet, with no download and no registration. Babylon.js renders the world —
node-material terrain that blends land, beach and underwater in a single shader, reflective
water with whitecaps and ship wakes, and glTF units — while the game engine is Java compiled to
WebAssembly via TeaVM, so client and server run identical simulation code. LGPL, alpha, feedback
very welcome. https://www.razarion.com

**One-liner:** Multiplayer browser RTS on one shared persistent planet — Babylon.js rendering,
Java/WASM game engine, open source.
