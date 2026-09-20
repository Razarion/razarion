package com.btxtech.server.rest.director;

import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.SyncService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * The half of director mode that CHANGES the world it films: it creates bases and spawns units
 * that attack. Split out of {@link DirectorController} so the two can be switched independently,
 * because they carry entirely different risk.
 * <p>
 * Filming the live world is watching: a camera flight, a recording, a query for the base list.
 * Staging a battle is playing — on a persistent shared planet, a strike force spawned for a clip
 * destroys buildings other people paid for, and no camera setting undoes that. Those two must not
 * hang off one switch, which is what {@code razarion.director.enabled} used to be.
 * <p>
 * POST /rest/director/create-base    → create the operator's green base at (x,y)
 * POST /rest/director/stage-attack   → spawn a green strike force that attacks a bot
 * POST /rest/director/clear-staging  → delete the operator's base again, once the take is in
 * <p>
 * Guarded twice, the same way {@link DirectorController} is:
 * 1. {@code razarion.director.staging.enabled} — default false everywhere except the local
 *    profile, so the bean does not exist and the endpoints answer 404.
 * 2. {@code @PreAuthorize} ADMIN, for wherever the property IS on.
 * <p>
 * Turning this on against the live planet is a deliberate act, and on a planet with players on it
 * the switch alone is not enough. Three rules hold wherever it is on, so that what a staged battle
 * can reach is a bot and empty ground rather than somebody's base:
 * <ul>
 * <li>the target must be a bot. Attacking a human base was possible by passing its id, and no
 *     amount of care in the studio makes that acceptable on a shared world.</li>
 * <li>both the spawn point and the target must be {@code razarion.director.staging.min-human-distance}
 *     away from every human base except the operator's own - far enough that the fight is not on
 *     anybody's doorstep and that no player watches units appear out of nothing. 0 switches the
 *     rule off, which is what a planet without players (the local profile) wants.</li>
 * <li>a strike force is capped at {@code razarion.director.staging.max-units}. A clip needs a
 *     handful of units; a typo asking for a thousand is not a clip.</li>
 * </ul>
 * What is staged is still meant to be cleared away afterwards - see {@code clear-staging}.
 */
@RestController
@RequestMapping("/rest/director")
@ConditionalOnProperty(name = "razarion.director.staging.enabled", havingValue = "true")
@PreAuthorize("hasAuthority('ADMIN')")
public class DirectorStagingController {
    private final BaseItemService baseItemService;
    private final CommandService commandService;
    private final ItemTypeService itemTypeService;
    private final UserService userService;
    private final SyncService syncService;
    /** How far a staged battle must stay from any other human base; 0 turns the rule off. */
    private final double minHumanDistance;
    /** The most units one stage-attack may spawn. */
    private final int maxUnits;
    /**
     * Serializes stage-attack engine mutations (mirrors PlanetMgmtController).
     */
    private final Object engineLock = new Object();

    public DirectorStagingController(BaseItemService baseItemService,
                                     CommandService commandService,
                                     ItemTypeService itemTypeService,
                                     UserService userService,
                                     SyncService syncService,
                                     @Value("${razarion.director.staging.min-human-distance:300}") double minHumanDistance,
                                     @Value("${razarion.director.staging.max-units:20}") int maxUnits) {
        this.baseItemService = baseItemService;
        this.commandService = commandService;
        this.itemTypeService = itemTypeService;
        this.userService = userService;
        this.syncService = syncService;
        this.minHumanDistance = minHumanDistance;
        this.maxUnits = maxUnits;
    }

    /**
     * Create (reset) the AUTHENTICATED operator's green (OWN/human) base with its
     * start building at (x, y). Use the same admin account in the studio and the
     * /game/director client so the base renders green in the client. Returns the
     * new base id.
     */
    @PostMapping("/create-base")
    public int createBase(@RequestBody CreateBaseRequest request) {
        synchronized (engineLock) {
            UserContext userContext = userService.getUserContextFromContext();
            return baseItemService.createHumanBaseWithBaseItem(
                    userContext.getLevelId(),
                    userContext.getUnlockedItemLimit(),
                    userContext.getUserId(),
                    "Director Base",
                    new DecimalPosition(request.getX(), request.getY())
            ).getBaseId();
        }
    }

