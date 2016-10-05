package com.btxtech.shared.datatypes;


import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * User: beat
 * Date: 01.05.13
 * Time: 12:55
 */
@Dependent
public class StartPointItemPlacerChecker {
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private TerrainService terrainService;
    @Inject
    private BaseItemService baseItemService;
    private boolean isAllowedAreaOk;
    private boolean isTerrainOk;
    private boolean isItemsOk;
    private boolean isEnemiesOk;
    private BaseItemType baseItemType;
    private Polygon2D allowedArea;
    private double enemyFreeRadius;

    public void init(BaseItemType baseItemType, double enemyFreeRadius, Polygon2D allowedArea) {
        this.baseItemType = baseItemType;
        this.allowedArea = allowedArea;
        this.enemyFreeRadius = enemyFreeRadius + baseItemType.getPhysicalAreaConfig().getRadius();
    }

    public void check(DecimalPosition position) {
        isAllowedAreaOk = allowedArea == null || allowedArea.isInside(position);
        isEnemiesOk = false;
        if (isAllowedAreaOk) {
            isEnemiesOk = !baseItemService.hasEnemyForSpawn(position, enemyFreeRadius);
        }
        isItemsOk = false;
        if (isEnemiesOk) {
            isItemsOk = !syncItemContainerService.hasItemsInRange(position, baseItemType.getPhysicalAreaConfig().getRadius());
        }
        isTerrainOk = false;
        if (isItemsOk) {
            isTerrainOk = !terrainService.overlap(position, baseItemType, null, null);
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
}
