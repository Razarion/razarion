package com.btxtech.uiservice.itemplacer;


import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;

/**
 * User: beat
 * Date: 01.05.13
 * Time: 12:55
 */

public class BaseItemPlacerChecker {
    // private Logger logger = Logger.getLogger(BaseItemPlacerChecker.class.getName());
    private static final double SAFETY_DISTANCE = 0.2;
    private final TerrainUiService terrainUiService;
    private final BaseItemUiService baseItemUiService;
    private Collection<DecimalPosition> relativeItemPositions;
    private boolean isAllowedAreaOk;
    private boolean isTerrainOk;
    private boolean isItemsOk;
    private boolean isEnemiesOk;
    private BaseItemType baseItemType;
    private PlaceConfig allowedArea;
    private double enemyFreeRadius;

    @Inject
    public BaseItemPlacerChecker(BaseItemUiService baseItemUiService, TerrainUiService terrainUiService) {
        this.baseItemUiService = baseItemUiService;
        this.terrainUiService = terrainUiService;
    }

    public void init(BaseItemType baseItemType, BaseItemPlacerConfig baseItemPlacerConfig) {
        this.baseItemType = baseItemType;
        allowedArea = baseItemPlacerConfig.getAllowedArea();
        setupGeometry(baseItemType, baseItemPlacerConfig);
    }

    public void check(DecimalPosition position) {
        Collection<DecimalPosition> absoluteItemPositions = setupAbsolutePositions(position);
        isAllowedAreaOk = allowedArea == null || allowedArea.checkInside(absoluteItemPositions);
        isEnemiesOk = false;
        if (isAllowedAreaOk) {
            isEnemiesOk = !baseItemUiService.hasEnemyForSpawn(position, enemyFreeRadius);
        }
        isItemsOk = false;
        if (isEnemiesOk) {
            isItemsOk = !baseItemUiService.hasItemsInRangeInViewField(absoluteItemPositions, baseItemType.getPhysicalAreaConfig().getRadius());
        }
        isTerrainOk = isItemsOk && terrainUiService.isTerrainFree(absoluteItemPositions, baseItemType);
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
        double itemRadius = baseItemType.getPhysicalAreaConfig().getRadius() + SAFETY_DISTANCE;
        double bipcEnemyFreeRadius = baseItemPlacerConfig.getEnemyFreeRadius() != null ? baseItemPlacerConfig.getEnemyFreeRadius() : 0;
        if (baseItemPlacerConfig.getBaseItemCount() == 1) {
            relativeItemPositions.add(new DecimalPosition(0, 0));
            enemyFreeRadius = itemRadius + bipcEnemyFreeRadius;
        } else {
            double value = Math.sqrt(baseItemPlacerConfig.getBaseItemCount());
            int columns = (int) Math.ceil(value);
            int rows = (int) Math.round(value);
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
            enemyFreeRadius = bipcEnemyFreeRadius + MathHelper.getPythagorasC((double) columns * itemRadius, (double) rows * itemRadius);
        }
    }
}
