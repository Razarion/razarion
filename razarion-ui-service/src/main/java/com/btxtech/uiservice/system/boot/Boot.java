package com.btxtech.uiservice.system.boot;

import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.utils.ExceptionUtil;
import com.btxtech.shared.utils.MathHelper;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: 04.12.2010
 * Time: 10:56:33
 */
public abstract class Boot {
    private Logger logger = Logger.getLogger(Boot.class.getName());
    private Collection<StartupProgressListener> listeners = new ArrayList<>();
    private List<AbstractStartupTask> startupList = new ArrayList<>();
    private List<DeferredStartup> deferredStartups = new ArrayList<>();
    private List<DeferredStartup> deferredBackgroundStartups = new ArrayList<>();
    private List<AbstractStartupTask> finishedTasks = new ArrayList<>();
    private AbstractStartupTask waitingTask;
    private boolean failed;
    private String gameSessionUuid;
    @Inject
    private Instance<AbstractStartupTask> taskInstance;
    @Inject
    private ExceptionHandler exceptionHandler;

    protected abstract StartupSeq getWarm();

    public void addStartupProgressListener(StartupProgressListener startupProgressListener) {
        listeners.add(startupProgressListener);
    }

    public void removeStartupProgressListener(StartupProgressListener startupProgressListener) {
        listeners.remove(startupProgressListener);
    }

    public void start(StartupSeq startupSeq) {
        gameSessionUuid = MathHelper.generateUuid();
        failed = false;
        for (StartupProgressListener listener : listeners) {
            listener.onStart(startupSeq);
        }
        setupStartupSeq(startupSeq);
        runNextTask();
    }

    public void startWarm() {
        start(getWarm());
    }

    private void runNextTask() {
        if (failed) {
            return;
        }
        if (startupList.isEmpty()) {
            onStartupFinish();
        } else {
            AbstractStartupTask task = startupList.remove(0);
            task.removeFinishedBackgroundTasks(deferredBackgroundStartups);
            if (task.isWaitingForBackgroundTasks()) {
                waitingTask = task;
            } else {
                runTask(task);
            }
        }
    }

    private void runTask(AbstractStartupTask task) {
        DeferredStartup deferredStartup = new DeferredStartup(task, this);
        for (StartupProgressListener listener : listeners) {
            try {
                listener.onNextTask(task.getTaskEnum());
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        }
        try {
            task.start(deferredStartup);
        } catch (Throwable t) {
            onTaskFailed(task, t);
            return;
        }
        if (deferredStartup.isDeferred()) {
            if (deferredStartup.isBackground()) {
                deferredBackgroundStartups.add(deferredStartup);
            }
            if (deferredStartup.isFinished()) {
                onTaskFinished(task);
            } else {
                deferredStartups.add(deferredStartup);
            }

            if (deferredStartup.isBackground()) {
                runNextTask();
            }
        } else {
            onTaskFinished(task);
        }
    }

    private void cleanup() {
        finishedTasks.clear();
        deferredBackgroundStartups.clear();
        startupList.clear();
        deferredStartups.clear();
    }

    private List<StartupTaskInfo> createTaskInfo(AbstractStartupTask failedTask, String error) {
        List<StartupTaskInfo> infos = new ArrayList<>();
        for (AbstractStartupTask finishedTask : finishedTasks) {
            infos.add(finishedTask.createStartupTaskInfo());
        }
        if (failedTask != null && error != null) {
            StartupTaskInfo failedTaskInfo = failedTask.createStartupTaskInfo();
            failedTaskInfo.setErrorText(error);
            infos.add(failedTaskInfo);
        }
        return infos;
    }


    private void onStartupFinish() {
        if (failed) {
            return;
        }
        if (deferredStartups.isEmpty()) {
            if (!listeners.isEmpty()) {
                long totalTime = finishedTasks.isEmpty() ? 0 : System.currentTimeMillis() - finishedTasks.get(0).getStartTime();
                List<StartupTaskInfo> startupTaskInfos = createTaskInfo(null, null);
                for (StartupProgressListener listener : listeners) {
                    listener.onStartupFinished(startupTaskInfos, totalTime);
                }
            }
            cleanup();
        }
    }


    void onTaskFinished(AbstractStartupTask task, DeferredStartup deferredStartup) {
        if (deferredStartups.remove(deferredStartup)) {
            onTaskFinished(task);
        }
    }

    void onTaskFinished(AbstractStartupTask abstractStartupTask) {
        if (failed) {
            return;
        }
        for (StartupProgressListener listener : listeners) {
            listener.onTaskFinished(abstractStartupTask);
        }

        finishedTasks.add(abstractStartupTask);
        if (waitingTask != null) {
            waitingTask.removeFinishedBackgroundTask(abstractStartupTask.getTaskEnum());
            if (!waitingTask.isWaitingForBackgroundTasks()) {
                AbstractStartupTask nextTask = waitingTask;
                waitingTask = null;
                runTask(nextTask);
            }
        } else if (!abstractStartupTask.isBackground()) {
            runNextTask();
        } else if (startupList.isEmpty()) {
            onStartupFinish();
        }
    }

    void onTaskFailed(AbstractStartupTask abstractStartupTask, String error, Throwable t) {
        if (failed) {
            return;
        }
        failed = true;
        if (listeners.isEmpty()) {
            logger.severe("Boot.onTaskFailed(): " + error);
        } else {
            for (StartupProgressListener listener : listeners) {
                listener.onTaskFailed(abstractStartupTask, error, t);
            }

            long totalTime = System.currentTimeMillis() - (finishedTasks.isEmpty() ? abstractStartupTask.getStartTime() : finishedTasks.get(0).getStartTime());
            List<StartupTaskInfo> startupTaskInfos = createTaskInfo(abstractStartupTask, error);
            for (StartupProgressListener listener : listeners) {
                listener.onStartupFailed(startupTaskInfos, totalTime);
            }
        }
        cleanup();
    }


    public void onFallback(Alarm.Type alarmType) {
        for (StartupProgressListener listener : listeners) {
            try {
                listener.onFallback(alarmType);
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        }
        cleanup();
    }

    void onTaskFailed(AbstractStartupTask abstractStartupTask, Throwable t) {
        onTaskFailed(abstractStartupTask, ExceptionUtil.setupStackTrace(null, t), t);
    }

    private void setupStartupSeq(StartupSeq startupSeq) {
        startupList.clear();
        for (StartupTaskEnum startupTaskEnum : startupSeq.getAbstractStartupTaskEnum()) {
            AbstractStartupTask abstractStartupTask = taskInstance.select(startupTaskEnum.getTaskClass()).get();
            abstractStartupTask.setTaskEnum(startupTaskEnum);
            startupList.add(abstractStartupTask);
        }
    }

    public String getGameSessionUuid() {
        return gameSessionUuid;
    }
}