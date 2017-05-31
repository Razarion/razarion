package com.btxtech.server.persistence.tracker;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.server.util.DateUtil;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;

import java.util.List;

/**
 * Created by Beat on 29.05.2017.
 */
public class TrackerPersistenceTest extends ArquillianBaseTest {
    @Inject
    private TrackerPersistence trackerPersistence;

    @Test
    public void testReadSessionTracking() throws Exception {
        runInTransaction(em -> {
            // TRACKER_SESSION
            em.createNativeQuery("INSERT INTO TRACKER_SESSION (language, referer, remoteAddr, remoteHost, sessionId, timeStamp, userAgent) VALUES(NULL, NULL, '184.105.247.195', 'scan-14.shadowserver.org', 'w71YXuT_6IlI0_JmgHq2WH1tUsppIMFz1gvNtBLY', '2017-05-04 09:48:39', NULL)").executeUpdate();
            em.createNativeQuery("INSERT INTO TRACKER_SESSION (language, referer, remoteAddr, remoteHost, sessionId, timeStamp, userAgent) VALUES('de-DE', NULL, '84.21.34.168', '84.21.34.168', 'cCCVV3m66Y7Xz9UwNDzMWrwdP6m3b5-giRmYnXa1', '2017-03-07 08:45:47', 'Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko')").executeUpdate();
            em.createNativeQuery("INSERT INTO TRACKER_SESSION (language, referer, remoteAddr, remoteHost, sessionId, timeStamp, userAgent) VALUES('zh-cn', NULL, '180.76.15.23', '180.76.15.23', 'tFVrprm3lrUr6sUfnqzZtbyZiHNY63jp2DIByMtj', '2017-03-08 12:14:32', 'Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)')").executeUpdate();
            // TRACKER_PAGE
            em.createNativeQuery("INSERT INTO `TRACKER_PAGE` (page, params, sessionId, timeStamp, uri) VALUES ('/main.xhtml', '', 'w71YXuT_6IlI0_JmgHq2WH1tUsppIMFz1gvNtBLY', '2017-03-05 21:44:14', '/')").executeUpdate();
            em.createNativeQuery("INSERT INTO `TRACKER_PAGE` (page, params, sessionId, timeStamp, uri) VALUES ('/facebookappstart.xhtml', 'ad_id=6069484325621||page_type=7||fbAdRazTrack=25c3a850c0f544d71cA', 'tFVrprm3lrUr6sUfnqzZtbyZiHNY63jp2DIByMtj', '2017-05-24 20:28:00', '/faces/facebookappstart.xhtml')").executeUpdate();
        });

        List<SessionTracker> sessionTrackers = trackerPersistence.readSessionTracking(createFromDate("2017-05-04 09:48:40"));
        Assert.assertEquals(0, sessionTrackers.size());

        sessionTrackers = trackerPersistence.readSessionTracking(createFromDate("2017-03-07 08:45:47"));
        Assert.assertEquals(3, sessionTrackers.size());

        SessionTracker sessionTracker1 = sessionTrackers.get(0);
        Assert.assertEquals("w71YXuT_6IlI0_JmgHq2WH1tUsppIMFz1gvNtBLY",sessionTracker1.getId());
        Assert.assertEquals("scan-14.shadowserver.org",sessionTracker1.getRemoteHost());
        Assert.assertNull(sessionTracker1.getFbAdRazTrack());

        SessionTracker sessionTracker2 = sessionTrackers.get(1);
        Assert.assertEquals("tFVrprm3lrUr6sUfnqzZtbyZiHNY63jp2DIByMtj",sessionTracker2.getId());
        Assert.assertEquals("180.76.15.23",sessionTracker2.getRemoteHost());
        Assert.assertEquals("25c3a850c0f544d71cA",sessionTracker2.getFbAdRazTrack());

        SessionTracker sessionTracker3 = sessionTrackers.get(2);
        Assert.assertEquals("cCCVV3m66Y7Xz9UwNDzMWrwdP6m3b5-giRmYnXa1",sessionTracker3.getId());
        Assert.assertEquals("84.21.34.168",sessionTracker3.getRemoteHost());
        Assert.assertNull(sessionTracker3.getFbAdRazTrack());

        sessionTrackers = trackerPersistence.readSessionTracking(createFromDate("2017-05-04 09:48:39"));
        Assert.assertEquals(1, sessionTrackers.size());

        sessionTracker1 = sessionTrackers.get(0);
        Assert.assertEquals("w71YXuT_6IlI0_JmgHq2WH1tUsppIMFz1gvNtBLY",sessionTracker1.getId());
        Assert.assertEquals("scan-14.shadowserver.org",sessionTracker1.getRemoteHost());
        Assert.assertNull(sessionTracker1.getFbAdRazTrack());

        runInTransaction(em -> {
            // TRACKER_SESSION
            em.createQuery("DELETE FROM SessionTrackerEntity ").executeUpdate();
            em.createQuery("DELETE FROM PageTrackerEntity ").executeUpdate();
        });

    }

    private SearchConfig createFromDate(String fromDateString) {
        return new SearchConfig().setFromDate(DateUtil.fromDbTimeString(fromDateString));
    }
}