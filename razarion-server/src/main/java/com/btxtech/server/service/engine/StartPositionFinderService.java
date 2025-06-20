package com.btxtech.server.service.engine;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.SlavePlanetConfig;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StartPositionFinderService {
    private final Logger logger = LoggerFactory.getLogger(StartPositionFinderService.class);
    private final SyncItemContainerServiceImpl syncItemContainerService;

    public StartPositionFinderService(SyncItemContainerServiceImpl syncItemContainerService) {
        this.syncItemContainerService = syncItemContainerService;
    }

    public DecimalPosition findFreePosition(SlavePlanetConfig slavePlanetConfig) {
        if (slavePlanetConfig.getPositionRadius() == null || slavePlanetConfig.getPositionMaxItems() == null) {
            throw new RuntimeException("Position radius or maxItems not set");
        }

        long startTime = System.currentTimeMillis();
        var context = new Context(slavePlanetConfig.getPositionMaxItems());
        var positionFound = slavePlanetConfig.getPositionPath().stream()
                .filter(position -> {
                    context.reset();
                    syncItemContainerService.iterateCellRadiusItem(position, slavePlanetConfig.getPositionRadius(), syncItem -> {
                        if (syncItem instanceof SyncBaseItem) {
                            context.increaseItemCount();
                        }
                    });
                    return context.isAllowed();
                })
                .findFirst()
                .orElse(null);

        if (positionFound != null) {
            logger.info("Free position found at {} iterations {} duration {}", positionFound, context.getResets(), System.currentTimeMillis() - startTime);
        } else {
            logger.info("Free position not found duration {}", System.currentTimeMillis() - startTime);
        }
        return positionFound;
    }

    private static class Context {
        private final int positionMaxItem;
        private int count = 0;
        private int resets = 0;

        public Context(int positionMaxItem) {
            this.positionMaxItem = positionMaxItem;
        }

        public void increaseItemCount() {
            count++;
        }

        public boolean isAllowed() {
            return count <= positionMaxItem;
        }

        public void reset() {
            count = 0;
            resets++;
        }

        public int getResets() {
            return resets;
        }
    }
}
