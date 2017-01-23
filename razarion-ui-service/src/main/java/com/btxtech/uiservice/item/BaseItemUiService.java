package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionShape3D;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionStepEffect;
import com.btxtech.shared.gameengine.datatypes.workerdto.GameInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncItemSimpleDto;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.cockpit.CockpitService;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.btxtech.uiservice.terrain.TerrainUiService;

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
    private TerrainUiService terrainUiService;
    @Inject
    private ResourceService resourceService;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    @Inject
    private SelectionHandler selectionHandler;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private CockpitService cockpitService;
    @Inject
    private ItemCockpitService itemCockpitService;
    private final Map<Integer, PlayerBaseDto> bases = new HashMap<>();
    private Map<Integer, SyncItemMonitor> syncItemMonitors = new HashMap<>();
    private PlayerBaseDto myBase;
    private int resources;
    private int usedHouseSpace;
    private int houseSpace;
    private int itemCount;
    private Collection<SyncBaseItemSimpleDto> syncBaseItems = new ArrayList<>();
    private MapList<BaseItemType, ModelMatrices> spawningModelMatrices = new MapList<>();
    private MapList<BaseItemType, ModelMatrices> buildupModelMatrices = new MapList<>();
    private MapList<BaseItemType, ModelMatrices> aliveModelMatrices = new MapList<>();
    private MapList<BaseItemType, ModelMatrices> demolitionModelMatrices = new MapList<>();
    private MapList<Integer, ModelMatrices> demolitionEffectModelMatrices = new MapList<>();
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

    public List<ModelMatrices> provideDemolitionEffectModelMatrices(Integer shape3DId) {
        return demolitionEffectModelMatrices.get(shape3DId);
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
        lastUpdateTimeStamp = System.currentTimeMillis();
        this.syncBaseItems = syncBaseItems;
        spawningModelMatrices.clear();
        buildupModelMatrices.clear();
        aliveModelMatrices.clear();
        demolitionModelMatrices.clear();
        demolitionEffectModelMatrices.clear();
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
            if (!terrainScrollHandler.getCurrentAabb().adjoinsCircleExclusive(syncBaseItem.getPosition2d(), baseItemType.getPhysicalAreaConfig().getRadius())) {
                // TODO move to worker
                continue;
            }
            // Spawning
            if (syncBaseItem.checkSpawning() && syncBaseItem.checkBuildup()) {
                spawningModelMatrices.put(baseItemType, new ModelMatrices(syncBaseItem.getModel(), syncBaseItem.getSpawning()));
            }
            // Buildup
            if (!syncBaseItem.checkSpawning() && !syncBaseItem.checkBuildup()) {
                buildupModelMatrices.put(baseItemType, new ModelMatrices(syncBaseItem.getModel(), syncBaseItem.getBuildup()));
            }
            // Alive
            if (!syncBaseItem.checkSpawning() && syncBaseItem.checkBuildup() && syncBaseItem.checkHealth()) {
                aliveModelMatrices.put(baseItemType, new ModelMatrices(syncBaseItem.getModel()).setInterpolatableVelocity(syncBaseItem.getInterpolatableVelocity()));
                if (syncBaseItem.getWeaponTurret() != null) {
                    weaponTurretModelMatrices.put(baseItemType, new ModelMatrices(syncBaseItem.getWeaponTurret()));
                }
            }
            // Demolition
            if (!syncBaseItem.checkSpawning() && syncBaseItem.checkBuildup() && !syncBaseItem.checkHealth()) {
                ModelMatrices modelMatrices = new ModelMatrices(syncBaseItem.getModel(), syncBaseItem.getHealth()).setInterpolatableVelocity(syncBaseItem.getInterpolatableVelocity());
                demolitionModelMatrices.put(baseItemType, modelMatrices);
                DemolitionStepEffect demolitionStepEffect = baseItemType.getDemolitionStepEffect(syncBaseItem.getHealth());
                if (demolitionStepEffect != null && demolitionStepEffect.getDemolitionShape3Ds() != null) {
                    for (DemolitionShape3D demolitionShape3D : demolitionStepEffect.getDemolitionShape3Ds()) {
                        if (demolitionShape3D.getShape3DId() != null) {
                            demolitionEffectModelMatrices.put(demolitionShape3D.getShape3DId(), modelMatrices.multiply(Matrix4.createTranslation(demolitionShape3D.getPosition())));
                        }
                    }
                }
            }

            // Harvesting
            if (syncBaseItem.getHarvestingResourcePosition() != null) {
                Vertex origin = syncBaseItem.getModel().multiply(baseItemType.getHarvesterType().getAnimationOrigin(), 1.0);
                Vertex direction = syncBaseItem.getHarvestingResourcePosition().sub(origin).normalize(1.0);
                harvestModelMatrices.put(baseItemType, ModelMatrices.createFromPositionAndZRotation(origin, direction));
            }
            // Building
            if (syncBaseItem.getBuildingPosition() != null) {
                Vertex origin = syncBaseItem.getModel().multiply(baseItemType.getBuilderType().getAnimationOrigin(), 1.0);
                Vertex direction = syncBaseItem.getBuildingPosition().sub(origin).normalize(1.0);
                builderModelMatrices.put(baseItemType, ModelMatrices.createFromPositionAndZRotation(origin, direction));
            }
        }
        if (itemCount != tmpItemCount) {
            itemCount = tmpItemCount;
            cockpitService.onItemCountChanged(itemCount);
            itemCockpitService.onStateChanged();
        }
    }

    public void addBase(PlayerBaseDto playerBase) {
        synchronized (bases) {
            if (bases.put(playerBase.getBaseId(), playerBase) != null) {
                logger.warning("Base already exists: " + playerBase);
            }
            if (playerBase.getUserId() != null && playerBase.getUserId() == gameUiControl.getUserContext().getUserId()) {
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

    public boolean isEnemy(SyncBaseItemSimpleDto syncBaseItem) {
        return getBase(syncBaseItem).getCharacter().isEnemy(getMyBase().getCharacter());
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

    public SyncItemMonitor monitorSyncItem(SyncItemSimpleDto syncItemSimpleDto) {
        SyncItemMonitor syncItemMonitor = syncItemMonitors.computeIfAbsent(syncItemSimpleDto.getId(), k -> new SyncItemMonitor(syncItemSimpleDto, this::releaseSyncItemMonitor));
        syncItemMonitor.increaseMonitorCount();
        return syncItemMonitor;
    }

    private void releaseSyncItemMonitor(SyncItemMonitor syncItemMonitor) {
        syncItemMonitors.remove(syncItemMonitor.getSyncItemId());
    }

    private void updateSyncItemMonitor(SyncBaseItemSimpleDto syncBaseItem) {
        SyncItemMonitor syncItemMonitor = syncItemMonitors.get(syncBaseItem.getId());
        if(syncItemMonitor == null) {
            return;
        }
        syncItemMonitor.update(syncBaseItem);
    }

    public SyncItemMonitor monitorMySyncBaseItemOfType(int itemTypeId) {
        SyncItemSimpleDto syncItemSimpleDto = findMyItemOfType(itemTypeId);
        if (syncItemSimpleDto != null) {
            return monitorSyncItem(syncItemSimpleDto);
        } else {
            return null;
        }
    }

    public SyncItemMonitor monitorEnemyItemWithPlace(PlaceConfig placeConfig) {
        SyncBaseItemSimpleDto enemy = findEnemyItemWithPlace(placeConfig);
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
        return usedHouseSpace + itemCount2Add * toBeBuiltType.getConsumingHouseSpace() > houseSpace + gameUiControl.getPlanetConfig().getHouseSpace();
    }

    public double setupInterpolationFactor() {
        return (double) (System.currentTimeMillis() - lastUpdateTimeStamp) / 1000.0;
    }

    public int getMyItemCount(int itemTypeId) {
        return findMyItemsOfType(itemTypeId).size();
    }

    private SyncItemSimpleDto findMyItemOfType(int baseItemTypeId) {
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

    private SyncBaseItemSimpleDto findEnemyItemWithPlace(PlaceConfig placeConfig) {
        for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
            if (!isEnemy(syncBaseItem)) {
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
