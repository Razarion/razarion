package com.btxtech.server.user;

import com.btxtech.shared.datatypes.UserContext;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;

/**
 * Created by Beat
 * 21.02.2017.
 */
@SessionScoped
public class UserSession implements Serializable{
    private UserContext userContext;

    public UserContext getUserContext() {
        return userContext;
    }

    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
    }
}
