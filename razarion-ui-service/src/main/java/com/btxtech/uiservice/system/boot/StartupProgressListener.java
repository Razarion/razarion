package com.btxtech.uiservice.system.boot;

import java.util.List;

/**
 * User: beat
 * Date: 18.02.2011
 * Time: 22:53:59
 */
public interface StartupProgressListener {
    default void onStart(StartupSeq startupSeq) {
    }

    default void onNextTask(StartupTaskEnum taskEnum) {
    }

    default void onTaskFinished(AbstractStartupTask task) {
    }

    default void onTaskFailed(AbstractStartupTask task, String error, Throwable t) {
    }

    default void onStartupFinished(List<StartupTaskInfo> taskInfo, long totalTime) {
    }

    default void onStartupFailed(List<StartupTaskInfo> taskInfo, long totalTime) {
    }
}
