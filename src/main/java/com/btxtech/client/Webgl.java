package com.btxtech.client;

import com.btxtech.client.system.boot.ClientRunner;
import com.btxtech.client.system.boot.GameStartupSeq;
import com.btxtech.client.system.boot.StartupProgressListener;
import com.btxtech.client.system.boot.StartupSeq;
import com.btxtech.client.system.boot.StartupTaskEnum;
import com.btxtech.client.system.boot.task.AbstractStartupTask;
import com.btxtech.shared.StartupTaskInfo;
import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.api.EntryPoint;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
@EntryPoint
public class Webgl {
    @Inject
    private ClientRunner clientRunner;
    // @Inject
    private Logger logger = Logger.getLogger(Webgl.class.getName());

    public Webgl() {
        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
            @Override
            public void onUncaughtException(Throwable e) {
                if (logger != null) {
                    logger.log(Level.SEVERE, "UncaughtExceptionHandler", e);
                } else {
                    GWT.log("UncaughtExceptionHandler", e);
                }
            }
        });
    }

    @AfterInitialization
    public void afterInitialization() {
        clientRunner.addStartupProgressListener(new StartupProgressListener() {
            @Override
            public void onStart(StartupSeq startupSeq) {
                logger.severe("onStart: " + startupSeq);
            }

            @Override
            public void onNextTask(StartupTaskEnum taskEnum) {
                logger.severe("onNextTask: " + taskEnum);
            }

            @Override
            public void onTaskFinished(AbstractStartupTask task) {
                logger.severe("onTaskFinished: " + task);
            }

            @Override
            public void onTaskFailed(AbstractStartupTask task, String error, Throwable t) {
                logger.log(Level.SEVERE, "onTaskFailed: " + task + " error:" + error, t);
            }

            @Override
            public void onStartupFinished(List<StartupTaskInfo> taskInfo, long totalTime) {
                logger.severe("onStartupFinished: " + taskInfo + " totalTime:" + totalTime);
            }

            @Override
            public void onStartupFailed(List<StartupTaskInfo> taskInfo, long totalTime) {
                logger.severe("onStartupFailed: " + taskInfo + " totalTime:" + totalTime);
            }
        });
        clientRunner.afterInitErrai(GameStartupSeq.COLD_SIMULATED);
    }

}
