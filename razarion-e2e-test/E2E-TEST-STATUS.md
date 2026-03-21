# E2E Test GameStartIT - Development Status

Continue developing the `GameStartIT.fullGameFlow()` E2E test that covers Phase 1 (levels 1-9) of the Razarion RTS game. **Levels 1-5 pass. Level 6 is in progress** — Dockyard placement fails because the quest region is not near the player base; the camera must be moved to the quest region (near the coastline) before placing.

## Files Modified

### E2E Test Files
- `razarion-e2e-test/src/test/java/com/btxtech/e2e/smoke/GameStartIT.java` - Test flow for all 9 levels
- `razarion-e2e-test/src/test/java/com/btxtech/e2e/page/GamePage.java` - Page object with JS commands (1268 lines)
- `razarion-e2e-test/src/test/java/com/btxtech/e2e/base/AdminApiClient.java` - REST API cleanup (delete bases, restart planet)

### Server/WASM Files (changes need rebuild + server restart)
- `razarion-frontend/src/app/app.component.ts` - Exposes `window.__e2eNgZone` and `window.__e2eAppRef`
- `razarion-ui-service/.../item/BaseItemUiService.java` - Added `getNearestEnemyId()` method — **DONE**
- `razarion-client-teavm/.../bridge/AngularProxyFactory.java` - Added proxy for `getNearestEnemyId` + `getActiveQuestPlaceConfig` — **DONE**
- `razarion-ui-service/.../control/GameEngineControl.java` - Fixed `int[]` → `IdsDto` marshalling in all `*CmdIds` methods — **DONE**
- `razarion-ui-service/.../questvisualization/InGameQuestVisualizationService.java` - Added `getActiveQuest()` public accessor — **DONE**
- `razarion-frontend/src/app/gwtangular/GwtAngularFacade.ts` - Added `getActiveQuestPlaceConfig()` to `InGameQuestVisualizationService` interface — **DONE**
- `razarion-frontend/src/app/game/game-mock.service.ts` - Added mock for `getActiveQuestPlaceConfig()` — **DONE**

### Uncommitted Changes (git diff summary)
All changes are **unstaged**. The following files have been modified:
1. `AngularProxyFactory.java` — +6 lines: `getActiveQuestPlaceConfig` proxy method using `DtoConverter.convertPlaceConfig()`
2. `JsGameCommandService.java` — +27 lines (details not verified)
3. `GamePage.java` — +187/-107 lines: quest region methods, transporter/sell/load commands
4. `GameStartIT.java` — +22 lines: level 6-9 method stubs
5. `GwtAngularFacade.ts` — +1 line: `getActiveQuestPlaceConfig()` interface method
6. `game-mock.service.ts` — +3 lines: mock returning null
7. `InGameQuestVisualizationService.java` — +5 lines: `getActiveQuest()` accessor
8. `underwater-normal.jpg` — deleted (was added then removed)

## How to Run
```bash
cd razarion-e2e-test
export JAVA_HOME="C:\dev\tech\Java\java-21-openjdk-21.0.1.0.12-3.win.jdk.x86_64"
export PATH="/c/dev/tech/apache-maven-3.9.6/bin:/c/dev/tech/Java/java-21-openjdk-21.0.1.0.12-3.win.jdk.x86_64/bin:$PATH"
mvn verify -Dit.test=GameStartIT#fullGameFlow -pl .
```
Uses failsafe plugin (not surefire). Add `-De2e.headless=false` to watch the browser.

After changes to server/WASM files:
```bash
# From root: rebuild everything
mvn clean install -DskipTests
# Then restart server
cd razarion-server && mvn spring-boot:run -DskipTests
```

