package com.btxtech.server.web;

import com.btxtech.server.persistence.tracker.TrackerPersistence;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.utils.ExceptionUtil;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 28.01.2018.
 */
@WebFilter(filterName = "razarion-common-filter", urlPatterns = {"/*", "/"})
public class CommonFilter implements Filter {
    public enum AngularType {
        FRONTEND,
        BACKEND,
        NONE
    }

    private static final String FRONTEND_FILE = CommonUrl.FRONTEND_ANGULAR_HTML_FILE.toUpperCase();
    private static final String BACKEND = CommonUrl.ANGULAR_BACKEND_PATH.toUpperCase();
    @Inject
    private Logger logger;
    @Inject
    private TrackerPersistence trackerPersistence;
    private Collection<String> excludePaths = convertFilterStrings(
            CommonUrl.APPLICATION_PATH,
            CommonUrl.CLIENT_PATH,
            CommonUrl.CLIENT_WORKER_PATH,
            CommonUrl.SYSTEM_CONNECTION_WEB_SOCKET_ENDPOINT,
            CommonUrl.GAME_CONNECTION_WEB_SOCKET_ENDPOINT,
            "/debug", // Source maps
            "/test", // Arquillian test
            "/images",
            "/marketinghist",
            "/resources",
            "/faces",
            "/assets" // Frontend
    );
    private Collection<String> excludeTypes = convertFilterStrings(
            ".js",
            ".html",
            ".css",
            ".ico"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest servletRequest = (HttpServletRequest) request;
            // logger.warning(servletRequest.getRequestURI() + ": " + extractAngularType(servletRequest));
            switch (extractAngularType(servletRequest)) {
                case FRONTEND:
                    trackerPersistence.onPage("Frontend", servletRequest);
                    request.getRequestDispatcher(CommonUrl.FRONTEND_ANGULAR_HTML_FILE).forward(request, response);
                    break;
                case BACKEND:
                    request.getRequestDispatcher(CommonUrl.BACKEND_ANGULAR_HTML_FILE).forward(request, response);
                    break;
                case NONE:
                    chain.doFilter(request, response);
                    break;
                default:
                    logger.warning("CommonFilter can not handle: " + servletRequest.getServletPath());
                    chain.doFilter(request, response);
            }
        } catch (Throwable t) {
            Throwable mostInnerThrowable = ExceptionUtil.getMostInnerThrowable(t);
            if (mostInnerThrowable instanceof ClosedChannelException) {
                log("ClosedChannelException", request);
            } else {
                throw t;
            }
        }
    }

    @Override
    public void destroy() {

    }

    private AngularType extractAngularType(HttpServletRequest req) {
        String requestPath = req.getRequestURI().toUpperCase();
        if (requestPath.startsWith(FRONTEND_FILE)) {
            return AngularType.FRONTEND;
        }
        if (requestPath.startsWith(BACKEND)) {
            if (isKnownType(requestPath)) {
                return AngularType.NONE;
            } else {
                return AngularType.BACKEND;
            }
        }

        if (isKnownType(requestPath)) {
            return AngularType.NONE;
        }
        for (String path : excludePaths) {
            if (requestPath.startsWith(path)) {
                return AngularType.NONE;
            }
        }
        return AngularType.FRONTEND;
    }

    private boolean isKnownType(String requestPath) {
        for (String path : excludeTypes) {
            if (requestPath.endsWith(path)) {
                return true;
            }
        }
        return false;
    }

    private Collection<String> convertFilterStrings(String... strings) {
        return Arrays.stream(strings).map(String::toUpperCase).collect(Collectors.toList());
    }

    private void log(String description, ServletRequest request) {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        String queryString = servletRequest.getQueryString();
        if (queryString != null && !queryString.trim().isEmpty()) {
            queryString = "&X&" + queryString;
        } else {
            queryString = "";
        }
        logger.severe(description + " path: " + servletRequest.getRequestURI() + queryString + ". SessionId: " + servletRequest.getSession().getId());
    }
}
