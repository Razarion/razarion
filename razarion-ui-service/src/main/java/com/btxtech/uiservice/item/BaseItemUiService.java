package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.GameInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.cockpit.CockpitService;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.btxtech.uiservice.effects.EffectVisualizationService;
import com.btxtech.uiservice.nativejs.NativeMatrixFactory;
import com.btxtech.uiservice.renderer.ViewService;
import com.btxtech.uiservice.user.UserUiService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
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
    private final Map<Integer, PlayerBaseDto> bases = new HashMap<>();
    private Map<Integer, SyncBaseItemState> syncItemStates = new HashMap<>();
    private PlayerBaseDto myBase;
    private int resources;
    private int houseSpace;
    private int usedHouseSpace;
    private int itemCount;
    private boolean hasRadar;
    private Collection<SyncBaseItemSimpleDto> syncBaseItems = new ArrayList<>();
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
        syncBaseItems.clear();
        spawningModelMatrices.clear();
        buildupModelMatrices.clear();
        aliveModelMatrices.clear();
        demolitionModelMatrices.clear();
        harvestModelMatrices.clear();
        builderModelMatrices.clear();
        weaponTurretModelMatrices.clear();
        lastUpdateTimeStamp = 0;
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

    public void updateSyncBaseItems(Collection<SyncBaseItemSimpleDto> syncBaseItems) {
        // May be easier if replaced with SyncItemState and SyncItemMonitor
        lastUpdateTimeStamp = System.currentTimeMillis();
        this.syncBaseItems = syncBaseItems;
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
        for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
            BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
            if (isMyOwnProperty(syncBaseItem)) {
                tmpItemCount++;
                usedHouseSpace += baseItemType.getConsumingHouseSpace();
                if (baseItemType.getSpecialType() != null && baseItemType.getSpecialType().isMiniTerrain() && syncBaseItem.checkBuildup() && !syncBaseItem.checkSpawning()) {
                    radar = true;
                }
            }
            updateSyncItemMonitor(syncBaseItem);
            if (viewService.getCurrentAabb() == null || !viewService.getCurrentAabb().adjoinsCircleExclusive(syncBaseItem.getPosition2d(), baseItemType.getPhysicalAreaConfig().getRadius())) {
                // TODO move to worker
                if (syncBaseItemSetPositionMonitor != null && viewService.getCurrentAabb() != null && isMyEnemy(syncBaseItem) && !syncBaseItem.checkSpawning() && syncBaseItem.checkBuildup()) {
                    syncBaseItemSetPositionMonitor.notInViewAabb(syncBaseItem, baseItemType);
                }
                continue;
            }
            boolean attackAble = true;
            // Spawning
            if (syncBaseItem.checkSpawning() && syncBaseItem.checkBuildup()) {
                attackAble = false;
                spawningModelMatrices.put(baseItemType, new ModelMatrices(syncBaseItem.getModel(), syncBaseItem.getSpawning(), nativeMatrixFactory));
            }
            // Buildup
            if (!syncBaseItem.checkSpawning() && !syncBaseItem.checkBuildup()) {
                attackAble = false;
                buildupModelMatrices.put(baseItemType, new ModelMatrices(syncBaseItem.getModel(), syncBaseItem.getBuildup(), nativeMatrixFactory));
            }
            // Alive
            if (!syncBaseItem.checkSpawning() && syncBaseItem.checkBuildup() && syncBaseItem.checkHealth()) {
                aliveModelMatrices.put(baseItemType, new ModelMatrices(syncBaseItem.getModel(), syncBaseItem.getInterpolatableVelocity(), nativeMatrixFactory));
                if (syncBaseItem.getWeaponTurret() != null) {
                    weaponTurretModelMatrices.put(baseItemType, new ModelMatrices(syncBaseItem.getWeaponTurret(), syncBaseItem.getInterpolatableVelocity(), nativeMatrixFactory));
                }
            }
            if (syncBaseItemSetPositionMonitor != null && viewService.getCurrentAabb() != null && attackAble && isMyEnemy(syncBaseItem)) {
                if (viewFieldCache == null) {
                    viewFieldCache = viewService.getCurrentViewField().toPolygon();
                }
                if (viewFieldCache.isInside(syncBaseItem.getPosition2d())) {
                    syncBaseItemSetPositionMonitor.inViewAabb(syncBaseItem, baseItemType);
                } else {
                    syncBaseItemSetPositionMonitor.notInViewAabb(syncBaseItem, baseItemType);
                }
            }

            // Demolition
            if (!syncBaseItem.checkSpawning() && syncBaseItem.checkBuildup() && !syncBaseItem.checkHealth()) {
                ModelMatrices modelMatrices = new ModelMatrices(syncBaseItem.getModel(), syncBaseItem.getInterpolatableVelocity(), syncBaseItem.getHealth(), nativeMatrixFactory);
                demolitionModelMatrices.put(baseItemType, modelMatrices);
                if (!baseItemType.getPhysicalAreaConfig().fulfilledMovable() && baseItemType.getDemolitionStepEffects() != null) {
                    effectVisualizationService.updateBuildingDemolitionEffect(syncBaseItem, baseItemType);
                }
                if (syncBaseItem.getWeaponTurret() != null) {
                    weaponTurretModelMatrices.put(baseItemType, new ModelMatrices(syncBaseItem.getWeaponTurret(), syncBaseItem.getInterpolatableVelocity(), nativeMatrixFactory));
                }
            }

            // Harvesting
            if (syncBaseItem.getHarvestingResourcePosition() != null) {
                Vertex origin = syncBaseItem.getModel().multiply(baseItemType.getHarvesterType().getAnimationOrigin(), 1.0);
                Vertex direction = syncBaseItem.getHarvestingResourcePosition().sub(origin).normalize(1.0);
                harvestModelMatrices.put(baseItemType, ModelMatrices.createFromPositionAndZRotation(origin, direction, nativeMatrixFactory));
            }
            // Building
            if (syncBaseItem.getBuildingPosition() != null) {
                Vertex origin = syncBaseItem.getModel().multiply(baseItemType.getBuilderType().getAnimationOrigin(), 1.0);
                Vertex direction = syncBaseItem.getBuildingPosition().sub(origin).normalize(1.0);
                builderModelMatrices.put(baseItemType, ModelMatrices.createFromPositionAndZRotation(origin, direction, nativeMatrixFactory));
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

    public boolean isMyEnemy(SyncBaseItemSimpleDto syncBaseItem) {
        try {
            return getBase(syncBaseItem).getCharacter() == Character.BOT;
        } catch (Exception e) {
            // This may happen if own base gets lost and notified while items are still in syncItemStates variable
            // Occurs white BaseItemPlacer in GameUiControl restart base scenario after base is lost
            return true;
        }
    }

    public SyncBaseItemMonitor monitorSyncItem(SyncBaseItemSimpleDto syncBaseItemSimpleDto) {
        double radius = itemTypeService.getBaseItemType(syncBaseItemSimpleDto.getItemTypeId()).getPhysicalAreaConfig().getRadius();
        SyncBaseItemState syncBaseItemState = syncItemStates.computeIfAbsent(syncBaseItemSimpleDto.getId(), k -> new SyncBaseItemState(syncBaseItemSimpleDto, syncBaseItemSimpleDto.getInterpolatableVelocity(), radius, this::releaseSyncItemMonitor));
        return (SyncBaseItemMonitor) syncBaseItemState.createSyncItemMonitor();
    }

    private void releaseSyncItemMonitor(SyncItemState syncItemState) {
        syncItemStates.remove(syncItemState.getSyncItemId());
    }

    private void updateSyncItemMonitor(SyncBaseItemSimpleDto syncBaseItem) {
        SyncItemState syncItemState = syncItemStates.get(syncBaseItem.getId());
        if (syncItemState == null) {
            return;
        }
        syncItemState.update(syncBaseItem, syncBaseItem.getInterpolatableVelocity());
    }

    public SyncItemMonitor monitorMySyncBaseItemOfType(int itemTypeId) {
        SyncBaseItemSimpleDto syncBaseItem = findMyItemOfType(itemTypeId);
        if (syncBaseItem != null) {
            return monitorSyncItem(syncBaseItem);
        } else {
            return null;
        }
    }

    public SyncItemMonitor monitorMyEnemyItemWithPlace(PlaceConfig placeConfig) {
        SyncBaseItemSimpleDto enemy = findMyEnemyItemWithPlace(placeConfig);
        if (enemy != null) {
            return monitorSyncItem(enemy);
        } else {
            return null;
        }
    }

    public SyncBaseItemSetPositionMonitor createSyncItemSetPositionMonitor(Set<Integer> itemTypeFilter) {
        if (syncBaseItemSetPositionMonitor != null) {
            throw new IllegalStateException("BaseItemUiService.createSyncItemSetPositionMonitor() syncBaseItemSetPositionMonitor != null");
        }
        syncBaseItemSetPositionMonitor = new SyncBaseItemSetPositionMonitor(itemTypeFilter, () -> syncBaseItemSetPositionMonitor = null);
        return syncBaseItemSetPositionMonitor;
    }

    public int getResources() {
        return resources;
    }

    public void updateGameInfo(GameInfo gameInfo) {
        if (resources != gameInfo.getResources()) {
            resources = gameInfo.getResources();
            cockpitService.updateResource(resources);
            itemCockpitService.onResourcesChanged(resources);
        }
        if (houseSpace != gameInfo.getHouseSpace()) {
            houseSpace = gameInfo.getHouseSpace();
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

    public int getMyItemCount(int itemTypeId) {
        return findMyItemsOfType(itemTypeId).size();
    }

    private SyncBaseItemSimpleDto findMyItemOfType(int baseItemTypeId) {
        for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
            if (!isMyOwnProperty(syncBaseItem)) {
                continue;
            }
            BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
            if (baseItemType.getId() == baseItemTypeId) {
                return syncBaseItem;
            }
        }
        return null;
    }

    private SyncBaseItemSimpleDto findMyEnemyItemWithPlace(PlaceConfig placeConfig) {
        for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
            if (!isMyEnemy(syncBaseItem)) {
                continue;
            }
            BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
            if (placeConfig.checkInside(syncBaseItem.getPosition2d(), baseItemType.getPhysicalAreaConfig().getRadius())) {
                return syncBaseItem;
            }
        }
        return null;
    }


    public boolean hasEnemyForSpawn(DecimalPosition position, double enemyFreeRadius) {
        return findMyEnemyItemWithPlace(new PlaceConfig().setPosition(position).setRadius(enemyFreeRadius)) != null;
    }

    public boolean hasItemsInRange(Collection<DecimalPosition> positions, double radius) {
        for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
            double itemRadius = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId()).getPhysicalAreaConfig().getRadius();
            for (DecimalPosition position : positions) {
                if (syncBaseItem.getPosition2d().getDistance(position) < radius + itemRadius) {
                    return true;
                }

            }
        }
        return false;
    }

    public SyncBaseItemSimpleDto findItemAtPosition(DecimalPosition decimalPosition) {
        for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
            BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
            if (syncBaseItem.getPosition2d().getDistance(decimalPosition) <= baseItemType.getPhysicalAreaConfig().getRadius()) {
                return syncBaseItem;
            }
        }
        return null;
    }

    public Collection<SyncBaseItemSimpleDto> findItemsInRect(Rectangle2D rectangle) {
        Collection<SyncBaseItemSimpleDto> result = new ArrayList<>();
        for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
            BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
            if (rectangle.adjoinsCircleExclusive(syncBaseItem.getPosition2d(), baseItemType.getPhysicalAreaConfig().getRadius())) {
                result.add(syncBaseItem);
            }
        }
        return result;
    }

    public Collection<SyncBaseItemSimpleDto> findMyItemsOfType(int baseItemTypeId) {
        Collection<SyncBaseItemSimpleDto> result = new ArrayList<>();
        for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
            if (!isMyOwnProperty(syncBaseItem)) {
                continue;
            }
            BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
            if (baseItemType.getId() == baseItemTypeId) {
                result.add(syncBaseItem);
            }
        }
        return result;
    }

    public Collection<SyncBaseItemSimpleDto> findMyItems() {
        Collection<SyncBaseItemSimpleDto> result = new ArrayList<>();
        for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
            if (isMyOwnProperty(syncBaseItem)) {
                result.add(syncBaseItem);
            }
        }
        return result;
    }

    public Collection<SyncBaseItemSimpleDto> getSyncBaseItems() {
        return syncBaseItems;
    }

    public boolean hasRadar() {
        return hasRadar;
    }
}
