package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSimpleSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeUtil;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncItemSimpleDto;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.Diplomacy;
import com.btxtech.uiservice.SelectionEvent;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.cockpit.MainCockpitService;
import com.btxtech.uiservice.cockpit.item.ItemCockpitService;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.btxtech.uiservice.effects.EffectVisualizationService;
import com.btxtech.uiservice.renderer.BabylonBaseItem;
import com.btxtech.uiservice.renderer.BabylonRendererService;
import com.btxtech.uiservice.renderer.MarkerConfig;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.user.UserUiService;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

import static com.btxtech.shared.gameengine.datatypes.workerdto.NativeUtil.toDecimalPosition;

/**
 * Created by Beat
 * 28.12.2015.
 * *
 */
@Singleton // This may lead to Errai problems
@JsType
public class BaseItemUiService {
    private final Logger logger = Logger.getLogger(BaseItemUiService.class.getName());
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private SelectionHandler selectionHandler;
    @Inject
    private Instance<GameUiControl> gameUiControl;
    @Inject
    private MainCockpitService cockpitService;
    @Inject
    private ItemCockpitService itemCockpitService;
    @Inject
    private ModalDialogManager modalDialogManager;
    @Inject
    private EffectVisualizationService effectVisualizationService;
    @Inject
    private UserUiService userUiService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private BabylonRendererService babylonRendererService;
    @Inject
    private AudioService audioService;
    private final Map<Integer, PlayerBaseDto> bases = new HashMap<>();
    private final Map<Integer, SyncBaseItemState> syncItemStates = new HashMap<>();
    private PlayerBaseDto myBase;
    private int resources;
    private int houseSpace;
    private int usedHouseSpace;
    private int itemCount;
    private boolean hasRadar;
    private NativeSyncBaseItemTickInfo[] nativeSyncBaseItemTickInfos = new NativeSyncBaseItemTickInfo[0];
    private final MapList<BaseItemType, ModelMatrices> spawningModelMatrices = new MapList<>();
    private final MapList<BaseItemType, ModelMatrices> buildupModelMatrices = new MapList<>();
    private final Map<Integer, BabylonBaseItem> babylonBaseItems = new HashMap<>();
    private final MapList<BaseItemType, ModelMatrices> harvestModelMatrices = new MapList<>();
    private final MapList<BaseItemType, ModelMatrices> builderModelMatrices = new MapList<>();
    private final MapList<BaseItemType, ModelMatrices> weaponTurretModelMatrices = new MapList<>();
    private long lastUpdateTimeStamp;
    private SyncBaseItemSetPositionMonitor syncBaseItemSetPositionMonitor;
    private final List<BabylonBaseItem> selectedBabylonBaseItems = new ArrayList<>();
    private final List<Integer> selectedOutOfViewIds = new ArrayList<>();
    private ViewField viewField;
    private Rectangle2D viewFieldAabb;

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
        babylonBaseItems.clear();
        harvestModelMatrices.clear();
        builderModelMatrices.clear();
        weaponTurretModelMatrices.clear();
        lastUpdateTimeStamp = 0;
        syncBaseItemSetPositionMonitor = null;
        selectedBabylonBaseItems.clear();
        selectedOutOfViewIds.clear();
    }

    public void updateSyncBaseItems(NativeSyncBaseItemTickInfo[] nativeSyncBaseItemTickInfos) {
        // May be easier if replaced with SyncItemState and SyncItemMonitor
        lastUpdateTimeStamp = System.currentTimeMillis();
        this.nativeSyncBaseItemTickInfos = nativeSyncBaseItemTickInfos;
        spawningModelMatrices.clear();
        buildupModelMatrices.clear();
        Collection<Integer> leftoversAliveBabylonBaseItems = new ArrayList<>(babylonBaseItems.keySet());
        harvestModelMatrices.clear();
        builderModelMatrices.clear();
        weaponTurretModelMatrices.clear();
        int tmpItemCount = 0;
        int usedHouseSpace = 0;
        boolean radar = false;
        DecimalPosition viewFiledCenter = viewField != null ? viewField.calculateCenter() : null;
        if (syncBaseItemSetPositionMonitor != null) {
            syncBaseItemSetPositionMonitor.setInvisibleSyncBaseItemTickInfo(null, null, null);
        }
        for (NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo : nativeSyncBaseItemTickInfos) {
            try {
                BaseItemType baseItemType = itemTypeService.getBaseItemType(nativeSyncBaseItemTickInfo.itemTypeId);
                DecimalPosition position2d = NativeUtil.toSyncBaseItemPosition2d(nativeSyncBaseItemTickInfo);
                if (position2d == null) {
                    continue;
                }
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
                if ((viewFieldAabb == null) || !viewFieldAabb.adjoinsCircleExclusive(position2d, baseItemType.getPhysicalAreaConfig().getRadius())) {
                    if (syncBaseItemSetPositionMonitor != null && isMyEnemy(nativeSyncBaseItemTickInfo)) {
                        syncBaseItemSetPositionMonitor.setInvisibleSyncBaseItemTickInfo(position2d, baseItemType, viewFiledCenter);
                    }
                    continue;
                }
                boolean attackAble = true;
                // Spawning
                if (isSpawning && isBuildup) {
                    attackAble = false;
                    // TODO spawningModelMatrices.put(baseItemType, new ModelMatrices(modelMatrix, nativeSyncBaseItemTickInfo.spawning, null));
                }
                // Buildup
                if (!isSpawning && !isBuildup) {
                    attackAble = false;
                    // TODO buildupModelMatrices.put(baseItemType, new ModelMatrices(modelMatrix, nativeSyncBaseItemTickInfo.buildup, color));
                }
                // Alive
                BabylonBaseItem babylonBaseItem = babylonBaseItems.get(nativeSyncBaseItemTickInfo.id);
                if (babylonBaseItem == null) {
                    babylonBaseItem = babylonRendererService.createSyncBaseItem(nativeSyncBaseItemTickInfo.id,
                            baseItemType,
                            diplomacy4SyncBaseItem(nativeSyncBaseItemTickInfo));
                    babylonBaseItems.put(nativeSyncBaseItemTickInfo.id, babylonBaseItem);
                    babylonBaseItem.setPosition(position2d);
                    babylonBaseItem.setAngle(nativeSyncBaseItemTickInfo.angle);
                    if (syncBaseItemSetPositionMonitor != null && attackAble && isMyEnemy(nativeSyncBaseItemTickInfo)) {
                        syncBaseItemSetPositionMonitor.addVisible(babylonBaseItem);
                    }
                    int selectedIndex = selectedOutOfViewIds.indexOf(nativeSyncBaseItemTickInfo.id);
                    if (selectedIndex >= 0) {
                        selectedOutOfViewIds.remove(selectedIndex);
                        selectedBabylonBaseItems.add(babylonBaseItem);
                        babylonBaseItem.select(true);
                    }
                }
                leftoversAliveBabylonBaseItems.remove(nativeSyncBaseItemTickInfo.id);

                if (babylonBaseItem.getPosition() != null) {
                    if (!babylonBaseItem.getPosition().equalsDelta(position2d, 0.000001)) {
                        babylonBaseItem.setPosition(position2d);
                    }
                } else {
                    babylonBaseItem.setPosition(position2d);
                }

                if (babylonBaseItem.getAngle() != nativeSyncBaseItemTickInfo.angle) {
                    babylonBaseItem.setAngle(nativeSyncBaseItemTickInfo.angle);
                }

                if (baseItemType.getWeaponType() != null && baseItemType.getWeaponType().getTurretType() != null) {
                    // TODO weaponTurretModelMatrices.put(baseItemType, new ModelMatrices(modelMatrices, nativeSyncBaseItemTickInfo.turretAngle));
                }

                // Demolition
                if (!isSpawning && isBuildup && !isHealthy) {
                    if (!baseItemType.getPhysicalAreaConfig().fulfilledMovable() && baseItemType.getDemolitionStepEffects() != null) {
                        effectVisualizationService.updateBuildingDemolitionEffect(nativeSyncBaseItemTickInfo, baseItemType);
                    }
                    if (baseItemType.getWeaponType() != null && baseItemType.getWeaponType().getTurretType() != null) {
                        // TODO weaponTurretModelMatrices.put(baseItemType, new ModelMatrices(modelMatrices, nativeSyncBaseItemTickInfo.turretAngle));
                    }
                }

                babylonBaseItem.setHealth(nativeSyncBaseItemTickInfo.health);
                babylonBaseItem.setHarvestingPosition(toDecimalPosition(nativeSyncBaseItemTickInfo.harvestingResourcePosition));
                babylonBaseItem.setBuildingPosition(toDecimalPosition(nativeSyncBaseItemTickInfo.buildingPosition));
                babylonBaseItem.setBuildup(nativeSyncBaseItemTickInfo.buildup);
                babylonBaseItem.setConstructing(nativeSyncBaseItemTickInfo.constructing);
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        }
        leftoversAliveBabylonBaseItems.forEach(id -> {
            BabylonBaseItem toRemove = babylonBaseItems.remove(id);
            if (syncBaseItemSetPositionMonitor != null) {
                syncBaseItemSetPositionMonitor.removeVisible(toRemove);
            }
            if (selectedBabylonBaseItems.remove(toRemove)) {
                selectedOutOfViewIds.add(id);
            }
            toRemove.dispose();
        });
        if (syncBaseItemSetPositionMonitor != null) {
            syncBaseItemSetPositionMonitor.handleOutOfView(viewFiledCenter);
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
            gameUiControl.get().onRadarStateChanged(hasRadar);
        }
    }

    public void onProjectileFired(int syncBaseItemId, DecimalPosition destination) {
        BabylonBaseItem babylonBaseItem = babylonBaseItems.get(syncBaseItemId);
        if (babylonBaseItem != null) {
            babylonBaseItem.onProjectileFired(destination);
            audioService.playAudioSafe(babylonBaseItem.getBaseItemType().getWeaponType().getMuzzleFlashAudioItemConfigId());
        }
    }

    public void onSyncBaseItemsExplode(NativeSimpleSyncBaseItemTickInfo[] nativeSimpleSyncBaseItemTickInfos) {
        for (NativeSimpleSyncBaseItemTickInfo nativeSimpleSyncBaseItemTickInfo : nativeSimpleSyncBaseItemTickInfos) {
            if (!nativeSimpleSyncBaseItemTickInfo.contained) {
                BabylonBaseItem babylonBaseItem = babylonBaseItems.get(nativeSimpleSyncBaseItemTickInfo.id);
                if (babylonBaseItem != null) {
                    babylonBaseItem.onExplode();
                    audioService.playAudioSafe(babylonBaseItem.getBaseItemType().getExplosionAudioItemConfigId());
                }
            }
        }
    }

    private void updateItemCountOnSideCockpit() {
        cockpitService.onItemCountChanged(itemCount, getMyTotalHouseSpace());
    }

    @SuppressWarnings("unused") // Used in angular
    public PlayerBaseDto[] getBases() {
        List<PlayerBaseDto> basesList;
        synchronized (bases) {
            basesList = new ArrayList<>(bases.values());
        }
        return basesList.toArray(new PlayerBaseDto[0]);
    }

    public void addBase(PlayerBaseDto playerBase) {
        synchronized (bases) {
            if (bases.put(playerBase.getBaseId(), playerBase) != null) {
                logger.warning("Base already exists: " + playerBase);
            }
            if (playerBase.getUserId() != null && playerBase.getUserId().equals(userUiService.getUserContext().getUserId())) {
                myBase = playerBase;
                gameUiControl.get().onOwnBaseCreated();
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
            gameUiControl.get().onBaseLost();
        }
    }

    public void updateBase(PlayerBaseDto playerBaseDto) {
        synchronized (bases) {
            bases.put(playerBaseDto.getBaseId(), playerBaseDto);
        }
    }

    @JsIgnore
    public PlayerBaseDto getBase(int baseId) {
        synchronized (bases) {
            PlayerBaseDto base = bases.get(baseId);
            if (base == null) {
                throw new IllegalArgumentException("No such base: " + baseId);
            }
            return base;
        }
    }

    @JsIgnore
    public PlayerBaseDto getBase(SyncBaseItemSimpleDto syncBaseItem) {
        return getBase(syncBaseItem.getBaseId());
    }

    @JsIgnore
    public boolean isMyOwnProperty(SyncBaseItemSimpleDto syncBaseItem) {
        return myBase != null && syncBaseItem.getBaseId() == myBase.getBaseId();
    }

    @JsIgnore
    public boolean isMyOwnProperty(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo) {
        return myBase != null && nativeSyncBaseItemTickInfo.baseId == myBase.getBaseId();
    }

    @JsIgnore
    public boolean isMyEnemy(SyncBaseItemSimpleDto syncBaseItem) {
        try {
            return getBase(syncBaseItem).getCharacter() == Character.BOT;
        } catch (Exception e) {
            // This may happen if own base gets lost and notified while items are still in syncItemStates variable
            // Occurs white BaseItemPlacer in GameUiControl restart base scenario after base is lost
            return true;
        }
    }

    @JsIgnore
    public boolean isMyEnemy(BabylonBaseItem babylonBaseItem) {
        return babylonBaseItem.isEnemy();
    }

    @JsIgnore
    public boolean isMyEnemy(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo) {
        try {
            return getBase(nativeSyncBaseItemTickInfo.baseId).getCharacter() == Character.BOT;
        } catch (Exception e) {
            // This may happen if own base gets lost and notified while items are still in syncItemStates variable
            // Occurs white BaseItemPlacer in GameUiControl restart base scenario after base is lost
            return true;
        }
    }

    @JsIgnore
    public SyncBaseItemMonitor monitorSyncItem(int basItemId) {
        // Does not work here Arrays.stream(nativeSyncBaseItemTickInfos)
        for (NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo : nativeSyncBaseItemTickInfos) {
            if (nativeSyncBaseItemTickInfo.id == basItemId) {
                return monitorSyncItem(nativeSyncBaseItemTickInfo);
            }
        }
        throw new IllegalArgumentException("No NativeSyncBaseItemTickInfo for basItemId: " + basItemId);
    }

    @JsIgnore
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
        syncItemState.update(nativeSyncBaseItemTickInfo);
    }

    public SyncBaseItemSetPositionMonitor createSyncItemSetPositionMonitor(MarkerConfig markerConfig, Set<Integer> itemTypeFilter, Set<Integer> botIdFilter) {
        if (syncBaseItemSetPositionMonitor != null) {
            throw new IllegalStateException("BaseItemUiService.createSyncItemSetPositionMonitor() syncBaseItemSetPositionMonitor != null");
        }
        syncBaseItemSetPositionMonitor = new SyncBaseItemSetPositionMonitor(babylonRendererService, markerConfig, itemTypeFilter, botIdFilter, () -> syncBaseItemSetPositionMonitor = null);
        babylonBaseItems.values().forEach(babylonBaseItem -> {
            if (isMyEnemy(babylonBaseItem)) {
                syncBaseItemSetPositionMonitor.addVisible(babylonBaseItem);
            }
        });
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
        return getMyItemCount(toBeBuiltType.getId()) + itemCount2Add > gameUiControl.get().getMyLimitation4ItemType(toBeBuiltType.getId());
    }

    public boolean isMyHouseSpaceExceeded(BaseItemType toBeBuiltType, int itemCount2Add) {
        return usedHouseSpace + itemCount2Add * toBeBuiltType.getConsumingHouseSpace() > getMyTotalHouseSpace();
    }

    public int getMyTotalHouseSpace() {
        return houseSpace + gameUiControl.get().getPlanetConfig().getHouseSpace();
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
        return findMyEnemyItemWithPlace(new PlaceConfig().position(position).radius(enemyFreeRadius)) != null;
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

    public SyncBaseItemSimpleDto getItem4Id(int baseItemId) {
        for (NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo : nativeSyncBaseItemTickInfos) {
            if (nativeSyncBaseItemTickInfo.id == baseItemId) {
                return SyncBaseItemSimpleDto.from(nativeSyncBaseItemTickInfo);
            }
        }
        throw new IllegalArgumentException("No NativeSyncBaseItemTickInfo for " + baseItemId);
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

    @SuppressWarnings("unused") // Called by Angular
    public NativeSyncBaseItemTickInfo[] getVisibleNativeSyncBaseItemTickInfos(DecimalPosition bottomLeft, DecimalPosition topRight) {
        List<NativeSyncBaseItemTickInfo> visibleNativeSyncBaseItemTickInfos = new ArrayList<>();
        Rectangle2D rectangle2D = new Rectangle2D(bottomLeft, topRight);
        for (NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo : nativeSyncBaseItemTickInfos) {
            if (nativeSyncBaseItemTickInfo.contained) {
                continue;
            }
            DecimalPosition position = NativeUtil.toSyncBaseItemPosition2d(nativeSyncBaseItemTickInfo);
            if (position == null || !rectangle2D.contains(position)) {
                continue;
            }
            visibleNativeSyncBaseItemTickInfos.add(nativeSyncBaseItemTickInfo);
        }

        return visibleNativeSyncBaseItemTickInfos.toArray(new NativeSyncBaseItemTickInfo[0]);
    }

    public boolean hasRadar() {
        return hasRadar;
    }

    @SuppressWarnings("unused") // Called by Angular
    public Diplomacy diplomacy4SyncBaseItem(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo) {
        if (isMyOwnProperty(nativeSyncBaseItemTickInfo)) {
            return Diplomacy.OWN;
        } else if (isMyEnemy(nativeSyncBaseItemTickInfo)) {
            return Diplomacy.ENEMY;
        } else {
            return Diplomacy.FRIEND;
        }
    }

    public void onViewChanged(ViewField viewField, Rectangle2D viewFieldAabb) {
        this.viewField = viewField;
        this.viewFieldAabb = viewFieldAabb;
    }

    public void onSelectionChanged(@Observes SelectionEvent selectionEvent) {
        selectedBabylonBaseItems.forEach(babylonBaseItem -> babylonBaseItem.select(false));
        selectedBabylonBaseItems.clear();
        selectedOutOfViewIds.clear();

        if (selectionEvent.getType() == SelectionEvent.Type.OWN) {
            selectionEvent.getSelectedGroup().getItems().stream()
                    .map(SyncItemSimpleDto::getId)
                    .map(id -> babylonBaseItems.get(id))
                    .filter(Objects::nonNull)
                    .forEach(babylonBaseItem -> {
                        selectedBabylonBaseItems.add(babylonBaseItem);
                        babylonBaseItem.select(true);
                    });
        } else if (selectionEvent.getType() == SelectionEvent.Type.OTHER) {
            BabylonBaseItem babylonBaseItem = babylonBaseItems.get(selectionEvent.getSelectedOther().getId());
            if (babylonBaseItem != null) {
                selectedBabylonBaseItems.add(babylonBaseItem);
                babylonBaseItem.select(true);
            }
        }
    }
}
