# Quest Tip System

The quest tip system guides new players through game actions with visual prompts (animated arrows, text labels, markers). It is built as a task-based flow where each step waits for a specific player action before advancing.

## Key Files

| File | Purpose |
|------|---------|
| `razarion-frontend/src/app/game/tip/tip.service.ts` | Main orchestrator, manages lifecycle |
| `razarion-frontend/src/app/game/tip/tip-task.container.ts` | Container managing main + fallback task sequences |
| `razarion-frontend/src/app/game/tip/tip-task.factory.ts` | Factory creating tip flows per quest type |
| `razarion-frontend/src/app/game/tip/tiptask/abstract-tip-task.ts` | Base class with `onSucceed()` / `onFailed()` |
| `razarion-frontend/src/app/game/tip/tiptask/select-tip-task.ts` | Prompts player to select a unit |
| `razarion-frontend/src/app/game/tip/tiptask/start-build-placer-tip-task.ts` | Prompts player to click building in ItemCockpit menu |
| `razarion-frontend/src/app/game/tip/tiptask/send-build-command-tip-task.ts` | Prompts player to place building or click existing one |
| `razarion-frontend/src/app/game/tip/tiptask/idle-item-tip-task.ts` | Waits for a unit to become idle |
| `razarion-frontend/src/app/game/tip/tiptask/send-fabricate-command-tip-task.ts` | Prompts player to fabricate a unit |
| `razarion-frontend/src/app/game/tip/tiptask/send-harvest-command-tip-task.ts` | Prompts player to harvest a resource |
| `razarion-frontend/src/app/game/tip/tiptask/send-attack-command-tip-task.ts` | Prompts player to attack an enemy |

## Tip Types

Defined in `GwtAngularFacade.ts`:

- **BUILD** - Guide through building construction (select builder, open menu, place building)
- **FABRICATE** - Guide through unit fabrication (select factory, click fabricate)
- **HARVEST** - Guide through harvesting (select harvester, click resource)
- **ATTACK** - Guide through attacking (select unit, click enemy)

## Task Lifecycle

Each `AbstractTipTask` has three lifecycle methods:

- **`isFulfilled()`** - Returns `true` if the task's goal is already met (allows skipping)
- **`start()`** - Activates the task: registers listeners, shows visual prompts
- **`cleanup()`** - Removes listeners and visual prompts

And two transition methods:

- **`onSucceed()`** - Task completed, advance to next task
- **`onFailed()`** - Task interrupted (e.g. unit deselected), backtrack to previous task

## Main Sequence and Fallback

Each tip type defines two sequences in `TipTaskFactory`:

1. **Main sequence** - The primary step-by-step flow
2. **Fallback sequence** - Recovery flow activated after the main sequence completes

### Example: BUILD Tip

**Main sequence:**
1. `SelectTipTask` - Select the builder unit
2. `StartBuildPlacerTipTask` - Click building type in ItemCockpit menu
3. `SendBuildCommandTipTask` - Place building on terrain

**Fallback sequence** (activated after building is placed):
1. `IdleItemTipTask` - Wait for builder to finish constructing
2. `SelectTipTask` - Re-select builder if deselected
3. `StartBuildPlacerTipTask` - Re-open build menu if needed
4. `SendBuildCommandTipTask` - Click existing building to resume construction

### Fallback Activation Flow

```
Main sequence completes (all tasks succeed)
    |
    v
TipService.onSucceed() calls activateFallback()
    |
    v
Fallback sequence starts from task 0
    |
    v
If task fails (e.g. unit deselected) -> backtrackTask() finds last unfulfilled task
```

## Task Container Navigation

`TipTaskContainer` provides:

- **`next()`** - Advance index, recursively skip fulfilled tasks
- **`backtrackTask()`** - Walk backwards to find the first non-fulfilled task
- **`activateFallback()`** - Switch from main to fallback sequence

The `isFulfilled()` check enables smart skipping. For example, if the builder is already selected when the fallback starts, `SelectTipTask` is skipped automatically.

## Visual Prompts

### Select Prompt (`showSelectPromptVisualization`)
Animated arrow pointing down at a unit with a text label. Used by:
- `SelectTipTask` - "Click to select" (default)
- `SendBuildCommandTipTask` - "Click to finish building" (when building exists)
- `SendHarvestCommandTipTask` - "Click to harvest"
- `SendAttackCommandTipTask` - "Click to attack"

### Place Marker (`showPlaceMarker`)
Colored disc on terrain showing where to place a building. Used by `SendBuildCommandTipTask` when no building exists yet.

### Out-of-View Marker (`showOutOfViewMarker`)
Directional indicator at screen edge when the target is off-screen. Managed by `TipService` via `ViewFieldListener`.

### ItemCockpit Tip (`showBuildupTip`)
Popover highlighting a specific building type in the build menu. Used by `StartBuildPlacerTipTask`.

## Deselection Handling

Several tasks detect when the player deselects the active unit:

- `StartBuildPlacerTipTask` uses `setSelectionCallback()` on the builder
- `SendBuildCommandTipTask` uses a global `selectionService` listener

When deselection is detected, `onFailed()` triggers `backtrackTask()` which walks back to find the appropriate recovery task (typically `SelectTipTask`).
