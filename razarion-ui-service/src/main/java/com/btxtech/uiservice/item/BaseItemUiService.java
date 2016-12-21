package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionShape3D;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionStepEffect;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.PlanetTickListener;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.model.SyncBuilder;
import com.btxtech.shared.gameengine.planet.model.SyncHarvester;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 28.12.2015.
 * *
 */
@Singleton
public class BaseItemUiService implements PlanetTickListener {
    // private Logger logger = Logger.getLogger(BaseItemUiService.class.getName());
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private ResourceService resourceService;
    @Inject
    private PlanetService planetService;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    private MapList<BaseItemType, ModelMatrices> spawningModelMatrices = new MapList<>();
    private MapList<BaseItemType, ModelMatrices> buildupModelMatrices = new MapList<>();
    private MapList<BaseItemType, ModelMatrices> aliveModelMatrices = new MapList<>();
    private MapList<BaseItemType, ModelMatrices> demolitionModelMatrices = new MapList<>();
    private MapList<Integer, ModelMatrices> demolitionEffectModelMatrices = new MapList<>();
    private MapList<BaseItemType, ModelMatrices> harvestModelMatrices = new MapList<>();
    private MapList<BaseItemType, ModelMatrices> builderModelMatrices = new MapList<>();
    private MapList<BaseItemType, ModelMatrices> weaponTurretModelMatrices = new MapList<>();

    @PostConstruct
    public void postConstruct() {
        planetService.addTickListener(this);
    }

    @Deprecated
    public VertexContainer getItemTypeVertexContainer(int id) {
        throw new UnsupportedOperationException();
        // return vertexContainers.get(id);
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

    @Override
    public void onPostTick() {
        spawningModelMatrices.clear();
        buildupModelMatrices.clear();
        aliveModelMatrices.clear();
        demolitionModelMatrices.clear();
        demolitionEffectModelMatrices.clear();
        harvestModelMatrices.clear();
        builderModelMatrices.clear();
        weaponTurretModelMatrices.clear();
        syncItemContainerService.iterateOverBaseItems(false, false, null, syncBaseItem -> {
            SyncPhysicalArea syncPhysicalArea = syncBaseItem.getSyncPhysicalArea();
            if (!terrainScrollHandler.getCurrentAabb().adjoinsCircleExclusive(syncPhysicalArea.getPosition2d(), syncPhysicalArea.getRadius())) {
                return null;
            }
            BaseItemType baseItemType = syncBaseItem.getBaseItemType();
            ModelMatrices modelMatrices = syncPhysicalArea.getModelMatrices();
            // Spawning
            if (syncBaseItem.isSpawning() && syncBaseItem.isBuildup()) {
                spawningModelMatrices.put(baseItemType, modelMatrices.copy(syncBaseItem.getSpawnProgress()));
            }
            // Buildup
            if (!syncBaseItem.isSpawning() && !syncBaseItem.isBuildup()) {
                buildupModelMatrices.put(baseItemType, modelMatrices.copy(syncBaseItem.getBuildup()));
            }
            // Alive
            if (!syncBaseItem.isSpawning() && syncBaseItem.isBuildup() && syncBaseItem.isHealthy()) {
                aliveModelMatrices.put(baseItemType, modelMatrices);
                if (syncBaseItem.getSyncWeapon() != null && syncBaseItem.getSyncWeapon().getSyncTurret() != null) {
                    weaponTurretModelMatrices.put(baseItemType, syncBaseItem.getSyncWeapon().createTurretModelMatrices4Shape3D());
                }
            }
            // Demolition
            if (!syncBaseItem.isSpawning() && syncBaseItem.isBuildup() && !syncBaseItem.isHealthy()) {
                demolitionModelMatrices.put(baseItemType, modelMatrices.copy(syncBaseItem.getNormalizedHealth()));
                DemolitionStepEffect demolitionStepEffect = baseItemType.getDemolitionStepEffect(syncBaseItem.getNormalizedHealth());
                if (demolitionStepEffect != null && demolitionStepEffect.getDemolitionShape3Ds() != null) {
                    for (DemolitionShape3D demolitionShape3D : demolitionStepEffect.getDemolitionShape3Ds()) {
                        if (demolitionShape3D.getShape3DId() != null) {
                            demolitionEffectModelMatrices.put(demolitionShape3D.getShape3DId(), modelMatrices.multiply(Matrix4.createTranslation(demolitionShape3D.getPosition())));
                        }
                    }
                }
            }

            // Harvesting
            SyncHarvester harvester = syncBaseItem.getSyncHarvester();
            if (harvester != null && harvester.isHarvesting()) {
                Vertex origin = modelMatrices.getModel().multiply(baseItemType.getHarvesterType().getAnimationOrigin(), 1.0);
                Vertex direction = harvester.getResource().getSyncPhysicalArea().getPosition3d().sub(origin).normalize(1.0);
                harvestModelMatrices.put(baseItemType, ModelMatrices.createFromPositionAndZRotation(origin, direction));
            }
            // Building
            SyncBuilder builder = syncBaseItem.getSyncBuilder();
            if (builder != null && builder.isBuilding()) {
                Vertex origin = modelMatrices.getModel().multiply(baseItemType.getBuilderType().getAnimationOrigin(), 1.0);
                Vertex direction = builder.getCurrentBuildup().getSyncPhysicalArea().getPosition3d().sub(origin).normalize(1.0);
                builderModelMatrices.put(baseItemType, ModelMatrices.createFromPositionAndZRotation(origin, direction));
            }
            // Turret
            return null;
        });
    }
}
