package com.btxtech.shared.datatypes;

import java.util.Arrays;

import static com.btxtech.shared.datatypes.DbPropertyType.*;

/**
 * Created by Beat
 * 15.05.2017.
 */
public enum DbPropertyKey {
    // System audios
    AUDIO_DIALOG_OPENED(AUDIO, "audio.dialog.opened"),
    AUDIO_DIALOG_CLOSED(AUDIO, "audio.dialog.closed"),
    AUDIO_QUEST_ACTIVATED(AUDIO, "audio.quest.activated"),
    AUDIO_QUEST_PASSED(AUDIO, "audio.quest.passed"),
    AUDIO_LEVEL_UP(AUDIO, "audio.level.up"),
    AUDIO_BOX_PICKED(AUDIO, "audio.box.picked"),
    AUDIO_SELECTION_CLEARED(AUDIO, "audio.selection.cleared"),
    AUDIO_SELECTION_OWN_MULTI(AUDIO, "audio.selection.own.multi"),
    AUDIO_SELECTION_OWN_SINGLE(AUDIO, "audio.selection.own.single"),
    AUDIO_SELECTION_OTHER(AUDIO, "audio.selection.other"),
    AUDIO_COMMAND_SENT(AUDIO, "audio.command.sent"),
    AUDIO_BASE_LOST(AUDIO, "audio.base.lost"),
    AUDIO_TERRAIN_LAND(AUDIO, "audio.terrain.land"),
    AUDIO_TERRAIN_WATER(AUDIO, "audio.terrain.water"),
    // Item
    ITEM_SELECTION_MATERIAL(BABYLON_MATERIAL, "item.selection.node-material"),
    ITEM_HEALTH_BAR_NODE_MATERIAL(BABYLON_MATERIAL, "item.health-bar.node-material"),
    ITEM_PROGRESS_BAR_NODE_MATERIAL(BABYLON_MATERIAL, "item.progress-bar.node-material"),
    // Tips
    TIP_CORNER_MOVE_DURATION(INTEGER, "tip.corner.move.duration"),
    TIP_CORNER_MOVE_DISTANCE(DOUBLE, "tip.corner.move.distance"),
    TIP_CORNER_LENGTH(DOUBLE, "tip.corner.length"),
    TIP_DEFAULT_COMMAND_SHAPE3D(UNKNOWN, "tip.default.command.shape3d"),
    TIP_SELECT_CORNER_COLOR(COLOR, "tip.select.corner.color"),
    TIP_SELECT_SHAPE3D(UNKNOWN, "tip.select.shape3d"),
    TIP_OUT_OF_VIEW_SHAPE3D(UNKNOWN, "tip.outOfView.shape3d"),
    TIP_ATTACK_COMMAND_CORNER_COLOR(COLOR, "tip.attack.command.corner.color"),
    TIP_BASE_ITEM_PLACER_SHAPE3D(UNKNOWN, "tip.baseItemPlacer.shape3d"),
    TIP_BASE_ITEM_PLACER_CORNER_COLOR(COLOR, "tip.baseItemPlacer.corner.color"),
    TIP_GRAB_COMMAND_CORNER_COLOR(COLOR, "tip.grab.command.corner.color"),
    TIP_MOVE_COMMAND_CORNER_COLOR(COLOR, "tip.move.command.corner.color"),
    TIP_TO_BE_FINALIZED_CORNER_COLOR(COLOR, "tip.toBeFinalized.corner.color"),
    TIP_WEST_LEFT_MOUSE_IMAGE(IMAGE, "tip.west.leftMouseImage"),
    TIP_SOUTH_LEFT_MOUSE_IMAGE(IMAGE, "tip.south.leftMouseImage"),
    TIP_DIRECTION_SHAPE3D(UNKNOWN, "tip.direction.shape3d"),
    TIP_SCROLL_DIALOG_KEYBOARD_IMAGE(IMAGE, "tip.scroll.dialog.keyboard.image"),
    // Quest in game visualization
    QUEST_IN_GAME_VISUALIZATION_NODES_MATERIAL(BABYLON_MATERIAL, "questInGameVisualization.nodes-material"),
    QUEST_IN_GAME_VISUALIZATION_PLACE_NODES_MATERIAL(BABYLON_MATERIAL, "questInGameVisualization.place.nodes-material"),
    QUEST_IN_GAME_VISUALIZATION_RADIUS(DOUBLE, "questInGameVisualization.radius"),
    QUEST_IN_GAME_VISUALIZATION_MOVE_DURATION(DOUBLE, "questInGameVisualization.move.duration"),
    QUEST_IN_GAME_VISUALIZATION_OUT_OF_VIEW_NODES_MATERIAL(BABYLON_MATERIAL, "questInGameVisualization.outOfView.nodes-material"),
    QUEST_IN_GAME_VISUALIZATION_OUT_OF_VIEW_SIZE(DOUBLE, "questInGameVisualization.outOfView.size"),
    QUEST_IN_GAME_VISUALIZATION_OUT_DISTANCE_FROM_CAMERA(DOUBLE, "questInGameVisualization.outOfView.distance-from-camera"),
    QUEST_IN_GAME_VISUALIZATION_CORNER_HARVEST_COLOR(COLOR, "questInGameVisualization.corner.harvest.color"),
    QUEST_IN_GAME_VISUALIZATION_CORNER_ATTACK_COLOR(COLOR, "questInGameVisualization.corner.attack.color"),
    QUEST_IN_GAME_VISUALIZATION_CORNER_PICK_COLOR(COLOR, "questInGameVisualization.corner.pick.color");

    private final String key;
    private final DbPropertyType dbPropertyType;
    DbPropertyKey(DbPropertyType dbPropertyType, String key) {
        this.dbPropertyType = dbPropertyType;
        this.key = key;
    }

    public static DbPropertyKey fromKey(String key) {
        return Arrays.stream(values())
                .filter(dbPropertyKey -> dbPropertyKey.getKey().equals(key))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public DbPropertyType getDbPropertyType() {
        return dbPropertyType;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "DbPropertyKey{" +
                "dbPropertyType='" + dbPropertyType + '\'' +
                " ,key='" + key + '\'' +
                " ,name='" + name() + '\'' +
                '}';
    }
}
