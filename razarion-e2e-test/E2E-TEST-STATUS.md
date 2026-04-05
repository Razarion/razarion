# E2E Test GameStartIT - Development Status

## Current Status (2026-03-28)

**Levels 1-5 PASS. Level 6 BLOCKED.**

| Level | Status | Quest | Notes |
|-------|--------|-------|-------|
| Deploy | **PASS** | Deploy unit | Place builder on free terrain |
| 1 | **PASS** | Build + Build | Build Factory, fabricate Harvester |
| 2 | **PASS** | Harvest + Build + Destroy | Harvest, fabricate Viper, kill enemy (excluding Bot Refinery 2) |
| 3 | **PASS** | Build + Build | Build Radar, build Powerplant |
| 4 | **PASS** | Harvest + Build | Harvest 30, fabricate 3 Vipers |
| 5 | **PASS** | Destroy | Kill Bot Refinery 2 — uses `getNearestEnemyId` + mass viper fabrication |
| 6 | **BLOCKED** | Region | Build Dockyard in quest region — see details below |
| 7 | **CODE WRITTEN** | Build + Destroy | Fabricate Hydra from Dockyard, kill Bot Hydra |
| 8 | **CODE WRITTEN** | Build + Region | Fabricate Transporter, load Builder, move to Phase 2 |
| 9 | **CODE WRITTEN** | Sell + Build + Build + Sell | Sell Factory, rebuild in Phase 2, fabricate units, sell Dockyard |

## BLOCKED: Level 6 - Dockyard in Quest Region

### Problem
The quest requires the Dockyard to be built inside a specific polygon region near the coastline (~y=200-280). The player base is at ~(178, 20), far from this region.

### Quest Region Polygon (PlaceConfig ID 1797)
From database query:
```
X range: 113 - 282
Y range: 150 - 282
Centroid: ~(210, 225)
```
The region is a coastal strip running along the water edge.

### What Works
- `jsMoveById(builderId, x, y)` — sends move command by item ID (works regardless of rendering)
- `jsBuildById(builderId, DOCKYARD, x, y)` — sends build command by item ID (works regardless of rendering)
- Both commands are confirmed sent and accepted by the game engine

### What Doesn't Work
The `buildCmd` is sent to coordinates (200, 245) inside the polygon, but the quest remains at "Dockyard on region 0 of 1". Possible causes:

1. **Build position snapped**: The game engine may snap the build position to the nearest valid terrain, which might be outside the polygon
2. **SYNC_ITEM_POSITION timing**: The condition checks the Dockyard's position only after it finishes building (buildup=1.0), not when the buildCmd is issued
3. **Water/terrain constraint**: The Dockyard might require water-adjacent terrain. If (200,245) is on water, the engine rejects/adjusts the position
4. **Build range**: Builder must be close enough to actually build — if builder didn't arrive at (200,240) in time, the buildCmd might fail silently

### Approaches Tried
1. **Babylon rendering approach** (`buildViaBuilderInQuestRegion`): Fails because builder goes out of render range when camera moves to quest region
2. **Camera follow approach**: Builder moves ~5-10 units/sec, camera lost tracking after 12s
3. **ID-based commands** (current): `jsMoveById` + `jsBuildById` — commands sent but quest doesn't complete. Dockyard appears to be built at wrong position or condition not met

### Next Steps to Try
1. **Verify build position**: Add logging to check where the Dockyard actually ends up (use `jsGetOwnItemPosition(DOCKYARD)` after build)
2. **Wait for buildup**: The `SYNC_ITEM_POSITION` condition likely checks position only after the building is fully constructed. Add explicit wait for Dockyard buildup completion
3. **Try different coordinates**: Test points deeper inside the polygon (e.g., 250,180 or 160,260)
4. **Check terrain**: Use the terrain minimap to verify which polygon coordinates are valid land (not water)
5. **Debug server-side**: Check server logs for the quest condition evaluation

## Files

### E2E Test Files
- `smoke/GameStartIT.java` — Test flow for all 9 levels
- `page/GamePage.java` — Page object with JS bridge commands (~1300 lines)
- `base/AdminApiClient.java` — REST API cleanup (delete bases, restart planet)
- `base/BaseE2eTest.java` — Abstract base class (WebDriver setup)
- `base/E2eTestWatcher.java` — Screenshots on failure + recording
- `config/WebDriverConfig.java` — Chrome WebDriver configuration

### Key GamePage Methods (ID-based, rendering-independent)
| Method | Description |
|--------|-------------|
| `jsGetOwnItemId(typeId)` | Gets game ID of first own item of type (requires rendering) |
| `jsMoveById(itemId, x, y)` | Sends moveCmd by item ID (rendering-independent) |
| `jsBuildById(builderId, typeId, x, y)` | Sends buildCmd by builder ID (rendering-independent) |

### Key GamePage Methods (rendering-dependent)
| Method | Description |
|--------|-------------|
| `jsMoveItemsOfType(typeId, x, y)` | Moves all items of type (needs rendering) |
| `jsBuildAtPosition(typeId, x, y)` | Builds via first visible builder (needs rendering) |
| `jsGetOwnItemPosition(typeId)` | Gets position of first own item (needs rendering) |
| `jsFabricate(factoryType, unitType)` | Fabricates unit (needs rendering) |
| `jsAttackEnemyOfTypeUntilDone(typeId)` | Retry loop: move + attack until quest done |
| `jsMoveCamera(x, y)` | Sets Babylon camera target position |

## Item Type IDs
| ID | Type |
|----|------|
| 1 | Builder |
| 2 | Harvester |
| 3 | Viper |
| 4 | Factory |
| 6 | Radar |
| 7 | Powerplant |
| 10 | (Bot) Hydra |
| 11 | Dockyard |
| 12 | Hydra |
| 18 | Transporter |
| 21 | Tower |
| 22 | (Bot) Refinery |
| 23 | House |
| 24 | (Bot) Refinery 2 |

## How to Run

```bash
# Set JDK 21 environment
export JAVA_HOME="C:\dev\tech\Java\java-21-openjdk-21.0.1.0.12-3.win.jdk.x86_64"
export PATH="/c/dev/tech/apache-maven-3.9.6/bin:/c/dev/tech/Java/java-21-openjdk-21.0.1.0.12-3.win.jdk.x86_64/bin:$PATH"

# Run full game flow test (headless)
mvn verify -Pe2e -DskipTests -Dit.test=GameStartIT#fullGameFlow -pl razarion-e2e-test

# With visible browser
mvn verify -Pe2e -DskipTests -Dit.test=GameStartIT#fullGameFlow -pl razarion-e2e-test "-De2e.headless=false"
```

After changes to server/WASM files, rebuild and restart:
```bash
mvn clean install -DskipTests   # From root
# Then restart server
```
