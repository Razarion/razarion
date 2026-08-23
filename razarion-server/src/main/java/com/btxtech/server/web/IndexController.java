package com.btxtech.server.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    @GetMapping("/")
    public String index(HttpServletRequest request, HttpServletResponse response, Model model) {
        // This page is not the same for two visitors: the query string below is baked into the
        // tracking pixel and the "Play Now" link, so a stored copy would fire one visitor's pixel
        // with another one's campaign parameters - or with none at all. It went out with no
        // Cache-Control header of any kind, which leaves every cache in the path free to guess,
        // and the CDN in front of production did: it kept a copy and fetched the pixel itself.
        response.setHeader(HttpHeaders.CACHE_CONTROL, CacheControl.noStore().getHeaderValue());
        // Forward the complete raw query string (already URL-encoded) so that any campaign
        // parameter (utm_*, rdt_cid, twclid, ...) is preserved and passed on to the tracking
        // pixel (/t.gif) and the "Play Now" link (/game). RequestInfoLoggingFilter then logs them.
        String queryString = request.getQueryString();
        model.addAttribute("qs", sanitizeQueryString(queryString));
        return "index";
    }

    // qs is reflected into an HTML attribute and a JS string literal in index.ftl. A correctly
    // percent-encoded query string never contains these characters unencoded, so stripping them
    // does not corrupt legitimate values but prevents reflected-XSS breakout from crafted URLs.
    private static String sanitizeQueryString(String queryString) {
        if (queryString == null || queryString.isEmpty()) {
            return "";
        }
        String cleaned = queryString.replaceAll("[\"'<>`\\s\\\\]", "");
        return cleaned.isEmpty() ? "" : "?" + cleaned;
    }
}
