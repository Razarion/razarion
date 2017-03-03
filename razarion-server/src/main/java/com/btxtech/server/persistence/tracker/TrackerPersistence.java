package com.btxtech.server.persistence.tracker;

import com.btxtech.server.web.Session;
import com.btxtech.shared.system.ExceptionHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Beat
 * 28.02.2017.
 */
@ApplicationScoped
public class TrackerPersistence {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private Session session;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void onNewSession(HttpServletRequest request) {
        SessionTrackerEntity sessionTrackerEntity = new SessionTrackerEntity();
        sessionTrackerEntity.setSessionId(session.getId());
        sessionTrackerEntity.setUserAgent(request.getHeader("user-agent"));
        sessionTrackerEntity.setRemoteAddr(request.getRemoteAddr());
        sessionTrackerEntity.setReferer(request.getHeader("Referer"));
        sessionTrackerEntity.setLanguage(request.getHeader("Accept-Language"));
        sessionTrackerEntity.setTimeStamp(new Date());
        try {
            InetAddress inetAddress = InetAddress.getByName(request.getRemoteAddr());
            sessionTrackerEntity.setRemoteHost(inetAddress.getHostName());
        } catch (UnknownHostException e) {
            exceptionHandler.handleException(e);
        }
        entityManager.persist(sessionTrackerEntity);
    }

    @Transactional
    public void onPage(String page, HttpServletRequest request) {
        PageTrackerEntity pageTrackerEntity = new PageTrackerEntity();
        pageTrackerEntity.setSessionId(session.getId());
        pageTrackerEntity.setPage(page);
        pageTrackerEntity.setTimeStamp(new Date());
        pageTrackerEntity.setUri(request.getRequestURI());
        StringBuilder params = new StringBuilder();
        for (Iterator<Map.Entry<String, String[]>> iterator = request.getParameterMap().entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, String[]> entry = iterator.next();
            params.append(entry.getKey());
            params.append("=");
            System.out.print(entry.getKey() + "=");
            String[] value = entry.getValue();
            for (int i = 0; i < value.length; i++) {
                params.append(value[i]);
                if (i + 1 < value.length) {
                    params.append("|");
                }
            }
            if(iterator.hasNext()) {
                params.append("||");
            }
        }
        pageTrackerEntity.setParams(params.toString());
        entityManager.persist(pageTrackerEntity);
    }
}
