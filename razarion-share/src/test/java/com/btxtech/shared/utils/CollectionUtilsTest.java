package com.btxtech.shared.utils;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class CollectionUtilsTest {
    @Test
    public void convertToUnsignedIntArray() {
        byte[] byteArray = {
                0, 0,
                1, 0,
                2, 0,
                (byte) 128, 0,
                (byte) 255, 0,
                0, 1,
                0, 2,
                1, 1,
                (byte) 255, (byte) 255,
        };
        int[] result = CollectionUtils.convertToUnsignedIntArray(byteArray);
        assertThat(result, equalTo(new int[]{
                0,
                1,
                2,
                128,
                255,
                256,
                512,
                257,
                65535
        }));
    }

}