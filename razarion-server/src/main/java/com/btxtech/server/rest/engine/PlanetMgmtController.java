package com.btxtech.server.rest.engine;

import com.btxtech.server.gameengine.ClientSystemConnectionService;
import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.model.Roles;
import com.btxtech.server.model.engine.BackupPlanetOverview;
import com.btxtech.server.service.engine.PlanetBackupService;
import com.btxtech.server.service.engine.ServerGameEngineService;
import com.btxtech.server.service.engine.ServerTerrainShapeService;
import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.dto.ServerGameEngineConfig;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rest/planet-mgmt-controller")
public class PlanetMgmtController {
    private final Logger logger = LoggerFactory.getLogger(PlanetMgmtController.class);
    private final BaseItemService baseItemService;
    private final ServerGameEngineControl serverGameEngineControl;
    private final ServerTerrainShapeService serverTerrainShapeService;
    private final ClientSystemConnectionService clientSystemConnectionService;
    private final ServerGameEngineService serverGameEngineService;
    private final PlanetBackupService planetBackupService;
    private final BotService botService;
    private final Object reloadLook = new Object();

    public PlanetMgmtController(BaseItemService baseItemService,
                                ServerGameEngineControl serverGameEngineControl,
                                ServerTerrainShapeService serverTerrainShapeService,
                                ClientSystemConnectionService clientSystemConnectionService,
                                ServerGameEngineService serverGameEngineService,
                                PlanetBackupService planetBackupService, BotService botService) {
        this.baseItemService = baseItemService;
        this.serverGameEngineControl = serverGameEngineControl;
        this.serverTerrainShapeService = serverTerrainShapeService;
        this.clientSystemConnectionService = clientSystemConnectionService;
        this.serverGameEngineService = serverGameEngineService;
        this.planetBackupService = planetBackupService;
        this.botService = botService;
    }

    @PostMapping("restartBots")
    @PreAuthorize("hasAuthority('ADMIN')") 
    public void restartBots() {
        ServerGameEngineConfig serverGameEngineConfig = serverGameEngineService.serverGameEngineConfig();
        synchronized (reloadLook) {
            botService.killAllBots();
            botService.startBots(serverGameEngineConfig.getBotConfigs());
        }
    }

    @PostMapping("reloadStatic")
    @PreAuthorize("hasAuthority('ADMIN')") 
    public void reloadStatic() {
        try {
            serverGameEngineControl.reloadStatic();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("restartResourceRegions")
    @PreAuthorize("hasAuthority('ADMIN')") 
    public void restartResourceRegions() {
        try {
            serverGameEngineControl.restartResourceRegions();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("reloadPlanetShapes")
    @PreAuthorize("hasAuthority('ADMIN')") 
    public void reloadPlanetShapes() {
        throw new UnsupportedOperationException("...TODO...");
    }

    @PostMapping("restartBoxRegions")
    @PreAuthorize("hasAuthority('ADMIN')") 
    public void restartBoxRegions() {
        try {
            serverGameEngineControl.restartBoxRegions();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("restartPlanetWarm")
    @PreAuthorize("hasAuthority('ADMIN')") 
    public void restartPlanetWarm() {
        restartPlanet(LifecyclePacket.Type.PLANET_RESTART_WARM);
    }

    @PostMapping("restartPlanetCold")
    @PreAuthorize("hasAuthority('ADMIN')") 
    public void restartPlanetCold() {
        restartPlanet(LifecyclePacket.Type.PLANET_RESTART_COLD);
    }

    @DeleteMapping("delete/{baseId}")
    @PreAuthorize("hasAuthority('ADMIN')") 
    public void deleteBase(@PathVariable int baseId) {
        try {
            baseItemService.deleteBase(baseId);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("loadAllBackupBaseOverviews")
    @PreAuthorize("hasAuthority('ADMIN')") 
    public List<BackupPlanetOverview> loadAllBackupBaseOverviews() {
        try {
            return planetBackupService.loadAllBackupBaseOverviews();
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
            throw t;
        }
    }

    @PostMapping("doBackup")
    @PreAuthorize("hasAuthority('ADMIN')") 
    public List<BackupPlanetOverview> doBackup() {
        try {
            serverGameEngineControl.backupPlanet();
            return planetBackupService.loadAllBackupBaseOverviews();
        } catch (JsonProcessingException e) {
            logger.warn(e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
            throw t;
        }
    }

    @PostMapping("doRestore")
    @PreAuthorize("hasAuthority('ADMIN')") 
    public void doRestore(@RequestBody BackupPlanetOverview backupPlanetOverview) {
        try {
            serverGameEngineControl.restorePlanet(backupPlanetOverview);
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
            throw t;
        }
    }

    @DeleteMapping("deleteBackup")
    @PreAuthorize("hasAuthority('ADMIN')") 
    public List<BackupPlanetOverview> deleteBackup(@RequestBody BackupPlanetOverview backupPlanetOverview) {
        try {
            planetBackupService.deleteBackup(backupPlanetOverview);
            return planetBackupService.loadAllBackupBaseOverviews();
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
            throw t;
        }
    }

    private void restartPlanet(LifecyclePacket.Type type) {
        try {
            clientSystemConnectionService.sendLifecyclePacket(
                    new LifecyclePacket().setType(LifecyclePacket.Type.HOLD)
                            .setDialog(LifecyclePacket.Dialog.PLANET_RESTART)
            );
            ServerGameEngineConfig serverGameEngineConfig = serverGameEngineService.serverGameEngineConfig();
            serverTerrainShapeService.createTerrainShape(
                    serverGameEngineConfig.getBotConfigs(),
                    serverGameEngineConfig.getPlanetConfigId()
            );
            serverGameEngineControl.restartPlanet();
            clientSystemConnectionService.sendLifecyclePacket(new LifecyclePacket().setType(type));
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }
}
