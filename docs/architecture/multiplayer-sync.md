# Multiplayer Synchronization Architecture

This document describes how units and game state are kept in sync across multiple connected browser clients.

## Overview

Razarion uses **local prediction for your own commands + position-sync for everything else**:

1. **Local prediction**: the browser that issued a command runs it immediately, so the unit reacts
   without waiting for the round trip
2. **Position Sync (TickInfo)**: the server periodically sends authoritative position snapshots,
   which is how every *other* player's actions arrive

```
Browser A (Sender)                Server (MASTER)              Browser B (Receiver)
     |                                |                              |
     |--- MoveCommand -------------->|                              |
     | (execute locally,             | (execute on MASTER)          |
     |  skip syncs 2 ticks)          |                              |
     |                               |-- tick: ORCA, movement -->   |
     |                               |                              |
     |<-- TickInfo (positions) ------|------- TickInfo ----------->|
     | (skip if within 2 ticks)      |                              | (apply positions)
```

### A third arrow used to be here

The server also broadcast each command to the other clients, which executed it locally and
predicted its outcome. That half was removed from the client in `4adfe4d75` ("removal of local
command forwarding in SLAVE mode"), as part of fixing a SharedArrayBuffer race — but the server
went on sending for seven months. `AbstractServerGameConnection` has no case for those packets,
so every recipient threw `IllegalArgumentException: Unknown Packet` on arrival.

It reached the tracking as `ENGINE_ERROR`: over seven days, 330 of them in 18 % of all sessions,
and concentrated where it mattered — 59 % of the sessions that got as far as issuing a command saw
one, against 6 % of the sessions that never placed a base. The broadcast is gone as of this
document's revision.

The consequence for anyone reading this to understand latency: **another player's units move on
your screen only as fast as TickInfo arrives.** There is no longer a faster path, and there is no
prediction of what somebody else did.

## Client Modes

- **MASTER** (Server): Executes all game logic, is authoritative for positions. Sends TickInfo to all clients.
- **SLAVE** (Browser): Executes its *own* player's commands locally for responsiveness. Accepts
  server TickInfo to correct drift, and learns about other players only from it.

## Reconnecting: the snapshot replaces the world, it does not add to it

The server sends the **full** picture of the world on every `afterConnectionEstablished` — not what
changed, and not only on the first connection. A mobile game socket closes far more often than a
desktop one (`Code: 1006 WasClean: false` on a radio handover is routine), so a client sees this
snapshot repeatedly.

`PlanetService.initialSlaveSyncItemInfo()` therefore clears the slave world before applying a
snapshot that arrives into a populated one:

```
if (!syncItemContainerService.isEmpty()) {     // a reconnect, not a first connect
    baseItemService.clearSlave();              // quiet removal, per item
    resourceService.clearSlave();
    boxService.clearSlave();
    syncItemContainerService.clear();
}
```

**"Quiet" is the requirement.** Each `clearSlave()` tells the UI the item is *gone*
(`onSyncBaseItemRemoved` → a plain id in the worker's removed list), never that it *died*
(`onSyncBaseItemKilledSlave` → explosions, sounds, quest progress). A reconnect must look like a
redraw, not like the player's base being destroyed. For the same reason `clearSlave()` does not go
through `removeSyncItem`, which carries base bookkeeping, energy accounting and an
`onBaseRemoved` that quests listen to. Nothing died.

The first connection is untouched: the container is empty, nothing is cleared, and the path is
exactly what it always was.

### What this fixed

**PROD, 2026-08-30.** A phone's socket closed five seconds after the player placed their factory.
The snapshot was applied additively, sixteen ids collided, and
`SyncItemContainerServiceImpl.initAndAddSlave()` — which put into the map *before* checking whether
the id was free — replaced each positioned item with the half-built newcomer and only then threw.
Every caller catches and logs, so the wreckage stayed: items with no `syncPhysicalArea`, and
`onPostTick failed` for every item on every tick from then on. The player watched a factory that
never finished and units that had vanished; only a browser reload recovered it.

Both halves are fixed — the check now precedes the mutation in `initAndAdd` **and**
`initAndAddSlave` (the master's ids are generated, so it cannot collide there, but the mistake was
the same), and the snapshot is idempotent. `SlaveReconnectTest` covers both and fails on the old
code with the exact production message, `no syncPhysicalArea|null`.

## Command Flow

### 1. Player Issues a Command (Browser A)

`CommandService.executeCommand()` in SLAVE mode:
- Executes the command **locally** for immediate visual feedback (no round-trip delay)
- Marks the unit with `skipSyncTicks = 2` so that stale TickInfo from the server doesn't cause teleportation
- Sends the command to the server via WebSocket

```
CommandService (SLAVE)
  -> executes locally, sets skipSyncTicks = 2
  -> gameLogicService.onSlaveCommandSent(item, cmd)  // sends to server
```

### 2. Server Receives Command

`ClientGameConnection.onPackageReceived()`:
- Deserializes the command
- Executes it on the **MASTER** simulation via `CommandService.executeCommand()`

```
ClientGameConnection
  -> commandService.executeCommand(cmd)    // MASTER queues for next tick
```

That is the whole step. It used to also broadcast the command to every other client — see
*A third arrow used to be here* above for what that cost and why it is gone.
`BaseCommand.forwardedByConnection` went with it: the flag was set on every command and read by
nobody.

### 3. Other Clients Learn About It

Only through TickInfo, below. There is no command-shaped message on the wire from server to
client, and `AbstractServerGameConnection` deliberately has no case for one.

### 4. Server Sends TickInfo

After each game tick, the server sends position/state snapshots for items that changed:

```
PlanetService.tick()
  -> pathingService.tick()       // ORCA collision avoidance, movement
  -> baseItemService.tick()      // command execution, item lifecycle
  -> syncService.sendTickInfo()  // batch all notified items into TickInfo
```

Items are registered for TickInfo via `syncService.notifySendSyncBaseItem()` when:
- A command starts executing (in `BaseItemService.executeCommand()`)
- A unit stops or becomes idle
- A unit is spawned or deleted

### 5. Client Applies TickInfo

`SyncPhysicalMovable.synchronize()`:
- If `skipSyncTicks > 0`: **Skip** this sync (own command was just issued, TickInfo is stale)
- Otherwise: Apply server position, velocity, and path as authoritative values
- Sets `tickSynchronized = true` so `implementPosition()` skips local movement for this tick (avoiding double-movement)

## Anti-Teleportation: skipSyncTicks

When a player issues a command, there's a brief window where the server's TickInfo still reflects the **old** state (before the command arrived). Applying this stale data would cause the unit to "teleport" back.

Solution: `SyncPhysicalMovable.skipSyncTicks`
- Set to `2` when a local command is issued (`markLocalCommand()`)
- Decremented each tick in `setupPreferredVelocity()`
- Reset to `0` on `stop()` (accept syncs immediately when unit stops)
- While `> 0`, `synchronize()` returns early (ignores server TickInfo)

After 2 ticks, the server TickInfo reflects the new command state, and syncs resume to correct any ORCA drift.

## Key Files

### Server

| File | Purpose |
|------|---------|
| `razarion-server/.../ClientGameConnection.java` | WebSocket endpoint per client, receives commands and runs them on the MASTER |
| `razarion-server/.../ClientGameConnectionService.java` | Manages all connections and `sendTickinfo()` |
| `razarion-server/.../ServerSyncService.java` | Implements `SyncService.internSendTickInfo()` for server-side broadcast |

### Shared (Server + Client)

| File | Purpose |
|------|---------|
| `razarion-share/.../CommandService.java` | MASTER/SLAVE command routing |
| `razarion-share/.../BaseItemService.java` | Command execution and item lifecycle |
| `razarion-share/.../SyncService.java` | Abstract TickInfo accumulation and dispatch |
| `razarion-share/.../PlanetService.java` | `initialSlaveSyncItemInfo()` — applies the snapshot, replacing on reconnect |
| `razarion-share/.../SyncItemContainerServiceImpl.java` | `initAndAddSlave()` — refuses a taken id without touching what is there |
| `razarion-share/.../SyncPhysicalMovable.java` | `synchronize()`, `skipSyncTicks`, movement physics |
| `razarion-share/.../AbstractServerGameConnection.java` | Client-side WebSocket handler. Deliberately has no case for command packets - see above |

### Client (TeaVM)

| File | Purpose |
|------|---------|
| `razarion-client-worker-teavm/.../TeaVMWorkerMarshaller.java` | Marshals commands to JSON on the way out, TickInfo on the way in |

## ORCA Collision Avoidance

The ORCA (Optimal Reciprocal Collision Avoidance) algorithm runs on both server and client. It is **deterministic** given the same inputs:
- `PathingService.tick()` iterates units in ID order (`TreeSet<Comparator.comparingInt(SyncItem::getId)>`)
- `ItemVelocityCalculator` iterates neighbors sorted by distance + ID
- Same unit positions + velocities = same ORCA output

However, clients may diverge from the server because:
- Commands arrive at different ticks (network latency)
- Floating-point timing differences

TickInfo corrects this drift periodically.

## Bot Commands

Bot commands are executed server-side only. They go through `BaseItemService.executeCommand()` which calls `syncService.notifySendSyncBaseItem()`. Clients receive bot unit updates via TickInfo position sync, not via command forwarding.

Player commands and bot commands take the same road out: both run on the MASTER and both call `notifySendSyncBaseItem()` for TickInfo inclusion. There is no longer any distinction on the wire - a `forwardedByConnection` flag used to mark the first kind and was read by nobody.
