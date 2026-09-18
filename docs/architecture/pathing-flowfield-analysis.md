# Pathing: why units jam at their own base, and whether a flowfield is the answer

Status: analysis, 18.09.2026. Levers #1 and #2 are implemented (see "Implementation" at the end);
#3 waits for the PROD numbers after the deploy.

## TL;DR

- **The jam isn't a pathfinding problem. It's a path-following problem.** A* already routes around
  buildings (`BuildingBlockerOverlay`). Then the way-point follower in `Path` throws that detour
  away. Its look-ahead jumps to the farthest way point that is *in sight*, and "in sight" is tested
  against **terrain only** (`TerrainAnalyzer.isInSight`). Buildings are invisible to it. So the unit
  steers straight at the destination, through the factory, and ORCA pins it against the wall.
  A replan hands back the same A* path, and the follower throws it away again. That explains the
  `replan returned the identical path` log signature.
- **Reproduced** in `razarion-test-share` (`BaseJamTest`). A factory and a powerplant with a
  gap narrower than the unit make the unit give up in 4 of 4 variants, with exactly the PROD log
  signature (`identical path`, `crowd5=0`, `sameDest=0`, `nearestBuilding@<unit radius>`).
- **A small prototype fixes all of them.** It adds buildings to the look-ahead sight test (~30
  lines in `Path`). All 9 scenes arrive with 0 replans, including the ones that work today. Those
  also arrive faster (39 instead of 137 ticks for "unit beside the factory").
- **A flowfield doesn't fit this problem.** The destinations are practically never shared
  (`sameDest = 0` in 72 of the 75 base cases). A whole-map field for planet 117 costs ~26 MB per
  destination (plus ~105 MB during computation). A sector/hybrid field would sit on the same
  follower and inherit the same bug.
- **Recommendation:** first a building-aware look-ahead, then a building-aware rally point, then
  an escalating replan. Revisit flowfields only if a real "many units, one goal" load shows up.

## 1. The problem, in numbers

Source: `gcloud logging read`, container `razarion-server`, since 15.09.2026 00:00 UTC, 177
`[PathingStuck]` lines (89 `gave up`, 88 `replan returned the identical path`).

| | count |
|---|---|
| `gave up` total | 89 (15–26 per day) |
| by type | Builder 40, Viper 30, Harvester 19 |
| nearest building belongs to the unit's own base | 78 |
| gap to nearest building ≤ 2 m | 84 |
| **own base and ≤ 2 m ("base cases")** | **75 (84 %)** |
| unit is **touching** a building (gap − unit radius ≤ 0.05 m) | **85 of 89** |
| nearest building | Factory 52, Radar 20, Powerplant 16 |
| `sameDest > 0` (another unit has the same goal) | 4 of 89, 3 of 75 |
| no other unit within 5 m (`crowd5 = 0`) | 36 of 89, 27 of 75 |
| another unit touching (gap ≤ unit radius + 0.1) | 44 of 89, 39 of 75 |
| give-up preceded by `identical path` for the same item | 78 of 89, 64 of 75 |
| remaining distance, base cases | median 10.9 m; ≤ 5 m: 13, 5–15 m: 35, 15–40 m: 19, > 40 m: 8 |
| distinct items / distinct bases | 62 / 45; 19 items gave up more than once |

What stands out:

1. The unit is **touching the building surface** (85 of 89). It isn't lost in open terrain. It is
   being pressed against a wall.
2. **The replan changes nothing** (64 of 75). A* finds the same way again. That is a strong hint
   that A*'s answer isn't the problem.
3. **No shared goals.** Flowfields pay off when many units head to one goal. That practically never
   happens here.

## 2. How the pieces fit today

```
command ─► PathingService.setupPathToDestination
             A* on PassabilityGrid (terrain) + BuildingBlockerOverlay (buildings, Minkowski-inflated)
             ─► SimplePath: one way point per 1 m node, staircase-shaped
tick    ─► SyncPhysicalMovable.setupPreferredVelocity
             Path.setupCurrentWayPoint: advance to the FARTHEST way point in sight (≤ 40 m)
                                         sight = TerrainAnalyzer.isInSight  ◄── terrain only
             preferred velocity along the facing, turning toward that way point
        ─► ItemVelocityCalculator / ORCA: other units + buildings as circles
             (terrain obstacles: TerrainAnalyzer.getObstacles() returns an empty set)
        ─► StuckDetector (MASTER): crowded && < 0.05 m for 15 ticks → replan, max 3 → stopUnreachable
```

