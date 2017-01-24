package com.btxtech.uiservice.system.boot;

import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.utils.ExceptionUtil;
import com.btxtech.shared.utils.MathHelper;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: 04.12.2010
 * Time: 10:56:33
 */
@Singleton
public class ClientRunner {
    private Logger logger = Logger.getLogger(ClientRunner.class.getName());
    private Collection<StartupProgressListener> listeners = new ArrayList<>();
    private List<AbstractStartupTask> startupList = new ArrayList<>();
    private List<DeferredStartup> deferredStartups = new ArrayList<>();
    private List<AbstractStartupTask> finishedTasks = new ArrayList<>();
    private boolean failed;
    private String startUuid;
    @Inject
    private Instance<AbstractStartupTask> taskInstance;
    @Inject
    private ExceptionHandler exceptionHandler;

    public void addStartupProgressListener(StartupProgressListener startupProgressListener) {
        listeners.add(startupProgressListener);
    }

    public void removeStartupProgressListener(StartupProgressListener startupProgressListener) {
        listeners.remove(startupProgressListener);
    }

    public void cleanupBeforeTest() {
        listeners.clear();
    }

    public void start(StartupSeq startupSeq) {
        startUuid = MathHelper.generateUuid();
        failed = false;
        for (StartupProgressListener listener : listeners) {
            listener.onStart(startupSeq);
        }
        setupStartupSeq(startupSeq);
        runNextTask();
    }

    private void runNextTask() {
        if (failed) {
            return;
        }
        if (startupList.isEmpty()) {
            onStartupFinish();
        } else {
            AbstractStartupTask task = startupList.remove(0);
            ClientRunnerDeferredStartupImpl deferredStartup = new ClientRunnerDeferredStartupImpl(task, this);
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
    }

    private void cleanup() {
        finishedTasks.clear();
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
        if (!abstractStartupTask.isBackground()) {
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
            logger.severe("ClientRunner.onTaskFailed(): " + error);
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

    private void setupStartupSeq(StartupSeq startupSeq) {
        startupList.clear();
        for (StartupTaskEnum startupTaskEnum : startupSeq.getAbstractStartupTaskEnum()) {
            AbstractStartupTask abstractStartupTask = taskInstance.select(startupTaskEnum.getTaskClass()).get();
            abstractStartupTask.setTaskEnum(startupTaskEnum);
            startupList.add(abstractStartupTask);
        }
    }

    public String getStartUuid() {
        return startUuid;
    }
}
