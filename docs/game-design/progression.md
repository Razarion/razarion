# Razarion Game Progression Design

This document defines the game flow, progression, and content design for Razarion. It serves as the source of truth for AI-driven content creation via the razarion-ai-content MCP server.

## 1. Game Concept

**Genre:** Browser-based multiplayer Real-Time Strategy (RTS) with a persistent shared world.

**Setting:** All units in the game are robots/machines — there are no human soldiers. Players command fleets of autonomous bots on a mechanized battlefield.

**Core Loop:** Harvest resources → Build base → Deploy units → Complete quests → Fight bots/players → Level up → Unlock new units/buildings → Repeat

**Player Goal:** Build and expand a base on a shared persistent planet, progress through four distinct map phases — from a safe beginner island to competitive alliance warfare.

**Session Length:** Designed for short sessions (10-20 min), with long-term progression across sessions.

---

## 2. Map Phases Overview

The planet is divided into **four geographic phases**. Each phase has its own gameplay identity, bot behavior, unlock mechanics, and difficulty. Players progress from Phase 1 to Phase 4 by leveling up. The phases are physically separated areas on the map.

```
Planet: 5120 x 5120 m (32 x 32 tiles)
Origin (0,0) = bottom-left corner, Y increases upward

Y
5120 ┌──────────────────────┬──────────────────────────────┐
     │                      │                              │
     │                      │                              │
     │    PHASE 4: Alliance Warzone                        │
     │    (PvP, guild warfare, bot alliances)              │
     │    [OPEN DESIGN]                                    │
     │                      │                              │
2500 ├──────────────────────┤                              │
     │                      │                              │
     │  P2: Semi-Noob       │                              │
     │  Frontier             │                              │
     │  (Crystal boxes,     │  PHASE 3: The Siege          │
     │   unlock buildings   │  (Aggressive bots,           │
     │   & vehicles)        │   survival & defense)        │
 800 ├────────┐             │                              │
     │ P1:    │             │                              │
     │ Noob   │             │                              │
     │ Island │             │                              │
   0 └────────┴─────────────┴──────────────────────────────┘
     0       820          2000                           5120  X
```

### Phase Coordinates (PlaceConfig regions)

| Phase | X Range | Y Range | Size | Area |
|-------|---------|---------|------|------|
| 1 - Noob Island | 0 – 820 | 0 – 800 | 820 x 800 m | 0.66 km² |
| 2 - Semi-Noob Frontier | 0 – 2000 x 0 – 2000 (minus P1) | — | ~2000 x 2000 m | ~3.34 km² |
| 3 - The Siege | 2000 – 5120 | 0 – 2500 | 3120 x 2500 m | 7.80 km² |
| 4 - Alliance Warzone | 0 – 5120 x 2500 – 5120 + 0 – 2000 x 2000 – 2500 (L-shape) | — | L-shaped | ~14.42 km² |

**Phase boundary logic:**
1. If X < 820 and Y < 800 → **Phase 1**
2. If X < 2000 and Y < 2000 (and not Phase 1) → **Phase 2**
3. If X ≥ 2000 and Y < 2500 → **Phase 3**
4. Everything else → **Phase 4**

### Phase Transition

Players transition between phases by reaching a required level. The transition is **not automatic** — the player must actively choose to leave.

**Phase 1 → Phase 2:** At Level 9, the player unlocks the **Transporter** — a special unit that carries a Razaworker from Noob Island across the terrain barrier to the Phase 2 region. This is a one-way trip: the Transporter delivers the Razaworker to the new territory, where the player starts building a new base from scratch. A final quest then asks the player to **sell their old base** on Noob Island — this gives them Razarion as starting capital for Phase 2 and frees up the island space for new players. This creates a clean "leaving home" moment with a tangible reward.

**Later transitions** (Phase 2→3, 3→4): Mechanism TBD (may also use Transporters or other gating mechanics).

