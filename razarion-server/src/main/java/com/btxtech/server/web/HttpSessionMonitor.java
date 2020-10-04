package com.btxtech.server.web;

import com.btxtech.server.persistence.history.HistoryPersistence;
import com.btxtech.server.persistence.tracker.TrackerPersistence;
import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.user.UserService;
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
    private SessionHolder sessionHolder;
    @Inject
    private HttpServletRequest httpRequest;
    @Inject
    private TrackerPersistence trackerPersistence;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private UserService userService;
    @Inject
    private SessionService sessionService;
    @Inject
    private HistoryPersistence historyPersistence;

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        try {
            if (sessionHolder.getPlayerSession() != null) {
                logger.warning("SessionHolder already set: " + sessionHolder.getPlayerSession().getHttpSessionId());
            }
            PlayerSession playerSession = sessionService.sessionCreated(se.getSession().getId(), httpRequest.getLocale());
            sessionHolder.setPlayerSession(playerSession);
            trackerPersistence.onNewSession(httpRequest);
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        PlayerSession playerSession = sessionService.sessionDestroyed(se.getSession().getId());
        if (playerSession.getUserContext() != null && playerSession.getUserContext().registered()) {
            historyPersistence.onUserLoggedOut(playerSession.getUserContext().getUserId(), playerSession.getHttpSessionId());
        }
    }
}
