package com.btxtech.shared.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * on 12.01.2019.
 */
public class SignalGeneratorTest {

    @Test
    public void triangle() {
        for (long millis = 5000; millis < 30000; millis++) {
            double value = SignalGenerator.triangle(millis, 1000, 0);
            System.out.println(millis + "->" + value);
        }
        Assert.fail("TODO: verify result");
    }

    @Test
    public void sawtooth1000() {
        for (long millis = 5000; millis < 30000; millis++) {
            double value = SignalGenerator.sawtooth(millis, 1000, 0);
            System.out.println(millis + "->" + value);
        }
        Assert.fail("TODO: verify result");
    }

    @Test
    public void sawtooth50000() {
        for (long millis = 5000; millis < 100000; millis++) {
            double value = SignalGenerator.sawtooth(millis, 50000, 0);
            System.out.println(millis + "->" + value);
        }
        Assert.fail("TODO: verify result");
    }

    @Test
    public void sinus() {
        for (long millis = 5000; millis < 30000; millis++) {
            double value = SignalGenerator.sinus(millis, 1000, 0);
            System.out.println(millis + "->" + value);
        }
        Assert.fail("TODO: verify result");
    }
}