## Test Setup (@BeforeEach)
`AdminApiClient` handles cleanup before each test run:
1. POST `/rest/user/auth` with Basic auth (admin@admin.com / 1234) -> JWT token
2. GET `/rest/editor/user-mgmt/get-user-backend-infos` -> find users with bases
3. DELETE `/rest/editor/user-mgmt/delete-users-bases` -> remove all human bases
4. POST `/rest/planet-mgmt-controller/restartPlanetWarm` + 5s sleep -> clean game state

This is **critical** - without restart, accumulated game state causes flaky failures.

## Current Status per Level

| Level | Status | Notes |
|-------|--------|-------|
| Deploy | **PASS** | Place builder on free terrain |
| 1 | **PASS** | Build Factory, fabricate Harvester |
| 2 | **PASS** | Harvest, fabricate Viper, kill enemy (excluding Bot Refinery 2). Occasionally needs 2-3 retries (10s each) if viper is far from enemy. |
| 3 | **PASS** | Build Radar, build Powerplant |
| 4 | **PASS** | Harvest 30, fabricate 3 Vipers |
| 5 | **PASS** | Kill Bot Refinery 2 — uses `getNearestEnemyId` + `moveCmd` + mass viper fabrication |
| 6 | **BLOCKED** | Build Dockyard in quest region — quest region is NOT near base, bridge for `getActiveQuestPlaceConfig` is implemented but **untested**. See details below. |
| 7 | **CODE WRITTEN, UNTESTED** | Fabricate Hydra from Dockyard, kill Bot Hydra |
| 8 | **CODE WRITTEN, UNTESTED** | Fabricate Transporter, load Builder, move to Phase 2 region, unload |
| 9 | **CODE WRITTEN, UNTESTED** | Sell Factory, rebuild in Phase 2, fabricate units, sell Dockyard |

## Verified Working (Session 2026-03-15)

### moveCmd WORKS
Confirmed via position logging. Vipers move ~10-15 units/sec toward target:
```
BEFORE MOVE: viper#142@184.4,44.7 viper#157@178.0,8.0 ...
AFTER  5s:   viper#142@174.4,81.3 viper#157@168.9,32.8 ...
AFTER 10s:   viper#162@164.5,70.3 viper#163@164.2,80.3  (4 died en route)
```

### getNearestEnemyId + attackCmd WORKS
When the enemy is not rendered by Babylon.js, `jsAttackEnemyOfType` now:
1. Tries rendered enemies first (direct `attackCmd`)
2. Falls back to `baseUi.getNearestEnemyId(x, y, typeId)` to get server-known enemy ID
3. Calls `attackCmd(attackerIds, enemyId)` with that ID
4. This works even if the enemy is not visually rendered — as long as the worker has it synced

Key log: `jsAttackEnemyOfType(24): attack by ID: enemyId=151` → quest completes.

### IdsDto Marshalling Fix WORKS
All `*CmdIds` methods in `GameEngineControl.java` now use `intArrayToIdsDto()`:
- `moveCmdIds`, `attackCmdIds`, `harvestCmdIds`, `pickBoxCmdIds`, `loadContainerCmdIds`, `finalizeBuildCmdIds`, `sellItemIds`

### Retry Interval Must Be 10s (Not 5s)
Changing `waitForQuestDoneWithRetry` from 10s to 5s caused level 2 to fail — the viper's attack command was resent too frequently, preventing it from actually reaching/killing the enemy. **Keep retry at 10s.**

## BLOCKED: Level 6 - Dockyard Placement in Quest Region

**Quest**: "Region" — Build Dockyard in a specific quest region (near coast/water)

### Root Problem
The quest requires the Dockyard to be placed inside a `PlaceConfig` region defined by the quest. This region is NOT near the player base (~178, 20). It is somewhere near the coastline/water which starts at y≈200 based on the terrain heightmap.

### Bridge Implementation (Done, Untested)
The `getActiveQuestPlaceConfig` bridge has been implemented across all layers:

