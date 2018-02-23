package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeUtil;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.nativejs.NativeMatrix;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.shared.nativejs.NativeVertexDto;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.Colors;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.cockpit.CockpitService;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.btxtech.uiservice.effects.EffectVisualizationService;
import com.btxtech.uiservice.renderer.ViewService;
import com.btxtech.uiservice.user.UserUiService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 28.12.2015.
 * *
 */
@ApplicationScoped
public class BaseItemUiService {
    private Logger logger = Logger.getLogger(BaseItemUiService.class.getName());
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private ViewService viewService;
    @Inject
    private SelectionHandler selectionHandler;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private CockpitService cockpitService;
    @Inject
    private ItemCockpitService itemCockpitService;
    @Inject
    private ModalDialogManager modalDialogManager;
    @Inject
    private EffectVisualizationService effectVisualizationService;
    @Inject
    private UserUiService userUiService;
    @Inject
    private NativeMatrixFactory nativeMatrixFactory;
    @Inject
    private ExceptionHandler exceptionHandler;
    private final Map<Integer, PlayerBaseDto> bases = new HashMap<>();
    private Map<Integer, SyncBaseItemState> syncItemStates = new HashMap<>();
    private PlayerBaseDto myBase;
    private int resources;
    private int houseSpace;
    private int usedHouseSpace;
    private int itemCount;
    private boolean hasRadar;
    private NativeSyncBaseItemTickInfo[] nativeSyncBaseItemTickInfos = new NativeSyncBaseItemTickInfo[0];
    private MapList<BaseItemType, ModelMatrices> spawningModelMatrices = new MapList<>();
    private MapList<BaseItemType, ModelMatrices> buildupModelMatrices = new MapList<>();
    private MapList<BaseItemType, ModelMatrices> aliveModelMatrices = new MapList<>();
    private MapList<BaseItemType, ModelMatrices> demolitionModelMatrices = new MapList<>();
    private MapList<BaseItemType, ModelMatrices> harvestModelMatrices = new MapList<>();
    private MapList<BaseItemType, ModelMatrices> builderModelMatrices = new MapList<>();
    private MapList<BaseItemType, ModelMatrices> weaponTurretModelMatrices = new MapList<>();
    private long lastUpdateTimeStamp;
    private SyncBaseItemSetPositionMonitor syncBaseItemSetPositionMonitor;

    public void clear() {
        bases.clear();
        syncItemStates.clear();
        myBase = null;
        resources = 0;
        houseSpace = 0;
        usedHouseSpace = 0;
        itemCount = 0;
        nativeSyncBaseItemTickInfos = new NativeSyncBaseItemTickInfo[0];
        spawningModelMatrices.clear();
        buildupModelMatrices.clear();
        aliveModelMatrices.clear();
        demolitionModelMatrices.clear();
        harvestModelMatrices.clear();
        builderModelMatrices.clear();
        weaponTurretModelMatrices.clear();
        lastUpdateTimeStamp = 0;
        syncBaseItemSetPositionMonitor = null;
    }

    public Collection<BaseItemType> getBaseItemTypes() {
        return itemTypeService.getBaseItemTypes();
    }

    public List<ModelMatrices> provideSpawningModelMatrices(BaseItemType baseItemType) {
        return spawningModelMatrices.get(baseItemType);
    }

    public List<ModelMatrices> provideBuildupModelMatrices(BaseItemType baseItemType) {
        return buildupModelMatrices.get(baseItemType);
    }

    public List<ModelMatrices> provideAliveModelMatrices(BaseItemType baseItemType) {
        return aliveModelMatrices.get(baseItemType);
    }

    public List<ModelMatrices> provideDemolitionModelMatrices(BaseItemType baseItemType) {
        return demolitionModelMatrices.get(baseItemType);
    }

    public List<ModelMatrices> provideHarvestAnimationModelMatrices(BaseItemType baseItemType) {
        return harvestModelMatrices.get(baseItemType);
    }

    public List<ModelMatrices> provideBuildAnimationModelMatrices(BaseItemType baseItemType) {
        return builderModelMatrices.get(baseItemType);
    }

    public List<ModelMatrices> provideTurretModelMatrices(BaseItemType baseItemType) {
        return weaponTurretModelMatrices.get(baseItemType);
    }

