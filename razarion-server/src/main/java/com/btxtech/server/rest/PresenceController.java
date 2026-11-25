package com.btxtech.server.rest;

import com.btxtech.server.gameengine.ClientSystemConnectionService;
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

    public record Presence(int count, List<OnlinePlayer> players) {
    }

    public PresenceController(ClientSystemConnectionService clientSystemConnectionService, UserService userService, BaseItemService baseItemService) {
        this.clientSystemConnectionService = clientSystemConnectionService;
        this.userService = userService;
        this.baseItemService = baseItemService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Presence presence() {
        String ownUserId = userService.getUserIdFromContextOrNull();
        Set<String> others = clientSystemConnectionService.getOpenConnections().keySet().stream()
                .filter(userId -> !Objects.equals(userId, ownUserId))
                .collect(Collectors.toSet());
        return build(others, userService.getUserNames(others), baseItemService::getPlayerBase4UserId);
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
        return new Presence(userIds.size(), players);
    }
}
