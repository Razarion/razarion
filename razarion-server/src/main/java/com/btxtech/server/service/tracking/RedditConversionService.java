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

@Service
public class RedditConversionService {
    private static final String CONVERSION_URL = "https://ads-api.reddit.com/api/v3/pixels/{pixelId}/conversion_events";
    private final Logger logger = LoggerFactory.getLogger(RedditConversionService.class);
    private final RestClient restClient;
    private final String pixelId;
    private final String accessToken;
    private final boolean enabled;

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

    @Async
    public void sendPageVisitEvent(String rdtCid) {
        if (!enabled) {
            return;
        }
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("event_at", System.currentTimeMillis());
            event.put("action_source", "web");
            event.put("type", Map.of("tracking_type", "CUSTOM", "custom_event_name", "GamePageVisit"));

            if (rdtCid != null && !rdtCid.isEmpty()) {
                event.put("click_id", rdtCid);
            }

            Map<String, Object> body = Map.of("data", Map.of("events", List.of(event)));

            restClient.post()
                    .uri(CONVERSION_URL, pixelId)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            logger.info("Reddit conversion event sent successfully");
        } catch (Exception e) {
            logger.warn("Failed to send Reddit conversion event: {}", e.getMessage());
        }
    }
}
