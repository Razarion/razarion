package com.btxtech.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A push to the operator's phone through ntfy (https://ntfy.sh): the ntfy app subscribes to a
 * secret topic and shows whatever is posted to it. Only the operator gets these - no player is
 * ever notified of anything (no mails or pushes without opt-in, 2026-11-25).
 * <p>
 * Without a topic (RAZARION_NTFY_TOPIC unset, the default and local) nothing is sent. The text
 * goes over a third-party service, so it carries a call sign, a level and an age - never what a
 * player wrote.
 */
@Service
public class NtfyService {
    static final String SERVER = "https://ntfy.sh/";
    /** One push per player in this time, however much he writes: the point is "go and answer". */
    static final long PER_KEY_MILLIS = 10 * 60 * 1000L;
    private final Logger logger = LoggerFactory.getLogger(NtfyService.class);
    private final String topic;
    private final HttpClient httpClient;
    private final Map<String, Long> lastSent = new ConcurrentHashMap<>();

    public NtfyService(@Value("${razarion.ntfy.topic:}") String topic) {
        this.topic = topic != null ? topic.trim() : "";
        this.httpClient = this.topic.isEmpty() ? null : HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    public boolean isEnabled() {
        return !topic.isEmpty();
    }

    /**
     * Sends {@code message} unless the same {@code key} (a userId) was pushed within
     * {@link #PER_KEY_MILLIS}. Never blocks and never throws: a push that fails is a log line.
     */
    public void notify(String key, String title, String message, String clickUrl) {
        if (!isEnabled() || !take(key, System.currentTimeMillis())) {
            return;
        }
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(SERVER + topic))
                    .timeout(Duration.ofSeconds(10))
                    .header("Title", ascii(title))
                    .header("Tags", "speech_balloon")
                    // At the default priority Android showed the push silently: in the list, but no
                    // icon in the status bar and nothing on the lock screen (tested 2026-11-27).
                    .header("Priority", "high")
                    .POST(HttpRequest.BodyPublishers.ofString(message, StandardCharsets.UTF_8));
            if (clickUrl != null) {
                request.header("Click", clickUrl);
            }
            httpClient.sendAsync(request.build(), HttpResponse.BodyHandlers.discarding())
                    .whenComplete((response, throwable) -> {
                        if (throwable != null) {
                            logger.warn("ntfy push failed: {}", throwable.getMessage());
                        } else if (response.statusCode() >= 300) {
                            logger.warn("ntfy push answered {}", response.statusCode());
                        }
                    });
        } catch (Exception e) {
            logger.warn("ntfy push failed: {}", e.getMessage());
        }
    }

    boolean take(String key, long now) {
        lastSent.values().removeIf(at -> now - at > PER_KEY_MILLIS);
        Long previous = lastSent.putIfAbsent(key, now);
        return previous == null;
    }

    /** HTTP headers are ASCII; a call sign is, a chosen name may not be. */
    static String ascii(String text) {
        return text.replaceAll("[^\\x20-\\x7E]", "?");
    }
}
