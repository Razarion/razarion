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

    /**
     * A deferred task is still waiting for its callback after {@code waitedMillis}. The startup
     * has not failed - the task may still complete - but it is stuck long enough to be worth
     * reporting.
     */
    default void onTaskTimeout(AbstractStartupTask task, long waitedMillis) {
    }

    default void onStartupFinished(List<StartupTaskInfo> taskInfo, long totalTime) {
    }

    default void onStartupFailed(List<StartupTaskInfo> taskInfo, long totalTime) {
    }
}
