package com.btxtech.server.rest;

import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.persistence.backup.BackupPlanetOverview;
import com.btxtech.server.persistence.backup.PlanetBackupMongoDb;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.system.ExceptionHandler;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * on 04.09.2017.
 */
@Path(CommonUrl.SERVER_GAME_ENGINE_MGMT_PATH)
public class ServerGameEngineMgmt {
    @Inject
    private ServerGameEngineControl serverGameEngineControl;
    @Inject
    private PlanetBackupMongoDb planetBackupMongoDb;
    @Inject
    private ExceptionHandler exceptionHandler;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("loadallbackupbaseoverviews")
    public List<BackupPlanetOverview> loadAllBackupBaseOverviews() {
        try {
            return planetBackupMongoDb.loadAllBackupBaseOverviews();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("dobackup")
    public List<BackupPlanetOverview> doBackup() {
        try {
            serverGameEngineControl.backupPlanet();
            return planetBackupMongoDb.loadAllBackupBaseOverviews();
        } catch (JsonProcessingException e) {
            exceptionHandler.handleException(e);
            throw new RuntimeException(e);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("dorestore")
    public void doRestore(BackupPlanetOverview backupPlanetOverview) {
        try {
            serverGameEngineControl.restorePlanet(backupPlanetOverview);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("deletebackup/{planetId}/{date}")
    public List<BackupPlanetOverview> deleteBackup(@PathParam("planetId") int planetId, @PathParam("date") long date) {
        try {
            planetBackupMongoDb.deleteBackup(new BackupPlanetOverview().setPlanetId(planetId).setDate(new Date(date)));
            return planetBackupMongoDb.loadAllBackupBaseOverviews();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

}
