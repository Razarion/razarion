package com.btxtech.server.persistence.backup;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.server.util.DateUtil;
import com.btxtech.shared.gameengine.datatypes.BackupBaseInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

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
        Assert.assertEquals(1, backupBaseInfo.getSyncBaseItemInfos().size());
    }

    @Test
    public void testLoadAllBackupBaseOverviews() {
        List<BackupBaseOverview> allBackups = planetBackupMongoDb.loadAllBackupBaseOverviews();
        Assert.assertEquals(4, allBackups.size());
    }

    @Test
    public void testGetAllBackups() {
        BackupBaseInfo backupBaseInfo = planetBackupMongoDb.loadBackup(new BackupBaseOverview().setPlanetId(2).setDate(DateUtil.fromJsonTimeString("2017-09-03 19:59:04.648")));
        Assert.assertEquals(2, backupBaseInfo.getPlanetId());
        Assert.assertEquals(DateUtil.fromJsonTimeString("2017-09-03 19:59:04.648"), backupBaseInfo.getDate());
        Assert.assertEquals(1, backupBaseInfo.getPlayerBaseInfos().size());
        Assert.assertEquals(1, backupBaseInfo.getSyncBaseItemInfos().size());
    }
}