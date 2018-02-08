package com.btxtech.server.persistence.backup;

import com.btxtech.server.ServerArquillianBaseTest;
import com.btxtech.server.util.DateUtil;
import com.btxtech.shared.gameengine.datatypes.BackupPlanetInfo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * on 03.09.2017.
 */
public class PlanetBackupMongoDbTest extends ServerArquillianBaseTest {
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
    public void testLoadLastBackup() {
        BackupPlanetInfo backupPlanetInfo = planetBackupMongoDb.loadLastBackup(2);
        Assert.assertEquals(2, backupPlanetInfo.getPlanetId());
        Assert.assertEquals(DateUtil.fromJsonTimeString("2017-09-03 19:59:04.648"), backupPlanetInfo.getDate());
        Assert.assertEquals(1, backupPlanetInfo.getPlayerBaseInfos().size());
        Assert.assertEquals(2, backupPlanetInfo.getSyncBaseItemInfos().size());
        Assert.assertEquals(2, backupPlanetInfo.getBackupComparisionInfos().size());
    }

    @Test
    public void testLoadAllBackupBaseOverviews() {
        List<BackupPlanetOverview> allBackups = planetBackupMongoDb.loadAllBackupBaseOverviews();
        Assert.assertEquals(4, allBackups.size());
        BackupPlanetOverview backupPlanetOverview = findBackupBaseOverviews(DateUtil.fromJsonTimeString("2017-09-03 19:59:04.648"), 2, allBackups);
        Assert.assertEquals(1, backupPlanetOverview.getBases());
        Assert.assertEquals(2, backupPlanetOverview.getItems());
        Assert.assertEquals(2, backupPlanetOverview.getQuests());
    }

    @Test
    public void testLoadBackup() {
        BackupPlanetInfo backupPlanetInfo = planetBackupMongoDb.loadBackup(new BackupPlanetOverview().setPlanetId(2).setDate(DateUtil.fromJsonTimeString("2017-09-03 19:59:04.648")));
        Assert.assertEquals(2, backupPlanetInfo.getPlanetId());
        Assert.assertEquals(DateUtil.fromJsonTimeString("2017-09-03 19:59:04.648"), backupPlanetInfo.getDate());
        Assert.assertEquals(1, backupPlanetInfo.getPlayerBaseInfos().size());
        Assert.assertEquals(2, backupPlanetInfo.getSyncBaseItemInfos().size());
        Assert.assertEquals(2, backupPlanetInfo.getBackupComparisionInfos().size());
    }

    private BackupPlanetOverview findBackupBaseOverviews(Date date, int planetId, List<BackupPlanetOverview> backupPlanetOverviews) {
        for (BackupPlanetOverview backupPlanetOverview : backupPlanetOverviews) {
            if (backupPlanetOverview.getPlanetId() == planetId && backupPlanetOverview.getDate().equals(date)) {
                return backupPlanetOverview;
            }
        }
        throw new IllegalArgumentException("No  BackupPlanetOverview for date: " + date + " planetId: " + planetId);
    }
}