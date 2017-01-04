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
        CREATE_RESOURCES
    }

    private Command command;
    private Object data;

    public GameEngineControlPackage(Command command, Object data) {
        this.command = command;
        this.data = data;
    }

    public Command getCommand() {
        return command;
    }

    public Object getData() {
        return data;
    }
}
