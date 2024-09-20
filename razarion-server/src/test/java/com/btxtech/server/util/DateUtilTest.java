package com.btxtech.server.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * Created by Beat
 * 03.05.2017.
 */
public class DateUtilTest {
    @Test
    public void toFacebookTimeString() throws Exception {
        Assert.assertEquals("2017-05-03 00:54:06+02", DateUtil.toFacebookTimeString(new Date(1493765646803L)));
    }

}