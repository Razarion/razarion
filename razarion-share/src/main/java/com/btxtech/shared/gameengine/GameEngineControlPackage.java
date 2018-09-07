package com.btxtech.shared.gameengine;

/**
 * Created by Beat
 * 04.01.2017.
 */
public class GameEngineControlPackage {
    public enum Command {
        // System
        LOADED,
        INITIALIZE,
        INITIALIZE_WARM,
        INITIALIZED,
        INITIALISING_FAILED,
        START,
        STOP_REQUEST,
        STOP_RESPONSE,
        TICK_UPDATE_REQUEST,
        TICK_UPDATE_RESPONSE,
        TICK_UPDATE_RESPONSE_FAIL,
        PERFMON_REQUEST,
        PERFMON_RESPONSE,
        CONNECTION_LOST,
        INITIAL_SLAVE_SYNCHRONIZED,
        // Bot
        START_BOTS,
        EXECUTE_BOT_COMMANDS,
        // Commands
        COMMAND_ATTACK,
        COMMAND_FINALIZE_BUILD,
        COMMAND_BUILD,
        COMMAND_FABRICATE,
        COMMAND_HARVEST,
        COMMAND_MOVE,
        COMMAND_PICK_BOX,
        COMMAND_LOAD_CONTAINER,
        COMMAND_UNLOAD_CONTAINER,
        // Resources
        CREATE_RESOURCES,
        RESOURCE_CREATED,
        RESOURCE_DELETED,
        // Energy
        ENERGY_CHANGED,
        // Base
        CREATE_HUMAN_BASE_WITH_BASE_ITEM,
        BASE_CREATED,
        BASE_DELETED,
        BASE_UPDATED,
        USE_INVENTORY_ITEM,
        UPDATE_LEVEL,
        // Base items
        SYNC_ITEM_START_SPAWNED,
        SYNC_ITEM_IDLE,
        // Boxes
        CREATE_BOXES,
        BOX_CREATED,
        BOX_DELETED,
        BOX_PICKED,
        // Quest
        ACTIVATE_QUEST,
        QUEST_PASSED,
        QUEST_PROGRESS,
        // Projectile
        PROJECTILE_FIRED,
        PROJECTILE_DETONATION,
        // Terrain
        SINGLE_Z_TERRAIN,
        SINGLE_Z_TERRAIN_ANSWER,
        SINGLE_Z_TERRAIN_ANSWER_FAIL,
        TERRAIN_TILE_REQUEST,
        TERRAIN_TILE_RESPONSE,
        // Playback
        PLAYBACK_PLAYER_BASE,
        PLAYBACK_SYNC_ITEM_DELETED,
        PLAYBACK_SYNC_BASE_ITEM,
        PLAYBACK_SYNC_RESOURCE_ITEM,
        PLAYBACK_SYNC_BOX_ITEM,
        // Cockpit
        SELL_ITEMS;
    }

    private Command command;
    private Object[] data;

    public GameEngineControlPackage(Command command, Object... data) {
        this.command = command;
        this.data = data;
    }

    public Command getCommand() {
        return command;
    }

    public Object getSingleData() {
        return data[0];
    }

    public Object getData(int index) {
        return data[index];
    }
}
