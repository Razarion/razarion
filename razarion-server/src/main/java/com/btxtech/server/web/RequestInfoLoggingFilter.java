package com.btxtech.server.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RequestInfoLoggingFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(RequestInfoLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            String referer = httpRequest.getHeader("Referer");
            String host = httpRequest.getHeader("Host");
            String userAgent = httpRequest.getHeader("User-Agent");
            String method = httpRequest.getMethod();
            String requestURI = httpRequest.getRequestURI();
            String queryString = httpRequest.getQueryString();
            String fullPath = requestURI + (queryString != null ? "?" + queryString : "");

            String refererPart = "";
            if (referer != null && (host == null || !referer.contains(host))) {
                refererPart = String.format(" referer=\"%s\"", referer);
            }

            boolean isGameExcluded = requestURI.startsWith("/game/")
                    && !requestURI.equals("/game/")
                    && !requestURI.equals("/game/index.html");

            boolean shouldLog = !isGameExcluded && !requestURI.startsWith("/rest/");

            if (shouldLog) {
                logger.info("{} \"{}\"{} user_agent=\"{}\"",
                        method,
                        fullPath,
                        refererPart,
                        userAgent != null ? userAgent : "/rest");
            }
        }
        chain.doFilter(request, response);
    }
}
