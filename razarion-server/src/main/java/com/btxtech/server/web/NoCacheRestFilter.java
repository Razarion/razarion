package com.btxtech.server.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Tells GCP Cloud CDN (and any other proxy/cache in front of this pod) to
 * never cache REST responses. Without this header, a misconfigured CDN can
 * return a stale 200 for POST requests without ever reaching origin —
 * particularly visible in the studio's thumbnail save flow.
 *
 * Headers are added before the controller runs so that even Spring's
 * exception paths inherit them.
 * <p>
 * Two exceptions: reading the model file, and reading the terrain height map. Together they are
 * fifteen megabytes that every game start paid for in full, because no-store forbids a browser
 * from keeping them at all. The guarantee behind the header - that edited content reaches the
 * player - is real, and both controllers keep it by other means: no-cache plus an entity tag, so
 * the file may be held but must be revalidated before every use. Excluded here rather than
 * overridden there, because this filter also sets Pragma and Expires, which a controller's
 * Cache-Control does not clear.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class NoCacheRestFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String uri = req.getRequestURI();
        if ((uri.startsWith("/rest/") || uri.startsWith("/editor/")) && !isConditionalModelRead(req, uri)) {
            res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            res.setHeader("Pragma", "no-cache");
            res.setHeader("Expires", "0");
        }
        chain.doFilter(req, res);
    }

    /**
     * Reading the model file or the terrain height map, and only reading. Both are megabytes, both
     * are asked for on every game start, and both declare their own entity tag. The uploads beside
     * them stay under the blanket rule: a write is exactly the case the filter was added for.
     */
    private boolean isConditionalModelRead(HttpServletRequest req, String uri) {
        return "GET".equals(req.getMethod())
                && (uri.startsWith("/rest/gltf/glb/") || uri.startsWith("/rest/terrainHeightMap/"));
    }
}
