package com.btxtech.server.rest;

import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.gameengine.ServerTerrainShapeService;
import com.btxtech.server.mgmt.ServerMgmt;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * Created by Beat
 * on 29.08.2017.
 */
@Path(CommonUrl.SERVER_GAME_ENGINE_PATH)
public class ServerGameEngineController {
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

    @POST
    @Path("restartBots")
    @SecurityCheck
    public void restartBots() {
        try {
            serverGameEngineControl.restartBots();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @POST
    @Path("reloadStatic")
    @SecurityCheck
    public void reloadStatic() {
        try {
            serverGameEngineControl.reloadStatic();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @POST
    @Path("restartResourceRegions")
    @SecurityCheck
    public void restartResourceRegions() {
        try {
            serverGameEngineControl.restartResourceRegions();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @POST
    @Path("reloadPlanetShapes")
    @SecurityCheck
    public void reloadPlanetShapes() {
        try {
            serverTerrainShapeService.start();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @POST
    @Path("restartBoxRegions")
    @SecurityCheck
    public void restartBoxRegions() {
        try {
            serverGameEngineControl.restartBoxRegions();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @POST
    @Path("restartPlanetWarm")
    @SecurityCheck
    public void restartPlanetWarm() {
        try {
            serverMgmt.restartPlanetWarm();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @POST
    @Path("restartPlanetCold")
    @SecurityCheck
    public void restartPlanetCold() {
        try {
            serverMgmt.restartPlanetCold();
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }

    @DELETE
    @Path("deletebase/{baseId}")
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