| Phase | Levels | Theme | Bot Behavior |
|-------|--------|-------|-------------|
| 1 - Noob Island | 1-9 | Safe tutorial & early growth | Passive, only fights back when attacked |
| 2 - Semi-Noob Frontier | 10-17 | Exploration & unlock | Defensive outposts, guards territory |
| 3 - The Siege | 18-24 | Survival & defense | Bots actively attack player bases |
| 4 - Alliance Warzone | 25+ | PvP & diplomacy | Bots can be allied, player conflict focus |

---

## 3. Phase 1: Noob Island

### 3.1 Concept

A safe, isolated area where new players learn the game over **9 levels**. Bots are passive and won't attack unless provoked. Resources are abundant. Quests guide the player through all basic mechanics. The extended level range allows gradual introduction of units, buildings, and increasingly complex bot encounters — all within a safe environment.

### 3.2 Available Units & Buildings

| Name | Role | Cost (Razarion) | Health | Unlocked At | Notes |
|------|------|-----------------|--------|-------------|-------|
| **Razaworker** | Construction & harvest bot | 30 | 50 | Level 1 (start) | Start unit, can harvest and build |
| **Razascout** | Fast recon drone | 20 | 30 | Level 1 | Cheap, fast, weak weapon |
| **Razabot** | Basic combat bot | 40 | 80 | Level 1 | Backbone of early army |
| **Command Center** | Main building | — | 500 | Level 1 (start) | One per player, spawns with base |
| **Supply Depot** | Provides +5 house space | 50 | 200 | Level 1 | Required to grow army |
| **Bot Factory** | Produces light units (Razascout, Razabot) | 100 | 300 | Level 1 | |
| **Tower** | Static defense turret | 80 | 250 | Level 3 | First defensive structure |
| **Razatron** | Heavy combat bot | 60 | 120 | Level 5 | Tougher than Razabot |
| **Advanced Bot Factory** | Produces heavy units (Razatron) | 150 | 350 | Level 5 | |
| **Refinery** | Boosts harvest efficiency | 100 | 300 | Level 7 | Economic advantage for late Phase 1 |
| **Transporter** | Carries a Razaworker to Phase 2 | 200 | 150 | Level 9 | One-way trip off the island, single use |

### 3.3 Economy

| Parameter | Value |
|-----------|-------|
| Start Razarion | 200 |
| Start Units | 1 Razaworker |
| Resource nodes | Abundant, close to start zones |
| Harvest rate | ~10 Razarion per trip |

### 3.4 Bots

**Raider Patrol (Trivial)** — Target for Level 1-2
- Composition: 2 Razascouts
- Behavior: **Passive** — does NOT attack players, only fights back when attacked
- Respawn: Very slow (rePopTime: 150s)
- Enragement: None
- Purpose: First combat encounter, nearly zero risk

**Raider Camp (Easy)** — Target for Level 3-4
- Composition: 3 Razascouts, 2 Razabots
- Behavior: **Passive** — fights back when attacked but does not pursue
- Respawn: Slow (rePopTime: 120s)
- Enragement: None
- Purpose: Teaches combat with mixed unit types

**Raider Outpost (Easy+)** — Target for Level 5-6
- Composition: 4 Razabots, 1 Tower
- Behavior: **Passive** — defends position but does not pursue
- Respawn: Slow (rePopTime: 100s)
- Enragement: None
- Purpose: Teaches attacking fortified positions, requires Tower or Razatron

**Raider Fortress (Moderate)** — Target for Level 7-9
- Composition: 3 Razabots, 2 Razatrons, 2 Towers
- Behavior: **Passive** — strong static defense, does not pursue
- Respawn: Medium (rePopTime: 90s)
- Enragement: After 3 kills → adds 1 Razabot
- Purpose: Final Phase 1 challenge, prepares player for Phase 2 difficulty

### 3.5 Quests

Quests are grouped by level range. Each group introduces mechanics appropriate for that stage.

**Level 1-2: Tutorial Basics**

| # | Quest Name | Objective | Condition | Reward |
|---|-----------|-----------|-----------|--------|
| 1 | First Steps | Build a Supply Depot | SYNC_ITEM_CREATED: Supply Depot x1 | 30 XP, 50 Razarion |
| 2 | Harvest Time | Harvest 50 Razarion | HARVEST: 50 | 30 XP |
| 3 | Deploy Bots | Build 2 Razabots | SYNC_ITEM_CREATED: Razabot x2 | 40 XP, 30 Razarion |
| 4 | First Contact | Kill 2 enemy Razascouts | SYNC_ITEM_KILLED: x2 (botId: Raider Patrol) | 50 XP |

