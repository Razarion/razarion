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
            String userAgent = httpRequest.getHeader("User-Agent");
            String method = httpRequest.getMethod();
            String requestURL = httpRequest.getRequestURL().toString();
            String queryString = httpRequest.getQueryString();
            String fullURL = requestURL + (queryString != null ? "?" + queryString : "");

            logger.info("{} {} referer=\"{}\" user_agent=\"{}\"", method, fullURL, referer != null ? referer : "", userAgent != null ? userAgent : "");
        }
        chain.doFilter(request, response);
    }
}