    public void updateSyncBaseItems(NativeSyncBaseItemTickInfo[] nativeSyncBaseItemTickInfos) {
        // May be easier if replaced with SyncItemState and SyncItemMonitor
        lastUpdateTimeStamp = System.currentTimeMillis();
        this.nativeSyncBaseItemTickInfos = nativeSyncBaseItemTickInfos;
        spawningModelMatrices.clear();
        buildupModelMatrices.clear();
        aliveModelMatrices.clear();
        demolitionModelMatrices.clear();
        harvestModelMatrices.clear();
        builderModelMatrices.clear();
        weaponTurretModelMatrices.clear();
        int tmpItemCount = 0;
        int usedHouseSpace = 0;
        boolean radar = false;
        Polygon2D viewFieldCache = null;
        if (syncBaseItemSetPositionMonitor != null && viewService.getCurrentAabb() != null) {
            syncBaseItemSetPositionMonitor.init(viewService.getCurrentViewField().calculateCenter());
        }
        for (NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo : nativeSyncBaseItemTickInfos) {
            try {
                BaseItemType baseItemType = itemTypeService.getBaseItemType(nativeSyncBaseItemTickInfo.itemTypeId);
                DecimalPosition position2d = NativeUtil.toSyncBaseItemPosition2d(nativeSyncBaseItemTickInfo);
                Vertex position3d = NativeUtil.toSyncBaseItemPosition3d(nativeSyncBaseItemTickInfo);
                boolean isSpawning = nativeSyncBaseItemTickInfo.spawning < 1.0;
                boolean isBuildup = nativeSyncBaseItemTickInfo.buildup >= 1.0;
                boolean isHealthy = nativeSyncBaseItemTickInfo.health >= 1.0;


                if (isMyOwnProperty(nativeSyncBaseItemTickInfo)) {
                    tmpItemCount++;
                    usedHouseSpace += baseItemType.getConsumingHouseSpace();
                    if (baseItemType.getSpecialType() != null && baseItemType.getSpecialType().isMiniTerrain() && isBuildup && !isSpawning) {
                        radar = true;
                    }
                }
                updateSyncItemMonitor(nativeSyncBaseItemTickInfo);
                if (nativeSyncBaseItemTickInfo.contained) {
                    continue;
                }
                Color color = color4SyncBaseItem(nativeSyncBaseItemTickInfo);
                NativeMatrix modelMatrix = nativeMatrixFactory.createFromNativeMatrixDto(nativeSyncBaseItemTickInfo.model);
                if (viewService.getCurrentAabb() == null || !viewService.getCurrentAabb().adjoinsCircleExclusive(position2d, baseItemType.getPhysicalAreaConfig().getRadius())) {
                    // TODO move to worker
                    if (syncBaseItemSetPositionMonitor != null && viewService.getCurrentAabb() != null && isMyEnemy(nativeSyncBaseItemTickInfo) && !isSpawning && isBuildup) {
                        syncBaseItemSetPositionMonitor.notInViewAabb(nativeSyncBaseItemTickInfo.baseId, position2d, baseItemType);
                    }
                    continue;
                }
                boolean attackAble = true;
                // Spawning
                if (isSpawning && isBuildup) {
                    attackAble = false;
                    spawningModelMatrices.put(baseItemType, new ModelMatrices(modelMatrix, nativeSyncBaseItemTickInfo.spawning, null));
                }
                // Buildup
                if (!isSpawning && !isBuildup) {
                    attackAble = false;
                    buildupModelMatrices.put(baseItemType, new ModelMatrices(modelMatrix, nativeSyncBaseItemTickInfo.buildup, color));
                }
                // Alive
                if (!isSpawning && isBuildup && isHealthy) {
                    ModelMatrices modelMatrices = new ModelMatrices(modelMatrix, nativeSyncBaseItemTickInfo.interpolatableVelocity, color);
                    aliveModelMatrices.put(baseItemType, modelMatrices);
                    if (baseItemType.getWeaponType() != null && baseItemType.getWeaponType().getTurretType() != null) {
                        weaponTurretModelMatrices.put(baseItemType, new ModelMatrices(modelMatrices, nativeSyncBaseItemTickInfo.turretAngle));
                    }
                }
                if (syncBaseItemSetPositionMonitor != null && viewService.getCurrentAabb() != null && attackAble && isMyEnemy(nativeSyncBaseItemTickInfo)) {
                    if (viewFieldCache == null) {
                        viewFieldCache = viewService.getCurrentViewField().toPolygon();
                    }
                    if (viewFieldCache.isInside(position2d)) {
                        syncBaseItemSetPositionMonitor.inViewAabb(nativeSyncBaseItemTickInfo.baseId, position3d, baseItemType);
                    } else {
                        syncBaseItemSetPositionMonitor.notInViewAabb(nativeSyncBaseItemTickInfo.baseId, position2d, baseItemType);
                    }
                }

                // Demolition
                if (!isSpawning && isBuildup && !isHealthy) {
                    ModelMatrices modelMatrices = new ModelMatrices(modelMatrix, nativeSyncBaseItemTickInfo.interpolatableVelocity, nativeSyncBaseItemTickInfo.health, color);
                    demolitionModelMatrices.put(baseItemType, modelMatrices);
                    if (!baseItemType.getPhysicalAreaConfig().fulfilledMovable() && baseItemType.getDemolitionStepEffects() != null) {
                        effectVisualizationService.updateBuildingDemolitionEffect(nativeSyncBaseItemTickInfo, position3d, baseItemType);
                    }
                    if (baseItemType.getWeaponType() != null && baseItemType.getWeaponType().getTurretType() != null) {
                        weaponTurretModelMatrices.put(baseItemType, new ModelMatrices(modelMatrices, nativeSyncBaseItemTickInfo.turretAngle));
                    }
                }

                // Harvesting
                if (nativeSyncBaseItemTickInfo.harvestingResourcePosition != null) {
                    NativeVertexDto origin = modelMatrix.multiplyVertex(NativeUtil.toNativeVertex(baseItemType.getHarvesterType().getAnimationOrigin()), 1.0);
                    NativeVertexDto direction = NativeUtil.subAndNormalize(nativeSyncBaseItemTickInfo.harvestingResourcePosition, origin);
                    harvestModelMatrices.put(baseItemType, ModelMatrices.createFromPositionAndZRotation(origin, direction, nativeMatrixFactory));
                }
                // Building
                if (nativeSyncBaseItemTickInfo.buildingPosition != null) {
                    NativeVertexDto origin = modelMatrix.multiplyVertex(NativeUtil.toNativeVertex(baseItemType.getBuilderType().getAnimationOrigin()), 1.0);
                    NativeVertexDto direction = NativeUtil.subAndNormalize(nativeSyncBaseItemTickInfo.buildingPosition, origin);
                    builderModelMatrices.put(baseItemType, ModelMatrices.createFromPositionAndZRotation(origin, direction, nativeMatrixFactory));
                }
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        }
        if (itemCount != tmpItemCount) {
            itemCount = tmpItemCount;
            updateItemCountOnSideCockpit();
            itemCockpitService.onStateChanged();
        }
        if (this.usedHouseSpace != usedHouseSpace) {
            this.usedHouseSpace = usedHouseSpace;
            itemCockpitService.onStateChanged();
        }
        if (hasRadar != radar) {
            hasRadar = radar;
            gameUiControl.onRadarStateChanged(hasRadar);
        }
    }

    private void updateItemCountOnSideCockpit() {
        cockpitService.onItemCountChanged(itemCount, getMyTotalHouseSpace());
    }

    public void addBase(PlayerBaseDto playerBase) {
        synchronized (bases) {
            if (bases.put(playerBase.getBaseId(), playerBase) != null) {
                logger.warning("Base already exists: " + playerBase);
            }
            if (playerBase.getHumanPlayerId() != null && playerBase.getHumanPlayerId().equals(userUiService.getUserContext().getHumanPlayerId())) {
                myBase = playerBase;
                gameUiControl.onOwnBaseCreated();
            }
        }
    }

    public void removeBase(int baseId) {
        boolean wasMyBase = false;
        synchronized (bases) {
            if (bases.remove(baseId) == null) {
                logger.warning("BaseItemUiService.removeBase(): Base does not exist: " + baseId);
            }
            if (myBase != null && myBase.getBaseId() == baseId) {
                myBase = null;
                wasMyBase = true;
            }
        }
        if (wasMyBase) {
            selectionHandler.onMyBaseRemoved();
            modalDialogManager.onShowBaseLost();
            gameUiControl.onBaseLost();
        }
    }

    public void updateBase(PlayerBaseDto playerBaseDto) {
        synchronized (bases) {
            bases.put(playerBaseDto.getBaseId(), playerBaseDto);
        }
    }

    public PlayerBaseDto getBase(int baseId) {
        synchronized (bases) {
            PlayerBaseDto base = bases.get(baseId);
            if (base == null) {
                throw new IllegalArgumentException("No such base: " + baseId);
            }
            return base;
        }
    }

    public PlayerBaseDto getBase(SyncBaseItemSimpleDto syncBaseItem) {
        return getBase(syncBaseItem.getBaseId());
    }

    public boolean isMyOwnProperty(SyncBaseItemSimpleDto syncBaseItem) {
        return myBase != null && syncBaseItem.getBaseId() == myBase.getBaseId();
    }

    public boolean isMyOwnProperty(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo) {
        return myBase != null && nativeSyncBaseItemTickInfo.baseId == myBase.getBaseId();
    }

    public boolean isMyEnemy(SyncBaseItemSimpleDto syncBaseItem) {
        try {
            return getBase(syncBaseItem).getCharacter() == Character.BOT;
        } catch (Exception e) {
            // This may happen if own base gets lost and notified while items are still in syncItemStates variable
            // Occurs white BaseItemPlacer in GameUiControl restart base scenario after base is lost
            return true;
        }
    }

    public boolean isMyEnemy(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo) {
        try {
            return getBase(nativeSyncBaseItemTickInfo.baseId).getCharacter() == Character.BOT;
        } catch (Exception e) {
            // This may happen if own base gets lost and notified while items are still in syncItemStates variable
            // Occurs white BaseItemPlacer in GameUiControl restart base scenario after base is lost
            return true;
        }
    }

    public SyncBaseItemMonitor monitorSyncItem(int basItemId) {
        // Does not work here Arrays.stream(nativeSyncBaseItemTickInfos)
        for (NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo : nativeSyncBaseItemTickInfos) {
            if (nativeSyncBaseItemTickInfo.id == basItemId) {
                return monitorSyncItem(nativeSyncBaseItemTickInfo);
            }
        }
        throw new IllegalArgumentException("No NativeSyncBaseItemTickInfo for basItemId: " + basItemId);
    }

    public SyncBaseItemMonitor monitorSyncItem(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo) {
        double radius = itemTypeService.getBaseItemType(nativeSyncBaseItemTickInfo.itemTypeId).getPhysicalAreaConfig().getRadius();
        SyncBaseItemState syncBaseItemState = syncItemStates.computeIfAbsent(nativeSyncBaseItemTickInfo.id, k -> new SyncBaseItemState(nativeSyncBaseItemTickInfo, radius, this::releaseSyncItemMonitor));
        return (SyncBaseItemMonitor) syncBaseItemState.createSyncItemMonitor();
    }

    private void releaseSyncItemMonitor(SyncItemState syncItemState) {
        syncItemStates.remove(syncItemState.getSyncItemId());
    }

    private void updateSyncItemMonitor(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo) {
        SyncItemState syncItemState = syncItemStates.get(nativeSyncBaseItemTickInfo.id);
        if (syncItemState == null) {
            return;
        }
        syncItemState.update(nativeSyncBaseItemTickInfo, nativeSyncBaseItemTickInfo.interpolatableVelocity);
    }

    public SyncItemMonitor monitorMySyncBaseItemOfType(int itemTypeId) {
        NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo = findMyItemOfType(itemTypeId);
        if (nativeSyncBaseItemTickInfo != null) {
            return monitorSyncItem(nativeSyncBaseItemTickInfo);
        } else {
            return null;
        }
    }

    public SyncItemMonitor monitorMyEnemyItemWithPlace(PlaceConfig placeConfig) {
        NativeSyncBaseItemTickInfo enemy = findMyEnemyItemWithPlace(placeConfig);
        if (enemy != null) {
            return monitorSyncItem(enemy);
        } else {
            return null;
        }
    }

    public SyncBaseItemSetPositionMonitor createSyncItemSetPositionMonitor(Set<Integer> itemTypeFilter, Set<Integer> botIdFilter) {
        if (syncBaseItemSetPositionMonitor != null) {
            throw new IllegalStateException("BaseItemUiService.createSyncItemSetPositionMonitor() syncBaseItemSetPositionMonitor != null");
        }
        syncBaseItemSetPositionMonitor = new SyncBaseItemSetPositionMonitor(this, itemTypeFilter, botIdFilter, () -> syncBaseItemSetPositionMonitor = null);
        return syncBaseItemSetPositionMonitor;
    }

    public int getResources() {
        return resources;
    }

    public void updateGameInfo(NativeTickInfo nativeTickInfo) {
        if (resources != nativeTickInfo.resources) {
            resources = nativeTickInfo.resources;
            cockpitService.updateResource(resources);
            itemCockpitService.onResourcesChanged(resources);
        }
        if (houseSpace != nativeTickInfo.houseSpace) {
            houseSpace = nativeTickInfo.houseSpace;
            itemCockpitService.onStateChanged();
            updateItemCountOnSideCockpit();
        }
    }

    public boolean isMyLevelLimitation4ItemTypeExceeded(BaseItemType toBeBuiltType, int itemCount2Add) {
        return getMyItemCount(toBeBuiltType.getId()) + itemCount2Add > gameUiControl.getMyLimitation4ItemType(toBeBuiltType.getId());
    }

    public boolean isMyHouseSpaceExceeded(BaseItemType toBeBuiltType, int itemCount2Add) {
        return usedHouseSpace + itemCount2Add * toBeBuiltType.getConsumingHouseSpace() > getMyTotalHouseSpace();
    }

    public int getMyTotalHouseSpace() {
        return houseSpace + gameUiControl.getPlanetConfig().getHouseSpace();
    }

    public double setupInterpolationFactor() {
        return (double) (System.currentTimeMillis() - lastUpdateTimeStamp) / 1000.0;
    }

    public int getMyItemCount(int baseItemTypeId) {
        int count = 0;
        for (NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo : nativeSyncBaseItemTickInfos) {
            if (!isMyOwnProperty(nativeSyncBaseItemTickInfo)) {
                continue;
            }
            if (nativeSyncBaseItemTickInfo.itemTypeId == baseItemTypeId) {
                count++;
            }
        }
        return count;
    }

    private NativeSyncBaseItemTickInfo findMyItemOfType(int baseItemTypeId) {
        for (NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo : nativeSyncBaseItemTickInfos) {
            if (nativeSyncBaseItemTickInfo.contained) {
                continue;
            }
            if (!isMyOwnProperty(nativeSyncBaseItemTickInfo)) {
                continue;
            }
            if (nativeSyncBaseItemTickInfo.itemTypeId == baseItemTypeId) {
                return nativeSyncBaseItemTickInfo;
            }
        }
        return null;
    }

    private NativeSyncBaseItemTickInfo findMyEnemyItemWithPlace(PlaceConfig placeConfig) {
        for (NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo : nativeSyncBaseItemTickInfos) {
            if (nativeSyncBaseItemTickInfo.contained) {
                continue;
            }
            if (!isMyEnemy(nativeSyncBaseItemTickInfo)) {
                continue;
            }
            BaseItemType baseItemType = itemTypeService.getBaseItemType(nativeSyncBaseItemTickInfo.itemTypeId);
            if (placeConfig.checkInside(NativeUtil.toSyncBaseItemPosition2d(nativeSyncBaseItemTickInfo), baseItemType.getPhysicalAreaConfig().getRadius())) {
                return nativeSyncBaseItemTickInfo;
            }
        }
        return null;
    }


    public boolean hasEnemyForSpawn(DecimalPosition position, double enemyFreeRadius) {
        return findMyEnemyItemWithPlace(new PlaceConfig().setPosition(position).setRadius(enemyFreeRadius)) != null;
    }

    public boolean hasItemsInRangeInViewField(Collection<DecimalPosition> positions, double radius) {
        for (NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo : nativeSyncBaseItemTickInfos) {
            if (nativeSyncBaseItemTickInfo.contained) {
                continue;
            }
            double itemRadius = itemTypeService.getBaseItemType(nativeSyncBaseItemTickInfo.itemTypeId).getPhysicalAreaConfig().getRadius();
            for (DecimalPosition position : positions) {
                if (position.getDistance(nativeSyncBaseItemTickInfo.x, nativeSyncBaseItemTickInfo.y) < radius + itemRadius) {
                    return true;
                }
            }
        }
        return false;
    }

    public SyncBaseItemSimpleDto findItemAtPosition(DecimalPosition decimalPosition) {
        for (NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo : nativeSyncBaseItemTickInfos) {
            if (nativeSyncBaseItemTickInfo.contained) {
                continue;
            }
            BaseItemType baseItemType = itemTypeService.getBaseItemType(nativeSyncBaseItemTickInfo.itemTypeId);
            if (decimalPosition.getDistance(nativeSyncBaseItemTickInfo.x, nativeSyncBaseItemTickInfo.y) <= baseItemType.getPhysicalAreaConfig().getRadius()) {
                return SyncBaseItemSimpleDto.from(nativeSyncBaseItemTickInfo);
            }
        }
        return null;
    }

    public Collection<SyncBaseItemSimpleDto> findItemsInRect(Rectangle2D rectangle) {
        Collection<SyncBaseItemSimpleDto> result = new ArrayList<>();
        for (NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo : nativeSyncBaseItemTickInfos) {
            if (nativeSyncBaseItemTickInfo.contained) {
                continue;
            }
            BaseItemType baseItemType = itemTypeService.getBaseItemType(nativeSyncBaseItemTickInfo.itemTypeId);
            if (rectangle.adjoinsCircleExclusive(NativeUtil.toSyncBaseItemPosition2d(nativeSyncBaseItemTickInfo), baseItemType.getPhysicalAreaConfig().getRadius())) {
                result.add(SyncBaseItemSimpleDto.from(nativeSyncBaseItemTickInfo));
            }
        }
        return result;
    }

    public Collection<SyncBaseItemSimpleDto> findMyItemsOfType(int baseItemTypeId, boolean includeContained) {
        Collection<SyncBaseItemSimpleDto> result = new ArrayList<>();
        for (NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo : nativeSyncBaseItemTickInfos) {
            if (!includeContained && nativeSyncBaseItemTickInfo.contained) {
                continue;
            }
            if (!isMyOwnProperty(nativeSyncBaseItemTickInfo)) {
                continue;
            }
            if (nativeSyncBaseItemTickInfo.itemTypeId == baseItemTypeId) {
                result.add(SyncBaseItemSimpleDto.from(nativeSyncBaseItemTickInfo));
            }
        }
        return result;
    }

    public Collection<SyncBaseItemSimpleDto> findMyItems() {
        Collection<SyncBaseItemSimpleDto> result = new ArrayList<>();
        for (NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo : nativeSyncBaseItemTickInfos) {
            if (nativeSyncBaseItemTickInfo.contained) {
                continue;
            }
            if (isMyOwnProperty(nativeSyncBaseItemTickInfo)) {
                result.add(SyncBaseItemSimpleDto.from(nativeSyncBaseItemTickInfo));
            }
        }
        return result;
    }

    public NativeSyncBaseItemTickInfo[] getNativeSyncBaseItemTickInfos() {
        return nativeSyncBaseItemTickInfos;
    }

    public SyncBaseItemSimpleDto getSyncBaseItemSimpleDto4IdPlayback(int itemId) {
        return Arrays.stream(nativeSyncBaseItemTickInfos).filter(syncBaseItemSimpleDto -> syncBaseItemSimpleDto.id == itemId).map(nativeSyncBaseItemTickInfo -> {
            // Problem with NativeSyncBaseItemTickInfos and SyncBaseItemSimpleDto::from.
            try {
                return SyncBaseItemSimpleDto.from(nativeSyncBaseItemTickInfo);
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
                return null;
            }
        }).findFirst().orElse(null);
    }

    public boolean hasRadar() {
        return hasRadar;
    }

    public Color color4SyncBaseItem(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo) {
        if (isMyOwnProperty(nativeSyncBaseItemTickInfo)) {
            return Colors.OWN;
        } else if (isMyEnemy(nativeSyncBaseItemTickInfo)) {
            return Colors.ENEMY;
        } else {
            return Colors.FRIEND;
        }
    }

}
