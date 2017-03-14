package com.btxtech.server.web;

import com.btxtech.server.persistence.tracker.TrackerPersistence;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 27.02.2017.
 */
@WebListener
public class HttpSessionMonitor implements HttpSessionListener {
    @Inject
    private Logger logger;
    @Inject
    private Session session;
    @Inject
    private HttpServletRequest httpRequest;
    @Inject
    private TrackerPersistence trackerPersistence;
    @Inject
    private ExceptionHandler exceptionHandler;

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        try {
            if (session.getId() != null) {
                logger.warning("Session already set: " + session);
            }
            session.setId(se.getSession().getId());
            session.setLocale(httpRequest.getLocale());
            trackerPersistence.onNewSession(httpRequest);
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
    }
}
