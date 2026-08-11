package com.btxtech.server.web;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Serves the landing page's tracking pixel and accepts its beacons.
 * <p>
 * Two methods on one URL because the page uses both: the <code>img</code> tag fetches it with a GET,
 * while the play and exit events are POSTs - the only thing <code>navigator.sendBeacon</code> sends,
 * and a beacon is the only way to report an event while the page is being navigated away from, when
 * an image request would simply be cancelled.
 * <p>
 * The GET has to be answered here rather than by a static resource handler. A controller mapping
 * claims its URL for every method: once <code>/t.gif</code> was mapped for POST, a GET of it no
 * longer fell through to the resource handler but was refused as a wrong method - one warning per
 * landing page view, and a broken image on the page.
 * <p>
 * The recording itself happens in {@link RequestInfoLoggingFilter}, the same place that records the
 * POST, so both paths stay in one piece of logic. The filter runs before any of this, which is why
 * the visits were still counted while the GET was being refused.
 */
@RestController
public class TrackingPixelController {
    private final byte[] pixel;

    public TrackingPixelController() throws IOException {
        pixel = new ClassPathResource("homepage/t.gif").getContentAsByteArray();
    }

    /**
     * Never cached: a cached pixel is a visit that is never reported again, and reporting the visit
     * is the only reason the image exists.
     */
    @GetMapping(value = "/t.gif", produces = MediaType.IMAGE_GIF_VALUE)
    public ResponseEntity<byte[]> pixel() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.IMAGE_GIF)
                .body(pixel);
    }

    @PostMapping("/t.gif")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void beacon() {
        // Body deliberately empty: everything of interest is in the query string and the headers.
    }
}
