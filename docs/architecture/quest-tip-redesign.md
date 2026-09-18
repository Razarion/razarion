# Quest Tip Redesign - Draft

As of 2026-09-18. Status: **done** - the guide runs the tips, the chain is removed. See section 8.

Companion to the [case catalog](quest-tip-case-catalog.md), which is the specification, and its
test bed, which is how this design will be judged: the 14 `tipDefect` cases are what it has to turn
into `tipCase`s, and the 27 `tipCase`s are what it must not break.

---

## 1. Why a rebuild and not twelve fixes

The test bed found twelve distinct causes. They are not twelve accidents - they come from three
patterns that run through the whole task chain:

| Pattern | Causes |
|---|---|
| **A task asks a rendered instance** for position, selection or state - and off screen there is none (W1, W2) | own unit never seen (SEL-03/ATK-06), attacker position unknown (ATK-05/SEL-05), harvester position asserted (HRV-03), idle step blind off screen (FAB-07), oldest instead of nearest unit (SEL-08) |
| **A task waits for an event** - a click callback, an idle callback, a placer event - and some events never come (W5, W8) | only the marked target counts (ATK-10a), destroyed site taken for out of view (BLD-11), site prompt after 10 s (BLD-09) |
| **The chain has a position**, and moving it back and forth is its own logic with its own bugs | backtrack stuck on the group step (SEL-13), arrow to a working unit because the idle step is its own task (IDL-02), no way from "no attacker" to "build one" (ATK-07), group step on every typed attack (GRP-05) |

Each fix so far repaired one instance of a pattern and left the pattern in place - the last one
(c3f1ce3a7) moved the select step from the rendered instance to the selection, and the attack step
after it is blind for the same reason.

## 2. The principle

**Every half second, compute what the player should be told from the state of the world, by id. Then
make the screen show that.** No task objects, no chain position, no callbacks on instances, no
backtracking.

```
             ┌──────────── every 500 ms, and at once on selection change / camera move ─────────┐
             ▼                                                                                   │
  TipSnapshot (world by id) ──► decide(quest, snapshot, memory) ──► Guidance ──► GuidanceView ──┘
     own units, targets,          pure function,                    one value     puts it on screen,
     resources, selection,        no side effects                               takes the old one down
     placer, cockpit, memory
```

- **Nothing to lose track of.** A unit that scrolls out and back in, a target that dies, a site that
  is destroyed, a selection that changes while nobody is looking - the next evaluation simply sees the
  new state. There is no "was I told" (W5), no "is this instance still alive" (W2).
- **No chain to get lost in.** "Nothing is selected" means "select" in every step, from every step,
  without a backtrack rule.
- **Success is a state, not a click.** An attacker that is attacking something the quest counts is
  working; nothing needs to see the click that started it.
- **Testable without a world.** `decide()` takes values and returns a value. The test bed keeps
  testing the whole thing end to end; `decide()` additionally gets a table test per catalog row.

## 3. The snapshot

`TipSnapshot` is built at each evaluation from three sources.

**The tick infos, via one new query on `BaseItemUiService`.** The UI service already holds a
`NativeSyncBaseItemTickInfo` for **every** item on the planet after each tick - off screen included,
bots included - with position, `idle`, `buildup`, `factoryBuildQueue` and `constructingBaseItemTypeId`.
`getNearestEnemyPosition()` and `getMyItemCount()` already read them. What is missing is a query that
returns them:

```java
// BaseItemUiService, exposed through the Angular facade
// every own selectable item, plus the enemies of the given type (0 = every enemy, < 0 = none)
TipItemState[] getTipItemStates(int enemyItemTypeId);

class TipItemState {   // plain JS object via DtoConverter.convertTipItemStates
    int id; int itemTypeId; boolean own;
    double x, y; boolean idle; double buildup;
    int[] factoryBuildQueue;
}
```

This closes W6: an own unit that was never rendered has a position. One int parameter rather than
type arrays: a Java array does not arrive as a JS array over the TeaVM bridge, and all own items of
a beginner are a few dozen - the decision filters them itself.

**The client, as it is.** `SelectionService.getSelectedOwnItemIds()` (by id, W3), the renderer's
view field and which ids currently have an instance on screen, `baseItemPlacerActive` plus the type
being placed, the cockpit's block reason per button, the resources (`getNearestResourcePosition`,
plus the rendered resource items for the prompt).

