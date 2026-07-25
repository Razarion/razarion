package com.btxtech.uiservice.system.boot;

import com.btxtech.shared.system.alarm.AlarmRaisedException;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.shared.utils.ExceptionUtil;
import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: 04.12.2010
 * Time: 10:56:33
 */
public abstract class Boot {
    /**
     * A deferred task waiting longer than this is reported as stuck. Chosen above the slowest
     * healthy startups seen in tracking (99th percentile is around 50s, dominated by the model
     * download) so a normal slow connection does not raise it.
     */
    private static final long DEFERRED_TASK_TIMEOUT_MILLIS = 60_000;
    private final Logger logger = Logger.getLogger(Boot.class.getName());
    private final Collection<StartupProgressListener> listeners = new ArrayList<>();
    private final List<AbstractStartupTask> startupList = new ArrayList<>();
    private final List<DeferredStartup> deferredStartups = new ArrayList<>();
    private final List<DeferredStartup> deferredBackgroundStartups = new ArrayList<>();
    private final List<AbstractStartupTask> finishedTasks = new ArrayList<>();
    private final AlarmService alarmService;
    private AbstractStartupTask waitingTask;
    private boolean failed;
    private String gameSessionUuid;

    public Boot(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    protected abstract StartupSeq getWarm();

    protected abstract BootContext createBootContext();

    public void addStartupProgressListener(StartupProgressListener startupProgressListener) {
        listeners.add(startupProgressListener);
    }

    public void removeStartupProgressListener(StartupProgressListener startupProgressListener) {
        listeners.remove(startupProgressListener);
    }

    public void start(StartupSeq startupSeq) {
        gameSessionUuid = createGameSessionUuid();
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
                logger.log(Level.SEVERE, "Listener.onNextTask() failed", t);
            }
        }
        try {
            task.start(deferredStartup);
        } catch (AlarmRaisedException are) {
            alarmService.riseAlarm(are);
            onTaskFailed(task, are);
            return;
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Boot.runTask() failed: " + t.getMessage(), t);
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
                watchDeferredTask(task, deferredStartup);
            }

            if (deferredStartup.isBackground()) {
                runNextTask();
            }
        } else {
            onTaskFinished(task);
        }
    }

    /**
     * A deferred task hands control to a callback. If that callback never comes - a fetch that
     * neither resolves nor rejects, a backgrounded tab - the whole startup stops silently: no
     * error, no listener call, nothing sent. That is how the bulk of the abandoned startups used
     * to disappear from tracking. This raises the alarm after {@link #DEFERRED_TASK_TIMEOUT_MILLIS}.
     * <p>
     * It reports and lets the task keep running instead of failing the startup. Killing a slow
     * boot would turn a player who was still going to make it into one who never does; the
     * missing terminated record marks the session as aborted anyway if it truly never finishes.
     */
    private void watchDeferredTask(AbstractStartupTask task, DeferredStartup deferredStartup) {
        scheduleDeferredTimeout(DEFERRED_TASK_TIMEOUT_MILLIS, () -> {
            if (failed || deferredStartup.isFinished()) {
                return;
            }
            logger.warning("Boot: task still waiting after " + DEFERRED_TASK_TIMEOUT_MILLIS + "ms: " + task.getTaskEnum());
            for (StartupProgressListener listener : listeners) {
                try {
                    listener.onTaskTimeout(task, DEFERRED_TASK_TIMEOUT_MILLIS);
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "Listener.onTaskTimeout() failed", t);
                }
            }
        });
    }

    /**
     * No scheduler down here - the client overrides this with its own executor. The default does
     * nothing, which keeps the boot tests free of timers.
     */
    protected void scheduleDeferredTimeout(long delayMillis, Runnable runnable) {
    }

    /**
     * Overridden on the client so a session started before the WebAssembly module was even
     * loaded keeps its id: the page hands one over, and the tasks reported from the page and
     * from the engine then belong to the same session.
     */
    protected String createGameSessionUuid() {
        return MathHelper.generateUuid();
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


    void onTaskFailed(AbstractStartupTask abstractStartupTask, Throwable t) {
        onTaskFailed(abstractStartupTask, ExceptionUtil.setupStackTrace(null, t), t);
    }

    public void raiseAlarmIfNeeded(Throwable t) {
        if (t instanceof AlarmRaisedException) {
            alarmService.riseAlarm((AlarmRaisedException) t);
        }
    }

    private void setupStartupSeq(StartupSeq startupSeq) {
        startupList.clear();
        BootContext bootContext = createBootContext();
        for (StartupTaskEnum startupTaskEnum : startupSeq.getAbstractStartupTaskEnum()) {
            AbstractStartupTask abstractStartupTask = startupTaskEnum.createAbstractStartupTask(bootContext);
            abstractStartupTask.setTaskEnum(startupTaskEnum);
            startupList.add(abstractStartupTask);
        }
    }

    public String getGameSessionUuid() {
        return gameSessionUuid;
    }

}
