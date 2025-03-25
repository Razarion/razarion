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
        PLANET_RESTART_COLD // Cold restart (reload)
    }

    public enum Dialog {
        PLANET_RESTART
    }

    private Type type;
    private Dialog dialog;

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
}
