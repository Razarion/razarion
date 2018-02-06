package com.btxtech.server.rest;

import com.btxtech.server.user.RegisterResult;
import com.btxtech.server.user.RegisterService;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.datatypes.FbAuthResponse;
import com.btxtech.shared.datatypes.RegisterInfo;
import com.btxtech.shared.datatypes.SetNameResult;
import com.btxtech.shared.dto.EmailPasswordInfo;
import com.btxtech.shared.rest.UserServiceProvider;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Context;

/**
 * Created by Beat
 * on 23.09.2017.
 */
public class UserServiceProviderImpl implements UserServiceProvider {
    @Inject
    private UserService userService;
    @Inject
    private SessionHolder sessionHolder;
    @Inject
    private RegisterService registerService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Context
    private HttpServletResponse httpServletResponse;

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
    public RegisterInfo createUnverifiedUser(EmailPasswordInfo emailPasswordInfo) {
        try {
            RegisterInfo registerInfo = new RegisterInfo();
            RegisterResult registerResult = userService.createUnverifiedUserAndLogin(emailPasswordInfo.getEmail(), emailPasswordInfo.getPassword());
            if (registerResult == RegisterResult.EMAIL_ALREADY_USED) {
                registerInfo.setUserAlreadyExits(true);
            } else if (registerResult == RegisterResult.OK) {
                if (emailPasswordInfo.isRememberMe()) {
                    httpServletResponse.addCookie(FrontendProvider.generateLoginServletCookie(registerService.setupLoginCookieEntry(emailPasswordInfo.getEmail())));
                }
                registerInfo.setHumanPlayerId(sessionHolder.getPlayerSession().getUserContext().getHumanPlayerId());
            }
            return registerInfo;
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

    @Override
    public boolean isEmailFree(String email) {
        try {
            return userService.verifyEmail(email) == null;
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw new InternalServerErrorException();
        }
    }
}
