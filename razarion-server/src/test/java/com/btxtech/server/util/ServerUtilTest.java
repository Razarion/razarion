package com.btxtech.server.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * 29.04.2017.
 */
public class ServerUtilTest {
    @Test
    public void generateSimpleUuid() throws Exception {
        String uuid1 = ServerUtil.generateSimpleUuid();
        String uuid2 = ServerUtil.generateSimpleUuid();
        Assert.assertNotEquals(uuid1, uuid2);

        Assert.assertTrue(uuid1.length() < 48);
        Assert.assertTrue(uuid2.length() < 48);
    }

}