1. **Java service**: `InGameQuestVisualizationService.getActiveQuest()` — returns the active `QuestConfig` (line 75-77)
2. **WASM proxy**: `AngularProxyFactory.java` — `setMethodRetObj(proxy, "getActiveQuestPlaceConfig", ...)` reads quest → conditionConfig → comparisonConfig → placeConfig, converts via `DtoConverter.convertPlaceConfig()`
3. **TS interface**: `GwtAngularFacade.ts` — `getActiveQuestPlaceConfig(): PlaceConfig | null`
4. **Mock**: `game-mock.service.ts` — returns `null`

**GamePage.java `getQuestRegionCenter()`** (line 222-251) calls this bridge:
```javascript
var questVis = window.gwtAngularFacade.inGameQuestVisualizationService;
var pc = questVis.getActiveQuestPlaceConfig();
// reads position or polygon2D centroid
```

**GamePage.java `buildViaBuilderInQuestRegion()`** (line 1236-1257):
1. Tries `getQuestRegionCenter()` → if found, calls `jsBuildAtPosition(itemTypeId, x, y)` directly
2. Fallback: activates placer UI, then searches 24 camera positions with `placeInQuestRegion()`

### What Needs Testing
1. **Does `getActiveQuestPlaceConfig()` return a non-null PlaceConfig at level 6?** — The quest's conditionTrigger is `SYNC_ITEM_POSITION`, and `showVisualization()` (line 119-128) does access the placeConfig, so it should be set.
2. **Does `DtoConverter.convertPlaceConfig()` produce correct JS object with position/polygon?** — This method already exists and is used elsewhere, so it should work.
3. **Does `jsBuildAtPosition()` succeed at the quest region coordinates?** — It calls `gameCmd.buildCmd(builderId, x, y, itemTypeId)`. The builder may need to be close enough to the build position, or it might auto-walk there.

### What Was Tried Previously
1. **Canvas click spiral grid** (73 positions around canvas center) — fails because camera is at base, quest region is off-screen
2. **REST API `/rest/quest-controller/readMyOpenQuests`** — returns empty array (anonymous browser session has no authenticated quests)
3. **Babylon scene mesh search** for quest markers — no quest region meshes found; only "Base Item Placer" mesh present
4. **Camera search grid** across 24 positions from (50,100) to (250,250) — coded as fallback in `placeInQuestRegion()`

### Scene Debug Output (Level 6)
```
Quest meshes: Base Item Placer@178.0,19.3
Quest marker nodes: NO_MARKER_CONTAINER top-level: Base items, Resource items, Box items,
  Terrain Tile x: 0 y: 0, Terrain Tile x: 1 y: 0, Dockyard
```
Note: There IS a "Dockyard" top-level transform node — this might be the quest region visualization or just the building label.

### Map Layout (from `terrain_minimap` MCP tool)
Planet is 5120x5120m. Phase 1 (Noob Island) = bottom-left 820x800m. The terrain heightmap shows:
- **Land**: y≈0–200 at x≈100–300 (where player base is)
- **Water/Lake**: starts at y≈200–250, extends to y≈700+ in the left portion
- **Coastline**: roughly y≈200 at x≈150–200 (this is where the Dockyard quest region likely is)

### Possible Solutions (try in order)
1. **Test the bridge (most likely to work)**: Rebuild (`mvn clean install -DskipTests`), restart server, run the E2E test. The `getActiveQuestPlaceConfig` bridge should return the PlaceConfig center, and `jsBuildAtPosition` should place the Dockyard directly.
2. **If bridge returns null**: Debug by checking if `quest` is set in `InGameQuestVisualizationService` at level 6. The quest trigger is `SYNC_ITEM_POSITION` which should have a placeConfig.
3. **If buildCmd fails at coordinates**: The builder may need to be within range. Move the builder (via `moveCmd`) to the quest region first, then build.
4. **Camera brute-force fallback**: The `placeInQuestRegion()` method already implements this — 24 camera positions with canvas click spiral at each.
5. **Check "Dockyard" scene node**: `scene.getTransformNodeByName('Dockyard').getChildren()` might have position data.

