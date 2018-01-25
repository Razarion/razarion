package com.btxtech.server.rest;

import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.ErrorResult;
import com.btxtech.shared.datatypes.FbAuthResponse;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.RegisterInfo;
import com.btxtech.shared.datatypes.SetNameResult;
import com.btxtech.shared.rest.UserServiceProvider;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 23.09.2017.
 */
public class UserServiceProviderImpl implements UserServiceProvider {
    @Inject
    private UserService userService;
    @Inject
    private ExceptionHandler exceptionHandler;

    @Override
    public RegisterInfo inGameFacebookRegister(FbAuthResponse fbAuthResponse) {
        try {
            return userService.handleInGameFacebookUserLogin(fbAuthResponse);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public SetNameResult setName(String name) {
        try {
            return userService.setName(name);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public SetNameResult verifySetName(String name) {
        try {
            return new SetNameResult().setErrorResult(userService.verifySetName(name));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }
}
