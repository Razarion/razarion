package com.btxtech.server.gameengine;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.server.persistence.backup.BackupPlanetOverview;
import com.btxtech.server.persistence.backup.PlanetBackupMongoDb;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.BackupPlanetInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * on 01.09.2017.
 */
public class ServerGameEngineControlTest extends ArquillianBaseTest {
    @Inject
    private ServerGameEngineControl serverGameEngineControl;
    @Inject
    private UserService userService;
    @Inject
    private ServerLevelQuestService serverLevelQuestService;
    @Inject
    private SessionHolder sessionHolder;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private PlanetBackupMongoDb planetBackupMongoDb;

    @Before
    public void before() throws Exception {
        setupPlanets();
        clearMongoDb();
        fillBackupInfoMongoDb("planet_backup", "/mongodb/PlanetBackup.json", BackupPlanetInfo.class);
    }

    @After
    public void after() throws Exception {
        cleanUsers();
        cleanPlanets();
        clearMongoDb();
    }

    @Test
    public void startRestartRegistered() throws Exception {
        // Start from ServletContextMonitor.contextInitialized() not working
        serverGameEngineControl.start(null, true);
        // Setup session
        String sessionId = sessionHolder.getPlayerSession().getHttpSessionId();
        // UserContext userContext = userService.getUserContextFromSession(); // Simulate anonymous login
        UserContext userContext = handleFacebookUserLogin("0000001");
        serverLevelQuestService.onClientLevelUpdate(sessionId, LEVEL_4_ID);
        baseItemService.createHumanBaseWithBaseItem(LEVEL_4_ID, Collections.emptyMap(), userContext.getHumanPlayerId(), userContext.getName(), new DecimalPosition(21, 34));
        serverGameEngineControl.backupPlanet();

        planetBackupMongoDb.loadLastBackup(2);

        serverGameEngineControl.restartPlanet();
        List<BackupPlanetOverview> backupPlanetOverviews = planetBackupMongoDb.loadAllBackupBaseOverviews();
        serverGameEngineControl.restorePlanet(backupPlanetOverviews.get(0));
    }
}