### PlaceConfig Architecture (for debugging)
```
QuestConfig
  → conditionConfig: ConditionConfig
    → comparisonConfig: ComparisonConfig
      → placeConfig: PlaceConfig
        → position: DecimalPosition (center)
        → radius: Double
        → polygon2D: Polygon2D (corners[])
```
- Java class: `razarion-share/.../config/PlaceConfig.java`
- TS type: `razarion-frontend/src/app/generated/razarion-share.ts` → `PlaceConfig`
- DtoConverter: `razarion-client-teavm/.../bridge/DtoConverter.java` line 243 `convertPlaceConfig()`
- Visualization: `razarion-ui-service/.../questvisualization/InGameQuestVisualizationService.java`

## Level 5 Solution (Working)

### Strategy
1. Before attacking, harvest resources and fabricate 6 vipers total
2. `jsMoveAttackersTowardEnemy(24)` sends vipers toward Bot Refinery 2 via `moveCmd`
3. `jsAttackEnemyOfType(24)` uses `getNearestEnemyId` to get server-known enemy ID and sends `attackCmd`
4. Retry loop (10s interval) re-sends move + attack commands
5. If vipers drop below 2, fabricate 4 more from Factory
6. Vipers die en route (killed by bot defenders around y≈40-80) but enough survive to reach the target

### Key Code
```java
// In jsAttackEnemyOfType — fallback when enemy not rendered:
var enemyId = baseUi.getNearestEnemyId(pos.getX(), pos.getY(), enemyItemTypeId);
if (enemyId > 0) {
    gameCmd.attackCmd(attackerIds, enemyId);
    return 'attack by ID: enemyId=' + enemyId;
}
```

## Levels 7-9 Code (Written, Untested)

### Level 7: Fabricate Hydra, Kill Bot Hydra
```java
// Quest 387: Fabricate Hydra from Dockyard
gamePage.jsFabricate(DOCKYARD, HYDRA);   // Dockyard=11, Hydra=12
// Quest 388: Kill (Bot) Hydra
gamePage.jsAttackEnemyOfTypeUntilDone(BOT_HYDRA);  // BotHydra=10
```
**Potential issues**: Bot Hydra location unknown — may require camera move + moveCmd like level 5. The `jsAttackEnemyOfTypeUntilDone` pattern should handle this (it uses `jsMoveAttackersTowardEnemy` + `getNearestEnemyId` fallback).

### Level 8: Transporter + Builder to Phase 2
```java
// Quest 389: Fabricate Transporter from Dockyard
gamePage.jsFabricate(DOCKYARD, TRANSPORTER);  // Transporter=18
// Quest 392: Move Builder to Phase 2 region
double[] regionCenter = gamePage.getQuestRegionCenter();  // Uses bridge
gamePage.jsLoadIntoTransporter(BUILDER);       // loadContainerCmd
gamePage.jsMoveItemsOfType(TRANSPORTER, destX, destY);  // moveCmd
// Retry loop: unload → wait → move again until quest completes
gamePage.waitForQuestCompletedWithRetry(() -> {
    gamePage.jsUnloadTransporter();
    gamePage.jsMoveItemsOfType(TRANSPORTER, destX, destY);
}, "Region", 120);
```
**Potential issues**:
- `getQuestRegionCenter()` must work (same bridge as level 6)
- `loadContainerCmd` sends through `GameEngineControl.loadContainerCmdIds()` which was fixed for IdsDto
- `requestUnload` uses `itemCockpitBridge.requestUnload(transporterId)` — need to verify this proxy exists
- Fallback coordinates (200, 500) if bridge returns null — may not be in the quest region

