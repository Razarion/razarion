package com.btxtech.server.service.tracking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sends server-side conversion events to the Meta (Facebook and Instagram) Conversions API.
 * <p>
 * Mirrors {@link XConversionService} and {@link RedditConversionService} - same funnel, same
 * fire-and-forget behaviour, same MOCK mode when the credentials are missing. What is different
 * about Meta:
 * <ul>
 *     <li>Auth is a system-user access token passed as a query parameter, the way the Graph API
 *     takes it. It is never written to a log line here - the url is not logged.</li>
 *     <li>The click id is not sent as itself. Meta matches on {@code fbc}, the value its browser
 *     pixel would have written into a cookie: {@code fb.1.<millis when the click was seen>.<fbclid>}.
 *     Built once, when the click id is first seen, and kept with the user - rebuilding it later
 *     would stamp a quest passed an hour after the click with the wrong click time.</li>
 *     <li>Event names are configurable per funnel step ({@code meta.ads.event.*}) rather than
 *     fixed. Meta optimises campaigns towards an event it knows; pointing "builder deployed" at a
 *     standard event such as {@code CompleteRegistration} has to be possible without a deploy.
 *     Empty means the custom name below, which shows up in Events Manager as a custom event.</li>
 * </ul>
 * The click id stays in memory only (userId -> fbc), the same GDPR-conservative decision taken for
 * Reddit's {@code rdtCid} and X's {@code twclid}.
 */
@Service
public class MetaConversionService {
    private static final String CONVERSION_URL = "https://graph.facebook.com/{version}/{pixelId}/events";
    private static final String EVENT_PAGE_VISIT = "GamePageVisit";
    private static final String EVENT_CLIENT_STARTUP = "GameClientStartup";
    private static final String EVENT_BUILDER_DEPLOYED = "GameBuilderDeployed";
    private static final String EVENT_QUEST_PASSED = "GameQuestPassed";
    private static final String EVENT_LEVEL_UP = "GameLevelUp";
    /**
     * The domain index of the cookie Meta's own pixel would have written. One is what a pixel on
     * razarion.com writes; it is part of the value and not a number we are free to choose.
     */
    private static final String FBC_DOMAIN_INDEX = "1";
    private static final ObjectMapper ACK_MAPPER = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(MetaConversionService.class);
    private final RestClient restClient;
    private final String apiVersion;
    private final String pixelId;
    private final String accessToken;
    private final String testEventCode;
    private final String siteUrl;
    private final Map<String, String> eventNames = new HashMap<>();
    private final boolean enabled;
    private final Map<String, Click> userIdToClick = new ConcurrentHashMap<>();

    /**
     * What is known about the click a player arrived on. Both halves have to survive from the ad
     * click to a quest passed an hour later: the browser is only named on the request that brought
     * them, and by then there is no request left to read it from.
     */
    private record Click(String fbc, String userAgent) {
    }

    public MetaConversionService(
            @Value("${meta.ads.api-version:v21.0}") String apiVersion,
            @Value("${meta.ads.pixel-id:}") String pixelId,
            @Value("${meta.ads.access-token:}") String accessToken,
            @Value("${meta.ads.test-event-code:}") String testEventCode,
            @Value("${meta.ads.site-url:https://www.razarion.com}") String siteUrl,
            @Value("${meta.ads.event.page-visit:}") String eventPageVisit,
            @Value("${meta.ads.event.client-startup:}") String eventClientStartup,
            @Value("${meta.ads.event.builder-deployed:}") String eventBuilderDeployed,
            @Value("${meta.ads.event.quest-passed:}") String eventQuestPassed,
            @Value("${meta.ads.event.level-up:}") String eventLevelUp) {
        this.apiVersion = apiVersion;
        this.pixelId = pixelId;
        this.accessToken = accessToken;
        this.testEventCode = testEventCode;
        // No trailing slash: the paths below add their own.
        this.siteUrl = siteUrl.endsWith("/") ? siteUrl.substring(0, siteUrl.length() - 1) : siteUrl;
        this.eventNames.put(EVENT_PAGE_VISIT, eventPageVisit);
        this.eventNames.put(EVENT_CLIENT_STARTUP, eventClientStartup);
        this.eventNames.put(EVENT_BUILDER_DEPLOYED, eventBuilderDeployed);
        this.eventNames.put(EVENT_QUEST_PASSED, eventQuestPassed);
        this.eventNames.put(EVENT_LEVEL_UP, eventLevelUp);
        this.enabled = !pixelId.isEmpty() && !accessToken.isEmpty();
        this.restClient = RestClient.create();
        if (enabled) {
            logger.info("Meta Conversions API enabled for pixel: {}{}", pixelId,
                    testEventCode.isEmpty() ? "" : " (test event code " + testEventCode + ")");
        } else {
            logger.info("Meta Conversions API disabled (missing configuration)");
        }
    }

