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
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class NoCacheRestFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String uri = req.getRequestURI();
        if (uri.startsWith("/rest/") || uri.startsWith("/editor/")) {
            res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            res.setHeader("Pragma", "no-cache");
            res.setHeader("Expires", "0");
        }
        chain.doFilter(req, res);
    }
}
