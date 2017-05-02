package com.btxtech.server.util.facebook;

import java.io.Serializable;

/**
 * User: beat
 * Date: 22.07.12
 * Time: 14:47
 */
public class FacebookAge implements Serializable {
    private int min;

    public FacebookAge(int min) {
        this.min = min;
    }

    public int getMin() {
        return min;
    }
}
