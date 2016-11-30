package com.btxtech.uiservice.mouse;

/**
 * User: beat
 * Date: 01.06.2010
 * Time: 20:31:59
 * <p/>
 * Make an ico file with gimp and rename it to cur
 * Make cur file with RealWorld Cursor Editor 2012.1: http://www.rw-designer.com/cursor-maker
 */
public enum CursorType {
    GO("go", 15, 16, "CROSSHAIR", "nogo", 15, 16, "POINTER"),
    ATTACK("attack", 15, 16, "CROSSHAIR", "noattack", 15, 16, "POINTER"),
    COLLECT("collect", 15, 16, "CROSSHAIR", "nocollect", 15, 16, "POINTER"),
    LOAD("load", 16, 30, "S_RESIZE", "noload", 16, 30, "POINTER"),
    UNLOAD("unload", 15, 1, "N_RESIZE", "nounload", 15, 1, "POINTER"),
    FINALIZE_BUILD("finalizebuild", 15, 16, "POINTER", "nofinalizebuild", 15, 16, "DEFAULT"),
    PICKUP("pickup", 15, 16, "CROSSHAIR", "nopickup", 15, 16, "POINTER");

    private String name;
    private String alternativeDefault;
    private String noName;
    private String noAlternativeDefault;
    private int hotSpotX;
    private int hotSpotY;
    private int hotSpotNoX;
    private int hotSpotNoY;

    CursorType(String name, int hotSpotX, int hotSpotY, String alternativeDefault, String noName, int hotSpotNoX, int hotSpotNoY, String noAlternativeDefault) {
        this.name = name;
        this.hotSpotX = hotSpotX;
        this.hotSpotY = hotSpotY;
        this.alternativeDefault = alternativeDefault;
        this.hotSpotNoX = hotSpotNoX;
        this.hotSpotNoY = hotSpotNoY;
        this.noName = noName;
        this.noAlternativeDefault = noAlternativeDefault;
    }

    public String getName() {
        return name;
    }

    public String getName(boolean allowed) {
        if (allowed) {
            return name;
        } else {
            return noName;
        }
    }

    public String getAlternativeDefault(boolean allowed) {
        if (allowed) {
            return alternativeDefault;
        } else {
            return noAlternativeDefault;
        }
    }

    public int getHotSpotX(boolean allowed) {
        if (allowed) {
            return hotSpotX;
        } else {
            return hotSpotNoX;
        }
    }

    public int getHotSpotY(boolean allowed) {
        if (allowed) {
            return hotSpotY;
        } else {
            return hotSpotNoY;
        }
    }
}
