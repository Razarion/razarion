package com.btxtech.server.user;

import com.btxtech.server.FakeEmailServer;
import com.btxtech.server.ServerArquillianBaseTest;
import com.btxtech.server.web.SessionHolder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Date;

/**
 * Created by Beat
 * 05.05.2017.
 */
public class RegisterServiceTest extends ServerArquillianBaseTest {
    @Inject
    private RegisterService registerService;
    @Inject
    private UserService userService;
    @Inject
    private SessionHolder sessionHolder;
    @Inject
    private FakeEmailServer fakeEmailServer;

    @Before
    public void before() throws Exception {
        setupPlanets();
    }

    @After
    public void after() throws Exception {
        fakeEmailServer.stopFakeMailServer();
        cleanUsers();
        cleanPlanets();
    }

    @Test
    public void testRemoveOldEmailVerification() throws Exception {
        setupPlanets();

        fakeEmailServer.startFakeMailServer();

        userService.createUnverifiedUserAndLogin("xxx@yyy.com", "123345789");
        sessionHolder.getPlayerSession().setUserContext(null); // Logout
        userService.createUnverifiedUserAndLogin("xxx@yy1.com", "123345789");
        runInTransaction(em -> {
            UserEntity userEntity = em.createQuery("select u from UserEntity u where u.email = 'xxx@yy1.com'", UserEntity.class).getSingleResult();
            userEntity.setVerificationStartedDate(new Date(System.currentTimeMillis() - 86400005));
            em.persist(userEntity);
        });

        RegisterService.CLEANUP_DELAY = 100;
        registerService.cleanup();
        registerService.init();
        Thread.sleep(200);

        runInTransaction(em -> {
            UserEntity userEntity = em.createQuery("select u from UserEntity u where u.email = 'xxx@yy1.com'", UserEntity.class).getSingleResult();
            Assert.assertNotNull(userEntity.getVerificationTimedOutDate());
            userEntity = em.createQuery("select u from UserEntity u where u.email = 'xxx@yyy.com'", UserEntity.class).getSingleResult();
            Assert.assertNull(userEntity.getVerificationTimedOutDate());
        });
    }

    @Test
    public void testRemoveOldForgotPassword() throws Exception {
        setupPlanets();

        fakeEmailServer.startFakeMailServer();

        userService.createUnverifiedUserAndLogin("xxx@yyy.com", "123345789");
        sessionHolder.getPlayerSession().setUserContext(null); // Logout
        userService.createUnverifiedUserAndLogin("xxx@yy1.com", "123345789");
        sessionHolder.getPlayerSession().setUserContext(null); // Logout

        registerService.onForgotPassword("xxx@yyy.com");
        registerService.onForgotPassword("xxx@yy1.com");

        runInTransaction(em -> {
            ForgotPasswordEntity forgotPasswordEntity = em.createQuery("select f from ForgotPasswordEntity f where f.user.email = 'xxx@yy1.com'", ForgotPasswordEntity.class).getSingleResult();
            forgotPasswordEntity.setDate(new Date(System.currentTimeMillis() - 86400005));
            em.persist(forgotPasswordEntity);
        });

        RegisterService.CLEANUP_DELAY = 100;
        registerService.cleanup();
        registerService.init();
        Thread.sleep(200);

        runInTransaction(em -> {
            ForgotPasswordEntity porgotPasswordEntity = em.createQuery("select f from ForgotPasswordEntity f", ForgotPasswordEntity.class).getSingleResult();
            Assert.assertEquals("xxx@yyy.com", porgotPasswordEntity.getUser().getEmail());
        });
    }

}