package com.btxtech.server.persistence.chat;

import com.btxtech.server.ClientSystemConnectionServiceTestHelper;
import com.btxtech.server.IgnoreOldArquillianTest;
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
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Date;

/**
 * Created by Beat
 * on 29.12.2017.
 */
@Ignore
public class ChatPersistenceTestRest extends IgnoreOldArquillianTest {

    private UserService userService;

    private ChatPersistence chatPersistence;

    private ClientSystemConnectionServiceTestHelper systemConnectionService;

    private SessionHolder sessionHolder;

    private SessionService sessionService;

    @Inject
    public ChatPersistenceTestRest(SessionService sessionService, SessionHolder sessionHolder, ClientSystemConnectionServiceTestHelper systemConnectionService, ChatPersistence chatPersistence, UserService userService) {
        this.sessionService = sessionService;
        this.sessionHolder = sessionHolder;
        this.systemConnectionService = systemConnectionService;
        this.chatPersistence = chatPersistence;
        this.userService = userService;
    }

    @Before
    public void before() throws Exception {
        setupLevelDb();
    }

    @After
    public void after() throws Exception {
        // TODO cleanLevels();
    }

    @Test
    public void testSendRegistered() throws Exception {
        UserContext userContext = handleFacebookUserLogin("0000001");
        userService.setName("sdifbj");

        TestClientSystemConnection testClientSystemConnection = systemConnectionService.connectClient(sessionHolder.getPlayerSession());

        chatPersistence.onMessage(sessionService.getSession(sessionHolder.getPlayerSession().getHttpSessionId()), "auishfd ahfuauihf aohfhae nafoihjeqaofjpo0 qoewhfjoifwjbnef");
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(0, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userContext.getUserId()).setUserName("sdifbj").setMessage("auishfd ahfuauihf aohfhae nafoihjeqaofjpo0 qoewhfjoifwjbnef"));

        chatPersistence.onMessage(sessionService.getSession(sessionHolder.getPlayerSession().getHttpSessionId()), "asdf kll wssxdvbhnmhjhki   äöpoöoöpo");
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(1, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userContext.getUserId()).setUserName("sdifbj").setMessage("asdf kll wssxdvbhnmhjhki   äöpoöoöpo"));

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
        handleFacebookUserLogin("0000001");
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
                chatMessageEntity.setTimestamp(new Date(timeStamp + i));
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


        UserContext userContext = handleFacebookUserLogin("0000001");
        userService.setName("sdifbj");
        TestClientSystemConnection testClientSystemConnection = systemConnectionService.connectClient(sessionHolder.getPlayerSession());
        chatPersistence.sendLastMessages(sessionService.getSession(sessionHolder.getPlayerSession().getHttpSessionId()));
        // Verify (order with the correct milliseconds in DB)
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(0, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity1.getId()).setUserName("name1").setMessage("xxxx3"));
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(1, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity1.getId()).setUserName("name1").setMessage("xxxx4"));
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(2, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity1.getId()).setUserName("name1").setMessage("xxxx5"));
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(3, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity1.getId()).setUserName("name1").setMessage("xxxx6"));
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(4, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity1.getId()).setUserName("name1").setMessage("xxxx7"));
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(5, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity1.getId()).setUserName("name1").setMessage("xxxx8"));
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(6, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity1.getId()).setUserName("name1").setMessage("xxxx9"));
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(7, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity1.getId()).setUserName("name1").setMessage("xxxx10"));
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(8, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity1.getId()).setUserName("name1").setMessage("xxxx11"));
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(9, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity1.getId()).setUserName("name1").setMessage("xxxx12"));
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(10, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity1.getId()).setUserName("name1").setMessage("xxxx13"));
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(11, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity1.getId()).setUserName("name1").setMessage("xxxx14"));
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(12, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity1.getId()).setUserName("name1").setMessage("xxxx15"));
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(13, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity1.getId()).setUserName("name1").setMessage("xxxx16"));
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(14, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity1.getId()).setUserName("name1").setMessage("xxxx17"));
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(15, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity1.getId()).setUserName("name1").setMessage("xxxx18"));
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(16, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity1.getId()).setUserName("name1").setMessage("xxxx19"));
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(17, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity1.getId()).setUserName("name1").setMessage("zuri ouhbdf ndswokijui"));
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(18, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity2.getId()).setUserName("name2").setMessage("asd gfrfsagh ewrfwrfew"));
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(19, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity1.getId()).setUserName("name1").setMessage("asdfsdfsdaf"));
        // Send message
        chatPersistence.onMessage(sessionService.getSession(sessionHolder.getPlayerSession().getHttpSessionId()), "frghstehllool");
        testClientSystemConnection.clear();
        chatPersistence.sendLastMessages(sessionService.getSession(sessionHolder.getPlayerSession().getHttpSessionId()));
        // Verify
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(17, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity2.getId()).setUserName("name2").setMessage("asd gfrfsagh ewrfwrfew"));
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(18, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userEntity1.getId()).setUserName("name1").setMessage("asdfsdfsdaf"));
        testClientSystemConnection.getWebsocketMessageHelper().assertMessageSent(19, "CHAT_RECEIVE_MESSAGE", ChatMessage.class, new ChatMessage().setUserId(userContext.getUserId()).setUserName("sdifbj").setMessage("frghstehllool"));


        cleanTable(ChatMessageEntity.class);
        cleanUsers();
    }

}