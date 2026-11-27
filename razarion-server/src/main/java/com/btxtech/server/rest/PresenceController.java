package com.btxtech.server.rest;

import com.btxtech.server.gameengine.ClientSystemConnectionService;
import com.btxtech.server.service.CheerService;
import com.btxtech.server.service.history.CommunityTickerService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Who else is playing right now: the players with the game open, by the name the others see over
 * their units, and their base so the mini map can point them out. The asking player is left out.
 * <p>
 * Measured 2026-11-18..24: in the first ten minutes of 91 % of new players at least one other
 * person had the game open, for 21 % one built within 50 m - and the chat showed the "hi" of those
 * who looked for them unanswered. The others were there and could not be seen; this shows them.
 * <p>
 * Online is an open system connection: the game page, not the game connection alone - the recorder
 * holds one of those around the clock and is nobody.
 */
@RestController
@RequestMapping("/rest/presence")
public class PresenceController {
    static final int MAX_NAMES = 20;
    private final ClientSystemConnectionService clientSystemConnectionService;
    private final UserService userService;
    private final BaseItemService baseItemService;

    public record OnlinePlayer(String name, Integer baseId) {
    }

    /**
     * The island goal a newcomer is shown: how many players crossed from the noob island to the
     * mainland today, of {@link #ISLAND_GOAL}. Null for a player who has left the island himself.
     */
    public record IslandGoal(int crossedToday, int goal) {
    }

    /**
     * {@code cheers}: greetings other players sent this one since the last poll (CheerService),
     * handed out once. {@code me}: the asking player's own name, the one the others see - shown
     * nowhere else, so "Gecko-49 waves at you" left the player guessing who "you" is to the others.
     */
    public record Presence(int count, List<OnlinePlayer> players, List<CheerService.Delivered> cheers, IslandGoal island, String me) {
        Presence with(List<CheerService.Delivered> cheers, IslandGoal island, String me) {
            return new Presence(count, players, cheers, island, me);
        }
    }

    /** About what the best days of 11/2026 reached (41 on 08.11., 18-26 a day after) - in reach, not given. */
    static final int ISLAND_GOAL = 25;
    private final CheerService cheerService;
    private final CommunityTickerService communityTickerService;

    public PresenceController(ClientSystemConnectionService clientSystemConnectionService, UserService userService, BaseItemService baseItemService,
                              CheerService cheerService, CommunityTickerService communityTickerService) {
        this.clientSystemConnectionService = clientSystemConnectionService;
        this.userService = userService;
        this.baseItemService = baseItemService;
        this.cheerService = cheerService;
        this.communityTickerService = communityTickerService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Presence presence() {
        String ownUserId = userService.getUserIdFromContextOrNull();
        Set<String> others = clientSystemConnectionService.getOpenConnections().keySet().stream()
                .filter(userId -> !Objects.equals(userId, ownUserId))
                .collect(Collectors.toSet());
        Presence presence = build(others, userService.getUserNames(others), baseItemService::getPlayerBase4UserId);
        IslandGoal island = null;
        String me = null;
        if (ownUserId != null) {
            me = userService.getUserNames(Set.of(ownUserId)).getOrDefault(ownUserId, CallSign.of(ownUserId));
            Integer level = userService.getUserLevelNumbers(Set.of(ownUserId)).get(ownUserId);
            if (level != null && level <= CheerService.NEWCOMER_MAX_LEVEL) {
                island = new IslandGoal(communityTickerService.crossedToday(), ISLAND_GOAL);
            }
        }
        return presence.with(cheerService.take(ownUserId), island, me);
    }

    static Presence build(Set<String> userIds, Map<String, String> names, java.util.function.Function<String, PlayerBase> base4UserId) {
        List<OnlinePlayer> players = userIds.stream()
                .map(userId -> {
                    PlayerBase base = base4UserId.apply(userId);
                    return new OnlinePlayer(names.getOrDefault(userId, CallSign.of(userId)), base != null ? base.getBaseId() : null);
                })
                // Players with a base first - they are the ones to be found on the map.
                .sorted(Comparator.comparing((OnlinePlayer p) -> p.baseId() == null).thenComparing(OnlinePlayer::name))
                .limit(MAX_NAMES)
                .toList();
        return new Presence(userIds.size(), players, List.of(), null, null);
    }
}