    /**
     * Remembers the click of this user, as the value Meta matches on. The click time is now: this
     * is called when the click id is read off the session the visitor arrived in.
     */
    public void registerUser(String userId, String fbclid, String userAgent) {
        if (userId != null && fbclid != null && !fbclid.isEmpty()) {
            userIdToClick.put(userId, new Click(fbc(fbclid), userAgent));
        }
    }

    public void unregisterUser(String userId) {
        if (userId != null) {
            userIdToClick.remove(userId);
        }
    }

    @Async
    public void sendPageVisitEvent(String fbclid, String userAgent) {
        sendEventForClickId(EVENT_PAGE_VISIT, null, fbclid, userAgent);
    }

    @Async
    public void sendClientStartupEvent(String fbclid, String userAgent) {
        sendEventForClickId(EVENT_CLIENT_STARTUP, null, fbclid, userAgent);
    }

    @Async
    public void sendBuilderDeployedEvent(String userId) {
        sendEvent(EVENT_BUILDER_DEPLOYED, null, userIdToClick.get(userId));
    }

    @Async
    public void sendQuestPassedEvent(String userId, int questId, int levelNumber) {
        String description = "level" + levelNumber + "_Quest" + questId;
        sendEvent(EVENT_QUEST_PASSED, description, userIdToClick.get(userId));
    }

    @Async
    public void sendLevelUpEvent(String userId, int newLevelNumber) {
        String description = "level" + newLevelNumber;
        sendEvent(EVENT_LEVEL_UP, description, userIdToClick.get(userId));
    }

    /** The click id as it arrives in the url - seen right now, so the click time is now. */
    private void sendEventForClickId(String funnelStep, String description, String fbclid, String userAgent) {
        sendEvent(funnelStep, description,
                fbclid == null || fbclid.isEmpty() ? null : new Click(fbc(fbclid), userAgent));
    }

    /**
     * What Meta said about the event, rather than only that the request did not throw.
     * <p>
     * The body used to be discarded. A 200 from this endpoint means the request was well formed,
     * not that anything was counted: the Graph API answers with {@code events_received} and a
     * {@code messages} array, and it is in that array that it says things like a click id it
     * cannot match or a field it ignored. Two hundred and thirty-nine events logged as "sent
     * successfully" told us nothing at all when Meta reported receiving none, because nobody had
     * ever read the answer.
     * <p>
     * Quiet on the ordinary case - one event in, one event received, nothing to say - and loud
     * on anything else. There is no secret in the body; the access token travels in the query
     * string and the url is never logged.
     */
    private void logSent(String funnelStep, String eventName, String response) {
        String problem = ackProblem(response);
        if (problem == null) {
            logger.info("Meta conversion event '{}' (eventName={}) received by Meta", funnelStep, eventName);
            return;
        }
        logger.warn("Meta conversion event '{}' (eventName={}) was not counted as expected: {} - body={}",
                funnelStep, eventName, problem, abbreviate(response));
    }

