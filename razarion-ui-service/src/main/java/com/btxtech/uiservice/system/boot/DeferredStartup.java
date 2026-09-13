package com.btxtech.uiservice.system.boot;

/**
 * User: Razarion contributors
 * Date: 04.12.2010
 * Time: 13:01:44
 */
public class DeferredStartup {
    private boolean isDeferred;
    private boolean isBackground;
    private AbstractStartupTask task;
    private Boot boot;
    private boolean isFinished = false;

    public DeferredStartup(AbstractStartupTask task, Boot boot) {
        this.task = task;
        this.boot = boot;
    }

    public void setDeferred() {
        isDeferred = true;
    }

    public void finished() {
        isFinished = true;
        task.correctDeferredDuration();
        boot.onTaskFinished(task, this);
    }

    public void failed(Throwable t) {
        isFinished = true;
        task.correctDeferredDuration();
        boot.raiseAlarmIfNeeded(t);
        boot.onTaskFailed(task, t);
    }

    public void failed(String error) {
        isFinished = true;
        task.correctDeferredDuration();
        boot.onTaskFailed(task, error, null);
    }

    public void setBackground() {
        isBackground = true;
    }

    public boolean isDeferred() {
        return isDeferred;
    }

    public boolean isBackground() {
        return isBackground;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public StartupTaskEnum getStartupTaskEnum() {
        return task.getTaskEnum();
    }
}
