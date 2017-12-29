package com.btxtech.server.persistence.chat;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.server.ClientSystemConnectionServiceTestHelper;
import com.btxtech.server.TestClientSystemConnection;
import com.btxtech.server.user.UserEntity;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.ChatMessage;
import com.btxtech.shared.datatypes.UserContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Date;

/**
 * Created by Beat
 * on 29.12.2017.
 */
public class ChatPersistenceTest extends ArquillianBaseTest {
    @Inject
    private UserService userService;
    @Inject
    private ChatPersistence chatPersistence;
    @Inject
    private ClientSystemConnectionServiceTestHelper systemConnectionService;
    @Inject
    private SessionHolder sessionHolder;
    @Inject
    private SessionService sessionService;

    @Before
    public void before() throws Exception {
        setupLevels();
    }

    @After
    public void after() throws Exception {
        cleanLevels();
    }

    @Test
    public void testSendRegistered() throws Exception {
        UserContext userContext = userService.handleFacebookUserLogin("0000001");
        userService.setName("sdifbj");

        TestClientSystemConnection testClientSystemConnection = systemConnectionService.connectClient(sessionHolder.getPlayerSession());

        chatPersistence.onMessage(sessionService.getSession(sessionHolder.getPlayerSession().getHttpSessionId()), "auishfd ahfuauihf aohfhae nafoihjeqaofjpo0 qoewhfjoifwjbnef");
        testClientSystemConnection.assertMessageSent(0, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userContext.getHumanPlayerId().getUserId()).setUserName("sdifbj").setMessage("auishfd ahfuauihf aohfhae nafoihjeqaofjpo0 qoewhfjoifwjbnef"));

        chatPersistence.onMessage(sessionService.getSession(sessionHolder.getPlayerSession().getHttpSessionId()), "asdf kll wssxdvbhnmhjhki   äöpoöoöpo");
        testClientSystemConnection.assertMessageSent(1, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userContext.getHumanPlayerId().getUserId()).setUserName("sdifbj").setMessage("asdf kll wssxdvbhnmhjhki   äöpoöoöpo"));

        assertCount(2, ChatMessageEntity.class);

        cleanTable(ChatMessageEntity.class);
        cleanUsers();
    }

    @Test
    public void testSendUnregistered() {
        try {
            userService.getUserContextFromSession(); // Simulate anonymous access
            chatPersistence.onMessage(sessionService.getSession(sessionHolder.getPlayerSession().getHttpSessionId()), "auishfd ahfuauihf aohfhae nafoihjeqaofjpo0 qoewhfjoifwjbnef");
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().startsWith("User is not registered. Session id:"));
        }
    }

    @Test
    public void testSendUnnamed() throws Exception {
        userService.handleFacebookUserLogin("0000001");
        try {
            chatPersistence.onMessage(sessionService.getSession(sessionHolder.getPlayerSession().getHttpSessionId()), "auishfd ahfuauihf aohfhae nafoihjeqaofjpo0 qoewhfjoifwjbnef");
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().startsWith("User has no name: "));
        }
        cleanUsers();
    }

    @Test
    public void testLoadCache() throws Exception {
        final UserEntity userEntity1 = new UserEntity();
        final UserEntity userEntity2 = new UserEntity();

        runInTransaction(entityManager -> {
            // Create users
            userEntity1.setName("name1");
            entityManager.persist(userEntity1);
            userEntity2.setName("name2");
            entityManager.persist(userEntity2);
            // Create Chat messages
            long timeStamp = System.currentTimeMillis() - 100000L;
            for(int i = 0; i < 20; i++) {
                ChatMessageEntity chatMessageEntity = new ChatMessageEntity();
                chatMessageEntity.setTimestamp(new Date(timeStamp + i * 1000));
                chatMessageEntity.setUserEntity(userEntity1);
                chatMessageEntity.setMessage("xxxx" + i);
                entityManager.persist(chatMessageEntity);
            }

            timeStamp += 100000L;
            ChatMessageEntity chatMessageEntity = new ChatMessageEntity();
            chatMessageEntity.setTimestamp(new Date(timeStamp + 1000));
            chatMessageEntity.setUserEntity(userEntity1);
            chatMessageEntity.setMessage("zuri ouhbdf ndswokijui");
            entityManager.persist(chatMessageEntity);
            chatMessageEntity = new ChatMessageEntity();
            chatMessageEntity.setTimestamp(new Date(timeStamp + 5000));
            chatMessageEntity.setUserEntity(userEntity1);
            chatMessageEntity.setMessage("asdfsdfsdaf");
            entityManager.persist(chatMessageEntity);
            chatMessageEntity = new ChatMessageEntity();
            chatMessageEntity.setTimestamp(new Date(timeStamp + 3000));
            chatMessageEntity.setUserEntity(userEntity2);
            chatMessageEntity.setMessage("asd gfrfsagh ewrfwrfew");
            entityManager.persist(chatMessageEntity);
        });

        chatPersistence.fillCacheFromDb();

        UserContext userContext = userService.handleFacebookUserLogin("0000001");
        userService.setName("sdifbj");
        TestClientSystemConnection testClientSystemConnection = systemConnectionService.connectClient(sessionHolder.getPlayerSession());
        chatPersistence.sendLastMessages(sessionService.getSession(sessionHolder.getPlayerSession().getHttpSessionId()));
        // Verify
        testClientSystemConnection.assertMessageSent(17, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity1.getId()).setUserName("name1").setMessage("zuri ouhbdf ndswokijui"));
        testClientSystemConnection.assertMessageSent(18, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity2.getId()).setUserName("name2").setMessage("asd gfrfsagh ewrfwrfew"));
        testClientSystemConnection.assertMessageSent(19, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity1.getId()).setUserName("name1").setMessage("asdfsdfsdaf"));
        // Send message
        chatPersistence.onMessage(sessionService.getSession(sessionHolder.getPlayerSession().getHttpSessionId()), "frghstehllool");
        testClientSystemConnection.clear();
        chatPersistence.sendLastMessages(sessionService.getSession(sessionHolder.getPlayerSession().getHttpSessionId()));
        // Verify
        testClientSystemConnection.assertMessageSent(17, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity2.getId()).setUserName("name2").setMessage("asd gfrfsagh ewrfwrfew"));
        testClientSystemConnection.assertMessageSent(18, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity1.getId()).setUserName("name1").setMessage("asdfsdfsdaf"));
        testClientSystemConnection.assertMessageSent(19, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userContext.getHumanPlayerId().getUserId()).setUserName("sdifbj").setMessage("frghstehllool"));

        cleanTable(ChatMessageEntity.class);
        cleanUsers();
    }

}