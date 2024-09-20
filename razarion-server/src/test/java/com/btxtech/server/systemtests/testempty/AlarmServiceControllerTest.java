package com.btxtech.server.systemtests.testempty;

import com.btxtech.server.systemtests.framework.AbstractSystemTest;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.rest.AlarmServiceController;
import com.btxtech.shared.rest.GameUiContextController;
import com.btxtech.shared.system.alarm.Alarm;
import org.junit.Before;
import org.junit.Test;

import jakarta.ws.rs.NotAuthorizedException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class AlarmServiceControllerTest extends AbstractSystemTest {
    private AlarmServiceController serviceController;
    private GameUiContextController gameUiContextController;

    @Before
    public void setup() {
        serviceController = setupRestAccess(AlarmServiceController.class);
        gameUiContextController = setupRestAccess(GameUiContextController.class);
    }

    @Test(expected = NotAuthorizedException.class)
    public void test() {
        getDefaultRestConnection().logout();
        serviceController.getAlarms();
    }

    @Test(expected = NotAuthorizedException.class)
    public void testUser() {
        getDefaultRestConnection().loginUser();
        serviceController.getAlarms();
    }

    @Test
    public void testAdmin() {
        getDefaultRestConnection().loginAdmin();
        gameUiContextController.loadColdGameUiContext(new GameUiControlInput());
        List<Alarm.Type> alarms = serviceController.getAlarms().stream().map(alarm -> alarm.getType()).collect(Collectors.toList());
        assertTrue(alarms.contains(Alarm.Type.USER_HAS_NO_LEVEL));
    }
}
