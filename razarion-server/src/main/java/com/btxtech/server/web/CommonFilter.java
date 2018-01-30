package com.btxtech.server.web;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.system.ExceptionHandler;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 28.01.2018.
 */
@WebFilter(filterName = "razarion-common-filter", urlPatterns = {"/*"})
public class CommonFilter implements Filter {
    public enum AngularType {
        FRONTEND,
        BACKEND,
        NONE
    }

    private static final String BACKEND = CommonUrl.ANGULAR_BACKEND_PATH.toUpperCase();
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private Logger logger;
    private Collection<String> excludePaths = convertFilterStrings(
            CommonUrl.APPLICATION_PATH,
            CommonUrl.CLIENT_PATH,
            CommonUrl.CLIENT_WORKER_PATH,
            CommonUrl.SYSTEM_CONNECTION_WEB_SOCKET_ENDPOINT,
            CommonUrl.GAME_CONNECTION_WEB_SOCKET_ENDPOINT,
            "/debug",
            "/images",
            "/marketinghist",
            "/resources",
            "/faces"
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
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
            throw throwable;
        }
    }

    @Override
    public void destroy() {

    }

    private AngularType extractAngularType(HttpServletRequest req) {
        String requestPath = req.getServletPath().toUpperCase();
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
}
