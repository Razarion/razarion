package com.btxtech.shared.gameengine;

/**
 * Created by Beat
 * 04.01.2017.
 */
public class GameEngineControlPackage {
    public enum Command {
        INITIALIZE,
        INITIALIZED,
        START,
        STARTED,
        START_BOTS,
        EXECUTE_BOT_COMMANDS,
        CREATE_RESOURCES,
        CREATE_HUMAN_BASE_WITH_BASE_ITEM
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