**Level 3-4: Combat Fundamentals**

| # | Quest Name | Objective | Condition | Reward |
|---|-----------|-----------|-----------|--------|
| 5 | Fortify | Build a Tower | SYNC_ITEM_CREATED: Tower x1 | 80 XP, 80 Razarion |
| 6 | Scouting Party | Build 3 Razascouts | SYNC_ITEM_CREATED: Razascout x3 | 60 XP |
| 7 | Clear the Camp | Destroy a Raider Camp | BASE_KILLED: x1 (botId: Raider Camp) | 120 XP, 100 Razarion |
| 8 | Growing Force | Have 6 combat units | SYNC_ITEM_CREATED: x6 (includeExisting) | 100 XP |

**Level 5-6: Advanced Units**

| # | Quest Name | Objective | Condition | Reward |
|---|-----------|-----------|-----------|--------|
| 9 | Heavy Metal | Build an Advanced Bot Factory | SYNC_ITEM_CREATED: Advanced Bot Factory x1 | 150 XP, 120 Razarion |
| 10 | Iron Guard | Build 2 Razatrons | SYNC_ITEM_CREATED: Razatron x2 | 150 XP |
| 11 | Outpost Assault | Destroy a Raider Outpost | BASE_KILLED: x1 (botId: Raider Outpost) | 200 XP, 150 Razarion |
| 12 | Harvest Master | Harvest 500 Razarion total | HARVEST: 500 | 180 XP |

**Level 7-9: Mastery & Preparation**

| # | Quest Name | Objective | Condition | Reward |
|---|-----------|-----------|-----------|--------|
| 13 | Efficiency | Build a Refinery | SYNC_ITEM_CREATED: Refinery x1 | 200 XP, 150 Razarion |
| 14 | Full Army | Have 12 combat units | SYNC_ITEM_CREATED: x12 (includeExisting) | 200 XP |
| 15 | Fortress Breaker | Destroy a Raider Fortress | BASE_KILLED: x1 (botId: Raider Fortress) | 350 XP, 250 Razarion |
| 16 | Island Champion | Destroy 3 bot bases total | BASE_KILLED: x3 | 400 XP, 300 Razarion |
| 17 | Build the Transporter | Build a Transporter | SYNC_ITEM_CREATED: Transporter x1 | 500 XP |
| 18 | Leave the Island | Transport a Razaworker to Phase 2 | SYNC_ITEM_POSITION: Razaworker in Phase 2 region | 600 XP |
| 19 | Sell the Old Base | Sell all remaining buildings on Noob Island | SELL: all buildings in Phase 1 region | 500 Razarion |

### 3.6 Map Layout

- **Region:** Bottom-left corner (X: 0–820, Y: 0–800), ~0.66 km²
- The lake with its island forms a natural boundary — water surrounds the play area
- Multiple start zones so new players don't overlap too much
- Resource nodes within short walking distance of every start zone
- Bot difficulty increases with distance from start zones:
  - Near start: 3-4 Raider Patrols (Level 1-2 targets)
  - Mid island: 2-3 Raider Camps (Level 3-4 targets)
  - Far from start: 2 Raider Outposts (Level 5-6 targets)
  - Island edges: 1-2 Raider Fortresses (Level 7-9 targets)
- The lake edge forms the boundary to Phase 2 — the Transporter crosses the water to reach P2
- **Transporter launch zone** near lake edge: designated area where the Transporter departs

---

## 4. Phase 2: Semi-Noob Frontier

### 4.1 Concept

Players leave the safety of Noob Island and enter a larger, more challenging territory. The key mechanic difference: **buildings and vehicles are unlocked by finding Crystal items in boxes**, not just by leveling up. This encourages exploration and creates a treasure-hunt element.

Bot outposts are more complex (multiple unit types, defensive structures) and guard the areas where valuable boxes spawn.

