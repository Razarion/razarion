package com.btxtech.client;

import com.btxtech.client.system.boot.ClientRunner;
import com.btxtech.client.system.boot.StartupProgressListener;
import com.btxtech.client.system.boot.StartupSeq;
import com.btxtech.client.system.boot.StartupTaskEnum;
import com.btxtech.client.system.boot.StartupTaskInfo;
import com.btxtech.client.system.boot.task.AbstractStartupTask;
import com.btxtech.shared.RestUrl;
import com.google.gwt.core.client.GWT;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
@EntryPoint
public class Client {
    private Logger logger = Logger.getLogger(Client.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ClientRunner clientRunner;

    public Client() {
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
        RestClient.setApplicationRoot(RestUrl.APPLICATION_PATH);
    }

    @PostConstruct
    public void init() {
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
    }
}
