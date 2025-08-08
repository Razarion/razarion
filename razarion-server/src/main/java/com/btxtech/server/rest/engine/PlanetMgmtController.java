package com.btxtech.server.rest.engine;

import com.btxtech.server.gameengine.ClientSystemConnectionService;
import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.model.Roles;
import com.btxtech.server.service.engine.ServerGameEngineService;
import com.btxtech.server.service.engine.ServerTerrainShapeService;
import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.dto.ServerGameEngineConfig;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/planet-mgmt-controller")
public class PlanetMgmtController {
    private final Logger logger = LoggerFactory.getLogger(PlanetMgmtController.class);
    private final BaseItemService baseItemService;
    private final ServerGameEngineControl serverGameEngineControl;
    private final ServerTerrainShapeService serverTerrainShapeService;
    private final ClientSystemConnectionService clientSystemConnectionService;
    private final ServerGameEngineService serverGameEngineCrudPersistence;

    public PlanetMgmtController(BaseItemService baseItemService,
                                ServerGameEngineControl serverGameEngineControl,
                                ServerTerrainShapeService serverTerrainShapeService,
                                ClientSystemConnectionService clientSystemConnectionService,
                                ServerGameEngineService serverGameEngineCrudPersistence) {
        this.baseItemService = baseItemService;
        this.serverGameEngineControl = serverGameEngineControl;
        this.serverTerrainShapeService = serverTerrainShapeService;
        this.clientSystemConnectionService = clientSystemConnectionService;
        this.serverGameEngineCrudPersistence = serverGameEngineCrudPersistence;
    }

    @PostMapping("restartBots")
    @RolesAllowed(Roles.ADMIN)
    public void restartBots() {
        throw new UnsupportedOperationException("...TODO...");
    }

    @PostMapping("reloadStatic")
    @RolesAllowed(Roles.ADMIN)
    public void reloadStatic() {
        try {
            serverGameEngineControl.reloadStatic();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("restartResourceRegions")
    @RolesAllowed(Roles.ADMIN)
    public void restartResourceRegions() {
        try {
            serverGameEngineControl.restartResourceRegions();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("reloadPlanetShapes")
    @RolesAllowed(Roles.ADMIN)
    public void reloadPlanetShapes() {
        throw new UnsupportedOperationException("...TODO...");
    }

    @PostMapping("restartBoxRegions")
    @RolesAllowed(Roles.ADMIN)
    public void restartBoxRegions() {
        try {
            serverGameEngineControl.restartBoxRegions();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("restartPlanetWarm")
    @RolesAllowed(Roles.ADMIN)
    public void restartPlanetWarm() {
        restartPlanet(LifecyclePacket.Type.PLANET_RESTART_WARM);
    }

    @PostMapping("restartPlanetCold")
    @RolesAllowed(Roles.ADMIN)
    public void restartPlanetCold() {
        restartPlanet(LifecyclePacket.Type.PLANET_RESTART_COLD);
    }

    @DeleteMapping(value = "delete/{baseId}")
    @RolesAllowed(Roles.ADMIN)
    public void deleteBase(@PathVariable("baseId") int baseId) {
        try {
            baseItemService.deleteBase(baseId);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    private void restartPlanet(LifecyclePacket.Type type) {
        try {
            clientSystemConnectionService.sendLifecyclePacket(new LifecyclePacket().setType(LifecyclePacket.Type.HOLD).setDialog(LifecyclePacket.Dialog.PLANET_RESTART));
            ServerGameEngineConfig serverGameEngineConfig = serverGameEngineCrudPersistence.read().get(0);
            serverTerrainShapeService.createTerrainShape(serverGameEngineConfig.getBotConfigs(), serverGameEngineConfig.getPlanetConfigId());
            serverGameEngineControl.restartPlanet();
            clientSystemConnectionService.sendLifecyclePacket(new LifecyclePacket().setType(type));
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

}
