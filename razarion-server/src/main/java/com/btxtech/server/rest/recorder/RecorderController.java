package com.btxtech.server.rest.recorder;

import com.btxtech.server.service.engine.QuestConfigService;
import com.btxtech.server.service.engine.StaticGameConfigService;
import com.btxtech.server.service.history.RecorderFeedService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * What the game recorder (razarion-recorder) reads beside the game connection: the history feed,
 * and the names behind the ids it sees on the wire.
 * <p>
 * Read-only, and open to the recorder's own account ({@code razarion.recorder.email}, role
 * RECORDER) as well as to admins. The game's own cold context would do for the names, but it is
 * built for a player: the caller's level, quest and unlocks, and an alarm for an account without a
 * level.
 */
@RestController
@RequestMapping("/rest/recorder")
@PreAuthorize("hasAnyAuthority('ADMIN', 'RECORDER')")
public class RecorderController {
    private static final int MAX_EVENTS = 2000;
    private final RecorderFeedService recorderFeedService;
    private final StaticGameConfigService staticGameConfigService;
    private final QuestConfigService questConfigService;
    private final BaseItemService baseItemService;
    private final UserService userService;
    private final PlanetService planetService;

    public RecorderController(RecorderFeedService recorderFeedService,
                              StaticGameConfigService staticGameConfigService,
                              QuestConfigService questConfigService,
                              BaseItemService baseItemService,
                              UserService userService,
                              PlanetService planetService) {
        this.recorderFeedService = recorderFeedService;
        this.staticGameConfigService = staticGameConfigService;
        this.questConfigService = questConfigService;
        this.baseItemService = baseItemService;
        this.userService = userService;
        this.planetService = planetService;
    }

    /** Where a player with a base stands now: what the feed only tells as it changes. */
    public record PlayerState(String userId, int baseId, Integer levelId, Integer activeQuestId) {
    }

    public record Named(int id, String name) {
    }

    public record Level(int id, int number) {
    }

    /**
     * A quest where the game runs it: the level it belongs to and its place there. Most have no name;
     * the condition is what says what they ask for.
     */
    public record Quest(int id, String name, int levelNumber, int orderInLevel, String trigger, ConditionConfig condition) {
    }

    /** A unit or building: how big it is, and whether it moves at all (speed null: a building). */
    public record UnitType(int id, String name, double radius, Double speed) {
    }

    /** The planet the server runs; the minimap image is /rest/image/minimap/{id} and covers all of it. */
    public record Planet(int id, double width, double height) {
    }

    public record Config(Planet planet,
                         List<UnitType> baseItemTypes,
                         List<Named> resourceItemTypes,
                         List<Named> boxItemTypes,
                         List<Level> levels,
                         List<Quest> quests) {
    }

    /** History rows after {@code after} of {@code epoch}; see {@link RecorderFeedService#after}. */
    @GetMapping(value = "events", produces = MediaType.APPLICATION_JSON_VALUE)
    public RecorderFeedService.Page events(@RequestParam(value = "epoch", required = false) String epoch,
                                           @RequestParam(value = "after", defaultValue = "0") long after) {
        return recorderFeedService.after(epoch, after, MAX_EVENTS);
    }

    /**
     * Level and active quest of every player with a base. The recorder asks on start: a player who
     * was already playing then is otherwise on a quest it never saw begin.
     */
    @GetMapping(value = "players", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PlayerState> players() {
        return baseItemService.getPlayerBaseInfos().stream()
                .filter(info -> info.getUserId() != null && info.getBotId() == null)
                .map(info -> new PlayerState(info.getUserId(),
                        info.getBaseId(),
                        userService.getUserContextTransactional(info.getUserId()).getLevelId(),
                        userService.findActiveQuestId(info.getUserId())))
                .toList();
    }

    @GetMapping(value = "config", produces = MediaType.APPLICATION_JSON_VALUE)
    public Config config() {
        StaticGameConfig staticGameConfig = staticGameConfigService.loadStaticGameConfig();
        PlanetConfig planetConfig = planetService.getPlanetConfig();
        return new Config(planetConfig == null ? null : new Planet(planetConfig.getId(), planetConfig.getSize().getX(), planetConfig.getSize().getY()),
                staticGameConfig.getBaseItemTypes() == null ? List.of() : staticGameConfig.getBaseItemTypes().stream()
                        .map(type -> new UnitType(type.getId(), type.getName(),
                                type.getPhysicalAreaConfig() != null ? type.getPhysicalAreaConfig().getRadius() : 1,
                                type.getPhysicalAreaConfig() != null ? type.getPhysicalAreaConfig().getSpeed() : null))
                        .toList(),
                named(staticGameConfig.getResourceItemTypes()),
                named(staticGameConfig.getBoxItemTypes()),
                staticGameConfig.getLevelConfigs().stream()
                        .map(levelConfig -> new Level(levelConfig.getId(), levelConfig.getNumber()))
                        .toList(),
                quests());
    }

    /** In the order the game runs them, see QuestConfigService.readQuestBackendInfos. */
    private List<Quest> quests() {
        Map<Integer, QuestConfig> configs = questConfigService.read().stream()
                .collect(Collectors.toMap(QuestConfig::getId, Function.identity()));
        return questConfigService.readQuestBackendInfos().stream()
                .map(info -> {
                    QuestConfig config = configs.get(info.getId());
                    var condition = info.getConditionConfig();
                    return new Quest(info.getId(),
                            config != null ? config.getInternalName() : null,
                            info.getLevelNumber(),
                            info.getOrderInLevel(),
                            condition != null && condition.getConditionTrigger() != null ? condition.getConditionTrigger().name() : null,
                            condition);
                })
                .toList();
    }

    private static List<Named> named(List<? extends ItemType> itemTypes) {
        return itemTypes == null ? List.of() : itemTypes.stream()
                .map(itemType -> new Named(itemType.getId(), itemType.getName()))
                .toList();
    }
}
