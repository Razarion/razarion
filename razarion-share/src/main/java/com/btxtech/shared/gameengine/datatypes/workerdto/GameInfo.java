package com.btxtech.shared.gameengine.datatypes.workerdto;

import java.util.List;

/**
 * Created by Beat
 * 08.01.2017.
 */
public class GameInfo {
    private int resources;
    private int xpFromKills;
    private int houseSpace;

    public int getResources() {
        return resources;
    }

    public void setResources(int resources) {
        this.resources = resources;
    }

    public int getXpFromKills() {
        return xpFromKills;
    }

    public void setXpFromKills(int xpFromKills) {
        this.xpFromKills = xpFromKills;
    }

    public int getHouseSpace() {
        return houseSpace;
    }

    public void setHouseSpace(int houseSpace) {
        this.houseSpace = houseSpace;
    }
}
