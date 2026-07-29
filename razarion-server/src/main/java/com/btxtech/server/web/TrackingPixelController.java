package com.btxtech.server.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Accepts the landing page's beacons, which are POSTs because that is the only thing
 * <code>navigator.sendBeacon</code> sends - and a beacon is the only way to report an event while
 * the page is being navigated away from, when an image request would simply be cancelled.
 * <p>
 * The recording itself happens in {@link RequestInfoLoggingFilter}, the same place that records
 * the GET of the pixel, so both paths stay in one piece of logic. All this needs to do is keep
 * the static resource handler - which serves GET only - from answering the POST with a 405.
 */
@RestController
public class TrackingPixelController {
    @PostMapping("/t.gif")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void beacon() {
        // Body deliberately empty: everything of interest is in the query string and the headers.
    }
}
