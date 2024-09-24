package com.btxtech.server.user;

import com.btxtech.server.web.SessionHolder;
import com.btxtech.server.system.FilePropertiesService;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * Created by Beat
 * 21.02.2017.
 */
@Interceptor
@SecurityCheck
public class SecurityInterceptor {

    private SessionHolder sessionHolder;

    private FilePropertiesService filePropertiesService;

    @Inject
    public SecurityInterceptor(FilePropertiesService filePropertiesService, SessionHolder sessionHolder) {
        this.filePropertiesService = filePropertiesService;
        this.sessionHolder = sessionHolder;
    }

    @AroundInvoke
    public Object checkAuthentication(InvocationContext invocationContext) throws Exception {
        if (!filePropertiesService.isDeveloperMode()) {
            if (sessionHolder.getPlayerSession() == null) {
                throw new SecurityException("sessionHolder.getPlayerSession() == null", invocationContext.getMethod());
            }
            if (sessionHolder.getPlayerSession().getUserContext() == null) {
                throw new SecurityException("sessionHolder.getPlayerSession().getUserContext() == null", invocationContext.getMethod());
            }

            if (!sessionHolder.getPlayerSession().getUserContext().isAdmin()) {
                throw new SecurityException(sessionHolder.getPlayerSession().getUserContext(), invocationContext.getMethod());
            }
        }

        return invocationContext.proceed();
    }
}
