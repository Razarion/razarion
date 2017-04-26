package com.btxtech.server.user;

import com.btxtech.server.persistence.GameEngineConfigPersistence;
import com.btxtech.server.system.FilePropertiesService;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FacebookUserLoginInfo;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 21.02.2017.
 */
@Singleton
public class UserService {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private Logger logger;
    @Inject
    private SessionHolder sessionHolder;
    @Inject
    private FilePropertiesService filePropertiesService;
    private Map<String, UserContext> loggedInUserContext = new HashMap<>();

    @Transactional
    public UserContext handleUserLoginInfo(FacebookUserLoginInfo facebookUserLoginInfo) {
        // TODO verify facebook signedRequest

        // facebookUserLoginInfo is never null. Errai Jackson JAX-RS does not accept null value in POST rest call
        if (facebookUserLoginInfo.getUserId() == null) {
            if (sessionHolder.getPlayerSession().getUserContext() != null) {
                return sessionHolder.getPlayerSession().getUserContext();
            }
            return createAndLoginUserContext(null);
        }

        UserEntity userEntity = getUserForFacebookId(facebookUserLoginInfo.getUserId());
        if (userEntity == null) {
            userEntity = createUser(facebookUserLoginInfo);
        }
        //TODO remove if all do have a HumanPlayerId
        if (userEntity.getHumanPlayerIdEntity() == null) {
            fixHumanPlayerIdEntity(userEntity);
        }
        //TODO ends
        UserContext userContext = userEntity.createUser();
        HumanPlayerId alreadyLoggerIn = null;
        if (sessionHolder.getPlayerSession().getUserContext() != null) {
            alreadyLoggerIn = sessionHolder.getPlayerSession().getUserContext().getHumanPlayerId();
        }
        if (alreadyLoggerIn != null && alreadyLoggerIn.getUserId() != null && alreadyLoggerIn.getUserId().equals(userContext.getHumanPlayerId().getUserId())) {
            return sessionHolder.getPlayerSession().getUserContext();
        }
        return createAndLoginUserContext(userContext);
    }

    private void fixHumanPlayerIdEntity(UserEntity userEntity) {
        userEntity.setHumanPlayerIdEntity(createHumanPlayerId());
        entityManager.merge(userEntity);
    }

    private UserContext createAndLoginUserContext(UserContext userContext) {
        if (userContext == null) {
            userContext = sessionHolder.getPlayerSession().getUserContext();
            if (userContext == null) {
                userContext = new UserContext();
                userContext.setHumanPlayerId(new HumanPlayerId().setPlayerId(createHumanPlayerId().getId()));
                userContext.setLevelId(GameEngineConfigPersistence.FIRST_LEVEL_ID);
            }
        }
        if (filePropertiesService.isDeveloperMode()) {
            userContext.setAdmin(true);
        }
        sessionHolder.getPlayerSession().setUserContext(userContext);
        userContext.setName("Emulator Name");// TODO
        loginUser(sessionHolder.getPlayerSession().getHttpSessionId(), userContext);
        return userContext;
    }

    @Transactional
    private UserEntity createUser(FacebookUserLoginInfo facebookUserLoginInfo) {
        UserEntity userEntity = new UserEntity();
        userEntity.fromFacebookUserLoginInfo(facebookUserLoginInfo, createHumanPlayerId());
        userEntity.setLevelId(GameEngineConfigPersistence.FIRST_LEVEL_ID);
        entityManager.persist(userEntity);
        return userEntity;
    }

    @Transactional
    public UserEntity getUserForFacebookId(String facebookUseId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> userQuery = criteriaBuilder.createQuery(UserEntity.class);
        Root<UserEntity> from = userQuery.from(UserEntity.class);
        CriteriaQuery<UserEntity> userSelect = userQuery.select(from);
        userSelect.where(criteriaBuilder.equal(from.get(UserEntity_.facebookUserId), facebookUseId));
        List<UserEntity> list = entityManager.createQuery(userQuery).getResultList();
        if (list == null || list.isEmpty()) {
            return null;
        } else if (list.size() > 1) {
            throw new IllegalStateException("More then one user for facebook id: " + facebookUseId);
        } else {
            return list.get(0);
        }
    }

    @Transactional
    public HumanPlayerIdEntity createHumanPlayerId() {
        HumanPlayerIdEntity humanPlayerIdEntity = new HumanPlayerIdEntity();
        humanPlayerIdEntity.setTimeStamp(new Date());
        humanPlayerIdEntity.setSessionId(sessionHolder.getPlayerSession().getHttpSessionId());
        entityManager.persist(humanPlayerIdEntity);
        return humanPlayerIdEntity;
    }

    private void loginUser(String sessionId, UserContext userContext) {
        loggedInUserContext.put(sessionId, userContext);
    }

    public void logoutUserUser(String sessionId) {
        loggedInUserContext.remove(sessionId);
    }

    public UserContext getLoggedInUser(String sessionId) {
        UserContext userContext = loggedInUserContext.get(sessionId);
        if (userContext == null) {
            throw new IllegalArgumentException("No User for sessionId: " + sessionId);
        }
        return userContext;
    }

    public void onLevelUpdate(String sessionId, int levelId) {
        UserContext userContext = getLoggedInUserContext(sessionId);
        userContext.setLevelId(levelId);
    }

    public UserContext getLoggedInUserContext(String sessionId) {
        UserContext userContext = loggedInUserContext.get(sessionId);
        if (userContext == null) {
            throw new IllegalArgumentException("No userContext for sessionId: " + sessionId);
        }
        return userContext;
    }
}
