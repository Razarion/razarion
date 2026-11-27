package com.btxtech.server.service;

import com.btxtech.server.user.UserService;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

/**
 * Tells the operator on his phone (ntfy) that a player wrote into the chat, so a "hi" gets an
 * answer from a person while the player is still there. Measured 2026-11-25..27: 29 players opened
 * the chat with something unread in it, 2 wrote, and the earlier "hi"s had stayed unanswered.
 * <p>
 * Only the operator is told; the admin's and the recorder's own lines are not reported, nor those of
 * the names in razarion.ntfy.ignore-names - the operator's own player (WarMind) is neither. The push
 * says who and how far along, never the message itself - it goes over ntfy.sh.
 */
@Service
public class OperatorAlertService {
    static final String CLICK_URL = "https://www.razarion.com/game";
    private final Logger logger = LoggerFactory.getLogger(OperatorAlertService.class);
    private final NtfyService ntfyService;
    private final UserService userService;
    private final BaseItemService baseItemService;
    private final Set<String> ignoredNames;

    public OperatorAlertService(NtfyService ntfyService, UserService userService, BaseItemService baseItemService,
                                @Value("${razarion.ntfy.ignore-names:}") String ignoredNames) {
        this.ntfyService = ntfyService;
        this.ignoredNames = Arrays.stream(ignoredNames.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        this.userService = userService;
        this.baseItemService = baseItemService;
    }

    /** Called after a player's chat line went out. Returns at once; the lookups run off the request. */
    public void onChatMessage(String userId, String name) {
        if (!ntfyService.isEnabled() || userId == null || (name != null && ignoredNames.contains(name.toLowerCase(Locale.ROOT)))) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                if (userService.isStaff(userId)) {
                    return;
                }
                Integer level = userService.getUserLevelNumbers(Set.of(userId)).get(userId);
                PlayerBase base = baseItemService.getPlayerBase4UserId(userId);
                ntfyService.notify(userId, "Razarion chat", chatText(name, level, base != null ? base.getCreatedMillis() : null, System.currentTimeMillis()), CLICK_URL);
            } catch (Exception e) {
                logger.warn("Operator alert for a chat line failed: {}", e.getMessage());
            }
        });
    }

    static String chatText(String name, Integer level, Long baseCreatedMillis, long now) {
        StringBuilder text = new StringBuilder(name).append(" wrote in the chat");
        StringBuilder about = new StringBuilder();
        if (level != null) {
            about.append("level ").append(level);
        }
        if (baseCreatedMillis != null && baseCreatedMillis > 0) {
            if (!about.isEmpty()) {
                about.append(", ");
            }
            long minutes = Math.max(0, (now - baseCreatedMillis) / 60_000);
            about.append(minutes < 120 ? "base " + minutes + " min old" : "base " + (minutes / 60) + " h old");
        } else {
            if (!about.isEmpty()) {
                about.append(", ");
            }
            about.append("no base");
        }
        return text.append(" (").append(about).append(")").toString();
    }
}
