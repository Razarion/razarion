package com.btxtech.server.user;

import com.btxtech.server.FakeEmailServer;
import com.btxtech.server.IgnoreOldArquillianTest;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.datatypes.ErrorResult;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Date;

/**
 * Created by Beat
 * 05.05.2017.
 */
@Ignore
public class RegisterServiceTestRest extends IgnoreOldArquillianTest {

    private RegisterService registerService;

    private UserService userService;

    private SessionHolder sessionHolder;

    private FakeEmailServer fakeEmailServer;

    @Inject
    public RegisterServiceTestRest(FakeEmailServer fakeEmailServer, SessionHolder sessionHolder, UserService userService, RegisterService registerService) {
        this.fakeEmailServer = fakeEmailServer;
        this.sessionHolder = sessionHolder;
        this.userService = userService;
        this.registerService = registerService;
    }

    @Before
    public void before() throws Exception {
        fakeEmailServer.startFakeMailServer();
        setupPlanetDb();
    }

    @After
    public void after() throws Exception {
        fakeEmailServer.stopFakeMailServer();
        cleanUsers();
        cleanPlanets();
    }

    @Test
    public void testRemoveOldEmailVerification() throws Exception {
        userService.createUnverifiedUserAndLogin("xxx@yyy.com", "123345789");
        sessionHolder.getPlayerSession().setUserContext(null); // Logout
        userService.createUnverifiedUserAndLogin("xxx@yy1.com", "123345789");
        runInTransaction(em -> {
            UserEntity userEntity = em.createQuery("select u from UserEntity u where u.email = 'xxx@yy1.com'", UserEntity.class).getSingleResult();
            userEntity.setVerificationStartedDate(new Date(System.currentTimeMillis() - 90000000L));
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
        userService.createUnverifiedUserAndLogin("xxx@yyy.com", "123345789");
        sessionHolder.getPlayerSession().setUserContext(null); // Logout
        userService.createUnverifiedUserAndLogin("xxx@yy1.com", "123345789");
        sessionHolder.getPlayerSession().setUserContext(null); // Logout

        registerService.onForgotPassword("xxx@yyy.com");
        registerService.onForgotPassword("xxx@yy1.com");

        runInTransaction(em -> {
            ForgotPasswordEntity forgotPasswordEntity = em.createQuery("select f from ForgotPasswordEntity f where f.user.email = 'xxx@yy1.com'", ForgotPasswordEntity.class).getSingleResult();
            forgotPasswordEntity.setDate(new Date(System.currentTimeMillis() - 90000000L));
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


    @Test
    public void isEmailFree() throws Exception {
        userService.createUnverifiedUserAndLogin("xxx@yyy.com", "123345789");
        sessionHolder.getPlayerSession().setUserContext(null); // Logout
        userService.createUnverifiedUserAndLogin("xxx@yy1.com", "123345789");
        sessionHolder.getPlayerSession().setUserContext(null); // Logout
        runInTransaction(em -> {
            UserEntity userEntity = em.createQuery("select u from UserEntity u where u.email = 'xxx@yy1.com'", UserEntity.class).getSingleResult();
            userEntity.setVerifiedTimedOut();
            em.persist(userEntity);
        });

        Assert.assertEquals(ErrorResult.TO_SHORT,userService.verifyEmail(null));
        Assert.assertEquals(ErrorResult.TO_SHORT,userService.verifyEmail(""));
        Assert.assertEquals(ErrorResult.ALREADY_USED,userService.verifyEmail("xxx@yyy.com"));
        Assert.assertNull(userService.verifyEmail("xxx@yyy.co"));
        Assert.assertNull(userService.verifyEmail("xxx@yy1.com"));
    }
}