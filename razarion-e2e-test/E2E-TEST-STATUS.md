# E2E Test GameStartIT - Development Status

Continue developing the `GameStartIT.fullGameFlow()` E2E test that covers Phase 1 (levels 1-9) of the Razarion RTS game. **Levels 1-4 pass reliably. Level 5 is blocked** because the Bot Refinery 2 enemy is not rendered and the attack bridge needs a `getNearestEnemyId` method. Levels 6-9 are not yet tested.

## Files Modified
- `razarion-e2e-test/src/test/java/com/btxtech/e2e/smoke/GameStartIT.java` - Test flow
- `razarion-e2e-test/src/test/java/com/btxtech/e2e/page/GamePage.java` - Page object with JS commands
- `razarion-e2e-test/src/test/java/com/btxtech/e2e/base/AdminApiClient.java` - REST API cleanup (delete bases, restart planet)
- `razarion-frontend/src/app/app.component.ts` - Exposes `window.__e2eNgZone` and `window.__e2eAppRef`
- `razarion-ui-service/.../item/BaseItemUiService.java` - Added `getNearestEnemyId()` method (IN PROGRESS)
- `razarion-client-teavm/.../bridge/AngularProxyFactory.java` - Needs proxy for `getNearestEnemyId` (NOT YET DONE)

## How to Run
```bash
cd razarion-e2e-test
export JAVA_HOME="C:\dev\tech\Java\java-21-openjdk-21.0.1.0.12-3.win.jdk.x86_64"
export PATH="/c/dev/tech/apache-maven-3.9.6/bin:/c/dev/tech/Java/java-21-openjdk-21.0.1.0.12-3.win.jdk.x86_64/bin:$PATH"
mvn verify -Dit.test=GameStartIT#fullGameFlow -pl .
```
Uses failsafe plugin (not surefire). Add `-De2e.headless=false` to watch the browser.

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
| Deploy | PASS | Place builder on free terrain |
| 1 | PASS | Build Factory, fabricate Harvester |
| 2 | PASS | Harvest, fabricate Viper, kill enemy (excluding Bot Refinery 2) |
| 3 | PASS | Build Radar, build Powerplant |
| 4 | PASS | Harvest 30, fabricate 3 Vipers |
| 5 | **BLOCKED** | Kill Bot Refinery 2 (type 24) - enemy not rendered, needs `getNearestEnemyId` |
| 6 | NOT TESTED | Build Dockyard in quest region |
| 7 | NOT TESTED | Fabricate Hydra, kill Bot Hydra |
| 8 | NOT TESTED | Fabricate Transporter, move Builder to region - **`moveCmd` is broken** |
| 9 | NOT TESTED | Sell, relocate, rebuild |

## Critical Finding: `moveCmd` Does NOT Work

The `gameCommandService.moveCmd(ids, x, y)` call goes through `MoveCmdCallback` functor with `double` parameters in the TeaVM WASM-GC bridge. **It silently fails** - the JS call succeeds without error, but the unit never moves. This was confirmed over multiple test runs with position tracking.

- `attackCmd` and `harvestCmd` use `ArrayIntCmdCallback` with `int` parameters and **work correctly**
- The `attackCmd` auto-navigates units to the target (pathfinding in `CommandService.attack()`)
- The game runs in **SLAVE mode** - commands go: JS -> WASM client -> Worker -> Server (via WebSocket) -> Server processes -> tick updates back

**Impact on test**: Level 8 requires `jsMoveItemsOfType(TRANSPORTER, 200, 500)` which uses `moveCmd`. Will need a workaround (e.g., attack a nearby enemy to force movement, or fix the `MoveCmdCallback` functor).

**Potential fix**: Investigate the `MoveCmdCallback` functor in `JsGameCommandService.java:72-74`. The issue may be how TeaVM WASM-GC handles `double` parameters in `@JSFunctor` interfaces. Compare with the working `ArrayIntCmdCallback` which uses `int`.

## Problem: Level 5 - Bot Refinery 2 Not Rendered

**Quest**: "Destroy - Refinery 2 destroyed 0 of 1"