### 4.2 Crystal Unlock Mechanic

Boxes in Phase 2 contain **Crystals**. Crystals are a special currency used exclusively to unlock new unit and building types.

| Unlock | Crystal Cost | Found In |
|--------|-------------|----------|
| Vehicle Factory | 3 Crystals | Boxes near outposts |
| Tank | 2 Crystals | Boxes near outposts |
| Razacannon | 4 Crystals | Boxes near Armored Outposts |

This means two players at the same level may have different unlocks depending on which boxes they've found. This creates variety and incentivizes exploration over pure grinding. Note: Razatron and Refinery are already available from Phase 1 (Level 5 and 7).

### 4.3 New Units & Buildings (unlocked via Crystals)

| Name | Role | Crystal Cost | Razarion Cost | Health | Notes |
|------|------|-------------|---------------|--------|-------|
| **Tank** | Armored vehicle | 2 Crystals | 120 | 200 | Strong but slow |
| **Vehicle Factory** | Produces vehicles | 3 Crystals | 200 | 400 | Required for Tank |
| **Razacannon** | Mobile artillery bot | 4 Crystals | 180 | 80 | Long range, fragile, Phase 2 exclusive |

### 4.4 Bots

**Frontier Outpost (Medium)**
- Composition: 3 Razatrons, 2 Razabots, 1 Tower
- Behavior: **Defensive** — patrols territory, attacks players who enter their realm, but does NOT leave realm to pursue
- Respawn: Medium (rePopTime: 90s)
- Enragement: After 5 kills → adds 2 Razabots
- Purpose: Guards crystal box regions, requires tactical approach

**Armored Outpost (Medium+)**
- Composition: 2 Razatrons, 2 Tanks, 2 Towers
- Behavior: **Defensive** — stronger static defense
- Respawn: Medium (rePopTime: 80s)
- Enragement: After 4 kills → adds 1 Tank
- Purpose: Guards high-value box areas, requires vehicles to defeat efficiently

### 4.5 Boxes & Crystal Distribution

| Box Type | Contents | Spawn Location | Spawn Interval | Count |
|----------|---------|----------------|----------------|-------|
| Crystal Box | 1 Crystal | Near Frontier Outposts | 180-300s | 3 |
| Rich Crystal Box | 2 Crystals | Near Armored Outposts (guarded) | 300-600s | 2 |
| Resource Box | 100 Razarion | Scattered across phase | 120-240s | 4 |
| XP Box | 50 XP | Scattered across phase | 150-300s | 3 |

### 4.6 Quests

| # | Quest Name | Objective | Condition | Reward |
|---|-----------|-----------|-----------|--------|
| 1 | New Horizons | Build a Command Center in Phase 2 | SYNC_ITEM_CREATED: Command Center x1 (in Phase 2 region) | 60 XP |
| 2 | Crystal Hunter | Pick up your first box | BOX_PICKED: x1 | 50 XP, 50 Razarion |
| 3 | Mechanized | Build a Vehicle Factory | SYNC_ITEM_CREATED: Vehicle Factory x1 | 120 XP |
| 4 | Tank Commander | Build 2 Tanks | SYNC_ITEM_CREATED: Tank x2 | 150 XP, 100 Razarion |
| 5 | Outpost Buster | Destroy a Frontier Outpost | BASE_KILLED: x1 (botId: Frontier Outpost) | 200 XP, 150 Razarion |
| 6 | Crystal Collector | Collect 5 Crystals total | BOX_PICKED: x5 (crystal type) | 150 XP, 2 Crystals |
| 7 | Armored Assault | Destroy an Armored Outpost | BASE_KILLED: x1 (botId: Armored Outpost) | 300 XP, 200 Razarion |

### 4.7 Map Layout

- **Region:** X: 0–2000, Y: 0–2000 (minus Phase 1 area), ~3.34 km²
- Wraps around Phase 1 (the lake) on the top and right side
- Includes the elevated ring/hill terrain feature (~X: 900–1800, Y: 1200–2100)
- Terrain is more varied (chokepoints, elevated areas, open plains)
- Bot outposts guard the approaches to valuable box spawn areas
- Resource nodes are more spread out than Phase 1 (longer supply lines)
- Transition to Phase 3 is across the eastern boundary (X=2000), transition to Phase 4 across the northern boundary (Y=2000)

