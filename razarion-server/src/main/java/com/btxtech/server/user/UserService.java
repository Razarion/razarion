package com.btxtech.server.user;

import com.btxtech.server.service.engine.LevelCrudPersistence;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.UserContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


@Service
public class UserService {
    private final LevelCrudPersistence levelCrudPersistence;
    private final SessionService sessionService;

    public UserService(LevelCrudPersistence levelCrudPersistence, SessionService sessionService) {
        this.levelCrudPersistence = levelCrudPersistence;
        this.sessionService = sessionService;
    }

    public UserContext createUserContext() {
        return new UserContext()
                .levelId(levelCrudPersistence.getStarterLevelId())
                .admin(true)
                .registerState(UserContext.RegisterState.UNREGISTERED);
    }

    public UserContext getUserContext(int userId) {
        PlayerSession playerSession = sessionService.findPlayerSession(userId);
        if (playerSession != null) {
            return playerSession.getUserContext();
        } else {
            // TODO return getUserEntity(userId).toUserContext();
            throw new UnsupportedOperationException();
        }
    }

    @Transactional
    public UserContext getUserContextTransactional(int userId) {
        return getUserContext(userId);
    }

}
