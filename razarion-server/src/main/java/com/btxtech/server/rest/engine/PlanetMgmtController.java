package com.btxtech.server.rest.engine;

import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.model.Roles;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest/planet-mgmt-controller")
public class PlanetMgmtController {
    private final Logger logger = LoggerFactory.getLogger(PlanetMgmtController.class);
    private final BaseItemService baseItemService;
    private final ServerGameEngineControl serverGameEngineControl;

    public PlanetMgmtController(BaseItemService baseItemService,
                                ServerGameEngineControl serverGameEngineControl) {
        this.baseItemService = baseItemService;
        this.serverGameEngineControl = serverGameEngineControl;
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
        throw new UnsupportedOperationException("...TODO...");
    }

    @PostMapping("restartPlanetCold")
    @RolesAllowed(Roles.ADMIN)
    public void restartPlanetCold() {
        throw new UnsupportedOperationException("...TODO...");
    }

    @DeleteMapping(value = "delete/{baseId}")
    @RolesAllowed(Roles.ADMIN)
    public void deleteBase(@PathVariable("baseId") int baseId) {
        try {
            baseItemService.mgmtDeleteBase(baseId);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

}
