package com.btxtech.server.persistence.item;

import com.btxtech.server.ServerArquillianBaseTest;
import com.btxtech.server.util.DateUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * on 08.01.2018.
 */
public class ItemTrackerAccessTest extends ServerArquillianBaseTest {
    @Inject
    private ItemTrackerAccess itemTrackerAccess;

    @Before
    public void before() throws Exception {
        setupPlanets();
        clearMongoDb();
        fillBackupInfoMongoDb("server_item_tracking", "/mongodb/ServerItemTracking.json", ItemTracking.class);
    }

    @After
    public void after() throws Exception {
        cleanUsers();
        cleanPlanets();
        clearMongoDb();
    }

    @Test
    public void testReadDate() {
        // No date, count = 1
        List<ItemTracking> result = itemTrackerAccess.read(new ItemTrackingSearch().setCount(1));
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(DateUtil.fromJsonTimeString("2018-01-01 15:00:06.000"), result.get(0).getTimeStamp());
        // No date
        result = itemTrackerAccess.read(new ItemTrackingSearch());
        Assert.assertEquals(6, result.size());
        Assert.assertEquals(DateUtil.fromJsonTimeString("2018-01-01 15:00:06.000"), result.get(0).getTimeStamp());
        Assert.assertEquals(DateUtil.fromJsonTimeString("2018-01-01 15:00:05.000"), result.get(1).getTimeStamp());
        Assert.assertEquals(DateUtil.fromJsonTimeString("2018-01-01 15:00:04.000"), result.get(2).getTimeStamp());
        Assert.assertEquals(DateUtil.fromJsonTimeString("2018-01-01 15:00:03.000"), result.get(3).getTimeStamp());
        Assert.assertEquals(DateUtil.fromJsonTimeString("2018-01-01 15:00:02.000"), result.get(4).getTimeStamp());
        Assert.assertEquals(DateUtil.fromJsonTimeString("2018-01-01 15:00:01.000"), result.get(5).getTimeStamp());
        // From date
        result = itemTrackerAccess.read(new ItemTrackingSearch().setFrom(DateUtil.fromJsonTimeString("2018-01-01 15:00:04.000")));
        Assert.assertEquals(3, result.size());
        Assert.assertEquals(DateUtil.fromJsonTimeString("2018-01-01 15:00:06.000"), result.get(0).getTimeStamp());
        Assert.assertEquals(DateUtil.fromJsonTimeString("2018-01-01 15:00:05.000"), result.get(1).getTimeStamp());
        Assert.assertEquals(DateUtil.fromJsonTimeString("2018-01-01 15:00:04.000"), result.get(2).getTimeStamp());
        // To date
        result = itemTrackerAccess.read(new ItemTrackingSearch().setTo(DateUtil.fromJsonTimeString("2018-01-01 15:00:03.000")));
        Assert.assertEquals(3, result.size());
        Assert.assertEquals(DateUtil.fromJsonTimeString("2018-01-01 15:00:03.000"), result.get(0).getTimeStamp());
        Assert.assertEquals(DateUtil.fromJsonTimeString("2018-01-01 15:00:02.000"), result.get(1).getTimeStamp());
        Assert.assertEquals(DateUtil.fromJsonTimeString("2018-01-01 15:00:01.000"), result.get(2).getTimeStamp());
        // From and, to date
        result = itemTrackerAccess.read(new ItemTrackingSearch().setFrom(DateUtil.fromJsonTimeString("2018-01-01 15:00:02.000")).setTo(DateUtil.fromJsonTimeString("2018-01-01 15:00:05.000")));
        Assert.assertEquals(4, result.size());
        Assert.assertEquals(DateUtil.fromJsonTimeString("2018-01-01 15:00:05.000"), result.get(0).getTimeStamp());
        Assert.assertEquals(DateUtil.fromJsonTimeString("2018-01-01 15:00:04.000"), result.get(1).getTimeStamp());
        Assert.assertEquals(DateUtil.fromJsonTimeString("2018-01-01 15:00:03.000"), result.get(2).getTimeStamp());
        Assert.assertEquals(DateUtil.fromJsonTimeString("2018-01-01 15:00:02.000"), result.get(3).getTimeStamp());
    }

}