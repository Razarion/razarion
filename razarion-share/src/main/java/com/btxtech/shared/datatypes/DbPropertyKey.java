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
    AUDIO_BASE_LOST(AUDIO, "audio.base.lost"),
    AUDIO_TERRAIN_LAND(AUDIO, "audio.terrain.land"),
    AUDIO_TERRAIN_WATER(AUDIO, "audio.terrain.water"),
    // Item
    ITEM_SELECTION_MATERIAL(BABYLON_MATERIAL, "item.selection.node-material"),
    // Quest in game visualization
    QUEST_IN_GAME_VISUALIZATION_NODES_MATERIAL(BABYLON_MATERIAL, "questInGameVisualization.nodes-material"),
    QUEST_IN_GAME_VISUALIZATION_PLACE_NODES_MATERIAL(BABYLON_MATERIAL, "questInGameVisualization.place.nodes-material"),
    QUEST_IN_GAME_VISUALIZATION_RADIUS(DOUBLE, "questInGameVisualization.radius"),
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
