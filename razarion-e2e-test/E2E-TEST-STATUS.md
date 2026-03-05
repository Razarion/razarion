# E2E Test GameStartIT - Development Status

Continue developing the `GameStartIT.fullGameFlow()` E2E test that covers Phase 1 (levels 1-9) of the Razarion RTS game. Levels 1-4 pass reliably. The test is blocked at **level 5** and has a **flaky selection issue**.

## Files Modified
- `razarion-e2e-test/src/test/java/com/btxtech/e2e/smoke/GameStartIT.java` - Test flow
- `razarion-e2e-test/src/test/java/com/btxtech/e2e/page/GamePage.java` - Page object with JS commands
- `razarion-frontend/src/app/app.component.ts` - Exposes `window.__e2eNgZone` and `window.__e2eAppRef`

## How to Run
```bash
cd razarion-e2e-test
mvn verify -Dit.test=GameStartIT#fullGameFlow -De2e.headless=false -pl .
```
Uses failsafe plugin (not surefire). Set JAVA_HOME and Maven path from CLAUDE.md first.

## Problem 1: Level 5 - Bot Refinery 2 Not Found
**Quest**: "Destroy - Refinery 2 destroyed 0 of 1"
**Issue**: `jsAttackEnemyOfType(24)` can't find enemy type 24 (Bot Refinery 2). Debug output: `enemy type 24 not found, available types: [22]`. Only type 22 (Bot Refinery) is visible via `getBabylonBaseItemsByDiplomacy('ENEMY')`.

**Root cause**: The Bot Refinery 2 (type 24) exists in the game but is NOT rendered — it's too far from the player's viewport. `getBabylonBaseItemsByDiplomacy('ENEMY')` only returns rendered items.

**Partial fix in progress**: `jsAttackEnemyOfType` now tries `baseItemUiService.getNearestEnemyPosition(fromX, fromY, enemyItemTypeId, true)` to find the enemy position server-side, then moves attackers there with `moveCmd`, then polls until the enemy is rendered and sends `attackCmd`. This approach was not yet tested.

**Key API**: `window.gwtAngularFacade.baseItemUiService.getNearestEnemyPosition(x, y, itemTypeId, itemTypeIdUsed)` returns a `Vertex` with `getX()`/`getY()` or null.

## Problem 2: Flaky Selection (Intermittent)
**Issue**: `selectItemByType(BUILDER)` sometimes fails — the item cockpit doesn't become visible after selection.

**Current approach**: Retry loop that calls `actionService.onItemClicked()` inside `NgZone.run()` + `AppRef.tick()` each iteration, polling for `item-cockpit` element visibility. Works ~80% of the time.

**Root cause**: Babylon.js runs outside Angular zone. Even with `NgZone.run()`, change detection sometimes doesn't propagate to render the item cockpit component.

## What Was Already Solved
1. **`waitForQuestCompleted()` missing transitions**: Removed from all level-ending quests. Next level's `verifyMainCockpit(N)` confirms transitions instead.
2. **Fabrication race condition**: Changed from fixed `Thread.sleep(2000)` to `waitForOwnItemCountByType()` after each `jsFabricate()` call.
3. **Quest count tracking**: Level 4's Viper quest requires 3 NEW Vipers (not total), so loop always fabricates 3.
4. **`fabricateCmd` doesn't exist**: Uses `itemCockpitBridge.requestFabricate(factoryIds[], itemTypeId)` instead.
5. **`sellItems`/`requestUnload`**: Uses `itemCockpitBridge.sellItems()`/`requestUnload()`.
6. **Level 2 destroy quest**: Uses `jsAttackEnemyExcludingType(BOT_REFINERY_2)` to avoid killing the Refinery 2 needed for level 5.

## Key Patterns
- **JS bridge access**: `window.gwtAngularFacade.babylonRenderServiceAccess` for rendered items, `.gameCommandService` for commands, `.baseItemUiService` for server-side queries, `.itemCockpitBridge` for build/fabricate/sell
- **Diplomacy strings**: `'OWN'`, `'ENEMY'`, `'RESOURCE'` (not numeric)
- **Angular zone**: `window.__e2eNgZone.run(fn)` for change detection, `window.__e2eAppRef.tick()` to force render
- **Item type IDs**: Builder=1, Harvester=2, Viper=3, Factory=4, Radar=6, Powerplant=7, (Bot)Hydra=10, Dockyard=11, Hydra=12, Transporter=18, Tower=21, (Bot)Refinery=22, House=23, (Bot)Refinery2=24

## Debug Logging
GamePage currently has `System.out.println("[E2E] ...")` debug logging in `jsHarvestNearest`, `jsAttackNearest`, `jsAttackEnemyOfType`, `jsAttackEnemyExcludingType`, `waitForQuestProgressContaining`, and `waitForLevel`. Remove these once the test is stable.

## Next Steps
1. Test the `getNearestEnemyPosition` + move + attack approach for level 5
2. If Bot Refinery 2 is never found via `getNearestEnemyPosition`, check bot configs to see if it's actually spawned (may need to query MongoDB or the admin API)
3. Continue through levels 6-9 once level 5 passes
4. Clean up debug logging
5. Commit the working test
