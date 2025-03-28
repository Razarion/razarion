package com.btxtech.server.user;

import com.btxtech.server.service.engine.LevelCrudPersistence;
import com.btxtech.shared.datatypes.UserContext;
import org.springframework.stereotype.Service;


/**
 * Created by Beat
 * 21.02.2017.
 */
@Service
public class UserService {
    private final LevelCrudPersistence levelCrudPersistence;

    public UserService(LevelCrudPersistence levelCrudPersistence) {
        this.levelCrudPersistence = levelCrudPersistence;
    }

    public UserContext createUserContext() {
        return new UserContext()
                .levelId(levelCrudPersistence.getStarterLevelId())
                .registerState(UserContext.RegisterState.UNREGISTERED);
    }
}
