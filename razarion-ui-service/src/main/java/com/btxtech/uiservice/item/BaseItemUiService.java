package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.GameInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.cockpit.CockpitService;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.btxtech.uiservice.effects.EffectVisualizationService;
import com.btxtech.uiservice.nativejs.NativeMatrixFactory;
import com.btxtech.uiservice.renderer.ViewService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.uiservice.user.UserUiService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private Collection<SyncBaseItemSimpleDto> syncBaseItems = new ArrayList<>();
    private MapList<BaseItemType, ModelMatrices> spawningModelMatrices = new MapList<>();
    private MapList<BaseItemType, ModelMatrices> buildupModelMatrices = new MapList<>();
    private MapList<BaseItemType, ModelMatrices> aliveModelMatrices = new MapList<>();
    private MapList<BaseItemType, ModelMatrices> demolitionModelMatrices = new MapList<>();
    private MapList<BaseItemType, ModelMatrices> harvestModelMatrices = new MapList<>();
    private MapList<BaseItemType, ModelMatrices> builderModelMatrices = new MapList<>();
    private MapList<BaseItemType, ModelMatrices> weaponTurretModelMatrices = new MapList<>();
    private long lastUpdateTimeStamp;

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
        for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
            if (isMyOwnProperty(syncBaseItem)) {
                tmpItemCount++;
            }
            updateSyncItemMonitor(syncBaseItem);
            BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
            if (viewService.getCurrentAabb() == null || !viewService.getCurrentAabb().adjoinsCircleExclusive(syncBaseItem.getPosition2d(), baseItemType.getPhysicalAreaConfig().getRadius())) {
                // TODO move to worker
                continue;
            }
            // Spawning
            if (syncBaseItem.checkSpawning() && syncBaseItem.checkBuildup()) {
                spawningModelMatrices.put(baseItemType, new ModelMatrices(syncBaseItem.getModel(), syncBaseItem.getSpawning(), nativeMatrixFactory));
            }
            // Buildup
            if (!syncBaseItem.checkSpawning() && !syncBaseItem.checkBuildup()) {
                buildupModelMatrices.put(baseItemType, new ModelMatrices(syncBaseItem.getModel(), syncBaseItem.getBuildup(), nativeMatrixFactory));
            }
            // Alive
            if (!syncBaseItem.checkSpawning() && syncBaseItem.checkBuildup() && syncBaseItem.checkHealth()) {
                aliveModelMatrices.put(baseItemType, new ModelMatrices(syncBaseItem.getModel(), syncBaseItem.getInterpolatableVelocity(), nativeMatrixFactory));
                if (syncBaseItem.getWeaponTurret() != null) {
                    weaponTurretModelMatrices.put(baseItemType, new ModelMatrices(syncBaseItem.getWeaponTurret(), syncBaseItem.getInterpolatableVelocity(), nativeMatrixFactory));
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
            updateItemCountOnSideCockput();
            itemCockpitService.onStateChanged();
        }
    }

    private void updateItemCountOnSideCockput() {
        cockpitService.onItemCountChanged(itemCount, getMyTotalHouseSpace());
    }

    public void addBase(PlayerBaseDto playerBase) {
        synchronized (bases) {
            if (bases.put(playerBase.getBaseId(), playerBase) != null) {
                logger.warning("Base already exists: " + playerBase);
            }
            if (playerBase.getUserId() != null && playerBase.getUserId() == userUiService.getUserContext().getUserId()) {
                myBase = playerBase;
            }
        }
    }

    public void removeBase(int baseId) {
        boolean wasMyBase = false;
        synchronized (bases) {
            if (bases.remove(baseId) == null) {
                logger.warning("Base does not exist already exists: " + baseId);
            }
            if (myBase != null && myBase.getBaseId() == baseId) {
                myBase = null;
                wasMyBase = true;
            }
        }
        if (wasMyBase) {
            selectionHandler.onMyBaseRemoved();
            modalDialogManager.onShowBaseLost();
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

    public PlayerBaseDto getMyBase() {
        return myBase;
    }

    public boolean isMyOwnProperty(SyncBaseItemSimpleDto syncBaseItem) {
        return myBase != null && syncBaseItem.getBaseId() == myBase.getBaseId();
    }

    public boolean isMyEnemy(SyncBaseItemSimpleDto syncBaseItem) {
        return getBase(syncBaseItem).getCharacter() == Character.BOT;
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
            updateItemCountOnSideCockput();
        }
        if (usedHouseSpace != gameInfo.getUsedHouseSpace()) {
            usedHouseSpace = gameInfo.getUsedHouseSpace();
            itemCockpitService.onStateChanged();
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

}
