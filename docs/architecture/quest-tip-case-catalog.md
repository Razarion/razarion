# Quest Tip Case Catalog

As of 2026-09-18. This is the basis for the tip test bed. Every row becomes a test case that runs
against a simulated world and checks **what the player sees**, not which state a task is in.

Status per case:

| Mark | Meaning |
|---|---|
| ✓ | proven correct (test or PROD measurement) |
| ~ | works in everyday play, but has no test |
| bed | result of the [test bed](#7-the-test-bed) |

Since 2026-09-18 the tips are the guide of the [redesign](quest-tip-redesign.md) and every case of the
test bed passes on it. Where a row says "the chain:", that is what the removed task chain did.
| ✗ | proven wrong (player report, PROD data, or unambiguous in the code) |
| ? | unverified - this is what the test bed is for |
| ❓ | expected behaviour not decided yet, see [Decisions](#6-decisions-and-analyses) |

The quest list and the configuration numbers come from the **local** DB; PROD may differ.

**Scope.** Tips are for beginners. They run on the quests of levels 1-8 (the last is 392) and, since 2026-09-25, on the quests of level 9 (393, 395, 396, 400, 401), and those quests are
prepared for them: each quest provides what the next one needs. A case that the quest chain
prepares against still has to be handled gracefully (no stuck chain, no wrong prompt), but it does
not have to teach anything.

---

## 1. Rules: what the player must see

Every case checks every rule at every point in time, not only the rule the case is about.

- **R1 There is always guidance.** While a quest with a tip is open and the player can act, exactly
  one piece of guidance is up: a prompt (arrow + text) on a visible object, a hint on a cockpit
  button, a place marker, or a direction arrow at the edge of the screen. Exceptions: while the
  unit is working (R3), and while the target does not exist (ATK-09, HRV-07) - then nothing until
  it is back.
- **R2 Never a step that is already done.** No "Click to select" on a unit that is selected -
  whether it is on screen or not. No button hint for something that is already ordered.
- **R3 Quiet while work is going on.** While the unit drives, builds, harvests or fights for the
  quest, nothing is shown - no prompt and no direction arrow to the unit, on screen or off.
- **R4 The arrow points at something that exists.** A living target, a real unit, a point *inside*
  a build region - never a stale position, never at nothing.
- **R5 Nothing left behind.** At most one prompt at a time. Quest done, quest changed or tips
  switched off: everything is gone at once.
- **R6 Recovers without the camera.** When the world changes (target dies, unit scrolls out or in,
  selection changes), the right guidance is back within ~1 s - without the player moving the
  camera.
- **R7 No exceptions.** The chain never throws; if it does anyway, it recovers and reports
  `CHAIN_ERROR`.
- **R8 The right stall reason.** If nothing moves for 30 s, tracking reports the reason a person
  watching would give.

---

## 2. The world: what the simulation has to reproduce

These are the rules the tips have failed on so far. The fake world has to follow them exactly,
otherwise the test bed tests our assumptions instead of the game.

- **W1 Rendered is not visible.** Instances exist inside the AABB of the view field
  (`BaseItemUiService`, `adjoinsCircleExclusive`). The AABB is larger than the visible trapezoid:
  a unit can have an instance and still not be on screen.
- **W2 Out = disposed, in = new instance, same id.** Prompt, click, idle and selection callbacks die
  with the old instance.
- **W3 Selection lives by id.** `selectedOwnItemIds`/`Types` keep units that are off screen,
  `getSelectedOwnItems()` holds only rendered ones. On scrolling back in, `tryReattachItem`
  re-attaches the selection - *before* the created listeners run.
- **W4 A fresh instance does not know its state yet.** The created listener fires before Java calls
  `setIdle()`; a new instance reports `idle = false` even if the unit is standing still.
- **W5 No removed listener for base items.** When a target dies, nobody is told (resources do have
  one).
- **W6 Off screen, only the worker knows.** `getNearestEnemyPosition`,
  `getNearestResourcePosition`, `getMyItemCount`. **There is no position query for own units off
  screen** - an own unit that was never seen cannot be pointed at today.
- **W7 Death and selling** dispose the instance *and* remove the id from the selection (fires a
  selection change).
- **W8 Commands take ticks.** It takes ticks until the unit stops reporting idle; without
  SharedArrayBuffer (Meta webview) considerably more.
- **W9 Cancelling, placing and a refused placement deactivate the placer.** The tip code says in
  several comments that the placer also clears the selection when it starts; no code that does so
  was found, so the fake world does not do it.
- **W10 The cockpit is rebuilt on every selection change.** Buttons disappear briefly, sit on
  another carousel page, or are inside the collapsed panel (compact layout).
- **W11 Bots respawn.** Targets disappear and reappear, in the extreme every 3 s.
- **W12 The direction arrow fires `onBecameVisible`** as soon as its *point* enters the view field -
  for a remembered position that is a place the unit left long ago.

---

## 3. Chains and quests

| Type | Main chain | Fallback chain (after the main chain) |
|---|---|---|
| BUILD | Select → StartBuildPlacer → SendBuild | Idle → Select → StartBuildPlacer → SendBuild |
| FABRICATE | Select → SendFabricate | Idle → Select → SendFabricate |
| HARVEST | Select → SendHarvest | Idle → Select → SendHarvest |
| ATTACK | Select → (SelectGroup) → SendAttack | Idle → Select → (SelectGroup) → SendAttack |

SelectGroup is added today whenever the quest names a target type. **Decided:** the first attack
(365) teaches the basics of attacking and gets no group tip; group selection is taught from 379 on
(see GRP-05).

| Level | Quest | Tip | Actor | Condition |
|---|---|---|---|---|
| 1 | 358 | BUILD | Builder | 1× Factory |
| 1 | 359 | FABRICATE | Factory | 1× Harvester |
| 2 | 363 | HARVEST | Harvester | harvest 5 |
| 2 | 364 | FABRICATE | Factory | 1× Viper |
| 2 | 365 | ATTACK | Viper | kill 1× (Bot1) Extractor |
| 3 | 361 | BUILD | Builder | 1× Radar |
| 3 | 362 | BUILD | Builder | 1× Powerplant |
| 4 | 366 | HARVEST | Harvester | harvest 10 |
| 4 | 369 | FABRICATE | Factory | **3×** Viper |
| 5 | 379 | ATTACK | Viper | kill 1× (Bot1) Refinery |
| 6 | 386 | BUILD | Builder | 1× Dockyard **in region 1797** (coast) |
| 7 | 387 | FABRICATE | Dockyard | 1× Hydra |
| 7 | 388 | ATTACK | Hydra | kill 1× (Bot1) Hydra (water) |
| 8 | 389 | FABRICATE | Dockyard | 1× Transporter |
| 8 | 485 | LOAD | Builder | 1× Builder loaded (SYNC_ITEM_LOADED) |
| 8 | 486 | SAIL | Builder | 1× Transporter **with cargo** in the strip of water along the Phase 2 coast, ~230 × 10 (LOADED_CONTAINER_POSITION) |
| 8 | 392 | UNLOAD | Builder | 1× Builder in the Phase 2 region |
| 9 | 393 | SELL | Factory | sell 1× Factory |
| 9 | 395 | BUILD | Builder | 1× Factory **in start region 122** (Phase 2) |
| 9 | 396 | BUILD | Builder | 1× Radar + 1× Powerplant **in start region 122** (Phase 2) |
| 9 | 400 | FABRICATE | Factory | 1× Harvester + 6× Viper **in start region 122** (Phase 2) |
| 9 | 401 | SELL | Dockyard | sell 1× Dockyard |

Every quest of level 9 has a tip since 2026-09-25. Corrected 2026-09-18: 389 was missing from this table. 2026-09-22: 392
("carry the builder off the noob island") split into load, sail and unload, each with a tip - it
passed at 0 % from 18.09. on, and PROD recorded not one load order: nobody was ever told to put
the builder into the transporter. 485 and 486 are the local ids.

---

## 4. Axes

The cases below are combinations of these axes. Not every combination is listed on its own, but the
test bed should run the **actor** axis in full for *every* step.

- **Actor:** selected / not selected × on screen / rendered but not visible (W1) / off screen,
  seen before / off screen, never seen · several of the type · does not exist (dead, sold, not
  built yet) · dies during the step
- **Target** (resource, enemy, construction site, build region): on screen / off screen / none on
  the planet / dies or runs empty / respawns
- **Player:** does it right / clicks something else / deselects / does nothing / scrolls away and
  back / cancels the placer
- **Environment:** desktop / touch · compact layout · without SharedArrayBuffer · quest changes ·
  tips off · reload

---

## 5. Cases

### SEL - Select the unit (all chains)

| Id | Situation | Expected | Today |
|---|---|---|---|
| SEL-01 | not selected, on screen | "Click to select" on it | ✓ bed |
| SEL-02 | not selected, off screen, seen before | direction arrow; prompt when scrolled in | ✓ bed |
| SEL-03 | not selected, **never seen** (camera elsewhere when the quest starts) | direction arrow to the real position | ✓ bed since the guide (2026-09-18); the chain: nothing on screen (report 2026-09-18); needs the W6 query |
| SEL-04 | selected, on screen | step skipped | ~ |
| SEL-05 | selected, off screen | step skipped | ✓ bed since the guide (2026-09-18); the chain: the step is skipped since c3f1ce3a7, but the attack step after it shows nothing - it has no attacker position (see ATK-05) |
| SEL-06 | selected, rendered but not visible (W1) | step skipped | ? |
| SEL-07 | several of the type, one selected | skipped, actor = the selected one | ✓ since 2026-09-18 |
| SEL-08 | several of the type, none selected | prompt on the one nearest to the view centre (Q6) | ✓ bed since the guide (2026-09-18); the chain: the prompt goes to the oldest unit of the type, not the nearest |
| SEL-09 | drives out and back in while prompted | arrow follows, prompt comes back | ✓ test |
| SEL-10 | another type selected | prompt stays | ✓ bed |
| SEL-11 | actor does not exist (viper dead, harvester sold) | attack quests: fabricate detour (Q1); otherwise fail gracefully | ✗ `ACTOR_NOT_FOUND`, nothing on screen |
| SEL-12 | box around several types, actor included | skipped | ? |
| SEL-13 | clicked on empty ground in a later step (deselected) | back to SEL after 1.5 s | ✓ bed since 365 has no group step (2026-09-18); before, the backtrack stopped at the group step and restarted the attack step |
| SEL-14 | deselected while the actor is off screen | back to SEL, arrow | ? |

### GRP - Select a group (ATTACK with a target type)

| Id | Situation | Expected | Today |
|---|---|---|---|
| GRP-01 | owns fewer than 3 | skipped, `TOO_FEW_TO_GROUP` | ✓ test |
| GRP-02 | owns ≥ 3, one selected | asks for a box, icon bar highlighted | ✓ bed |
| GRP-03 | 3 selected, all off screen | skipped | ✓ since 2026-09-18 |
| GRP-04 | units off screen, not selected | direction arrow to them | ? |
| GRP-05 | quest 365 (first attack) | no group tip (Q4) | ✓ bed since the `group` field of the tip config (2026-09-18), ✗ from 2026-09-16 until then |
| GRP-06 | one of the group dies while asked | count adapts | ? |
| GRP-07 | the box falls back to a single viper three times in ten seconds | the group tip comes and goes with the selection, and none of it is a restart loop | ✓ bed since 2026-09-20; before: `SELECT_GROUP|CHAIN_THRASHING` |

### PLC - Press the build button (BUILD)

| Id | Situation | Expected | Today |
|---|---|---|---|
| PLC-01 | builder selected, button enabled | hint on the button | ✓ bed |
| PLC-02 | button greyed out (money, limit, house space) | should not occur - quests pay Razarion (Q3); if it does: fail gracefully. Money: see MNY | ✓ bed: fails gracefully - no hint on the greyed-out button |
| PLC-03 | button on another carousel page | hint reaches the button | ? `BUTTON_NOT_RENDERED` |
| PLC-04 | compact layout, panel closed | hint on the panel icon | ~ |
| PLC-05 | builder selected, drives off screen | cockpit stays, hint stays | ✓ bed |
| PLC-06 | deselected | back to SEL | ✓ bed |
| PLC-07 | a construction site of the type already stands | skipped | ? `isFulfilled` sees rendered ones only |
| PLC-08 | cockpit rebuilt (selection change) | hint comes back | ~ |

### BLD - Place and build (BUILD)

| Id | Situation | Expected | Today |
|---|---|---|---|
| BLD-01 | placer open, no region | placer, no further prompt | ? |
| BLD-02 | region (386) on screen | place marker | ✓ bed |
| BLD-03 | region off screen | arrow to the nearest point *inside* the region | ✓ bed |
| BLD-04 | placer cancelled | back to the button | ✓ bed |
| BLD-05 | placement refused | back to the button, reason visible | ✓ since 2026-09-16 |
| BLD-06 | placed, builder drives there (off screen) | quiet (R3) | ✓ bed |
| BLD-07 | builder gets another order before arriving | back to the button | ✓ bed |
| BLD-08 | construction site going up | no prompt | ✓ bed |
| BLD-09 | construction site stalled > 10 s | "Click to continue building", select the builder first | ✓ bed since the guide (2026-09-18); the chain: the prompt comes only after the site has not grown for 10 s, although the builder stands idle elsewhere from the moment it arrives - nothing on screen until then |
| BLD-10 | construction site off screen | arrow | ? |
| BLD-11 | construction site destroyed | back to placing | ✓ bed since the guide (2026-09-18); the chain: a destroyed site looks like one out of view (no removed-listener, W5); the task waits for it forever and never offers the placement again |
| BLD-12 | builder dies (e.g. drove through the Tesla bot) | a factory present: select it, hint on the builder button, quiet while it is built; no factory: quiet (Q1) | ✓ bed since 2026-09-28 |
| BLD-13 | finished while off screen | quest done, everything gone | ✓ bed |
| BLD-14 | 386, placer open and the region off screen, the player does not scroll for 30 s | arrow into the region the whole time, stall reason `TARGET_OUT_OF_VIEW` | ✓ bed since 2026-09-20 |
| BLD-15 | 386, drive to the coast plus the build take longer than the watchdog | quiet (R3); the only reasons reported are `AWAIT_BUILD_SITE` and `AWAIT_BUILD_FINALIZE` | ✓ bed since 2026-09-20 |

### FAB - Fabricate (FABRICATE)

| Id | Situation | Expected | Today |
|---|---|---|---|
| FAB-01 | factory selected, button enabled | hint on the button; click → quiet | ✓ bed |
| FAB-02 | button greyed out | as PLC-02 (Q3) | ✓ bed: fails gracefully - no hint on the greyed-out button |
| FAB-03 | queue full | fail gracefully (Q3) | `FACTORY_QUEUE_FULL` |
| FAB-04 | factory selected, off screen | hint on the button stays | ✓ bed |
| FAB-05 | several factories / dockyards | hint on the selected one | ? |
| FAB-06 | 369 (3 vipers): after the first | hint for the next one, without a detour through SEL | ✓ bed |
| FAB-07 | unit finished, factory off screen | quest done or next hint | ✓ bed since the guide (2026-09-18); the chain: the idle step can only read a factory on screen - with the camera elsewhere it points the arrow at the working factory and the hint for the next viper never comes |
| FAB-08 | 389 (transporter), dockyard not selected | "Click to select" on the dockyard, then the hint on the transporter button; `AWAIT_SELECTION` while the player does not act | ✓ bed since 2026-09-20; the chain: `ACTOR_NOT_FOUND` (11 records on PROD, 1 with the guide) |

### HRV - Harvest (HARVEST)

| Id | Situation | Expected | Today |
|---|---|---|---|
| HRV-01 | harvester selected, field on screen | "Click to harvest" on the nearest field | ✓ bed |
| HRV-02 | field off screen | arrow to the nearest field | ✓ bed |
| HRV-03 | harvester selected, **off screen** | arrow to the field | ✓ bed since the guide (2026-09-18); the chain: throws out of `TipService.activate()` (`babylonBaseItemImpl!` in `findNearestResourcePosition`); the quest cockpit swallows it and the tip is dead for the whole quest |
| HRV-04 | field runs empty or scrolls out | next field | ✓ bed |
| HRV-05 | harvesting | quiet | ✓ bed |
| HRV-06 | idle, amount not reached yet | "Click to harvest" again | ✓ bed |
| HRV-07 | no field on the planet | nothing until a field is back (Q7) | ✓ bed |

### TRN - Cross the water (LOAD, SAIL, UNLOAD)

One decision for all three quests: each can find the world in an earlier quest's state.

| Id | Situation | Expected | Today |
|---|---|---|---|
| TRN-01 | 485, builder not selected | "Click to select" on the builder, then "Click to load" on the transporter; quiet while it walks (`AWAIT_LOAD`) | ✓ bed since 2026-09-22 |
| TRN-02 | 485, transporter off screen | arrow to it (`CONTAINER_OUT_OF_VIEW`), the prompt once it is on screen | ✓ bed since 2026-09-22 |
| TRN-03 | 485, transporter sunk | select the dockyard, hint on the transporter button, then back to the builder | ✓ bed since 2026-09-22 |
| TRN-04 | 486, loaded transporter | select it, arrow to the coast, the water circle marked once in view (`AWAIT_MOVE_CLICK`), quiet while sailing | ✓ bed since 2026-09-22 |
| TRN-05 | 486, builder not aboard | the load steps of 485 | ✓ bed since 2026-09-22 |
| TRN-06 | 392, transporter at the coast | select it, hint on the Unload button, the region marked while placing | ✓ bed since 2026-09-22 |
| TRN-07 | 392, transporter still at home | sail first (arrow, marked region); the Unload hint once the region is within reach (range - 2) | ✓ bed since 2026-09-22 |
| TRN-08 | 486, transporter sunk with the builder aboard (naval bot) | a new builder from the factory first, then a new transporter from the dockyard; was silent (`ACTOR_NOT_FOUND`) - the first phone player at level 13 lost a whole run to it | ✓ bed since 2026-09-28 |

### SLL - Sell a building (SELL)

Level 9 moves the base: 393 sells the factory, 401 the dockyard, both on the island the player has
just left. On PROD 393 fell from 30/36 to 1/7 once the transport tips brought players there
(2026-09-25). The sell button is a small '$' that wants two taps; the hint says which one is next.

| Id | Situation | Expected | Today |
|---|---|---|---|
| SLL-01 | 393, factory far behind the player | arrow and minimap marker back to it, "Click to select", the hint on the sell button through both taps | ✓ bed since 2026-09-25 |
| SLL-02 | 401, dockyard already selected | straight to the sell button | ✓ bed since 2026-09-25 |
| SLL-03 | 393, the dockyard selected instead | no sell hint; "Click to select" on the factory | ✓ bed since 2026-09-25 |

### RBL - Build the base again (BUILD, counting the region)

395 and 396 count what stands in the Phase 2 start region (`startRegionId` 122, which the client
gets as the quest's place), whenever it was built. Level 9 allows one factory, radar and powerplant
each - and the radar and powerplant from level 3 still stand on the noob island. They do not
count, yet they fill the limit: the button of the new one is greyed out, and no quest says to sell
the old one. The tip reads the region off the base like the server, asks for a type the limit
still allows first, and sends the player back to sell the old building when that is what stands in
the way. On PROD both passed at 97 % before the transport tips (31/32, 30/31).

| Id | Situation | Expected | Today |
|---|---|---|---|
| RBL-01 | 395, builder in the region | "Click to select", hint on the factory button, quiet while it builds | ✓ bed since 2026-09-25 |
| RBL-02 | 396, radar built | the hint moves on to the powerplant button | ✓ bed since 2026-09-25 |
| RBL-03 | 396, the player places the powerplant first | quiet while it goes up, then the radar | ✓ bed since 2026-09-25 |
| RBL-04 | 396, radar and powerplant from level 3 on the old island | arrow and minimap marker back to the old radar, select, sell twice, then the radar button | ✓ bed since 2026-09-25 |
| RBL-05 | 396, only the old radar left | the powerplant first, then back to the old radar | ✓ bed since 2026-09-25 |

### ARM - The army on the new island (FABRICATE, counting the region)

400 wants a harvester and six vipers in the Phase 2 start region, made by the factory built there
in 395. Level 9 allows one harvester and six vipers, and land units cannot cross: old ones on the
noob island fill the limit until they are sold. The tip asks for a type the limit still allows
first, then sends the player back to sell what stands in the way (as RBL, [396](#rbl---build-the-base-again-build-counting-the-region)).

| Id | Situation | Expected | Today |
|---|---|---|---|
| ARM-01 | 400, nothing old left | "Click to select" on the factory, the harvester button, then the viper button six times | ✓ bed since 2026-09-25 |
| ARM-02 | the old harvester still on the noob island | the vipers first, then arrow back to the old harvester, sell it, then the harvester button | ✓ bed since 2026-09-25 |
| ARM-03 | the player queues all six vipers at once | quiet while the queue runs, then the harvester | ✓ bed since 2026-09-25 |

### MNY - Too little Razarion (BUILD, FABRICATE)

The quests pay for the way (Q3), but not for losses: on PROD a player who lost the builder and
bought a new one for 50 stood before a greyed powerplant button (362), another lost harvester and
army and had no income left (386) - and the tip said nothing (tip_stall `BUTTON_DISABLED`,
2026-09-25). The cockpit now names the reason (`NO_MONEY`, `ITEM_LIMIT`, `HOUSE_SPACE_FULL`), and
the tip checks the price against the base's Razarion before it asks for the actor: short of money,
it sends a harvester out and waits while one is at work.

| Id | Situation | Expected | Today |
|---|---|---|---|
| MNY-01 | 362 with 20 Razarion, harvester idle | "Click to select" on the harvester, "Click to harvest", quiet while the money comes in, then the builder and the powerplant button | ✓ bed since 2026-09-25 |
| MNY-02 | the harvester already at work | nothing until the money is there, then the button hint | ✓ bed since 2026-09-25 |
| MNY-03 | no harvester at all | nothing wrong shown (graceful) | ✓ bed since 2026-09-25 |

### MAP - The quest marker on the minimap (all tips)

Since 2026-09-23 the minimap is on screen from level 1 and marks what the tip points at: the arrow's
target, the prompt's unit, or the quest region. Outside the map's window it is a wedge on the edge.

| Id | Situation | Expected | Today |
|---|---|---|---|
| MAP-01 | target off screen | the map marks the target the arrow points at | ✓ bed since 2026-09-23 |
| MAP-02 | prompt on screen | the map marks the same unit | ✓ bed since 2026-09-23 |
| MAP-03 | sailing quest (486) | the water region is marked | ✓ bed since 2026-09-23 |
| MAP-04 | unit working, no region; quest done | nothing marked | ✓ bed since 2026-09-23 |
| MAP-05 | target off screen, the player taps the "go there" chip at the arrow's tip | the camera on the target, the prompt there, arrow and chip gone (tracked as `ARROW_JUMP`) | ✓ bed since 2026-09-25, checked in the browser |

### ATK - Attack (ATTACK)

| Id | Situation | Expected | Today |
|---|---|---|---|
| ATK-01 | target on screen | "Click to attack" | ✓ bed |
| ATK-02 | target off screen | arrow to the nearest target | ✓ bed |
| ATK-03 | target dies (other unit, bot) | new target within ≤ 1 s | ✓ bed |
| ATK-04 | target keeps respawning (W11) | prompt does not jump every second | ✓ test |
| ATK-05 | click, attacker drives in from off screen | quiet (R3), **no select prompt on arrival** | ✓ bed since the guide (2026-09-18); the chain: the select prompt on arrival is fixed (c3f1ce3a7, regression test), but after scrolling to the target there is no "Click to attack" - the attack step never samples where the attacker is |
| ATK-06 | attacker never seen | arrow to the target | ✓ bed since the guide (2026-09-18); the chain: no target search without an attacker position |
| ATK-07 | attacker dies on the way (tesla range 15, viper 10) | last one gone: select factory → fabricate → attack again (Q1) | ✓ bed since the guide (2026-09-18); the chain: nothing on screen |
| ATK-08 | target type off screen, another enemy on screen (379: teslas in front of the refinery) | arrow to the refinery, no prompt on the tesla | ? |
| ATK-09 | target type absent for now (bot dead, respawn coming) | nothing until the target is back (Q7) | ✓ bed |
| ATK-10a | player attacks another enemy **of the quest's type** than the marked one | counts as done → quiet (Q5) | ✓ bed since the guide (2026-09-18); the chain: the prompt stays on the marked extractor while the viper attacks the other one |
| ATK-10b | player attacks an enemy **of another type** | does not count, prompt stays (Q5) | ? |
| ATK-11 | water (388, hydra against hydra) | as ATK-01..10 | ? |

### VIS - On screen is not the same as seen (all chains)

Being inside the view field does not mean the player can see a prompt there. The view field is
built from the NDC corners ±1, so it covers the strip behind the bottom row of the HUD and the
very top edge of the picture, and the prompt is not a dot: label, gap and arrow are 180 px and the
label floats another 150-200 px above the item. On a phone in portrait (backbuffer about 369x683)
that was 42 % of the height of clearance against 26 % of HUD - a target had to sit in a band of
less than a third of the picture for its prompt to be seen, and nothing checked.

Two changes answer this, and the cases below pin both: the prompt is scaled to the picture
(`PROMPT_IDEAL_HEIGHT_PX`) so one side of the anchor always has room, and `isPromptReadable`
decides between prompt and arrow instead of the view field alone.

| Id | Situation | Expected | Today |
|---|---|---|---|
| VIS-01 | target inside the view field, behind the bottom HUD | arrow, no prompt | ✓ bed since 2026-09-22 |
| VIS-02 | target high in the picture, no room above | prompt with the label **below** the item | ✓ bed since 2026-09-22 |
| VIS-03 | target scrolls from readable to under the HUD | prompt down, arrow up within ~1 s | ✓ bed since 2026-09-22 |
| VIS-04 | the label's side stops fitting while the prompt is up | rebuilt on the other side, not left hidden | ✓ code (`isSelectPromptMisplaced`), no case |
| VIS-05 | HUD panel open on a compact phone | prompt keeps clear of the panel | ✓ code (measured, `getHudBottomPixels`), no case |

The bed models the bands with the live camera's geometry (`CAMERA` in fake-world) and takes the
clearance from the renderer's own constant, so the two cannot drift. What it cannot check is the
projection itself and the HUD measurement - those are `prompt-geometry.spec.ts` and the live
client.

### IDL - Wait until the unit is done (entry of the fallback chain)

| Id | Situation | Expected | Today |
|---|---|---|---|
| IDL-01 | working on screen | quiet | ~ |
| IDL-02 | working off screen | nothing, no arrow (Q2) | ✓ bed since the guide (2026-09-18); the chain: direction arrow to the viper for its whole drive |
| IDL-03 | order never arrived (15 s watched idle) | back to the step before | ✓ test |
| IDL-04 | comes into view idle (W4) | not counted as "has worked" | ✗ code: a fresh instance reports `idle=false` |
| IDL-05 | without SharedArrayBuffer, order arrives late | no step back | ✓ bed |
| IDL-06 | done, quest still open (369, HARVEST) | next step directly | ✓ bed |
| IDL-07 | arrow point enters the view, unit is elsewhere (W12) | no restart that wipes what was observed | ? |

### X - Cross-cutting

| Id | Situation | Expected | Today |
|---|---|---|---|
| X-01 | quest changes in the middle of a step | old prompts and arrows gone (R5) | ✓ bed |
| X-02 | quest fulfilled in the middle of a step | everything gone | ? |
| X-03 | tips switched off (director) | everything gone | ✓ bed |
| X-04 | reload / reconnect in the middle of a tip | tip starts over correctly | ? |
| X-05 | touch: pan starts on a unit | no click, no step change | ~ |
| X-06 | quest starts while the placer is open | ? | ? |
| X-07 | chain throws | recovery + `CHAIN_ERROR` (R7) | ? |
| X-08 | the tip keeps going back and forth **on its own** | `CHAIN_THRASHING` | ✓ test |
| X-09 | 366, three small fields run dry one after the other | every round trip starts the next step; none of it is a restart loop | ✓ bed since 2026-09-20; before: `SEND_HARVEST_COMMAND|CHAIN_THRASHING` |
| X-10 | the player puts his selection down and picks it up three times in ten seconds | the tip follows him (SEL-13) and reports no restart loop | ✓ bed since 2026-09-20; before: `SELECT|CHAIN_THRASHING` |

---

## 6. Decisions and analyses

Answered 2026-09-18.

| Question | Decision |
|---|---|
| Q1 actor dead or missing (SEL-11, BLD-12, ATK-07, TRN-08) | no unit of the actor type left: the tip becomes the fabricate tip wherever an own factory can make one - attack quests since the guide, build, harvest and transport quests since 2026-09-28 ("graceful" had meant stuck: TRN-08) - then continues with the quest. Only where nothing can make it does it fail gracefully |
| Q2 working off screen (IDL-02) | show nothing: the unit is working |
| Q3 greyed-out button (PLC-02, FAB-02, FAB-03) | fixed in the quests: 50 Razarion reward on 363 and on 366 |
| Q4 group tip on 365 (GRP-05) | no: the first attack teaches the basics, group selection comes later (379) |
| Q5 different target attacked (ATK-10) | another enemy of the quest's type counts; another type does not |
| Q6 several units, none selected (SEL-08) | point at the nearest one |
| Q7 no target present (ATK-09, HRV-07) | nothing until the target is back |

### Q1 - Can the actor be missing? Yes, in the three combat quests

The chain prepares the actor *at the start* of every quest: 358 builds the factory that 359 needs,
359 the harvester for 363, 364 the viper for 365, 369 the vipers for 379, 386 the dockyard for 387,
387 the hydra for 388. The start builder comes with the base. So a quest never *starts* without its
actor unless the player got rid of it.

It goes missing *during* a quest in three ways:

1. **Killed in combat - 365, 379, 388.** This is the real case. Measured on PROD over 21 days
   (2026-08/09): the 52 players who failed 379 lost **193 vipers** - the tesla out-ranges a viper
   (15 against 10) and kills it on the way to the refinery. On 365 the loss of own units is about 0.1
   per player, rare but not zero. The attack tip then has no attacker, reports `ACTOR_NOT_FOUND` and
   shows nothing, while the fix is one click away: the factory can build a new viper for 10 Razarion.
2. **Sold.** Selling is not suppressed on the planet (`isSellSuppressed()` is a scene setting), so a
   player can sell the builder, factory or harvester from level 1 on. Rare, and the player did it
   on purpose.
3. **Killed outside combat.** Bots that attack the base. Not evaluated.

What can be rebuilt: the factory builds builder, harvester and viper; the builder builds factory,
radar, powerplant and dockyard; the dockyard builds the hydra. Each type is limited to 1 on levels
1-7 except vipers (3 on levels 2-3, 6 from level 4) and hydras (3 from level 7). **Dead end:** on
quest 358 there is no factory yet, so a lost builder cannot be replaced.

**Decided:** handle only case 1 and treat it as a step of the chain, not as an error. When an
attack quest has no unit of the actor type left, the tip turns into the fabricate tip for it
(select the factory → fabricate a viper, or the dockyard → hydra) and then continues with the
attack. Everything else (sold, bot raid, 358 without a builder) only has to fail gracefully: no
stuck chain, no wrong prompt.

### Q3 - Can a button be greyed out? Only for money

Of the three reasons, two cannot occur on the tipped quests:

- **Item limit - no.** Every tipped quest asks for at most what the level allows. The only one that
  asks for more than one is 369 (3 vipers, level 4, limit 6), and it counts existing vipers
  (`includeExisting`), so the viper from 364 is part of the three.
- **House space - no.** The planet gives 15 and every item consumes 1. The most a player can own up
  to level 7 within the limits is builder, factory, harvester, radar, powerplant, dockyard (6) +
  6 vipers + 3 hydras = **exactly 15**. It fits, with no margin; the transporter at level 8 would be
  the 16th, but level 8 has no tips.
- **Money - yes.** The start gives 100 Razarion, and no tipped quest pays Razarion. The tipped chain
  costs 35 (factory) + 15 (harvester) + 10 (viper) + 35 (radar) + 35 (powerplant) + 20 (two more
  vipers for 369) + 35 (dockyard) + 13 (hydra) = **198**. At least 98 have to be harvested, and the
  two harvest quests only guarantee 15. The rest comes from the harvester continuing on its field -
  but a harvester **stops when its field is empty** (fields hold 50 or 150) and does not move on by
  itself. Tightest spot: level 3, radar + powerplant for 70 after 60 already spent, with only
  40 + whatever the harvester collected since 363. Extra vipers (up to 3 on level 2) or a viper
  rebuilt after 365 make it tighter.
- **Queue full** only happens if the player queues more than asked; not a preparation problem.

How often money actually runs short is in PROD `tip_stall` (`BUTTON_DISABLED` per quest). Reading
it was blocked in this session.

**Decided:** fixed in the quests - 50 Razarion on 363 and 50 on 366. With them the chain has 100 + 100
+ 15 harvested = 215 against 198, before anything the harvester collects beyond the quests.

**Found on the way:** the server never paid a quest's Razarion reward - `onQuestPassed` only added
xp. No quest had one set, so it went unnoticed. Paid since 2026-09-18
(`ServerGameEngineControl.addRazarion`).

---

## 7. The test bed

In `razarion-frontend/src/app/game/tip/testbed/`, run with
`npx ng test --watch=false --include='**/tip/testbed/*.spec.ts'`.

- **`fake-world.ts`** - a small game world that follows W1-W12: units with id, type, position,
  orders and health; drive, attack, harvest, build, fabricate; bot respawns; a camera with the real
  `ViewField` trapezoid and a larger render box. `FakeRenderer` feeds it to the tip code the way
  `BaseItemUiService` feeds the real renderer: instances only inside the render box, created before
  their engine state is set, removed from view or disposed with the matching selection call.
  `FakeItemCockpit` is rebuilt from the rendered selection on every selection change.
- **`tip-testbed.ts`** - the real `TipService` with its guide, `SelectionService`, `ActionService`
  and `TipStallTrackerService` on top of it; only renderer, worker queries, cockpit
  and network are faked. The player clicks, boxes, deselects, scrolls and places through the real
  `ActionService`. `view()` is what the player sees: prompts on items that are on screen, the
  direction arrow, the cockpit hint, the place marker, the group prompt. R1-R5 and R7 are checked
  after every 100 ms step, with 1.5 s grace (R6). Of R8, the half that needs no case of its own is
  checked everywhere since 2026-09-20: a `CHAIN_THRASHING` record is a violation in every case,
  because a tip that is doing its job never reports itself as a restart loop. The rest of R8 -
  which reason is the honest one - is checked case by case through `stallReasons()`.
- **`tip-case.ts`** - three kinds of test:
  - `tipCase` - the case's own checks and every rule hold.
  - `tipRegression` - a reported bug, pinned to the rules it broke.
  - `tipGraceful` - a case the quests are prepared against: every rule but R1 (nothing to teach,
    only nothing wrong).
- **`select`, `attack`, `build`, `fabricate`, `harvest`, `transport`, `cross` `.testbed.spec.ts`** - 57
  cases across all seven tip types, quests 358 to 392 (the 7 TRN cases in `transport` since 2026-09-22). Not covered yet: SEL-04/06/07/09/12/14,
  GRP-01/03/04/06, PLC-03/04/07/08, BLD-01/05/10/12, FAB-03/05, ATK-04/08/10b/11, IDL-01/03/04/07,
  X-02/04/05/06/07/08 - several of them
  are covered by the unit specs, and X-04 (reload) and X-05 (touch) are outside what the
  bed simulates.

Exceptions out of the tip code count as R7 findings rather than test errors: out of a timer the loop
it drove is dead, and out of `TipService.activate()` the quest cockpit swallows it - either way the
player is left without a tip.

Until the switch every case also ran against the removed chain (`tipDefect` marked the cases it got
wrong); the bed now runs the guide only.

The bed was checked against the bug reported on 2026-09-18: with c3f1ce3a7 reverted it reports R2
and R3 at 3.8 s - "Click to select" on the selected viper as it arrives - exactly as seen in the
game.

### What PROD said after the switch (measured 2026-09-20)

The guide went live on 2026-09-18 15:32 UTC. Over the next 1.5 days `tip_stall` fell from 25.0 to
19.6 records per 100 quest activations, and three of the signatures the chain used to write
disappeared completely (`361|SELECT|ACTOR_NOT_FOUND`, `386|SEND_BUILD_COMMAND|ACTOR_OUT_OF_VIEW`,
`379|IDLE_ITEM|ACTOR_OUT_OF_VIEW`). Seven signatures were **new**, and each one became a case:

| New on PROD | Records | What it turned out to be |
|---|---|---|
| `363/366 SEND_HARVEST_COMMAND\|CHAIN_THRASHING` | 3 | the tracker, not the tip: **X-09** |
| `365/379 SELECT\|CHAIN_THRASHING`, `379 SELECT_GROUP\|CHAIN_THRASHING` | 16 | the tracker, not the tip: **X-10**, **GRP-07** |
| `386 SEND_BUILD_COMMAND\|TARGET_OUT_OF_VIEW` | 10 | correct: the coast is off screen and the player does not scroll - **BLD-14** |
| `386 SEND_BUILD_COMMAND\|AWAIT_BUILD_SITE/FINALIZE` | 8 | correct: the drive to the coast plus the build outlast the watchdog - **BLD-15** |
| `389 SELECT\|AWAIT_SELECTION` | 3 | an improvement: the chain said `ACTOR_NOT_FOUND` here - **FAB-08** |

The two thrashing families were one defect with two halves, both fixed on 2026-09-20 and both
pinned by the cases above:

1. `TipGuide.reportStep` ranked `IDLE_ITEM` highest and called every step below it a failure. But
   leaving `IDLE_ITEM` means the unit finished its work - it is how HRV-06 and FAB-06 go round.
2. `TipStallTrackerService.checkThrashing` counted those backtracks without asking whether the
   player caused them. A step back is what the *world* did, and the world is mostly the player:
   a selection put down and picked up, a box tried out, a field run dry. The detector now asks the
   same question `COLD_MILLIS` asks - was anybody there - and stays silent if they were. A tip
   looping on its own, with nobody touching it, is still reported (X-08).

Still needed outside the bed: **a worker query** for the position of own units by id, or the
nearest own unit of a type. Without it SEL-03 and ATK-06 cannot be solved - that is a change to the
game, not only to the tips.