---

## 5. Phase 3: The Siege

### 5.1 Concept

A fundamental shift in gameplay. In Phases 1 and 2, bots were passive or defensive — players chose when to fight. **In Phase 3, bots actively attack player bases.** This creates a survival/defense dynamic where players must:

- Build strong defenses (towers, walls)
- Maintain a standing army to repel attacks
- Manage economy under pressure
- Optionally cooperate with nearby players for mutual defense

The bot aggression is periodic (wave-based), giving players time to recover and build between attacks.

### 5.2 New Units & Buildings

| Name | Role | Unlocked At | Cost | Health | Notes |
|------|------|-------------|------|--------|-------|
| **Artillery** | Long-range siege | Level 18 | 150 Razarion | 100 | Long range, fragile |
| **Fortified Tower** | Heavy defense | Level 18 | 120 Razarion | 400 | Stronger tower variant |
| **Repair Station** | Heals nearby units | Level 21 | 100 Razarion | 200 | Passive area heal |

### 5.3 Bot Behavior: Attack Waves

Unlike Phases 1-2, bots in Phase 3 **actively send attack waves** against nearby player bases.

**Siege Bot (Aggressive)**
- Composition varies by wave:
  - Wave 1: 4 Razabots, 2 Razascouts
  - Wave 2: 6 Razabots, 2 Tanks
  - Wave 3: 4 Razabots, 3 Tanks, 1 Artillery
- Behavior: **Aggressive** — periodically sends units to attack nearest player base
- Attack interval: Every 5-10 minutes (minActiveMs/maxActiveMs)
- Between waves: Rebuilds forces at home base (minInactiveMs/maxInactiveMs)
- Enragement: Escalates wave composition when player destroys bot units

**Siege Fortress (Hard)**
- Composition: 6 Razabots, 4 Tanks, 2 Artillery, 3 Fortified Towers
- Behavior: **Aggressive** — sends large attack waves AND has strong home defense
- Attack interval: Every 8-15 minutes
- Enragement: After losing 5 units → next wave includes +2 Tanks; After losing 10 → adds Artillery

### 5.4 Defense Mechanics

Players need to think about:
- **Tower placement**: Covering approaches to their base
- **Chokepoints**: Using terrain to funnel attackers
- **Standing army**: Keeping units alive between waves, not just building when attacked
- **Economy under siege**: Harvesters may be targeted, requiring escort or protection

### 5.5 Quests

| # | Quest Name | Objective | Condition | Reward |
|---|-----------|-----------|-----------|--------|
| 1 | Brace Yourself | Build 2 Fortified Towers | SYNC_ITEM_CREATED: Fortified Tower x2 | 200 XP, 150 Razarion |
| 2 | First Defense | Survive a bot attack wave (kill 5 attackers) | SYNC_ITEM_KILLED: x5 (botId: Siege Bot) | 250 XP |
| 3 | Artillery Line | Build 2 Artillery | SYNC_ITEM_CREATED: Artillery x2 | 200 XP, 100 Razarion |
| 4 | Siege Breaker | Destroy a Siege Bot base | BASE_KILLED: x1 (botId: Siege Bot) | 400 XP, 300 Razarion |
| 5 | Fortress Storm | Destroy a Siege Fortress | BASE_KILLED: x1 (botId: Siege Fortress) | 600 XP, 500 Razarion |
| 6 | Repair Protocol | Build a Repair Station | SYNC_ITEM_CREATED: Repair Station x1 | 300 XP |

### 5.6 Map Layout

- **Region:** X: 2000–5120, Y: 0–2500, 3120 x 2500 m = 7.80 km²
- Located to the right of Phase 2, separated by terrain barriers along X=2000
- Harsh, contested territory
- Fewer safe spots — players must create their own safety through defense
- Siege Bot bases are positioned to threaten multiple player build areas
- Resource nodes exist but are in exposed locations (risk/reward)
- Narrow corridors and defensive terrain features encourage smart base placement

