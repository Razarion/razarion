package com.btxtech.uiservice.system.boot;

/**
 * User: Razarion contributors
 * Date: 06.12.2010
 * Time: 19:36:23
 */
public interface StartupTaskEnum {
    AbstractStartupTask createAbstractStartupTask(BootContext bootContext);

    String name();

    default StartupTaskEnum[] getWaitForBackgroundTasks() {
        return null;
    }
}
