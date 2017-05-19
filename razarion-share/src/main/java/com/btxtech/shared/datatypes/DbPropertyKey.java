package com.btxtech.shared.datatypes;

/**
 * Created by Beat
 * 15.05.2017.
 */
public enum DbPropertyKey {
    // System audios
    AUDIO_DIALOG_OPENED("audio.dialog.opened"),
    AUDIO_DIALOG_CLOSED("audio.dialog.closed"),
    AUDIO_QUEST_ACTIVATED("audio.quest.activated"),
    AUDIO_QUEST_PASSED("audio.quest.passed"),
    AUDIO_LEVEL_UP("audio.level.up"),
    AUDIO_BOX_PICKED("audio.box.picked"),
    AUDIO_SELECTION_CLEARED("audio.selection.cleared"),
    AUDIO_SELECTION_OWN_MULTI("audio.selection.own.multi"),
    AUDIO_SELECTION_OWN_SINGLE("audio.selection.own.single"),
    AUDIO_SELECTION_OTHER("audio.selection.other"),
    AUDIO_COMMAND_SENT("audio.command.sent"),
    AUDIO_BASE_LOST("audio.base.lost"),
    AUDIO_TERRAIN_LAND("audio.terrain.land"),
    AUDIO_TERRAIN_WATER("audio.terrain.water"),
    // Tips
    TIP_CORNER_MOVE_DURATION("tip.corner.move.duration"),
    TIP_CORNER_MOVE_DISTANCE("tip.corner.move.distance"),
    TIP_CORNER_LENGTH("tip.corner.length"),
    TIP_DEFAULT_COMMAND_SHAPE3D("tip.default.command.shape3d"),
    TIP_SELECT_CORNER_COLOR("tip.select.corner.color"),
    TIP_SELECT_SHAPE3D("tip.select.shape3d"),
    TIP_OUT_OF_VIEW_SHAPE3D("tip.outOfView.shape3d"),
    TIP_ATTACK_COMMAND_CORNER_COLOR("tip.attack.command.corner.color"),
    TIP_BASE_ITEM_PLACER_SHAPE3D("tip.baseItemPlacer.shape3d"),
    TIP_BASE_ITEM_PLACER_CORNER_COLOR("tip.baseItemPlacer.corner.color"),
    TIP_GRAB_COMMAND_CORNER_COLOR("tip.grab.command.corner.color"),
    TIP_MOVE_COMMAND_CORNER_COLOR("tip.move.command.corner.color"),
    TIP_TO_BE_FINALIZED_CORENER_COLOR("tip.toBeFinalized.corner.color"),
    TIP_WEST_LEFT_MOUSE_IMAGE("tip.west.leftMouseImage"),
    TIP_SOUTH_LEFT_MOUSE_IMAGE("tip.south.leftMouseImage"),
    TIP_DIRECTION_SHAPE3D("tip.direction.shape3d"),
    TIP_SPLASH_SCROLL_IMAGE("tip.splash.scroll.image");

    private String key;

    DbPropertyKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "DbPropertyKey{" +
                "key='" + key + '\'' +
                " ,name='" + name() + '\'' +
                '}';
    }
}
