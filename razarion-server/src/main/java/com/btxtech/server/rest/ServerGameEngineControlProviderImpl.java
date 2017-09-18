package com.btxtech.server.rest;

import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.shared.rest.ServerGameEngineControlProvider;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 29.08.2017.
 */
public class ServerGameEngineControlProviderImpl implements ServerGameEngineControlProvider {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private ServerGameEngineControl serverGameEngineControl;

    @Override
    public void restartBots() {
        try {
            serverGameEngineControl.restartBots();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void reloadStatic() {
        try {
            serverGameEngineControl.reloadStatic();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void restartResourceRegions() {
        try {
            serverGameEngineControl.restartResourceRegions();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void reloadPlanet() {
        try {
            serverGameEngineControl.reloadPlanet();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @Override
    public void restartBoxRegions() {
        try {
            serverGameEngineControl.restartBoxRegions();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}
