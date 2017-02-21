package com.btxtech.server.user;

import java.lang.reflect.Method;

/**
 * Created by Beat
 * 21.02.2017.
 */
public class SecurityException extends RuntimeException {
    public SecurityException(String message, Method method) {
        super(message + " Method " + method);
    }

    public SecurityException(UserSession userSession, Method method) {
        super("User '" + userSession.getUserContext().getUserId() + "' does not have admin rights to call: " + method);
    }
}