### Level 9: Sell, Relocate, Rebuild
```java
// Move camera to builder's Phase 2 location
double[] builderPos = gamePage.jsGetOwnItemPosition(BUILDER);
gamePage.jsMoveCamera(builderPos[0], builderPos[1]);
// Quest 393: Sell Factory
gamePage.jsSellItemsOfType(FACTORY);  // uses itemCockpitBridge.sellItems()
// Quest 395: Build Factory in Phase 2 start region
gamePage.buildViaBuilder(FACTORY);     // standard build (near builder)
// Quest 396: Radar + Powerplant
gamePage.buildViaBuilder(RADAR);
gamePage.buildViaBuilder(POWERPLANT);
// Quest 400: Fabricate 2 Harvesters + 6 Vipers
// Quest 401: Sell Dockyard
gamePage.jsSellItemsOfType(DOCKYARD);
```
**Potential issues**:
- Quest 395 may require building in a specific start region (use `buildViaBuilderInQuestRegion` instead of `buildViaBuilder`)
- Quest 400 progress text check: `waitForQuestProgressContaining("Harvester")` — verify this matches the actual quest progress UI
- Selling Dockyard at the end — the Dockyard may be far away (in Phase 1 area) while the builder is in Phase 2. `jsSellItemsOfType` sells via `itemCockpitBridge.sellItems(ids)` which should work regardless of camera/distance

## What Was Already Solved
1. **Admin cleanup before tests**: `AdminApiClient` deletes human bases and restarts planet warm
2. **`waitForQuestCompleted()` missing transitions**: Removed; `verifyMainCockpit(N)` confirms level transitions
3. **Fabrication race condition**: Uses `waitForOwnItemCountByType()` after `jsFabricate()`
4. **Quest count tracking**: Level 4's Viper quest fabricates 3 Vipers counting from current
5. **`fabricateCmd` doesn't exist**: Uses `itemCockpitBridge.requestFabricate(factoryIds[], itemTypeId)`
6. **`sellItems`/`requestUnload`**: Uses `itemCockpitBridge.sellItems()`/`requestUnload()`
7. **Level 2 destroy quest**: Uses `jsAttackEnemyExcludingType(BOT_REFINERY_2)` to preserve enemy for level 5
8. **Attacker readiness**: `waitForAttackerReady()` waits for buildup >= 1.0 before attacking
9. **waitForQuestDoneWithRetry**: Fixed to not use Angular tick in polling loop, increased timeout to 120s
10. **`isIdle` not a function**: Removed from `logAttackerState`, wrapped logging in try-catch
11. **Viper auto-fabrication**: If attackers die during combat, fabricate new ones from Factory
12. **moveCmd IdsDto fix**: All `*CmdIds` methods convert `int[]` → `IdsDto` before sending to worker
13. **getNearestEnemyId proxy**: Added to `AngularProxyFactory` for server-side enemy lookup
14. **Attack by server-known ID**: `jsAttackEnemyOfType` falls back to `getNearestEnemyId` when enemy not rendered
15. **MoveTargetOutOfBounds**: `placeOnFreePosition` wraps `clickCanvasAt` in try-catch to handle out-of-bounds offsets

## Key Patterns
- **JS bridge access**: `window.gwtAngularFacade.babylonRenderServiceAccess` for rendered items, `.gameCommandService` for commands, `.baseItemUiService` for server-side queries, `.itemCockpitBridge` for build/fabricate/sell, `.inGameQuestVisualizationService` for quest data
- **Diplomacy strings**: `'OWN'`, `'ENEMY'`, `'RESOURCE'` (not numeric)
- **Angular zone**: `window.__e2eNgZone.run(fn)` for change detection, `window.__e2eAppRef.tick()` to force render (but NOT in polling loops)
- **Item type IDs**: Builder=1, Harvester=2, Viper=3, Factory=4, Radar=6, Powerplant=7, (Bot)Hydra=10, Dockyard=11, Hydra=12, Transporter=18, Tower=21, (Bot)Refinery=22, House=23, (Bot)Refinery2=24
- **Camera move**: `jsMoveCamera(x, y)` sets `camera.target.x = x; camera.target.z = y;` (z = game Y axis)
- **Quest region API**: `/rest/quest-controller/readMyOpenQuests` exists but returns empty for anonymous sessions
- **Build placement**: `buildViaBuilder` clicks build button → waits for placer active → `placeOnFreePosition()` clicks canvas offsets → waits for placer inactive. For quest regions, use `buildViaBuilderInQuestRegion` which tries bridge first, then camera grid fallback.
- **Quest region bridge**: `getQuestRegionCenter()` calls `inGameQuestVisualizationService.getActiveQuestPlaceConfig()` → reads position or polygon2D centroid

