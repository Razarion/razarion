package com.btxtech.server.user;

import com.btxtech.server.model.UserEntity;
import com.btxtech.server.repository.UserRepository;
import com.btxtech.server.service.engine.LevelCrudPersistence;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.UserContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


@Service
public class UserService {
    private final LevelCrudPersistence levelCrudPersistence;
    private final SessionService sessionService;
    private final UserRepository userRepository;

    public UserService(LevelCrudPersistence levelCrudPersistence,
                       SessionService sessionService,
                       UserRepository userRepository) {
        this.levelCrudPersistence = levelCrudPersistence;
        this.sessionService = sessionService;
        this.userRepository = userRepository;
    }

    @Transactional
    public UserContext getUserContext(String httpSessionId) {
        var session = sessionService.getSession(httpSessionId);
        if (session.getUserContext() == null) {
            session.setUserContext(createUserContext());
        }
        return session.getUserContext();
    }


    private UserContext createUserContext() {
        var userEntity = new UserEntity();
        userEntity.setLevel(levelCrudPersistence.getStarterLevel());
        return userRepository.save(userEntity).toUserContext();
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
