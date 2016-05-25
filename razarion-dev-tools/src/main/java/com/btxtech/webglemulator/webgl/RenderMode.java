package com.btxtech.webglemulator.webgl;

/**
 * Created by Beat
 * 25.05.2016.
 */
public enum RenderMode {
    TRIANGLES(9),
    LINES(6);

    private int doubleCount;

    RenderMode(int doubleCount) {
        this.doubleCount = doubleCount;
    }

    public int getDoubleCount() {
        return doubleCount;
    }
}
