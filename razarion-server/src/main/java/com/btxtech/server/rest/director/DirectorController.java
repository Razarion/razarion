package com.btxtech.server.rest.director;

import com.btxtech.server.service.director.DirectorService;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Director mode = filming the live local game world for social-media clips.
 * <p>
 * DEV-ONLY: the whole controller is gated behind {@code razarion.director.enabled}
 * (true only in application-local.properties). On prod the property is unset, so
 * the bean is never created and none of these endpoints exist — a deliberate
 * safety guard since these can drive/record the running world.
 * <p>
 * GET    /rest/director/plan          → plan summary list
 * GET    /rest/director/plan/{id}     → full plan payload
 * POST   /rest/director/plan          → create
 * POST   /rest/director/plan/{id}     → update
 * DELETE /rest/director/plan/{id}     → delete
 * POST   /rest/director/command       → studio publishes a transport command
 * GET    /rest/director/command       → director-mode client polls the latest
 * POST   /rest/director/create-base   → create the operator's green base at (x,y)
 * POST   /rest/director/stage-attack  → spawn a green strike force that attacks the bot
 */
@RestController
@RequestMapping("/rest/director")
// Defense-in-depth so this is never reachable on prod:
//  1. @ConditionalOnProperty — the bean is only created when razarion.director.enabled=true.
//     application.properties defaults it to false; only application-local.properties sets it
//     true. So on prod the controller (and all its endpoints) simply does not exist → 404.
//  2. @PreAuthorize ADMIN — even where the property IS enabled, only authenticated admins
//     pass. Both the studio and the rendering client send the JWT via AuthInterceptor.
@ConditionalOnProperty(name = "razarion.director.enabled", havingValue = "true")
@PreAuthorize("hasAuthority('ADMIN')")
public class DirectorController {
    private final DirectorService service;
    private final BaseItemService baseItemService;
    private final CommandService commandService;
    private final ItemTypeService itemTypeService;
    private final UserService userService;
    private final SyncService syncService;
    /**
     * Serializes stage-attack engine mutations (mirrors PlanetMgmtController).
     */
    private final Object engineLock = new Object();

    public DirectorController(DirectorService service,
                              BaseItemService baseItemService,
                              CommandService commandService,
                              ItemTypeService itemTypeService,
                              UserService userService,
                              SyncService syncService) {
        this.service = service;
        this.baseItemService = baseItemService;
        this.commandService = commandService;
        this.itemTypeService = itemTypeService;
        this.userService = userService;
        this.syncService = syncService;
    }

    @GetMapping("/plan")
    public List<DirectorPlanSummary> list() {
        return service.list();
    }

    @GetMapping("/plan/{id}")
    public DirectorPlanDto read(@PathVariable("id") int id) {
        return service.read(id);
    }

    @PostMapping("/plan")
    public DirectorPlanDto create(@RequestBody DirectorPlanDto dto) {
        return service.create(dto);
    }

    @PostMapping("/plan/{id}")
    public DirectorPlanDto update(@PathVariable("id") int id, @RequestBody DirectorPlanDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/plan/{id}")
    public void delete(@PathVariable("id") int id) {
        service.delete(id);
    }

    @PostMapping("/command")
    public DirectorCommand postCommand(@RequestBody DirectorCommand command) {
        return service.postCommand(command);
    }

    @GetMapping("/command")
    public DirectorCommand lastCommand() {
        return service.lastCommand();
    }

    @PostMapping("/camera")
    public void postCamera(@RequestBody DirectorCameraPose pose) {
        service.setCamera(pose);
    }

    @GetMapping("/camera")
    public DirectorCameraPose lastCamera() {
        return service.lastCamera();
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

    /** All current bases (human + bots) — the studio uses this to pick an attack target. */
    @GetMapping("/bases")
    public List<PlayerBaseInfo> bases() {
        return baseItemService.getPlayerBaseInfos();
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