## GamePage.java Method Reference

### Canvas & Loading
| Method | Line | Description |
|--------|------|-------------|
| `waitForCanvasPresent()` | 39 | Waits for `canvas.canvas` element |
| `waitForGameReady()` | 43 | Waits for loading overlay to disappear |
| `isCanvasDisplayed()` | 47 | Checks canvas visibility |

### Quest Cockpit
| Method | Line | Description |
|--------|------|-------------|
| `waitForQuestCockpitVisible()` | 83 | Waits for quest-cockpit element |
| `getQuestTitle()` | 91 | Reads quest title text |
| `waitForQuestTitle(title)` | 95 | Waits for specific quest title |
| `waitForQuestProgressContaining(text)` | 128 | Waits for progress text substring |
| `waitForQuestCompleted()` | 120 | Waits for all progress rows done |
| `waitForQuestDoneWithRetry(action, title)` | 964 | Polls quest done, retries action every 10s |
| `waitForQuestCompletedWithRetry(action, title, timeout)` | 957 | Public version with custom timeout |

### Build & Place
| Method | Line | Description |
|--------|------|-------------|
| `waitForBaseItemPlacerActive()` | 162 | Waits for placer mode active |
| `placeOnFreePosition()` | 194 | Spiral canvas click to find free terrain |
| `getQuestRegionCenter()` | 222 | Bridge: reads PlaceConfig from active quest |
| `placeInQuestRegion()` | 257 | Camera grid search + canvas click fallback |
| `jsBuildAtPosition(type, x, y)` | 285 | Direct buildCmd at terrain coordinates |
| `buildViaBuilder(type)` | 1220 | Click build button → placer → place |
| `buildViaBuilderInQuestRegion(type)` | 1236 | Bridge-first build, camera grid fallback |

### Item Selection & Cockpit
| Method | Line | Description |
|--------|------|-------------|
| `selectItemByType(typeId)` | 1190 | Selects own item via `actionService.onItemClicked` |
| `waitForItemCockpitVisible()` | 310 | Waits for item cockpit to show |
| `waitForBuildButtonForItemType(typeId)` | 358 | Waits for build button enabled |
| `clickBuildButtonForItemType(typeId)` | 347 | Clicks specific build button |

### Game Commands (JS Bridge)
| Method | Line | Description |
|--------|------|-------------|
| `jsHarvestNearest()` | 572 | Sends harvesters to nearest resource |
| `jsAttackNearest()` | 595 | Attacks nearest rendered enemy |
| `jsAttackEnemyOfType(typeId)` | 637 | Attacks specific type, fallback to getNearestEnemyId |
| `jsAttackEnemyExcludingType(typeId)` | 732 | Attacks any enemy except excluded type |
| `jsAttackEnemyOfTypeUntilDone(typeId)` | 804 | Retry loop: move + attack until quest done |
| `jsAttackEnemyExcludingTypeUntilDone(typeId)` | 763 | Retry loop: attack non-excluded until done |
| `jsMoveAttackersTowardEnemy(typeId)` | 831 | moveCmd toward enemy via getNearestEnemyPosition |
| `jsMoveCamera(x, y)` | 620 | Sets camera target position |
| `jsFabricate(factoryType, unitType)` | 1030 | Fabricates unit via itemCockpitBridge |
| `jsSellItemsOfType(typeId)` | 1050 | Sells all items of type via itemCockpitBridge |
| `jsMoveItemsOfType(typeId, x, y)` | 1010 | Moves items of type to position |
| `jsLoadIntoTransporter(typeId)` | 1070 | loadContainerCmd for type into transporter(18) |
| `jsUnloadTransporter()` | 1094 | requestUnload via itemCockpitBridge |
| `jsGetOwnItemPosition(typeId)` | 1111 | Returns [x,y] of first item of type |
| `jsHasEnemyOfType(typeId)` | 1133 | Checks if enemy type is rendered |

