package com.btxtech.server.service.tracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sends server-side conversion events to the X (Twitter) Ads Conversion API (CAPI).
 * <p>
 * Mirrors {@link RedditConversionService} but for X. Notable differences to Reddit:
 * <ul>
 *     <li>Auth is a single {@code X-Pixel-Token} header (generated once in the X Ads Events
 *     Manager "Manual / Conversion API" setup), not a Bearer token.</li>
 *     <li>Each funnel step maps to an {@code event_id} tag that must be created in the X Ads
 *     Events Manager (format {@code tw-{pixelId}-{eventId}}, e.g. {@code tw-pthod-o8z21}).
 *     These are configured via {@code x.ads.event.*} properties; the dynamic quest/level detail
 *     is carried in the {@code description} field.</li>
 * </ul>
 * The X click id ({@code twclid}) is kept in-memory only (userId -> twclid), matching the
 * GDPR-conservative decision taken for Reddit's {@code rdtCid}.
 */
@Service
public class XConversionService {
    private static final String CONVERSION_URL = "https://ads-api.x.com/{version}/measurement/conversions/{pixelId}";
    private static final String EVENT_PAGE_VISIT = "GamePageVisit";
    private static final String EVENT_CLIENT_STARTUP = "GameClientStartup";
    private static final String EVENT_BUILDER_DEPLOYED = "GameBuilderDeployed";
    private static final String EVENT_QUEST_PASSED = "GameQuestPassed";
    private static final String EVENT_LEVEL_UP = "GameLevelUp";
    private final Logger logger = LoggerFactory.getLogger(XConversionService.class);
    private final RestClient restClient;
    private final String apiVersion;
    private final String pixelId;
    private final String pixelToken;
    private final Map<String, String> eventIds = new HashMap<>();
    private final boolean enabled;
    private final Map<String, String> userIdToTwclid = new ConcurrentHashMap<>();

    public XConversionService(
            @Value("${x.ads.api-version:12}") String apiVersion,
            @Value("${x.ads.pixel-id:}") String pixelId,
            @Value("${x.ads.pixel-token:}") String pixelToken,
            @Value("${x.ads.event.page-visit:}") String eventPageVisit,
            @Value("${x.ads.event.client-startup:}") String eventClientStartup,
            @Value("${x.ads.event.builder-deployed:}") String eventBuilderDeployed,
            @Value("${x.ads.event.quest-passed:}") String eventQuestPassed,
            @Value("${x.ads.event.level-up:}") String eventLevelUp) {
        this.apiVersion = apiVersion;
        this.pixelId = pixelId;
        this.pixelToken = pixelToken;
        this.eventIds.put(EVENT_PAGE_VISIT, eventPageVisit);
        this.eventIds.put(EVENT_CLIENT_STARTUP, eventClientStartup);
        this.eventIds.put(EVENT_BUILDER_DEPLOYED, eventBuilderDeployed);
        this.eventIds.put(EVENT_QUEST_PASSED, eventQuestPassed);
        this.eventIds.put(EVENT_LEVEL_UP, eventLevelUp);
        this.enabled = !pixelId.isEmpty() && !pixelToken.isEmpty();
        this.restClient = RestClient.create();
        if (enabled) {
            logger.info("X Conversions API enabled for pixel: {}", pixelId);
        } else {
            logger.info("X Conversions API disabled (missing configuration)");
        }
    }

    public void registerUser(String userId, String twclid) {
        if (userId != null && twclid != null && !twclid.isEmpty()) {
            userIdToTwclid.put(userId, twclid);
        }
    }

    public void unregisterUser(String userId) {
        if (userId != null) {
            userIdToTwclid.remove(userId);
        }
    }

    @Async
    public void sendPageVisitEvent(String twclid) {
        sendEvent(EVENT_PAGE_VISIT, null, twclid);
    }

    @Async
    public void sendClientStartupEvent(String twclid) {
        sendEvent(EVENT_CLIENT_STARTUP, null, twclid);
    }

    @Async
    public void sendBuilderDeployedEvent(String userId) {
        sendEvent(EVENT_BUILDER_DEPLOYED, null, userIdToTwclid.get(userId));
    }

    @Async
    public void sendQuestPassedEvent(String userId, int questId, int levelNumber) {
        String description = "level" + levelNumber + "_Quest" + questId;
        sendEvent(EVENT_QUEST_PASSED, description, userIdToTwclid.get(userId));
    }

    @Async
    public void sendLevelUpEvent(String userId, int newLevelNumber) {
        String description = "level" + newLevelNumber;
        sendEvent(EVENT_LEVEL_UP, description, userIdToTwclid.get(userId));
    }

    private void sendEvent(String funnelStep, String description, String twclid) {
        if (twclid == null || twclid.isEmpty()) {
            logger.debug("X conversion event '{}' skipped (no twclid)", funnelStep);
            return;
        }
        // Fall back to the funnel-step name when no X Ads event tag is configured (e.g. in MOCK mode).
        String configuredEventId = eventIds.get(funnelStep);
        String eventId = configuredEventId == null || configuredEventId.isEmpty() ? funnelStep : configuredEventId;

        if (!enabled) {
            logger.info("X conversion event '{}' (eventId={}, desc={}) [MOCK — not sent, missing config] twclid={}",
                    funnelStep, eventId, description, twclid);
            return;
        }
        try {
            Map<String, Object> conversion = new HashMap<>();
            conversion.put("conversion_time", Instant.now().toString());
            conversion.put("event_id", eventId);
            conversion.put("identifiers", List.of(Map.of("twclid", twclid)));
            conversion.put("conversion_id", UUID.randomUUID().toString());
            if (description != null) {
                conversion.put("description", description);
            }

            Map<String, Object> body = Map.of("conversions", List.of(conversion));

            restClient.post()
                    .uri(CONVERSION_URL, apiVersion, pixelId)
                    .header("X-Pixel-Token", pixelToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            logger.info("X conversion event '{}' (eventId={}) sent successfully", funnelStep, eventId);
        } catch (Exception e) {
            logger.warn("Failed to send X conversion event '{}': {}", funnelStep, e.getMessage());
        }
    }
}
