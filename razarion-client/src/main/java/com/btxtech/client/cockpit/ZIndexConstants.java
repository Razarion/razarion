package com.btxtech.client.cockpit;

import static elemental2.dom.CSSProperties.*;

/**
 * Created by Beat
 * 10.07.2016.
 */
public class ZIndexConstants {
    private static int counter = 1;
    public static final ZIndexUnionType WEBGL_CANVAS = ZIndexUnionType.of(counter++);
    public static final ZIndexUnionType MAIN_COCKPIT = ZIndexUnionType.of(counter++);
    public static final ZIndexUnionType CHAT_COCKPIT = ZIndexUnionType.of(counter++);
    public static final ZIndexUnionType TOP_RIGHT_BAR = ZIndexUnionType.of(counter++);
    public static final ZIndexUnionType ITEM_COCKPIT = ZIndexUnionType.of(counter++);
    public static final ZIndexUnionType STORY_COVER = ZIndexUnionType.of(counter++);
    public static final ZIndexUnionType DIALOG = ZIndexUnionType.of(counter++);
    public static final ZIndexUnionType TIP = ZIndexUnionType.of(counter++);
    public static final ZIndexUnionType LOADING_COVER = ZIndexUnionType.of(counter++); // Edit in razarion.css
    public static final ZIndexUnionType EMPTY_COVER = ZIndexUnionType.of(counter++); // Edit in razarion.css
    public static final ZIndexUnionType PLAYBACK_MOUSE_CANVAS = ZIndexUnionType.of(counter++);
    public static final ZIndexUnionType PLAYBACK_SIDE_BAR = ZIndexUnionType.of(counter++);

}
