package com.btxtech.server.user;

import com.btxtech.server.web.Session;
import com.btxtech.servercommon.FilePropertiesService;

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
    @Inject
    private Session session;
    @Inject
    private FilePropertiesService filePropertiesService;

    @AroundInvoke
    public Object logMethodEntry(InvocationContext invocationContext) throws Exception {
        if (!filePropertiesService.isDeveloperMode()) {
            if (session.getUser() == null) {
                throw new SecurityException("session.getUser() == null", invocationContext.getMethod());
            }

            if (!session.getUser().isAdmin()) {
                throw new SecurityException(session.getUser(), invocationContext.getMethod());
            }
        }

        return invocationContext.proceed();
    }
}
