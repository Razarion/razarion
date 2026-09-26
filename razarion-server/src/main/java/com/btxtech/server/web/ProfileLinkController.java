package com.btxtech.server.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * The short links in the account's profiles: razarion.com/x, /ig, /fb, /yt.
 * <p>
 * A profile shows its link, and the tagged one - {@code ?utm_source=social-x&utm_medium=social...}
 * - is cut off at "razarion.com/?utm_source=so..." and reads like spam. So the profile carries the
 * short path, and this sends the visitor on to the landing page with the tags the tracking
 * expects (TrackingPlatforms.isOwnPost, razarion-social/pipeline/lib/links.mjs).
 * <p>
 * Whatever query the request arrived with is passed on: Meta's in-app browser appends an fbclid
 * to the bio link, and the visitor's records are joined by it. A 302 keeps the original referrer
 * on the next request, so the landing page still sees where the visitor came from.
 */
@RestController
public class ProfileLinkController {
    private static final Map<String, String> NETWORKS = Map.of(
            "x", "social-x",
            "ig", "social-ig",
            "fb", "social-fb",
            "yt", "social-yt");

    @GetMapping({"/x", "/ig", "/fb", "/yt"})
    public ResponseEntity<Void> profileLink(HttpServletRequest request) {
        String network = request.getRequestURI().substring(1);
        return redirect(NETWORKS.get(network), "bio", request.getQueryString());
    }

    /** razarion.com/x/{campaign}: a link inside a post, when one is short enough to matter. */
    @GetMapping({"/x/{campaign}", "/ig/{campaign}", "/fb/{campaign}", "/yt/{campaign}"})
    public ResponseEntity<Void> postLink(HttpServletRequest request, @PathVariable("campaign") String campaign) {
        String network = request.getRequestURI().substring(1, request.getRequestURI().indexOf('/', 1));
        return redirect(NETWORKS.get(network), campaign, request.getQueryString());
    }

    private static ResponseEntity<Void> redirect(String source, String campaign, String incomingQuery) {
        StringBuilder target = new StringBuilder("/?utm_source=").append(source)
                .append("&utm_medium=social&utm_campaign=").append(safe(campaign));
        String passed = incomingQuery != null ? incomingQuery.replaceAll("[\"'<>`\\s\\\\]", "") : "";
        if (!passed.isEmpty()) {
            target.append('&').append(passed);
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, target.toString())
                // One visitor's fbclid must never be handed to the next one by a cache in the path.
                .header(HttpHeaders.CACHE_CONTROL, CacheControl.noStore().getHeaderValue())
                .build();
    }

    /** A post id or "bio" - nothing else belongs in the campaign, so nothing else gets through. */
    private static String safe(String campaign) {
        String cleaned = campaign.replaceAll("[^A-Za-z0-9_-]", "");
        return cleaned.isEmpty() ? "untagged" : cleaned.substring(0, Math.min(cleaned.length(), 64));
    }
}
