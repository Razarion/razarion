package com.btxtech.server.persistence.chat;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.server.ClientSystemConnectionServiceTestHelper;
import com.btxtech.server.TestClientSystemConnection;
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

}