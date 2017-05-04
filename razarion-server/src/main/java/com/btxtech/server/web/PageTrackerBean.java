package com.btxtech.server.web;

import com.btxtech.server.persistence.tracker.TrackerPersistence;
import com.btxtech.shared.system.ExceptionHandler;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by Beat
 * 03.03.2017.
 */
@Named
@RequestScoped
public class PageTrackerBean {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private HttpServletRequest httpServletRequest;
    @Inject
    private TrackerPersistence trackerPersistence;

    public void trackPage(String page) {
        try {
            trackerPersistence.onPage(page, httpServletRequest);
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }
}
