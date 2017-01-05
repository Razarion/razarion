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
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.btxtech.uiservice.terrain.TerrainUiService;

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
public class BaseItemUiService {
    // private Logger logger = Logger.getLogger(BaseItemUiService.class.getName());
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private TerrainUiService terrainUiService;
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

    public void updateSyncBaseItems(Collection<SyncBaseItemSimpleDto> syncBaseItems) {
        spawningModelMatrices.clear();
        buildupModelMatrices.clear();
        aliveModelMatrices.clear();
        demolitionModelMatrices.clear();
        demolitionEffectModelMatrices.clear();
        harvestModelMatrices.clear();
        builderModelMatrices.clear();
        weaponTurretModelMatrices.clear();
        for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
            BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getBaseItemTypeId());
            if (!terrainScrollHandler.getCurrentAabb().adjoinsCircleExclusive(syncBaseItem.getPosition(), baseItemType.getPhysicalAreaConfig().getRadius())) {
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
                aliveModelMatrices.put(baseItemType, new ModelMatrices(syncBaseItem.getModel()));
                if (syncBaseItem.getWeaponTurret() != null) {
                    weaponTurretModelMatrices.put(baseItemType, new ModelMatrices(syncBaseItem.getWeaponTurret()));
                }
            }
            // Demolition
            if (!syncBaseItem.checkSpawning() && syncBaseItem.checkBuildup() && !syncBaseItem.checkHealth()) {
                ModelMatrices modelMatrices = new ModelMatrices(syncBaseItem.getModel(), syncBaseItem.getHealth());
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
    }
}