---

## 6. Phase 4: Alliance Warzone (Open Design)

> **Status: Early concept — many details TBD**

### 6.1 Vision

The endgame phase where the game transitions from PvE to PvP. Key ideas:

- **Bot alliances**: Players can ally with certain bot factions. The allied bot provides support units and defends the player, but expects the player to fight its enemies (rival bots and their allied players)
- **Guild system**: Players form guilds for coordinated attacks, shared defense, and territory control
- **Territory control**: Guilds compete to control resource-rich zones
- **Player vs Player**: The primary conflict is between players/guilds, with bots as allies rather than primary enemies

### 6.2 Bot Alliance Mechanic (Concept)

- Several bot factions exist in Phase 4, each controlling territory
- A player can choose to ally with ONE faction
- Allied bots:
  - Stop attacking the player
  - Provide support units during attacks
  - Share territory (player can build in bot realm)
- In return, the player is expected to fight rival factions (and their allied players)
- Switching alliances has a cooldown and costs resources

### 6.3 Guild Mechanics (Concept)

- Players form guilds (exact implementation TBD)
- Guild benefits:
  - Shared defense: Guild members' bases reinforce each other
  - Coordinated attacks: Quest rewards for guild-wide objectives
  - Territory bonuses: Guilds controlling a zone get resource bonuses
- Guild wars: Guilds can formally declare war, enabling base attacks between members

### 6.4 Open Questions

- How does alliance switching work? Cooldown? Cost?
- How are guild territories defined? Fixed zones or dynamic borders?
- What prevents a dominant guild from snowballing?
- Are there NPC quests in Phase 4, or is it purely player-driven?
- How do new Phase 4 players survive against established guilds?
- What new units/buildings are specific to Phase 4?
- How does the bot alliance affect the economy?

### 6.5 Map Layout (Concept)

- **Region:** L-shaped — X: 0–5120, Y: 2500–5120 plus X: 0–2000, Y: 2000–2500, ~14.42 km²
- Largest area on the map (above Phase 2 and Phase 3)
- Multiple bot faction territories
- Rich resource zones at contested borders between factions
- Open terrain favoring large army battles
- Guild "capitals" — defensible positions for guild HQs

---

## 7. Units & Buildings Summary (All Phases)

### 7.1 Buildings

| Name | Role | Phase | Unlock Method | Cost (Razarion) | Health |
|------|------|-------|--------------|-----------------|--------|
| Command Center | Main building | 1 | Start | — | 500 |
| Supply Depot | +5 house space | 1 | Start | 50 | 200 |
| Bot Factory | Produces light units | 1 | Start | 100 | 300 |
| Tower | Static defense | 1 | Level 3 | 80 | 250 |
| Advanced Bot Factory | Produces heavy units | 1 | Level 5 | 150 | 350 |
| Refinery | Harvest efficiency | 1 | Level 7 | 100 | 300 |
| Transporter | Carries Razaworker to Phase 2 | 1 | Level 9 | 200 | 150 |
| Vehicle Factory | Produces vehicles | 2 | Crystals | 200 | 400 |
| Fortified Tower | Heavy defense | 3 | Level 18 | 120 | 400 |
| Repair Station | Area heal | 3 | Level 21 | 100 | 200 |

### 7.2 Units

| Name | Role | Phase | Unlock Method | Cost | Health | Speed | Weapon |
|------|------|-------|--------------|------|--------|-------|--------|
| Razaworker | Construction & harvest bot | 1 | Start | 30 | 50 | Medium | None |
| Razascout | Fast recon drone | 1 | Start | 20 | 30 | Fast | Weak |
| Razabot | Basic combat bot | 1 | Start | 40 | 80 | Medium | Medium |
| Razatron | Heavy combat bot | 1 | Level 5 | 60 | 120 | Medium | Medium+ |
| Tank | Armored vehicle | 2 | Crystals | 120 | 200 | Slow | Strong |
| Artillery | Long-range siege | 3 | Level 18 | 150 | 100 | Very slow | Very strong |

### 7.3 Balance Philosophy

