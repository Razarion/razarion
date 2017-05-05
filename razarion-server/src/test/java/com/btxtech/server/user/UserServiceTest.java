package com.btxtech.server.user;

import com.btxtech.server.ArquillianBaseTest;
import org.junit.Test;

import javax.inject.Inject;

/**
 * Created by Beat
 * 05.05.2017.
 */
public class UserServiceTest extends ArquillianBaseTest {
    @Inject
    private UserService userService;

    @Test
    public void handleFacebookUserLogin() throws Exception {
        System.out.println("userService: " + userService);
        userService.handleFacebookUserLogin("0000001");
    }

}