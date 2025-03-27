package com.btxtech.server.user;

import com.btxtech.shared.datatypes.UserContext;
import org.springframework.stereotype.Service;


/**
 * Created by Beat
 * 21.02.2017.
 */
@Service
public class UserService {
    public UserContext getUserContextFromSession() {
        return new UserContext().registerState(UserContext.RegisterState.UNREGISTERED);
    }
}
