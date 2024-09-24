package com.btxtech.server.gameengine;

import com.btxtech.server.IgnoreOldArquillianTest;
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
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * on 01.09.2017.
 */
@Ignore
public class RestServerGameEngineControlTestBase extends IgnoreOldArquillianTest {

    private ServerGameEngineControl serverGameEngineControl;

    private UserService userService;

    private ServerLevelQuestService serverLevelQuestService;

    private SessionHolder sessionHolder;

    private BaseItemService baseItemService;

    private PlanetBackupMongoDb planetBackupMongoDb;

    @Inject
    public RestServerGameEngineControlTestBase(PlanetBackupMongoDb planetBackupMongoDb, BaseItemService baseItemService, SessionHolder sessionHolder, ServerLevelQuestService serverLevelQuestService, UserService userService, ServerGameEngineControl serverGameEngineControl) {
        this.planetBackupMongoDb = planetBackupMongoDb;
        this.baseItemService = baseItemService;
        this.sessionHolder = sessionHolder;
        this.serverLevelQuestService = serverLevelQuestService;
        this.userService = userService;
        this.serverGameEngineControl = serverGameEngineControl;
    }

    @Before
    public void before() throws Exception {
        setupPlanetDb();
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
        baseItemService.createHumanBaseWithBaseItem(LEVEL_4_ID, Collections.emptyMap(), userContext.getUserId(), userContext.getName(), new DecimalPosition(21, 34));
        serverGameEngineControl.backupPlanet();

        planetBackupMongoDb.loadLastBackup(2);

        serverGameEngineControl.restartPlanet();
        List<BackupPlanetOverview> backupPlanetOverviews = planetBackupMongoDb.loadAllBackupBaseOverviews();
        serverGameEngineControl.restorePlanet(backupPlanetOverviews.get(0));
    }
}