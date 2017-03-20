package com.btxtech.server.user;

import com.btxtech.server.web.Session;
import com.btxtech.server.system.FilePropertiesService;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FacebookUserLoginInfo;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 21.02.2017.
 */
@ApplicationScoped
public class UserService {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private Logger logger;
    @Inject
    private Session session;
    @Inject
    private FilePropertiesService filePropertiesService;

    public UserContext handleUserLoginInfo(FacebookUserLoginInfo facebookUserLoginInfo) {
        // TODO verify facebook signedRequest

        // facebookUserLoginInfo is never null. Errai Jackson JAX-RS does not accept null value in POST rest call
        if (facebookUserLoginInfo.getUserId() == null) {
            return createUserContext(null);
        }

        UserEntity userEntity = getUserForFacebookId(facebookUserLoginInfo.getUserId());
        if (userEntity == null) {
            userEntity = createUser(facebookUserLoginInfo);
        }
        User user = userEntity.createUser();
        session.setUser(user);
        return createUserContext(user);
    }

    private UserContext createUserContext(User user) {
        UserContext userContext = new UserContext();
        if (user != null) {
            userContext.setUserId((int) user.getUserId());
            userContext.setAdmin(user.isAdmin());
        } else {
            userContext.setUserId(999999999); // TODO
        }
        if(filePropertiesService.isDeveloperMode()) {
            userContext.setAdmin(true);
        }
        userContext.setName("Emulator Name");// TODO
        userContext.setLevelId(1);
        return userContext;
    }

    @Transactional
    private UserEntity createUser(FacebookUserLoginInfo facebookUserLoginInfo) {
        UserEntity userEntity = new UserEntity();
        userEntity.fromFacebookUserLoginInfo(facebookUserLoginInfo);
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
}
