# Multiplayer Synchronization Architecture

This document describes how units and game state are kept in sync across multiple connected browser clients.

## Overview

Razarion uses a **hybrid command-forwarding + position-sync** architecture:

1. **Command Forwarding**: Player commands are forwarded by the server to all other clients, which execute them locally
2. **Position Sync (TickInfo)**: The server periodically sends authoritative position snapshots to correct client-side simulation drift

```
Browser A (Sender)                Server (MASTER)              Browser B (Receiver)
     |                                |                              |
     |--- MoveCommand -------------->|                              |
     | (execute locally,             |--- MoveCommand ------------->|
     |  skip syncs 2 ticks)          |   (forward to others)       | (execute locally,
     |                               |                              |  accept syncs)
     |                               |-- tick: ORCA, movement -->  |
     |                               |                              |
     |<-- TickInfo (positions) ------|------- TickInfo ----------->|
     | (skip if within 2 ticks)      |                              | (apply positions)
```

## Client Modes

- **MASTER** (Server): Executes all game logic, is authoritative for positions. Sends TickInfo to all clients.
- **SLAVE** (Browser): Executes commands locally for responsiveness. Accepts server TickInfo to correct drift.

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
  -> BaseItemService.executeForwardedCommand(cmd, markLocallyCommanded=true)
  -> gameLogicService.onSlaveCommandSent(item, cmd)  // sends to server
```

### 2. Server Receives Command

`ClientGameConnection.onPackageReceived()`:
- Deserializes the command
- Sets `forwardedByConnection = true` (transient flag, not serialized)
- Executes the command on the **MASTER** simulation via `CommandService.executeCommand()`
- **Broadcasts** the command to all OTHER clients via `ClientGameConnectionService.broadcastCommand()`

```
ClientGameConnection
  -> cmd.setForwardedByConnection(true)
  -> commandService.executeCommand(cmd)    // MASTER queues for next tick
  -> clientGameConnectionService.broadcastCommand(packet, cmd, excludeUserId)
```

### 3. Other Clients Receive Forwarded Command (Browser B)

`AbstractServerGameConnection.handleMessage()`:
- Deserializes the command using the platform marshaller (e.g. `TeaVMWorkerMarshaller`)
- Executes locally via `BaseItemService.executeForwardedCommand(cmd, markLocallyCommanded=false)`
- Since `markLocallyCommanded=false`, the unit accepts server TickInfo corrections immediately

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
| `razarion-server/.../ClientGameConnection.java` | WebSocket endpoint per client, receives commands, triggers broadcast |
| `razarion-server/.../ClientGameConnectionService.java` | Manages all connections, `broadcastCommand()` and `sendTickinfo()` |
| `razarion-server/.../ServerSyncService.java` | Implements `SyncService.internSendTickInfo()` for server-side broadcast |

### Shared (Server + Client)

| File | Purpose |
|------|---------|
| `razarion-share/.../CommandService.java` | MASTER/SLAVE command routing |
| `razarion-share/.../BaseItemService.java` | `executeForwardedCommand()` for command execution on clients |
| `razarion-share/.../SyncService.java` | Abstract TickInfo accumulation and dispatch |
| `razarion-share/.../PlanetService.java` | `initialSlaveSyncItemInfo()` — applies the snapshot, replacing on reconnect |
| `razarion-share/.../SyncItemContainerServiceImpl.java` | `initAndAddSlave()` — refuses a taken id without touching what is there |
| `razarion-share/.../SyncPhysicalMovable.java` | `synchronize()`, `skipSyncTicks`, movement physics |
| `razarion-share/.../AbstractServerGameConnection.java` | Client-side WebSocket handler, dispatches received commands |
| `razarion-share/.../command/BaseCommand.java` | `forwardedByConnection` transient flag |

### Client (TeaVM)

| File | Purpose |
|------|---------|
| `razarion-client-worker-teavm/.../TeaVMWorkerMarshaller.java` | Deserializes forwarded commands from JSON |

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

The `forwardedByConnection` flag distinguishes player commands (forwarded via WebSocket) from bot commands (internal server execution). Both call `notifySendSyncBaseItem()` for TickInfo inclusion.
