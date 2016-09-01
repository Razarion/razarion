package com.btxtech.client.cockpit;

/**
 * Created by Beat
 * 10.07.2016.
 */
public class ZIndexConstants {
    public static final int WEBGL_CANVAS = 1;
    public static final int MAIN_COCKPIT = WEBGL_CANVAS + 1;
    public static final int STORY_COVER = MAIN_COCKPIT + 1;
    public static final int EDITOR_SIDE_BAR = STORY_COVER + 1;
    public static final int QUEST_SIDE_BAR = EDITOR_SIDE_BAR + 1;
    public static final int DIALOG = QUEST_SIDE_BAR + 1;
}
