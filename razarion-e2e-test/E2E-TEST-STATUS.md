# E2E Test GameStartIT - Development Status

Continue developing the `GameStartIT.fullGameFlow()` E2E test that covers Phase 1 (levels 1-9) of the Razarion RTS game. **Levels 1-5 pass. Level 6 is in progress** — Dockyard placement fails because the quest region is not near the player base; the camera must be moved to the quest region (near the coastline) before placing.

## Files Modified

### E2E Test Files
- `razarion-e2e-test/src/test/java/com/btxtech/e2e/smoke/GameStartIT.java` - Test flow for all 9 levels
- `razarion-e2e-test/src/test/java/com/btxtech/e2e/page/GamePage.java` - Page object with JS commands
- `razarion-e2e-test/src/test/java/com/btxtech/e2e/base/AdminApiClient.java` - REST API cleanup (delete bases, restart planet)

### Server/WASM Files (changes need rebuild + server restart)
- `razarion-frontend/src/app/app.component.ts` - Exposes `window.__e2eNgZone` and `window.__e2eAppRef`
- `razarion-ui-service/.../item/BaseItemUiService.java` - Added `getNearestEnemyId()` method — **DONE**
- `razarion-client-teavm/.../bridge/AngularProxyFactory.java` - Added proxy for `getNearestEnemyId` — **DONE**
- `razarion-ui-service/.../control/GameEngineControl.java` - Fixed `int[]` → `IdsDto` marshalling in all `*CmdIds` methods — **DONE**

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
| 6 | **BLOCKED** | Build Dockyard in quest region — quest region is NOT near base, camera search needed. See details below. |
| 7 | NOT TESTED | Fabricate Hydra, kill Bot Hydra |
| 8 | NOT TESTED | Fabricate Transporter, move Builder to region |
| 9 | NOT TESTED | Sell, relocate, rebuild |

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

### What Was Tried
1. **Canvas click spiral grid** (73 positions around canvas center) — fails because camera is at base, quest region is off-screen
2. **REST API `/rest/quest-controller/readMyOpenQuests`** — returns empty array (anonymous browser session has no authenticated quests)
3. **Babylon scene mesh search** for quest markers — no quest region meshes found; only "Base Item Placer" mesh present
4. **Camera search grid** across 24 positions from (50,100) to (250,250) — **currently running, untested result**

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
1. **Query quest PlaceConfig from game client**: The `InGameQuestVisualizationService` (Java UI service) stores the active quest's `PlaceConfig`. If exposed via the WASM bridge, JS could read it. Key method: `quest.getConditionConfig().getComparisonConfig().getPlaceConfig()`. Fields: `position` (center), `radius`, or `polygon2D.corners[]`.
2. **Expose quest region via new proxy**: Add a proxy method like `getActiveQuestRegionCenter()` in `AngularProxyFactory.java` that returns the PlaceConfig center. This requires server rebuild.
3. **Use `finalizeBuildCmd` directly**: Instead of clicking canvas, call `gameCommandService.finalizeBuildCmd(builderIds, x, y)` at terrain coordinates within the quest region. Need to know the coordinates.
4. **Camera brute-force grid search**: Move camera across Phase 1 area in 50m steps, try placement at each. Slow (~2min per camera position with 73 clicks) but guaranteed to find the region eventually.
5. **Check if quest region mesh exists**: The "Dockyard" transform node might contain child meshes with position data. Investigate with `scene.getTransformNodeByName('Dockyard').getChildren()`.

### PlaceConfig Architecture (for implementing solution 1 or 2)
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
- **JS bridge access**: `window.gwtAngularFacade.babylonRenderServiceAccess` for rendered items, `.gameCommandService` for commands, `.baseItemUiService` for server-side queries, `.itemCockpitBridge` for build/fabricate/sell
- **Diplomacy strings**: `'OWN'`, `'ENEMY'`, `'RESOURCE'` (not numeric)
- **Angular zone**: `window.__e2eNgZone.run(fn)` for change detection, `window.__e2eAppRef.tick()` to force render (but NOT in polling loops)
- **Item type IDs**: Builder=1, Harvester=2, Viper=3, Factory=4, Radar=6, Powerplant=7, (Bot)Hydra=10, Dockyard=11, Hydra=12, Transporter=18, Tower=21, (Bot)Refinery=22, House=23, (Bot)Refinery2=24
- **Camera move**: `jsMoveCamera(x, y)` sets `camera.target.x = x; camera.target.z = y;` (z = game Y axis)
- **Quest region API**: `/rest/quest-controller/readMyOpenQuests` exists but returns empty for anonymous sessions
- **Build placement**: `buildViaBuilder` clicks build button → waits for placer active → `placeOnFreePosition()` clicks canvas offsets → waits for placer inactive. For quest regions, use `buildViaBuilderInQuestRegion` which moves camera first.

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
- `razarion-client-teavm/.../bridge/AngularProxyFactory.java` - Service proxies (getNearestEnemyId added here)
- `razarion-client-teavm/.../TeaVMClientMarshaller.java` - Client-side marshalling
- `razarion-client-worker-teavm/.../TeaVMWorkerMarshaller.java` - Worker demarshalling
- `razarion-ui-service/.../control/GameEngineControl.java` - Command dispatch (IdsDto fix here)
- `razarion-share/.../planet/CommandService.java` - Command execution + pathfinding
- `razarion-share/.../GameEngineWorker.java` - Worker dispatch + SLAVE forwarding

## Next Steps (Priority Order)
1. **Fix Level 6 Dockyard placement** — Find the quest region coordinates. Best approach: expose quest PlaceConfig via WASM bridge proxy (option 2 above) or investigate the "Dockyard" scene node.
2. **Level 7**: Fabricate Hydra + kill Bot Hydra — should work with existing `jsFabricate` + `jsAttackEnemyOfTypeUntilDone` patterns
3. **Level 8**: Transporter movement — `moveCmd` works now. `jsLoadIntoTransporter` + `jsMoveItemsOfType` should work. Need quest region for destination (use `getQuestRegionCenter`).
4. **Level 9**: Sell + build + fabricate in new region — use `jsSellItemsOfType`, then `buildViaBuilderInQuestRegion` for region-aware placement
5. **Clean up debug logging** (remove `logViperPositions`, quest mesh debugging, etc.)
6. **Commit the working test**
