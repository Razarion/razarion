package com.btxtech.server.service.tracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RedditConversionService {
    private static final String CONVERSION_URL = "https://ads-api.reddit.com/api/v3/pixels/{pixelId}/conversion_events";
    private static final String EVENT_PAGE_VISIT = "GamePageVisit";
    private static final String EVENT_CLIENT_STARTUP = "GameClientStartup";
    private static final String EVENT_BUILDER_DEPLOYED = "GameBuilderDeployed";
    private static final String EVENT_QUEST_PASSED_PREFIX = "GameQuestPassed";
    private static final String EVENT_LEVEL_UP_PREFIX = "GameLevelUp";
    private final Logger logger = LoggerFactory.getLogger(RedditConversionService.class);
    private final RestClient restClient;
    private final String pixelId;
    private final String accessToken;
    private final boolean enabled;
    private final Map<String, String> userIdToRdtCid = new ConcurrentHashMap<>();

    public RedditConversionService(
            @Value("${reddit.ads.pixel-id:}") String pixelId,
            @Value("${reddit.ads.access-token:}") String accessToken) {
        this.pixelId = pixelId;
        this.accessToken = accessToken;
        this.enabled = !pixelId.isEmpty() && !accessToken.isEmpty();
        this.restClient = RestClient.create();
        if (enabled) {
            logger.info("Reddit Conversions API enabled for pixel: {}", pixelId);
        } else {
            logger.info("Reddit Conversions API disabled (missing configuration)");
        }
    }

    public void registerUser(String userId, String rdtCid) {
        if (userId != null && rdtCid != null && !rdtCid.isEmpty()) {
            userIdToRdtCid.put(userId, rdtCid);
        }
    }

    public void unregisterUser(String userId) {
        if (userId != null) {
            userIdToRdtCid.remove(userId);
        }
    }

    @Async
    public void sendPageVisitEvent(String rdtCid) {
        sendEvent(EVENT_PAGE_VISIT, rdtCid);
    }

    @Async
    public void sendClientStartupEvent(String rdtCid) {
        sendEvent(EVENT_CLIENT_STARTUP, rdtCid);
    }

    @Async
    public void sendBuilderDeployedEvent(String userId) {
        sendEvent(EVENT_BUILDER_DEPLOYED, userIdToRdtCid.get(userId));
    }

    @Async
    public void sendQuestPassedEvent(String userId, int questId, int levelNumber) {
        String eventName = EVENT_QUEST_PASSED_PREFIX + "_level" + levelNumber + "_Quest" + questId;
        sendEvent(eventName, userIdToRdtCid.get(userId));
    }

    @Async
    public void sendLevelUpEvent(String userId, int newLevelNumber) {
        String eventName = EVENT_LEVEL_UP_PREFIX + "_level" + newLevelNumber;
        sendEvent(eventName, userIdToRdtCid.get(userId));
    }

    private void sendEvent(String customEventName, String rdtCid) {
        if (rdtCid == null || rdtCid.isEmpty()) {
            logger.debug("Reddit conversion event '{}' skipped (no rdtCid)", customEventName);
            return;
        }
        if (!enabled) {
            logger.info("Reddit conversion event '{}' [MOCK — not sent, missing config] rdtCid={}", customEventName, rdtCid);
            return;
        }
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("event_at", System.currentTimeMillis());
            event.put("action_source", "WEBSITE");
            event.put("type", Map.of("tracking_type", "CUSTOM", "custom_event_name", customEventName));
            event.put("click_id", rdtCid);

            Map<String, Object> body = Map.of("data", Map.of("events", List.of(event)));

            restClient.post()
                    .uri(CONVERSION_URL, pixelId)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            logger.info("Reddit conversion event '{}' sent successfully", customEventName);
        } catch (Exception e) {
            logger.warn("Failed to send Reddit conversion event '{}': {}", customEventName, e.getMessage());
        }
    }
}
