package com.btxtech.server.gameengine;

import com.btxtech.server.SimpleTestEnvironment;
import com.btxtech.server.TestServerExceptionHandler;
import com.btxtech.server.user.User;
import com.btxtech.server.web.Session;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * Created by Beat
 * 20.04.2017.
 */
public class ClientConnectionTest {
//    @Test
//    public void onMessage() {
//        // Mock BaseItemService
//        BaseItemService baseItemServiceMock = EasyMock.createStrictMock(BaseItemService.class);
//        baseItemServiceMock.createHumanBaseWithBaseItem(4, 12, "User Name", new DecimalPosition(101.543, 205.495));
//        EasyMock.replay(baseItemServiceMock);
//        // Session
//        User user = new User(12, 4, false);
//        user.setName("User Name");
//        Session session = new Session();
//        session.setUser(user);
//
//        ClientConnection clientConnection = new ClientConnection();
//        TestServerExceptionHandler testServerExceptionHandler = new TestServerExceptionHandler();
//        SimpleTestEnvironment.injectService("exceptionHandler", clientConnection, testServerExceptionHandler);
//        SimpleTestEnvironment.injectService("baseItemService", clientConnection, baseItemServiceMock);
//        SimpleTestEnvironment.injectService("session", clientConnection, session);
//        // Run test
//        String message = "CREATE_BASE#{\"x\":101.543, \"y\":205.495}]";
//        clientConnection.onMessage(null, message);
//        // Verify
//        testServerExceptionHandler.assertNoException();
//        EasyMock.verify(baseItemServiceMock);
//    }


}