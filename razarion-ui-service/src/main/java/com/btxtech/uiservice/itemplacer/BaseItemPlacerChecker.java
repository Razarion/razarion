package com.btxtech.uiservice.itemplacer;


import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;

/**
 * User: beat
 * Date: 01.05.13
 * Time: 12:55
 */
@Dependent
public class BaseItemPlacerChecker {
    private static final double SAFETY_DISTANCE = 0.2;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private BaseItemService baseItemService;
    private Collection<DecimalPosition> relativeItemPositions;
    private boolean isAllowedAreaOk;
    private boolean isTerrainOk;
    private boolean isItemsOk;
    private boolean isEnemiesOk;
    private BaseItemType baseItemType;
    private Polygon2D allowedArea;
    private double enemyFreeRadius;

    public void init(BaseItemType baseItemType, BaseItemPlacerConfig baseItemPlacerConfig) {
        this.baseItemType = baseItemType;
        allowedArea = baseItemPlacerConfig.getAllowedArea();
        setupGeometry(baseItemType, baseItemPlacerConfig);
    }

    public void check(DecimalPosition position) {
        Collection<DecimalPosition> absoluteItemPositions = setupAbsolutePositions(position);
        isAllowedAreaOk = allowedArea == null || allowedArea.isInside(absoluteItemPositions);
        isEnemiesOk = false;
        if (isAllowedAreaOk) {
            isEnemiesOk = !baseItemService.hasEnemyForSpawn(position, enemyFreeRadius);
        }
        isItemsOk = false;
        if (isEnemiesOk) {
            isItemsOk = !syncItemContainerService.hasItemsInRange(absoluteItemPositions, baseItemType.getPhysicalAreaConfig().getRadius());
        }
        if (isItemsOk) {
            terrainUiService.overlap(absoluteItemPositions, baseItemType, overlap -> isTerrainOk = !overlap);
        } else {
            isTerrainOk = false;
        }
    }

    public boolean isAllowedAreaOk() {
        return isAllowedAreaOk;
    }

    public boolean isTerrainOk() {
        return isTerrainOk;
    }

    public boolean isItemsOk() {
        return isItemsOk;
    }

    public boolean isEnemiesOk() {
        return isEnemiesOk;
    }

    public boolean isPositionValid() {
        return isAllowedAreaOk && isItemsOk && isEnemiesOk && isTerrainOk;
    }

    public double getEnemyFreeRadius() {
        return enemyFreeRadius;
    }

    public Collection<DecimalPosition> setupAbsolutePositions(DecimalPosition position) {
        return DecimalPosition.add(relativeItemPositions, position);
    }

    private void setupGeometry(BaseItemType baseItemType, BaseItemPlacerConfig baseItemPlacerConfig) {
        relativeItemPositions = new ArrayList<>();
        double value = Math.sqrt(baseItemPlacerConfig.getBaseItemCount());
        int columns = (int) Math.ceil(value);
        int rows = (int) Math.round(value);
        double itemRadius = baseItemType.getPhysicalAreaConfig().getRadius() + SAFETY_DISTANCE;
        double columnOffset = (double) (columns - 1) * itemRadius;
        double rowOffset = (double) (rows - 1) * itemRadius;
        int count = 0;
        for (int column = 0; column < columns; column++) {
            for (int row = 0; row < rows; row++) {
                count++;
                if (count <= baseItemPlacerConfig.getBaseItemCount()) {
                    double xPos = (double) column * baseItemType.getPhysicalAreaConfig().getRadius() * 2.0 - columnOffset;
                    double yPos = (double) row * baseItemType.getPhysicalAreaConfig().getRadius() * 2.0 - rowOffset;
                    relativeItemPositions.add(new DecimalPosition(xPos, yPos));
                }
            }
        }

        enemyFreeRadius = baseItemPlacerConfig.getEnemyFreeRadius() + MathHelper.getPythagorasC((double) columns * itemRadius, (double) rows * itemRadius);
    }
}