### Verification & Debug
| Method | Line | Description |
|--------|------|-------------|
| `verifyMainCockpit(level)` | 1149 | Asserts main cockpit visible + level |
| `verifyQuestCockpit(title)` | 1157 | Waits for quest title |
| `setupErrorCapture()` | 901 | Installs console.error/warn capture |
| `logBrowserErrors()` | 881 | Prints captured browser errors |
| `logAttackerState()` | 927 | Prints own + enemy item positions |
| `logViperPositions(label)` | 865 | Prints viper positions |

## WASM Bridge Architecture (for debugging)

Command flow (SLAVE mode):
```
JS: gameCmd.attackCmd([141], 126)
  -> JsGameCommandService.ArrayIntCmdCallback (WASM functor)
  -> GameEngineControl.attackCmdIds(int[], int)
  -> sendToWorker(COMMAND_ATTACK, IdsDto, targetId)   // Fixed: was int[], now IdsDto
  -> TeaVMClientMarshaller.marshall() -> postMessage to Worker
  -> TeaVMWorkerMarshaller.deMarshall() -> IdsDto + Integer
  -> GameEngineWorker.dispatch() -> commandService.attack(IdsDto, int)
  -> CommandService.attack() -> pathfinding + AttackCommand
  -> executeCommand() -> SLAVE: gameLogicService.onSlaveCommandSent()
  -> serverConnection.onCommandSent(baseCommand)  // WebSocket to server
  -> Server processes, sends tick updates back
```

Key files:
- `razarion-client-teavm/.../jso/facade/JsGameCommandService.java` - JS proxy with functors
- `razarion-client-teavm/.../bridge/AngularProxyFactory.java` - Service proxies (getNearestEnemyId + getActiveQuestPlaceConfig)
- `razarion-client-teavm/.../TeaVMClientMarshaller.java` - Client-side marshalling
- `razarion-client-worker-teavm/.../TeaVMWorkerMarshaller.java` - Worker demarshalling
- `razarion-ui-service/.../control/GameEngineControl.java` - Command dispatch (IdsDto fix here)
- `razarion-share/.../planet/CommandService.java` - Command execution + pathfinding
- `razarion-share/.../GameEngineWorker.java` - Worker dispatch + SLAVE forwarding

## Next Steps (Priority Order)
1. **Test Level 6 Dockyard placement** — Rebuild (`mvn clean install -DskipTests`), restart server, run E2E test. The `getActiveQuestPlaceConfig` bridge should return coordinates. If `jsBuildAtPosition` fails, try moving the builder to the region first via `moveCmd`.
2. **Test Levels 7-9** — Code is already written. Run sequentially after level 6 passes.
3. **Level 7 potential fix**: If Bot Hydra is far away, the existing `jsAttackEnemyOfTypeUntilDone` pattern with `getNearestEnemyId` should handle it.
4. **Level 8 potential fix**: If quest region bridge fails, hardcode fallback coordinates from the terrain minimap.
5. **Level 9 potential fix**: Check if Factory build needs `buildViaBuilderInQuestRegion` instead of `buildViaBuilder` (Quest 395 might require placement in Phase 2 start region).
6. **Clean up debug logging** (remove `logViperPositions`, quest mesh debugging, etc.)
7. **Commit the working test**
