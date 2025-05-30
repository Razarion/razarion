package com.btxtech.server.rest.engine;

import com.btxtech.server.service.engine.ServerInventoryService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.dto.InventoryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/inventory-controller")
public class InventoryController {
    private final Logger logger = LoggerFactory.getLogger(InventoryController.class);
    private final ServerInventoryService serverInventoryService;
    private final UserService userService;

    public InventoryController(ServerInventoryService serverInventoryService, UserService userService) {
        this.serverInventoryService = serverInventoryService;
        this.userService = userService;
    }

    @GetMapping(value = "loadInventory", produces = MediaType.APPLICATION_JSON_VALUE)
    public InventoryInfo loadInventory() {
        try {
            var userId = userService.getOrCreateUserIdFromContext();
            return serverInventoryService.loadInventory(userId);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping(value = "loadCrystals", produces = MediaType.APPLICATION_JSON_VALUE)
    public int loadCrystals() {
        try {
            var userId = userService.getOrCreateUserIdFromContext();
            return serverInventoryService.loadCrystals(userId);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }
}
