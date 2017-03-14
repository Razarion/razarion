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
        INITIALIZED,
        INITIALISING_FAILED,
        START,
        TICK_UPDATE,
        PERFMON_REQUEST,
        PERFMON_RESPONSE,
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
        // Resources
        CREATE_RESOURCES,
        RESOURCE_CREATED,
        RESOURCE_DELETED,
        // Base
        CREATE_HUMAN_BASE_WITH_BASE_ITEM,
        BASE_CREATED,
        BASE_DELETED,
        SPAWN_BASE_ITEMS,
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
        // Projectile
        PROJECTILE_FIRED,
        PROJECTILE_DETONATION,
        // Terrain
        SINGLE_Z_TERRAIN,
        SINGLE_Z_TERRAIN_ANSWER,
        SINGLE_Z_TERRAIN_ANSWER_FAIL,
        TERRAIN_PICK_RAY,
        TERRAIN_PICK_RAY_ANSWER,
        TERRAIN_PICK_RAY_ANSWER_FAIL,
        TERRAIN_OVERLAP,
        TERRAIN_OVERLAP_ANSWER,
        TERRAIN_OVERLAP_TYPE,
        TERRAIN_OVERLAP_TYPE_ANSWER
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
