package com.btxtech.server.rest.director;

import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.SyncService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
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
 * POST /rest/director/create-base   → create the operator's green base at (x,y)
 * POST /rest/director/stage-attack  → spawn a green strike force that attacks a bot
 * <p>
 * Guarded twice, the same way {@link DirectorController} is:
 * 1. {@code razarion.director.staging.enabled} — default false everywhere except the local
 *    profile, so the bean does not exist and the endpoints answer 404.
 * 2. {@code @PreAuthorize} ADMIN, for wherever the property IS on.
 * <p>
 * Turning this on against the live planet is a deliberate act. There is no reason to.
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
    /**
     * Serializes stage-attack engine mutations (mirrors PlanetMgmtController).
     */
    private final Object engineLock = new Object();

    public DirectorStagingController(BaseItemService baseItemService,
                                     CommandService commandService,
                                     ItemTypeService itemTypeService,
                                     UserService userService,
                                     SyncService syncService) {
        this.baseItemService = baseItemService;
        this.commandService = commandService;
        this.itemTypeService = itemTypeService;
        this.userService = userService;
        this.syncService = syncService;
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
            PlayerBaseFull humanBase = firstBaseOfCharacter(Character.HUMAN);
            if (humanBase == null) {
                throw new IllegalStateException("No human (green) base found — create your base first (Create base).");
            }
            PlayerBaseFull botBase;
            if (request.getTargetBaseId() != null) {
                botBase = (PlayerBaseFull) baseItemService.getPlayerBase4BaseId(request.getTargetBaseId());
                if (botBase == null) {
                    throw new IllegalStateException("Target base " + request.getTargetBaseId() + " not found.");
                }
            } else {
                botBase = firstNonHumanBase();
                if (botBase == null) {
                    throw new IllegalStateException("No bot base found to attack.");
                }
            }
            SyncBaseItem target = botBase.getItems().stream().findFirst()
                    .orElseThrow(() -> new IllegalStateException("Bot base has no units to target."));

            BaseItemType attackerType = request.getBaseItemTypeId() != null
                    ? itemTypeService.getBaseItemType(request.getBaseItemTypeId())
                    : firstWeaponType();

            int count = request.getCount() != null ? Math.max(1, request.getCount()) : 5;
            List<SyncBaseItem> spawned = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                // Small grid spread so units don't stack on one point.
                DecimalPosition pos = new DecimalPosition(
                        request.getX() + (i % 3) * 3.0,
                        request.getY() + (i / 3) * 3.0);
                try {
                    SyncBaseItem unit = baseItemService.spawnSyncBaseItem(attackerType, pos, 0.0, humanBase, true);
                    syncService.notifySendSyncBaseItem(unit); // make it visible on the clients
                    spawned.add(unit);
                } catch (Exception e) {
                    errors.add(e.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
            for (SyncBaseItem unit : spawned) {
                // followTarget only if the unit can move (mirrors CommandService.attack(IdsDto,...)).
                commandService.attack(unit, target, unit.getAbstractSyncPhysical().canMove());
            }
            return new StageAttackResult(spawned.size(), attackerType.getInternalName(),
                    botBase.getBaseId(), target.getId(), errors);
        }
    }

    private PlayerBaseFull firstBaseOfCharacter(Character character) {
        return baseItemService.getPlayerBaseInfos().stream()
                .filter(info -> info.getCharacter() == character)
                .map(info -> (PlayerBaseFull) baseItemService.getPlayerBase4BaseId(info.getBaseId()))
                .findFirst().orElse(null);
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