    /**
     * What is wrong with Meta's answer, or null when nothing is.
     * <p>
     * A clean acceptance is one event in and one event received with nothing to say about it.
     * Anything else - a different count, a warning in {@code messages}, a body that is not the
     * shape this endpoint returns - is worth a line in the log, because none of it raises an
     * exception and all of it used to be invisible.
     */
    static String ackProblem(String response) {
        if (response == null || response.isBlank()) {
            return "empty answer";
        }
        JsonNode node;
        try {
            node = ACK_MAPPER.readTree(response);
        } catch (Exception e) {
            return "answer is not JSON";
        }
        if (!node.hasNonNull("events_received")) {
            return "no events_received in the answer";
        }
        int received = node.get("events_received").asInt();
        if (received != 1) {
            return "events_received=" + received + ", one was sent";
        }
        JsonNode messages = node.get("messages");
        if (messages != null && !messages.isEmpty()) {
            return "Meta had something to say: " + messages;
        }
        return null;
    }

    private static String abbreviate(String text) {
        if (text == null) {
            return "(empty)";
        }
        return text.length() <= 400 ? text : text.substring(0, 400) + "...";
    }

    private String fbc(String fbclid) {
        return fbc(fbclid, System.currentTimeMillis());
    }

    /**
     * Where the events happen. All of them are in the game now that the landing page is no
     * longer reported: the client starting up, a builder deployed, a quest, a level.
     */
    String sourceUrl() {
        return siteUrl + "/game";
    }

    /**
     * What Meta's own pixel would have put in the _fbc cookie, which is what it matches on. The
     * format is Meta's, not ours: get it wrong and the events are accepted and matched to nobody,
     * which looks exactly like a campaign that converts nothing.
     */
    static String fbc(String fbclid, long clickMillis) {
        return "fb." + FBC_DOMAIN_INDEX + "." + clickMillis + "." + fbclid;
    }

    private void sendEvent(String funnelStep, String description, Click click) {
        if (click == null || click.fbc() == null || click.fbc().isEmpty()) {
            logger.debug("Meta conversion event '{}' skipped (no fbclid)", funnelStep);
            return;
        }
        // Fall back to the funnel-step name when no event name is configured (e.g. in MOCK mode).
        String configuredEventName = eventNames.get(funnelStep);
        String eventName = configuredEventName == null || configuredEventName.isEmpty()
                ? funnelStep : configuredEventName;

        if (!enabled) {
            logger.info("Meta conversion event '{}' (eventName={}, desc={}) [MOCK — not sent, missing config] fbc={}",
                    funnelStep, eventName, description, click.fbc());
            return;
        }
        try {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("event_name", eventName);
            // Seconds, not millis - and Meta rejects anything older than seven days.
            event.put("event_time", System.currentTimeMillis() / 1000L);
            event.put("action_source", "website");
            /*
             * Where it happened. Meta asks for this on every website event and drops the ones
             * that arrive without it - which is a way to have every request answered 200 and
             * still be told that no events were processed.
             */
            event.put("event_source_url", sourceUrl());
            // Nothing of ours fires a browser pixel for the same conversion, so this only has to
            // be unique: it is what a retry would be deduplicated against.
            event.put("event_id", UUID.randomUUID().toString());
            // The browser belongs in here: Meta counts client_user_agent as required for a website
            // event and matches worse without it. The client's IP would be the other half and is
            // deliberately left out - we store no IP addresses anywhere, and that stands.
            Map<String, Object> userData = new LinkedHashMap<>();
            userData.put("fbc", click.fbc());
            if (click.userAgent() != null && !click.userAgent().isEmpty()) {
                userData.put("client_user_agent", click.userAgent());
            }
            event.put("user_data", userData);
            if (description != null) {
                event.put("custom_data", Map.of("description", description));
            }

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("data", List.of(event));
            if (!testEventCode.isEmpty()) {
                body.put("test_event_code", testEventCode);
            }

            String response = restClient.post()
                    .uri(UriComponentsBuilder.fromUriString(CONVERSION_URL)
                            .queryParam("access_token", accessToken)
                            .build(apiVersion, pixelId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            logSent(funnelStep, eventName, response);
        } catch (Exception e) {
            logger.warn("Failed to send Meta conversion event '{}': {}", funnelStep, e.getMessage());
        }
    }
}
