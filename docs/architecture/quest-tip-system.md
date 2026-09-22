# Quest Tip System

The quest tip guides beginners through the quests of levels 1-8 that carry a tip config (358-392):
it tells the player what to click next - a prompt on a unit, an arrow to something off screen, a
hint on a cockpit button, the place marker of a build region, the group prompt - and says nothing
while the player's units are doing what the quest needs.

Since 2026-09-18 it is a single decision computed from the state of the world, not a chain of
tasks. Why, and what it replaced: [quest-tip-redesign.md](quest-tip-redesign.md). What it has to do in
every situation: [quest-tip-case-catalog.md](quest-tip-case-catalog.md).

## Key files

All under `razarion-frontend/src/app/game/tip/`.

| File | Purpose |
|------|---------|
| `tip.service.ts` | Entry point: `activate(questConfig)` from the quest cockpit, `deactivate()`, the tips-visible switch |
| `guide/tip-guide.ts` | Evaluates every 500 ms and on selection change, camera move and order; remembers orders until the engine reports them; stall tracking |
| `guide/tip-decision.ts` | `decide()`: the pure function from world to `Guidance`, one section per tip type |
| `guide/guidance-view.ts` | Puts one `Guidance` on the screen and takes the previous one down |
| `guide/tip-region.ts` | Build region of a position quest (386): in view, and a point inside it for the arrow |
| `tip-stall-tracker.service.ts`, `tip-stall.ts` | Stall watchdog and its task names and reasons (stable strings, read from `tip_stall`) |
| `testbed/` | The test bed: a fake world, the real tip code on it, one spec per catalog group |

## Inputs

- **`BaseItemUiService.getTipItemStates(enemyItemTypeId)`** (Java, via the TeaVM bridge): every own
  selectable item and the enemies of the quest's type, over the whole planet, with position, `idle`,
  `buildup` and `factoryBuildQueue`. An item off screen has no rendered instance, so nothing is read
  from instances.
- **`SelectionService.getSelectedOwnItemIds()`** - the selection by id, off screen included.
- **`ActionService.addOrderListener()`** - the orders this client sends. A unit reads idle until the
  engine has taken an order up; the guide treats it as working in between (up to 15 s).
- The cockpit's block reason per button, the placer, the rendered resource fields.

## Tip config

`TipConfig` on the quest: `tipString` (BUILD, FABRICATE, HARVEST, ATTACK, and LOAD, SAIL, UNLOAD for crossing the water - see the TRN cases), `actorItemTypeId`, and
`group` - whether the attack tip asks for a group first (set on 379, not on the first attack 365).
Edited in the server quest editor, stored in `QUEST.tipString`, `tipActorItemType_id`, `tipGroup`.

## Testing

`npx ng test --watch=false --include='**/tip/**/*.spec.ts'`. Every change to the tips runs against
the test bed; a new report becomes a catalog case and a test before it is fixed.
