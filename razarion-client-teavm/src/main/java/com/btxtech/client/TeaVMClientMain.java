package com.btxtech.client;

import com.btxtech.client.di.DaggerClientComponent;
import com.btxtech.client.di.ClientComponent;
import com.btxtech.client.jso.JsConsole;

public class TeaVMClientMain {

    public static void main(String[] args) {
        try {
            JsConsole.log("TeaVM Client initializing...");

            ClientComponent component = DaggerClientComponent.create();
            component.gwtAngularService().init();
            component.lifecycleService().startCold();

            JsConsole.log("TeaVM Client initialized successfully");
        } catch (Throwable t) {
            JsConsole.error("TeaVM Client initialization failed: " + t.getMessage());
            throw t;
        }
    }
}