**A small memory of what this client ordered.** The one thing the world cannot tell yet is an order
still on its way (W8): the unit reads idle for a few ticks, in the Meta webview for seconds.
`ActionService` reports every command it sends (it already has `reportCommand()` at all six places);
the placer's PLACED event counts as a build order.

```ts
interface OrderNote { unitIds: number[]; kind: 'attack' | 'harvest' | 'build' | 'finalize' | 'fabricate' | 'move';
                      targetTypeId: number | null; at: number; }
```

A unit counts as **working for the quest** when it is not idle *and* its last order was one the
quest counts - or when that order is less than 15 s old and the engine has not reported it busy yet.
This replaces `IdleItemTipTask` and its watched-time clock.

## 4. The decision

`decide()` returns one `Guidance`:

```ts
type Guidance =
  | { kind: 'quiet', reason: string }                          // working, or nothing to point at
  | { kind: 'prompt', itemId: number, text: string, reason: string }
  | { kind: 'arrow', x: number, y: number, reason: string }    // target off screen
  | { kind: 'button', itemTypeId: number, reason: string }     // cockpit hint
  | { kind: 'region', reason: string }                         // place marker, or arrow into the region
  | { kind: 'group', arrow: {x: number, y: number} | null, reason: string };
```

`reason` is the stall reason (`AWAIT_SELECTION`, `ENEMY_OUT_OF_VIEW`, ...) - the same strings as
today, so `tip_stall` stays comparable across the switch.

### Shared: point at a thing

`pointAt(thing, text)`: on screen → `prompt` on its id; off screen → `arrow` to its position. Every
step below uses it, which is where "on screen or not" is decided - once.

### Shared: the select step

Candidates: own units of the actor type. **If one of them is selected, the step is done.** Otherwise
`pointAt(nearest to the view centre, 'Click to select')` (Q6).

### ATTACK - quests 365, 379, 388

```
attackers = own units of actor type
if attackers is empty:            return FABRICATE(actor type)          // Q1, ATK-07
if an attacker works on a quest target (or the order is on its way):
                                  return quiet                          // R3, IDL-02, ATK-10a
targets = enemies of the quest's type (any enemy if untyped)
if targets is empty:              return quiet('NO_ENEMY')              // Q7, ATK-09
if no attacker selected:          return select step                    // SEL-13, SEL-05
if quest asks for a group and fewer than 2 selected and 2+ owned:
                                  return group                          // Q4, GRP-05
target = the marked target if still alive, else nearest to the selected attackers
                                  return pointAt(target, 'Click to attack')
```

The marked target is kept while it lives, so a respawning bot does not make the prompt jump (ATK-04).

### BUILD - quests 358, 361, 362, 386

```
site = own item of the building type with buildup < 1
if site:
    if a builder works on it (or the order is on its way):   return quiet
    if no builder selected:                                  return select step
    return pointAt(site, 'Click to continue building')      // at once, not after 10 s - BLD-09
if a builder has a build order on its way (placed, driving): return quiet   // BLD-06
if the placer is open for the building type:
    return region (quest 386) or quiet (the placer is the guidance)
if no builder selected:                                      return select step
if the button is enabled:                                    return button
return quiet(block reason)                                  // graceful - PLC-02
```

A destroyed site is simply no site any more (BLD-11). A builder sent elsewhere has no build order,
so the next evaluation is back at the button (BLD-07).

### FABRICATE - quests 359, 364, 369, 387

```
if a factory of the actor type has the unit type in its queue:   return quiet
if no factory selected:                                           return select step
if the button is enabled:                                         return button
return quiet(block reason)                                       // graceful - FAB-02
```

The factory's queue comes from the tick infos, so it does not matter whether the factory is on
screen (FAB-07). After each unit of 369 the queue is empty and the button hint is back (FAB-06).

### HARVEST - quests 363, 366

```
if a harvester harvests (or the order is on its way):   return quiet
if no field on the planet:                              return quiet('NO_RESOURCE')   // HRV-07
if no harvester selected:                               return select step
field = nearest to the selected harvester              // position from the snapshot - HRV-03
return pointAt(field, 'Click to harvest')
```

