package com.btxtech.server.user;

import javax.enterprise.context.ApplicationScoped;
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
    private UserSession userSession;

    @AroundInvoke
    public Object logMethodEntry(InvocationContext invocationContext) throws Exception {
        if (userSession.getUserContext() == null) {
            throw new SecurityException("userSession.getUserContext() == null", invocationContext.getMethod());
        }

        if (!userSession.getUserContext().isAdmin()) {
            throw new SecurityException(userSession, invocationContext.getMethod());
        }

        return invocationContext.proceed();
    }
}
