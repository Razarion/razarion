package com.btxtech.shared.datatypes;

import org.dominokit.jackson.annotation.JSONMapper;

/**
 * Created by Beat
 * on 10.09.2017.
 */
@JSONMapper
public class LifecyclePacket {
    public enum Type {
        HOLD,        // All timers, rest consumers, connection etc stop. Only SystemConnection open. Show dialog (blocking) to user (E.g. Server warm restarting)
        RESTART,    // Everything stopped. Show dialog (blocking) to user. (E.g. other connection opened)
        PLANET_RESTART_WARM, // Warm restart
        PLANET_RESTART_COLD, // Cold restart (reload)
        // The whole server (JVM) goes down in restartInSeconds. The game keeps running until then,
        // the client only shows a countdown. Sent by the deploy script and by the shutdown hook.
        SERVER_RESTART_ANNOUNCEMENT
    }

    public enum Dialog {
        PLANET_RESTART
    }

    private Type type;
    private Dialog dialog;
    // Only set for SERVER_RESTART_ANNOUNCEMENT: seconds left until the server goes down.
    private Integer restartInSeconds;

    public Type getType() {
        return type;
    }

    public LifecyclePacket setType(Type type) {
        this.type = type;
        return this;
    }

    public Dialog getDialog() {
        return dialog;
    }

    public LifecyclePacket setDialog(Dialog dialog) {
        this.dialog = dialog;
        return this;
    }

    public Integer getRestartInSeconds() {
        return restartInSeconds;
    }

    public LifecyclePacket setRestartInSeconds(Integer restartInSeconds) {
        this.restartInSeconds = restartInSeconds;
        return this;
    }
}
