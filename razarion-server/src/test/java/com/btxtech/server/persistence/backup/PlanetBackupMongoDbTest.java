package com.btxtech.server.persistence.backup;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.server.util.DateUtil;
import com.btxtech.shared.gameengine.datatypes.BackupBaseInfo;
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
public class PlanetBackupMongoDbTest extends ArquillianBaseTest {
    @Inject
    private PlanetBackupMongoDb planetBackupMongoDb;

    @Before
    public void before() throws Exception {
        setupPlanets();
        clearMongoDb();
        fillBackupInfoMongoDb("planet_backup", "/mongodb/PlanetBackup.json");
    }

    @After
    public void after() throws Exception {
        cleanUsers();
        cleanPlanets();
        clearMongoDb();
    }

    @Test
    public void testLoadLastBackup() {
        BackupBaseInfo backupBaseInfo = planetBackupMongoDb.loadLastBackup(2);
        Assert.assertEquals(2, backupBaseInfo.getPlanetId());
        Assert.assertEquals(DateUtil.fromJsonTimeString("2017-09-03 19:59:04.648"), backupBaseInfo.getDate());
        Assert.assertEquals(1, backupBaseInfo.getPlayerBaseInfos().size());
        Assert.assertEquals(2, backupBaseInfo.getSyncBaseItemInfos().size());
    }

    @Test
    public void testLoadAllBackupBaseOverviews() {
        List<BackupBaseOverview> allBackups = planetBackupMongoDb.loadAllBackupBaseOverviews();
        Assert.assertEquals(4, allBackups.size());
        BackupBaseOverview backupBaseOverview = findBackupBaseOverviews(DateUtil.fromJsonTimeString("2017-09-03 19:59:04.648"), 2, allBackups);
        Assert.assertEquals(1, backupBaseOverview.getBases());
        Assert.assertEquals(2, backupBaseOverview.getItems());
    }

    @Test
    public void testLoadBackup() {
        BackupBaseInfo backupBaseInfo = planetBackupMongoDb.loadBackup(new BackupBaseOverview().setPlanetId(2).setDate(DateUtil.fromJsonTimeString("2017-09-03 19:59:04.648")));
        Assert.assertEquals(2, backupBaseInfo.getPlanetId());
        Assert.assertEquals(DateUtil.fromJsonTimeString("2017-09-03 19:59:04.648"), backupBaseInfo.getDate());
        Assert.assertEquals(1, backupBaseInfo.getPlayerBaseInfos().size());
        Assert.assertEquals(2, backupBaseInfo.getSyncBaseItemInfos().size());
    }

    private BackupBaseOverview findBackupBaseOverviews(Date date, int planetId, List<BackupBaseOverview> backupBaseOverviews) {
        for (BackupBaseOverview backupBaseOverview : backupBaseOverviews) {
            if (backupBaseOverview.getPlanetId() == planetId && backupBaseOverview.getDate().equals(date)) {
                return backupBaseOverview;
            }
        }
        throw new IllegalArgumentException("No  BackupBaseOverview for date: " + date + " planetId: " + planetId);
    }
}