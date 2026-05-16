# Razarion Studio

Internal development tool. Sits inside the `razarion-frontend` Angular workspace
as a separate `application` project (`projects/studio`), but is **never bundled
into the production game**.

## Purposes

1. **Versuchswerkstatt** — Iterate on the Babylon renderer, cockpit UI, terrain
   shaders, unit effects without the full TeaVM/login/WebSocket bootstrap. Spirit
   successor to the old `gwt-mock-server`.
2. **Bild-Export** — Render scenes for ItemCockpit thumbnails, quest visuals.
3. **Landingpage- & Social-Media-Renderings** — Inszenierte Szenen, hochauflösender
   Export.

## Architecture

Studio bootstraps the same production renderer pipeline that
`razarion-frontend`'s `GameComponent` uses in `gwtMock` mode. The single route
is `StudioGameHostComponent`, which mirrors `GameComponent.initAndStart()`:

- `BabylonRenderServiceAccessImpl.setup(canvas)` — production renderer
- `BabylonAudioService.init()`
- `gwtAngularFacade.{screenCover, mainCockpit, questCockpit, chatCockpit,
   modelDialogPresenter, baseItemPlacerPresenter}` — wired to no-op stubs
   from `src/app/stubs/stub-cockpits.ts` (no in-game HUD in studio)
- `actionService.setRendererService(...)`
- `gameMockService.startGame(true, gameComponentStub)` — runs the same
  mock-game-setup that `gwtMock` mode uses

Everything visual is rendered by production code: NodeMaterials loaded via
`BabylonModelService`, terrain tiles via `BabylonTerrainTileImpl`, items via
`BabylonRenderServiceAccessImpl.createBabylonBaseItem`, effects via
`BabylonLightning` / `BabylonImpact` / `BabylonExplosion`.

## Why this isn't in production

Two independent safeguards keep studio out of the published game:

1. **npm**: `package.json` pins `npm run build` to `ng build razarion-frontend`
   (the only thing Maven invokes). Studio is built via the separate
   `npm run studio:build` script.
2. **Maven**: `razarion-frontend/pom.xml` only copies
   `dist/razarion-frontend/browser` into `razarion-server/.../generated/game`.
   Studio's output goes to `dist/studio/browser` and is never picked up.

If you change either of those, mind the invariant.

## Module boundary

> **Studio darf aus `razarion-frontend/src/` importieren — `razarion-frontend`
> darf NIE aus `projects/studio/` importieren.**

Reason: studio is a throwaway-friendly experimentation playground. If frontend
depended on studio, accidental experiments would leak into the shipped game and
the dependency direction would invert the "kein Prod-Code" rule.

## Running

Studio uses the same two mock servers as the main frontend's `gwtMock`
configuration. Start them in two separate terminals **before** starting the
dev server — they serve NodeMaterials, heightmaps, ground configs, etc. for
production renderer services:

```bash
cd razarion-frontend
npm run studio:fake-server   # :8080 — REST + assets (BabylonMaterials.zip, etc.)
npm run studio:gwt-mock      # :9090 — /gwt-mock/static-game-config
```

Then in a third terminal:

```bash
npm run studio:start         # dev server on http://localhost:4300
npm run studio:build         # production build → dist/studio/
```

The main game frontend stays on port 4200; studio runs on 4300, so both can run
side-by-side. The dev server proxies `/rest`, `/editor`, `/gz`, `/gwt-mock` to
the mock servers (see `src/proxy.conf.js`).

> If `studio:fake-server` fails with `Cannot find module 'express'` or `body-parser`,
> install them once at the repo root: `npm i -D express body-parser`. They are
> dependencies of the mock server, not the studio app itself.