- Phase 1 units are cheap and fast but individually weak
- Phase 2 introduces specialization through crystal unlocks — not everyone has the same army
- Phase 3 requires balanced army composition (pure offense fails against waves)
- No single unit should dominate; counters exist (Razascouts counter Artillery, Tanks counter Razabots)
- Buildings should be affordable enough that losing a base is recoverable

---

## 8. Economy

### 8.1 Resources

| Resource | How Obtained | Used For |
|----------|-------------|----------|
| Razarion | Harvested from resource nodes by Razaworkers | Construction, unit production |
| Crystals | Found in boxes (Phase 2+) | Unlocking new unit/building types |

### 8.2 Cost Scaling by Phase

| Phase | Unit/Building Costs | Resource Availability |
|-------|--------------------|--------------------|
| 1 - Noob Island | 20-100 Razarion | Abundant, close to base |
| 2 - Semi-Noob | 60-200 Razarion + Crystals for unlocks | Spread out, some guarded |
| 3 - The Siege | 100-200 Razarion | Exposed, requires defense |
| 4 - Alliance Warzone | TBD | Contested between factions |

---

## 9. Level Progression & Unlocks

### 9.1 Level Table

| Level | XP to Next | Cumulative XP | Phase | Key Unlocks |
|-------|-----------|---------------|-------|-------------|
| 1 | 50 | 0 | Phase 1 | Razaworker, Razascout, Razabot, Supply Depot, Bot Factory |
| 2 | 80 | 50 | Phase 1 | Higher item limits |
| 3 | 120 | 130 | Phase 1 | Tower |
| 4 | 180 | 250 | Phase 1 | Higher item limits |
| 5 | 250 | 430 | Phase 1 | Razatron, Advanced Bot Factory |
| 6 | 350 | 680 | Phase 1 | Higher item limits |
| 7 | 500 | 1030 | Phase 1 | Refinery |
| 8 | 700 | 1530 | Phase 1 | Higher item limits |
| 9 | 1000 | 2230 | Phase 1 | Higher item limits; ready for Phase 2 |
| 10 | 1500 | 3230 | Phase 2 | Access to Phase 2; Crystal unlocks available |
| 11-17 | ... | ... | Phase 2 | Gradual unlocks via Crystals + levels |
| 18 | ... | ... | Phase 3 | Access to Phase 3; Artillery, Fortified Tower |
| 19-24 | ... | ... | Phase 3 | Repair Station, higher item limits |
| 25 | — | ... | Phase 4 | Access to Phase 4; Alliance system |

### 9.2 Item Limits Per Level (Phase 1)

| Level | Max Razaworkers | Max Combat Units | Max Buildings |
|-------|-------------|-----------------|---------------|
| 1 | 1 | 3 | 2 |
| 2 | 2 | 5 | 3 |
| 3 | 2 | 6 | 4 |
| 4 | 3 | 8 | 5 |
| 5 | 3 | 10 | 6 |
| 6 | 4 | 12 | 7 |
| 7 | 4 | 14 | 8 |
| 8 | 5 | 16 | 9 |
| 9 | 5 | 18 | 10 |

---

## 10. Difficulty Curve Summary

```
Phase 1 (Level 1-9): SAFE — Learn basics, passive bots, guided quests, gradual unlocks
    Level 1-2: "I'm learning how to play"
    Level 3-4: "I can fight and defend"
    Level 5-6: "I have heavy units now"
    Level 7-9: "I've mastered the island"

Phase 2 (Level 10-17): EXPLORATORY — Find crystals, unlock new tech, fight defensive bots
    "I'm discovering what's possible"

Phase 3 (Level 18-24): INTENSE — Survive bot attacks, build defenses, manage under pressure
    "I need to fight to survive"

Phase 4 (Level 25+): POLITICAL — Choose alliances, guild warfare, player conflict
    "I'm competing with other players"
```

The emotional arc:
- **Phase 1**: Safety → Confidence
- **Phase 2**: Curiosity → Achievement (finding crystals, unlocking tech)
- **Phase 3**: Tension → Mastery (surviving waves, offensive counterattacks)
- **Phase 4**: Strategy → Dominance (alliances, territory, guild power)