    /**
     * Spawn a green (OWN/human) strike force at the requested position and order
     * it to attack the enemy bot — stages a filmed battle. Operates on the first
     * existing HUMAN base (the operator's green base) so it works regardless of
     * which app/identity triggers it; the bot is the first non-human base.
     * <p>
     * Units are spawned instantly-finished (noSpawn) AND explicitly synced to the
     * clients via {@link SyncService#notifySendSyncBaseItem} — spawnSyncBaseItem
     * only notifies clients on the animated (noSpawn=false) path, so without this
     * the spawned units would be invisible in the /game/director tab.
     */
    @PostMapping("/stage-attack")
    public StageAttackResult stageAttack(@RequestBody StageAttackRequest request) {
        synchronized (engineLock) {
            // The operator's own base, not the first human one: on a copy of the live planet that is
            // somebody else's, and the strike force would be red in the director client.
            PlayerBaseFull humanBase = baseItemService.getPlayerBaseFull4UserId(userService.getUserContextFromContext().getUserId());
            if (humanBase == null) {
                throw new IllegalStateException("You have no base — create your base first (Create base).");
            }
            PlayerBaseFull botBase;
            if (request.getTargetBaseId() != null) {
                botBase = (PlayerBaseFull) baseItemService.getPlayerBase4BaseId(request.getTargetBaseId());
                if (botBase == null) {
                    throw new IllegalStateException("Target base " + request.getTargetBaseId() + " not found.");
                }
                if (botBase.getCharacter() == null || !botBase.getCharacter().isBot()) {
                    throw new IllegalStateException("Base " + request.getTargetBaseId()
                            + " belongs to a player. A staged battle may only be aimed at a bot.");
                }
            } else {
                botBase = firstNonHumanBase();
                if (botBase == null) {
                    throw new IllegalStateException("No bot base found to attack.");
                }
            }
            List<SyncBaseItem> targets = new ArrayList<>(botBase.getItems());
            if (targets.isEmpty()) {
                throw new IllegalStateException("Bot base has no units to target.");
            }
            DecimalPosition spawnAt = new DecimalPosition(request.getX(), request.getY());
            assertClearOfPlayers(spawnAt, BaseGeometry.of(botBase), humanBase.getBaseId());

            BaseItemType attackerType = request.getBaseItemTypeId() != null
                    ? itemTypeService.getBaseItemType(request.getBaseItemTypeId())
                    : firstWeaponType();

            int count = request.getCount() != null ? Math.max(1, Math.min(maxUnits, request.getCount())) : 5;
            // Two diameters apart: closer than that a spawn overlaps its neighbour and is refused.
            double spacing = attackerType.getPhysicalAreaConfig().getRadius() * 4.0;
            List<SyncBaseItem> spawned = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                DecimalPosition pos = new DecimalPosition(
                        request.getX() + (i % 4) * spacing,
                        request.getY() + (i / 4) * spacing);
                try {
                    SyncBaseItem unit = baseItemService.spawnSyncBaseItem(attackerType, pos, 0.0, humanBase, true);
                    syncService.notifySendSyncBaseItem(unit); // make it visible on the clients
                    spawned.add(unit);
                } catch (Exception e) {
                    errors.add(e.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
            // Spread over the whole base rather than all on one item: a force that kills one building
            // and then stands still is a short clip.
            for (int i = 0; i < spawned.size(); i++) {
                SyncBaseItem unit = spawned.get(i);
                SyncBaseItem target = targets.get(i % targets.size());
                try {
                    // followTarget only if the unit can move (mirrors CommandService.attack(IdsDto,...)).
                    commandService.attack(unit, target, unit.getAbstractSyncPhysical().canMove());
                } catch (Exception e) {
                    errors.add("attack " + target.getId() + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
            return new StageAttackResult(spawned.size(), attackerType.getInternalName(),
                    botBase.getBaseId(), targets.get(0).getId(), errors);
        }
    }

    /**
     * Delete the operator's base again, with everything still standing in it.
     * <p>
     * The counterpart to create-base, and the reason staging can be switched on for an evening
     * rather than left on: what a shoot leaves behind is a green base full of units in a world
     * other people play in, and "delete it by hand afterwards" is a step that gets forgotten.
     *
     * @return the base id that was removed, or null when there was nothing to remove
     */
    @PostMapping("/clear-staging")
    public Integer clearStaging() {
        synchronized (engineLock) {
            PlayerBaseFull humanBase = baseItemService.getPlayerBaseFull4UserId(
                    userService.getUserContextFromContext().getUserId());
            if (humanBase == null) {
                return null;
            }
            int baseId = humanBase.getBaseId();
            baseItemService.deleteBase(baseId);
            return baseId;
        }
    }

    /**
     * Refuse a staged battle that would land near somebody who is playing.
     * <p>
     * Both ends are checked, because they are different distances: the strike force appears at the
     * spawn point out of nothing, and the fighting happens at the bot. A player close to either
     * sees a world that does not behave like the game they are playing.
     *
     * @param ownBaseId the operator's own base, which is the one base a staged battle may be near
     */
    private void assertClearOfPlayers(DecimalPosition spawnAt, BaseGeometry target, int ownBaseId) {
        if (minHumanDistance <= 0) {
            return;
        }
        DecimalPosition targetCentre = target.centre();
        for (PlayerBaseInfo info : baseItemService.getPlayerBaseInfos()) {
            if (info.getCharacter() != Character.HUMAN || info.getBaseId() == ownBaseId) {
                continue;
            }
            BaseGeometry other = BaseGeometry.of(baseItemService.getPlayerBase4BaseId(info.getBaseId()));
            double distance = Math.min(other.distanceFrom(spawnAt),
                    targetCentre != null ? other.distanceFrom(targetCentre) : Double.MAX_VALUE);
            if (distance < minHumanDistance) {
                throw new IllegalStateException(String.format(
                        "A player's base is %.0f away, and a staged battle must keep %.0f. "
                                + "Pick a bot further from the players.", distance, minHumanDistance));
            }
        }
    }

    private PlayerBaseFull firstNonHumanBase() {
        return baseItemService.getPlayerBaseInfos().stream()
                .filter(info -> info.getCharacter() != Character.HUMAN)
                .map(info -> (PlayerBaseFull) baseItemService.getPlayerBase4BaseId(info.getBaseId()))
                .findFirst().orElse(null);
    }

    private BaseItemType firstWeaponType() {
        return itemTypeService.getBaseItemTypes().stream()
                .filter(type -> type.getWeaponType() != null)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No combat (weapon) unit type available."));
    }
}
