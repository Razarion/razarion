package com.btxtech.server.user;

import com.btxtech.shared.datatypes.UserContext;

import java.lang.reflect.Method;

/**
 * Created by Beat
 * 21.02.2017.
 */
public class SecurityException extends RuntimeException {
    public SecurityException(String message, Method method) {
        super(message + " Method " + method);
    }

    public SecurityException(UserContext userContext, Method method) {
        super("User '" + userContext.getHumanPlayerId() + "' does not have admin rights to call: " + method);
    }
}