**Issue**: `jsAttackEnemyOfType(24)` can't find enemy type 24 in rendered items. The Bot Refinery 2 exists at ~(145, 125) but is too far from the player's base at ~(185, 20) to be rendered by Babylon.js.

**Solution in progress** (partially implemented):
1. Added `getNearestEnemyId(fromX, fromY, enemyItemTypeId)` to `BaseItemUiService.java` - **DONE**
2. Need to add proxy method in `AngularProxyFactory.java` - **NOT DONE**
3. Need to rebuild TeaVM WASM module (`mvn clean install -DskipTests` from root)
4. The E2E test's `jsAttackEnemyOfType` then calls `baseUi.getNearestEnemyId(...)` to get the server-side enemy ID and sends `attackCmd` directly with that ID

**Alternative approach** (no Java changes needed): The `attackCmd` works with any valid enemy ID. The `baseItemUiService.getNearestEnemyPosition()` already works and returns the enemy position. If we could iterate `nativeSyncBaseItemTickInfos` from JS to find the ID... but the proxy doesn't expose raw tick infos.

**Key API**: `window.gwtAngularFacade.baseItemUiService.getNearestEnemyPosition(x, y, itemTypeId, true)` returns `Vertex` with `getX()`/`getY()`.

## Problem: waitForQuestDoneWithRetry Timing

The `waitForQuestDoneWithRetry` method was initially timing out even when the quest completed. Root cause: calling `window.__e2eAppRef.tick()` in the polling loop caused issues - all subsequent `executeScript` calls would fail/block for the rest of the wait period.

**Fix applied**: Removed Angular tick from the wait loop. Uses a 120-second `WebDriverWait` with separate try/catch for logging vs condition checking. The quest title check `!currentQuestTitle.equals(title)` detects when the quest advances.

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

## Key Patterns
- **JS bridge access**: `window.gwtAngularFacade.babylonRenderServiceAccess` for rendered items, `.gameCommandService` for commands, `.baseItemUiService` for server-side queries, `.itemCockpitBridge` for build/fabricate/sell
- **Diplomacy strings**: `'OWN'`, `'ENEMY'`, `'RESOURCE'` (not numeric)
- **Angular zone**: `window.__e2eNgZone.run(fn)` for change detection, `window.__e2eAppRef.tick()` to force render (but NOT in polling loops)
- **Item type IDs**: Builder=1, Harvester=2, Viper=3, Factory=4, Radar=6, Powerplant=7, (Bot)Hydra=10, Dockyard=11, Hydra=12, Transporter=18, Tower=21, (Bot)Refinery=22, House=23, (Bot)Refinery2=24

## WASM Bridge Architecture (for debugging)

Command flow (SLAVE mode):
```
JS: gameCmd.attackCmd([141], 126)
  -> JsGameCommandService.ArrayIntCmdCallback (WASM functor)
  -> GameEngineControl.attackCmdIds(int[], int)
  -> sendToWorker(COMMAND_ATTACK, int[], targetId)
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
- `razarion-client-teavm/.../TeaVMClientMarshaller.java` - Client-side marshalling
- `razarion-client-worker-teavm/.../TeaVMWorkerMarshaller.java` - Worker demarshalling
- `razarion-share/.../planet/CommandService.java` - Command execution + pathfinding
- `razarion-share/.../GameEngineWorker.java` - Worker dispatch + SLAVE forwarding

## Next Steps (Priority Order)
1. **Fix level 5**: Complete `getNearestEnemyId` proxy in `AngularProxyFactory.java`, rebuild WASM, update `jsAttackEnemyOfType` to use it
2. **Test levels 6-7**: These use build + fabricate + attack - should work with existing methods
3. **Fix level 8**: `moveCmd` is broken. Options:
   - Fix `MoveCmdCallback` double-param issue in TeaVM WASM-GC
   - Workaround: use `loadContainerCmd` to load Builder into Transporter, then `attackCmd` toward an enemy near the target region to force movement
   - Workaround: add a REST endpoint that sends move commands server-side
4. **Test level 9**: Sell + build + fabricate - should mostly work
5. **Clean up debug logging**
6. **Commit the working test**
