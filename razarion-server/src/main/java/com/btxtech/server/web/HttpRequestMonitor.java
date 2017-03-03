package com.btxtech.server.web;

import com.btxtech.server.persistence.tracker.TrackerPersistence;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by Beat
 * 03.03.2017.
 */
@WebListener
public class HttpRequestMonitor implements ServletRequestListener {
    private static final String MAIN_PAGE = "/main.xhtml";
    private static final String THANK_YOU_PAGE = "/ThankYou.html";
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private TrackerPersistence trackerPersistence;

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) sre.getServletRequest();
            if (httpServletRequest.getRequestURI().startsWith(THANK_YOU_PAGE)) {
                trackerPersistence.onPage(THANK_YOU_PAGE, httpServletRequest);
            } else if (httpServletRequest.getPathInfo() != null && httpServletRequest.getPathInfo().startsWith(MAIN_PAGE)) {
                trackerPersistence.onPage(MAIN_PAGE, httpServletRequest);
            }
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
    }

}