## 5. Putting it on screen

`GuidanceView` owns everything a tip shows and applies each new `Guidance` as a difference:

- **prompt** - looks the item up by id on every evaluation and puts the prompt on it if it is not
  showing (W2: an instance that came back gets it again), takes it off the previous id if that one is
  still rendered.
- **arrow** - `showOutOfViewMarker` towards the position, recomputed on camera moves.
- **button** - `itemCockpit.showBuildupTip(typeId)`, re-anchored on every evaluation as today.
- **region** - place marker or arrow into the region (the interior-point logic of today's
  `SendBuildCommandTipTask` moves here unchanged).
- **group** - `touchSelectionMode.setAsked(true)`.
- **quiet** - takes all of it down.

It is the only code that touches the renderer, and it never reads state back from it except "which
instance has this id right now".

## 6. What stays

- `TipService`'s public face (`activate`, `deactivate`, `setItemCockpit`, the tips-visible switch), so
  the quest cockpit and the game component do not change.
- Stall tracking: an evaluation whose `reason` differs from the last one counts as a new task for the
  watchdog, with the task name mapped to today's `TipTaskName` (a select step reports `SELECT`, a
  button step `START_BUILD_PLACER` or `SEND_FABRICATE_COMMAND`, ...). `TIP_COLD` stays as it is.
- The cockpit's own hint placement, the out-of-view marker, the place marker - drawing is not what
  was broken.

## 7. What goes

All eight task classes with `TipTaskContext`, `TipTaskContainer` and `TipTaskFactory`: about 2,100
lines of code, plus about 1,650 lines of their specs - the test bed replaces those. The new code is estimated at 600-800 lines TypeScript plus about 80 lines Java for
the query and its bridge conversion.

## 8. How it gets in

1. **The query.** `getTipItemStates()` in `BaseItemUiService`, the facade, `DtoConverter`, and the
   fake in the test bed. Deployable alone; nothing uses it yet. **Done 2026-09-18**, checked in the
   running local game: 4 own items (same as `getMyOwnSyncItemTickInfos()`), 155 enemies over the
   whole planet, 6 of them extractors when filtered by type 22, `factoryBuildQueue` a real JS array.
2. **The new code beside the old.** `decide()`, `GuidanceView`, the order notes in `ActionService`.
   The test bed wires either implementation behind one switch and runs all 44 cases against both.
   Done when the 14 defects are `tipCase`s and nothing else went red. **Done 2026-09-18**: in
   `game/tip/guide/` (`tip-decision.ts`, `guidance-view.ts`, `tip-guide.ts`, `tip-region.ts`). Every
   case passes on the guide, the chain's known defects included. Checked against a guide that always
   says nothing: 39 of the 44 guide cases fail, so the bed does tell a working guide from a broken one.
   `?tips=guide` in the game URL runs it; without it the chain runs. Tried in the local game: nothing
   selected, the viper off screen and never seen - the arrow points at it (SEL-03).
3. **Switch and removal together** - decided: no A/B. Once the test bed is green on the new code, the
   old classes go in the same change. The tipped quests' pass rates (358-389) and `tip_stall` per
   quest are read before and after, per browser and not per session. **Done 2026-09-18** in the code:
   `TipService` runs the guide only, and the task classes, `TipTaskContainer`, `TipTaskFactory` and
   their specs are gone. The guide took over the two things the chain did besides guiding: the
   `GROUP_TIP` first-interaction record, and `CHAIN_ERROR` when an evaluation throws. The before
   and after reading waits for the deploy.

## 9. Decisions

- **The group tip is a `group` field on the quest's tip config (Q4)** - decided 2026-09-18. A DB
  column on `QUEST`, and the field through every hand-written path: `TipConfig`, `QuestConfigEntity`,
  the client's `JsonDeserializer`, `DtoConverter`, `GwtAngularFacade.ts`, the quest editor. Set on
  379 (and 388 if wanted), not on 365.
- **No A/B** - decided 2026-09-18, see section 8.
- **The half-second interval** stays as proposed: short enough for R6 (~1 s), and the evaluation is
  a loop over a few dozen items. Selection changes and camera moves evaluate at once in addition.