The two stages disagree about buildings. A* knows them and the follower doesn't. The comment in
`Path.advanceToFarthestVisible` already names the constraint ("AStar model must overlap
Obstacle-Model"). It held for terrain. It broke when buildings got into A* (`f9d358fef`) but not
into the sight test.

## 3. Causes, with evidence

### 3.1 Reproducer

`razarion-test-share/.../pathing/move/BaseJamTest.java` (first a printing reproducer, now a regression test). It uses test content
(factory r = 5, generator r = 2, unit r = 2, i.e. PROD geometry scaled about 2×) on open terrain.
It prints the A* way, whether the current steering target lies behind a building, and the outcome.

| scene | today | with building-aware sight |
|---|---|---|
| single factory, touching E, goal W | arrives, 45 ticks | arrives, 39 |
| single factory, touching E, goal W-N | arrives, 45 | arrives, 42 |
| single factory, 15 m E, goal W | arrives, 51 | arrives, 44 |
| factory + powerplant (gap 1.5 m N), touching, goal W | **gave up**, 3 replans | arrives, 39, 0 replans |
| same, goal W-N | **gave up**, 3 replans | arrives, 45 |
| factory + powerplant (gap NE), touching, goal NW | **gave up**, 3 replans | arrives, 42 |
| same, from 15 m away | **gave up**, 3 replans | arrives, 48 |
| factory + idle unit beside it, goal W | arrives after **137 ticks, 1 replan** | arrives, 39 |
| factory + idle unit NE, goal NW | arrives, 48 | arrives, 38 |

Today, in every scene the steering target lies **behind the building from tick 1 on**, because
the look-ahead jumps straight to the destination. A single round building is harmless: ORCA
slides the unit along the circle. Two buildings with a gap narrower than the unit form a pocket.
ORCA can't slide out of it, the unit stops, and the replan produces the identical path. The give-up
lines the test prints match PROD field for field:

```
[PathingStuck] replan returned the identical path ... remaining=16.48 replans=1 nearestBuilding=Factory test#1@2.0 crowd5=0 sameDest=0
[PathingStuck] gave up ...                         remaining=16.48 replans=3 nearestBuilding=Factory test#1@2.0 crowd5=0 sameDest=0
```

This is exactly how phase-1 bases are laid out. The level limits allow Factory 1, Powerplant 1 and
Radar 1 (`docs/game-design/phase-1-plan.md`), placed next to each other, with the builder working
beside them.

### 3.2 Shares of the 75 base cases

The log line doesn't carry the neighbouring buildings' positions, and the world state from
`planet_backup` was not read (see §7). The split below is therefore **inferred from the log fields
plus the reproducer**, not counted case by case.

| cause | evidence | estimated share |
|---|---|---|
| **(a′) the follower short-cuts A*'s detour through a building** (look-ahead ignores buildings) | touching a building 85/89; `identical path` before 64/75; reproduced; the prototype removes it | **main cause; present in essentially every base case**, decisive at least in the 27 cases with `crowd5=0` and most of the 64 `identical path` cases |
| (a) the A* path itself runs through or too close to a building | the reproducer's paths never enter a footprint except inside the start clearance disc (see 3.3) | small |
| (b) units as obstacles A* doesn't know about | a unit touches in 39/75, 25 of them Vipers (idle army parked at the base); the reproducer shows that a unit next to a factory costs a replan today and nothing with the prototype | contributing in ~⅓ of the cases, mostly *after* (a′) has pushed the unit into the wall |
| (c) factory spawn / rally point | `setupRallyPoint` checks free positions against **terrain only**, starts at a fixed angle (south), and puts the unit right against the factory (`factory r + unit r + 0.2`, then 2 m further). Harvesters and Vipers start touching the factory, so their first way points sit inside the start clearance disc | contributing for Harvester and Viper (40 of 75) |
| (d) ORCA against static obstacles | ORCA does what it should (the single-building scenes slide free); it can't leave a concave pocket when the steering target is behind the wall | a consequence of (a′), not a cause on its own |
| (e) builder ends the job | 35 of 75 are Builders; `SyncBuilder` ends the job on `destinationUnreachable`, and the half-built shell stays | a consequence, and the most visible one for beginners |

### 3.3 A second detail: way points inside the footprint

`BuildingBlockerOverlay` frees a disc of `unitRadius + 1 m` (more for units that are inside a
footprint) around the start. A unit touching a building therefore gets its first A* way points
*inside* the building's Minkowski footprint (the reproducer counts up to 3). Today that is
harmless. For a building-aware sight test it matters: the first prototype stopped a unit dead
because those way points counted as hidden. The prototype now shrinks the building circle to the
smaller of the unit's and the target's distance from the building centre. A productive version
needs the same care.

## 4. Smaller levers before a big rebuild

| # | lever | effort | expected effect on the 75 | risk |
|---|---|---|---|---|
| 1 | **Building-aware look-ahead** in `Path` (prototype on the branch): `isInSight` additionally tests the sight segment against building circles inflated by the unit radius, using one `iterateCellQuadBaseItem` query per test | S (~30 lines + tests) | removes cause (a′); estimate **50–65 of 75**. All 4 give-ups in the reproducer are gone, and the unit-beside case drops from 137 ticks / 1 replan to 39 / 0 | runs on MASTER and SLAVE. Deterministic as long as both see the same buildings, which they already need for ORCA. Cost: one cell query per sight test, and the look-ahead does ~1 test per way point over a path's lifetime, so small next to the three terrain raycasts it already does. Measure the tick time before rollout |
| 2 | **Building-aware rally point**: `SyncFactory.findFreePosition` also checks other buildings, and places the rally point with a clear line away from the base instead of at a fixed south angle | S | Harvester/Viper start positions no longer inside pockets; a few cases on its own, mainly makes #1 more robust | only affects new factories or a recomputed rally point |
| 3 | **Escalating replan**: if the replan returns the identical path, (i) add standing units within ~5 m to the overlay (cause b), (ii) on the next try block the building the unit is touching with a larger margin | S–M | covers the unit-blocked rest (~10–15 cases); after #1 the `identical path` counter should drop sharply, so this lever is measurable | replan remains MASTER-only, and paths go to clients as before; no desync risk |
| 4 | **Builder out of the way**: a building builder is already treated as a "worker" by ORCA (`isWorking`), i.e. the others go around it. Optionally have builders approach build sites from the side facing away from the factory | M | small; most builder cases are the builder *itself* stuck (a′) | — |
| 5 | **String-pull the path once at creation** (smoothing against terrain *and* overlay) and let the follower advance only to the next way point | M | same effect as #1, plus fewer way points on the wire | larger change to `Path`; #1 is enough for the problem at hand |

## 5. Flowfield: costs, fit, risk

Planet 117: 5120 × 5120 m, node size 1 m, so **26.2 M cells**. That is the ~52 MB uint16 heightmap
already in the worker.

| variant | memory | CPU | fit to the problem |
|---|---|---|---|
| **whole-map flowfield per destination** | direction byte 26 MB **per destination**, integration field 26–105 MB (uint16/float) while computing | Dijkstra/eikonal over 26 M cells: seconds on a phone, far over the 100 ms tick; per destination, and destinations are per unit | **no.** Only pays off with shared goals, and `sameDest` is 0 in 72 of 75 base cases. Every build or demolition invalidates the fields of every unit in the area |
| **hierarchical/sector flowfield** (e.g. 64 × 64 m sectors, portal graph) | ~4 KB direction + 8–16 KB integration per sector; a 10–40 m base path touches 1–4 sectors | similar to today's A*, plus portal graph maintenance on build/demolition (lazy invalidation, as shelved in May 2026) | covers what A* + overlay already does. The unit still has to *follow* the field, and without buildings in the sampling it runs into the same wall |
| **hybrid: A* global + local vector field around the base** (e.g. 64 × 64 m window around a crowded area) | ~16–64 KB per window | recomputed on build/demolition and per destination group | only helps where many units share the local goal (rally point, harvesting spot). Neither shows up in the logs |

Determinism between MASTER and SLAVE: today only the MASTER computes paths, and they reach clients
inside `SyncPhysicalAreaInfo`. The follower runs on both sides. A field computed on the SLAVE would
need bit-identical results (float order, tie-breaking) on JVM and TeaVM-WASM. That is feasible,
because WASM floats are IEEE-754, but it is a new source of desync and costs CPU on the phone
(~12 fps). A field computed only on the MASTER would have to be sent, which is far larger than a
path.

TeaVM/WASM: arrays of `byte`/`short` over a few sectors are fine. Whole-map fields aren't, next to
52 MB of heightmap in a worker without `SharedArrayBuffer` (the Meta in-app browser, ~90 % of
starts).

Effort: sector flowfield plus portal graph plus invalidation plus follower integration is several
weeks, with regression risk across all of movement. Benefit for the 75 cases: none that #1 doesn't
deliver.

## 6. Recommendation and order

1. **Building-aware look-ahead (#1).** Harden the prototype: the `@Inject` constructor stays the
   only one, and the server's `GameEngineConfiguration.path()` passes `SyncItemContainerServiceImpl`;
   add the reproducer scenes as a regression test with assertions; measure the tick time
   (`PathingServiceTracker`). Expected: base give-ups down from ~19/day to under 6/day.
2. **Rally point (#2)** together with #1, because both touch the first seconds of a new unit.
3. **Escalating replan (#3)**, driven by what is left in the logs after #1.
4. **Flowfield: not now.** Revisit if the logs show many units with the same goal
   (`sameDest ≥ 3` regularly), or if tick time for A* becomes the bottleneck. Not before.

## 7. Measuring success on PROD

The existing log line is enough. Compare 4 days before and after the deploy:

| metric | today | goal after #1/#2 |
|---|---|---|
| `[PathingStuck] gave up` per day | 15–26 (mean 22) | < 8 |
| of which own base, ≤ 2 m | 84 % (75/89) | < 50 % of the rest |
| `replan returned the identical path` per day | ~22 | < 5 |
| Builder give-ups (ended build jobs) | 40 in 4 days | < 10 |

Also useful: the tick time from `PathingServiceTracker` (look-ahead cost) and the rate of half-built
buildings left behind.

Not done in this analysis, and worth doing before #3: reconstructing 5–10 log cases from
`planet_backup` (building positions around the give-up position). Reading the PROD MongoDB was
blocked in this session because it holds player data. The reproducer shows the mechanism; the
exact split of causes (b) and (c) in §3.2 remains an estimate.

## Implementation (18.09.2026)

**#1 Building-aware look-ahead** (`Path`). The sight test checks the segment against each building
footprint inflated by the unit radius, the same selection as `PathingService.collectBuildings()`.
Two details from the overlay are mirrored:
- The footprint never reaches the unit or the target (so way points inside the start clearance
  disc stay visible, see §3.3).
- A building the destination lies in is the goal and is skipped.

Buildings are collected at most once per tick and only when a sight test passes the terrain:
- first only the rectangle around the sight segment;
- the whole look-ahead square only when a second test in the same tick needs it.

`Path` now always gets `SyncItemContainerServiceImpl` injected; there is no second constructor and
no switch.

**#2 Rally point** (`SyncFactory`):
- The factory checks its rally point against buildings when a unit is about to appear, i.e. at the
  start of the cooldown and before `displaceUnitsAtRallyPoint`.
- If a building took the spot, the rally point moves to the spot around the factory farthest from
  the other buildings, and the change is synced.
- The auto-computed rally point of bot factories also avoids buildings. If there is no free spot,
  it falls back to the first spot the terrain allows, because a factory without a rally point
  cannot be created.

**Tests.** `BaseJamTest` (`razarion-test-share`, pathing/move) holds the nine scenes as assertions
(arrive, no replan, within 80 ticks) plus a rally point taken by a later building. With the change
switched off, 6 of 10 fail (the four pairs, the unit beside the factory, and the rally point).

The whole `razarion-test-share` suite, run on `master` and with the change, shows the same failures.
One exception is `BotServiceTest.abandonedShellIsFinishedByTheBot`, which is flaky on `master` too
(random bot placement). Both runs hang in `quest.SyncItemPositionTest`, on `master` as well.
`basic.MoveTest` hangs and stays excluded.

**Cost** (JVM, 24 units driving through a base of 20 buildings, 400 ticks, 3 runs each): the
average engine tick went from 480 µs to about 550 µs, i.e. roughly 3 µs per moving unit per tick.
Part of that is intended: next to buildings, the look-ahead no longer jumps 40 m ahead, so a moving
unit runs more terrain sight tests per tick.
