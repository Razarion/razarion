package com.btxtech.server.rest;

import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.gameengine.ServerTerrainShapeService;
import com.btxtech.server.mgmt.ServerMgmt;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.rest.ServerGameEngineController;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.ws.rs.PathParam;

/**
 * Created by Beat
 * on 29.08.2017.
 */
public class ServerGameEngineControllerImpl implements ServerGameEngineController {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private ServerGameEngineControl serverGameEngineControl;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private ServerTerrainShapeService serverTerrainShapeService;
    @Inject
    private ServerMgmt serverMgmt;

    @SecurityCheck
    public void restartBots() {
        try {
            serverGameEngineControl.restartBots();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @SecurityCheck
    public void reloadStatic() {
        try {
            serverGameEngineControl.reloadStatic();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @SecurityCheck
    public void restartResourceRegions() {
        try {
            serverGameEngineControl.restartResourceRegions();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @SecurityCheck
    public void reloadPlanetShapes() {
        try {
            serverTerrainShapeService.start();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @SecurityCheck
    public void restartBoxRegions() {
        try {
            serverGameEngineControl.restartBoxRegions();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @SecurityCheck
    public void restartPlanetWarm() {
        try {
            serverMgmt.restartPlanetWarm();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @SecurityCheck
    public void restartPlanetCold() {
        try {
            serverMgmt.restartPlanetCold();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @SecurityCheck
    public void deleteBase(@PathParam("baseId") int baseId) {
        try {
            baseItemService.mgmtDeleteBase(baseId);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}
