package com.btxtech.server.rest.engine;

import com.btxtech.server.service.engine.ServerInventoryService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.dto.InventoryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    /**
     * Assemble an inventory item from owned artifacts (workshop). Returns false if the user
     * does not own the required artifact set.
     */
    @PostMapping(value = "assembleInventoryItem/{inventoryItemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean assembleInventoryItem(@PathVariable("inventoryItemId") int inventoryItemId) {
        try {
            var userId = userService.getOrCreateUserIdFromContext();
            return serverInventoryService.assembleInventoryItem(userId, inventoryItemId);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Buy an inventory item for crystals (trader). Returns false if the user has too few crystals.
     */
    @PostMapping(value = "buyInventoryItem/{inventoryItemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean buyInventoryItem(@PathVariable("inventoryItemId") int inventoryItemId) {
        try {
            var userId = userService.getOrCreateUserIdFromContext();
            return serverInventoryService.buyInventoryItem(userId, inventoryItemId);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Buy an artifact for crystals (trader). Returns false if the user has too few crystals.
     */
    @PostMapping(value = "buyInventoryArtifact/{inventoryArtifactId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean buyInventoryArtifact(@PathVariable("inventoryArtifactId") int inventoryArtifactId) {
        try {
            var userId = userService.getOrCreateUserIdFromContext();
            return serverInventoryService.buyInventoryArtifact(userId, inventoryArtifactId);
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }
    }
}
