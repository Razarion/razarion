package com.btxtech.server.rest;

import com.btxtech.server.service.CheerService;
import com.btxtech.server.service.history.CommunityTickerService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.dto.CommunityTickerEntry;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The community ticker over the game field: what other players did lately. The asking player's own
 * events are left out - they know what they did.
 */
@RestController
@RequestMapping("/rest/community-ticker")
public class CommunityTickerController {
    private static final int MAX_ENTRIES = 20;
    private final CommunityTickerService communityTickerService;
    private final UserService userService;

    public CommunityTickerController(CommunityTickerService communityTickerService, UserService userService) {
        this.communityTickerService = communityTickerService;
        this.userService = userService;
    }

    @GetMapping(value = "recent", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CommunityTickerEntry> recent(@RequestParam("after") long after) {
        String ownUserId = userService.getUserIdFromContextOrNull();
        List<CommunityTickerService.Event> events = communityTickerService.recent(after, MAX_ENTRIES).stream()
                .filter(event -> !Objects.equals(event.userId(), ownUserId))
                .toList();
        Set<String> userIds = events.stream()
                .map(CommunityTickerService.Event::userId)
                .collect(Collectors.toSet());
        Map<String, String> names = userService.getUserNames(userIds);
        // A line can be greeted while the player it is about has the game open (CheerService); a
        // player off the island greeting a newcomer is told that it may earn him a bonus.
        Set<String> online = userIds.stream()
                .filter(userService::hasOpenSystemConnection)
                .collect(Collectors.toSet());
        Map<String, Integer> levels = new HashMap<>();
        if (ownUserId != null && !online.isEmpty()) {
            Set<String> asked = new HashSet<>(online);
            asked.add(ownUserId);
            levels.putAll(userService.getUserLevelNumbers(asked));
        }
        Integer ownLevel = levels.get(ownUserId);
        return events.stream()
                .map(event -> new CommunityTickerEntry()
                        .setSeq(event.seq())
                        .setTimeMillis(event.timeMillis())
                        .setName(names.getOrDefault(event.userId(), CallSign.of(event.userId())))
                        .setKind(event.kind())
                        .setLevel(event.level())
                        .setWhat(event.what())
                        .setCheer(online.contains(event.userId()) ? CheerService.cheerFor(event.kind()) : null)
                        .setCheerBonus(online.contains(event.userId()) && CheerService.cheerFor(event.kind()) != null
                                && CheerService.isHelperAndNewcomer(ownLevel, levels.get(event.userId())) ? CheerService.HELPER_BONUS : null))
                .toList();
    }
}
