package com.btxtech.server.rest;

import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.gameengine.ServerTerrainShapeService;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.gameengine.planet.BaseItemService;
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
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private ServerTerrainShapeService serverTerrainShapeService;

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
    public void reloadPlanetShapes() {
        try {
            serverTerrainShapeService.start();
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

    @Override
    @SecurityCheck
    public void deleteBase(int baseId) {
        try {
            baseItemService.mgmtDeleteBase(baseId);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}
