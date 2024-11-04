package com.btxtech.shared.utils;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Beat
 * on 28.02.2018.
 */
public class ExceptionUtilTest {
    @Test
    public void getMostInnerThrowable() {
        Exception e1 = new Exception("E1");
        Exception e2 = new Exception("E2", null);
        Throwable t1 = new Throwable("T1", e1);

        Assert.assertEquals(e1, ExceptionUtil.getMostInnerThrowable(e1));
        Assert.assertEquals(e2, ExceptionUtil.getMostInnerThrowable(e2));
        Assert.assertEquals(e1, ExceptionUtil.getMostInnerThrowable(t1));
    }

}