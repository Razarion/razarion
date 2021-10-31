package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.datatypes.asset.Mesh;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.uiservice.AssetService;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.renderer.task.progress.BuildupState;
import com.btxtech.uiservice.renderer.task.progress.DemolitionState;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.btxtech.shared.system.alarm.Alarm.Type.INVALID_BASE_ITEM;

/**
 * Created by Beat
 * 31.08.2016.
 */
@ApplicationScoped
public class BaseItemRenderTaskRunner extends AbstractShape3DRenderTaskRunner {
    // private Logger logger = Logger.getLogger(BaseItemRenderTaskRunner.class.getName());

    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private Shape3DUiService shape3DUiService;
    @Inject
    private AssetService mashService;
    @Inject
    private AlarmService alarmService;
    private MapList<Mesh, BaseItemType> meshes;

    @PostConstruct
    public void postConstruct() {
        meshes = new MapList<>();
        baseItemUiService.getBaseItemTypes().forEach(this::setupBaseItemType);

        meshes.getMap().forEach((mesh, baseItemTypes) -> createMeshRenderTask(
                shape3DUiService.getShape3D(mesh.getShape3DId()),
                mesh.getElement3DId(),
                getMeshModelMatrices(baseItemTypes),
                null));
    }

    private Function<Long, List<ModelMatrices>> getMeshModelMatrices(List<BaseItemType> baseItemTypes) {
        return (timestamp) -> baseItemTypes.stream()
                .flatMap(baseItemType -> {
                    List<ModelMatrices> modelMatrices = baseItemUiService.provideAliveModelMatrices(baseItemType);
                    if (modelMatrices != null) {
                        return modelMatrices.stream();
                    } else {
                        return null;
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    protected double setupInterpolationFactor(long timeStamp) {
        return baseItemUiService.setupInterpolationFactor(timeStamp);
    }

    public void onEditorBaseItemTypeChanged() {
        destroyRenderAllTasks();
        postConstruct();
    }

    private void setupBaseItemType(BaseItemType baseItemType) {
        if (baseItemType.getShape3DId() != null || baseItemType.getMeshId() != null) {
            if (baseItemType.getMeshId() != null) {
                meshes.put(mashService.getMesh(baseItemType.getMeshId()), baseItemType);
                return;
            }
            Shape3D shape3D = shape3DUiService.getShape3D(baseItemType.getShape3DId());
            build(baseItemType, shape3D);
            alive(baseItemType, shape3D);
            demolition(baseItemType, shape3D);
            weaponTurret(baseItemType, shape3D);
        } else {
            alarmService.riseAlarm(INVALID_BASE_ITEM, "No shape3DId or meshId", baseItemType.getId());
        }
        spawn(baseItemType);
        buildBeam(baseItemType);
        harvest(baseItemType);
    }

    private void spawn(BaseItemType baseItemType) {
        if (baseItemType.getSpawnShape3DId() != null) {
            createShape3DRenderTasks(shape3DUiService.getShape3D(baseItemType.getSpawnShape3DId()),
                    timeStamp -> baseItemUiService.provideSpawningModelMatrices(baseItemType));
        } else {
            alarmService.riseAlarm(INVALID_BASE_ITEM, "No spawnShape3DId", baseItemType.getId());
        }
    }

    private void build(BaseItemType baseItemType, Shape3D shape3D) {
        if (baseItemType.getPhysicalAreaConfig().fulfilledMovable()) {
            return; // Movable are built in a factory
        }

        double maxZ = shape3D.getElement3Ds().stream()
                .flatMap(element3D -> element3D.getVertexContainers().stream())
                .mapToDouble(vertexContainer -> shape3DUiService.getMaxZ(vertexContainer))
                .max().orElseThrow(NoSuchElementException::new);

        createShape3DRenderTasks(shape3D,
                timeStamp -> baseItemUiService.provideBuildupModelMatrices(baseItemType),
                null,
                new BuildupState(maxZ, baseItemType.getBuildupTextureId()));
    }

    private void alive(BaseItemType baseItemType, Shape3D shape3D) {
        String turretMaterialId = getTurretMaterialId(baseItemType);
        createShape3DRenderTasks(shape3D,
                timeStamp -> baseItemUiService.provideAliveModelMatrices(baseItemType),
                vertexContainer -> turretMaterialId == null || !turretMaterialId.equals(vertexContainer.getVertexContainerMaterial().getMaterialId()),
                null);
    }

    private void demolition(BaseItemType baseItemType, Shape3D shape3D) {
        String turretMaterialId = getTurretMaterialId(baseItemType);

        createShape3DRenderTasks(shape3D,
                timeStamp -> baseItemUiService.provideDemolitionModelMatrices(baseItemType),
                vertexContainer -> turretMaterialId == null || !turretMaterialId.equals(vertexContainer.getVertexContainerMaterial().getMaterialId()),
                new DemolitionState(baseItemType.getDemolitionImageId()));
    }

    private void harvest(BaseItemType baseItemType) {
        if (baseItemType.getHarvesterType() != null) {
            HarvesterType harvesterType = baseItemType.getHarvesterType();
            if (harvesterType.getAnimationShape3dId() == null) {
                alarmService.riseAlarm(INVALID_BASE_ITEM, "No animationShape3dId in HarvesterType", baseItemType.getId());
                return;
            }
            if (harvesterType.getAnimationOrigin() == null) {
                alarmService.riseAlarm(INVALID_BASE_ITEM, "No animationOrigin in HarvesterType", baseItemType.getId());
                return;
            }

            createShape3DRenderTasks(shape3DUiService.getShape3D(harvesterType.getAnimationShape3dId()),
                    timeStamp -> baseItemUiService.provideHarvestAnimationModelMatrices(baseItemType));
        }
    }

    private void buildBeam(BaseItemType baseItemType) {
        if (baseItemType.getBuilderType() != null) {
            BuilderType builderType = baseItemType.getBuilderType();
            if (builderType.getAnimationShape3dId() == null) {
                alarmService.riseAlarm(INVALID_BASE_ITEM, "no animationShape3dId in BuilderType", baseItemType.getId());
                return;
            }
            if (builderType.getAnimationOrigin() == null) {
                alarmService.riseAlarm(INVALID_BASE_ITEM, "no AnimationOrigin in BuilderType", baseItemType.getId());
                return;
            }
            createShape3DRenderTasks(shape3DUiService.getShape3D(builderType.getAnimationShape3dId()),
                    timeStamp -> baseItemUiService.provideBuildAnimationModelMatrices(baseItemType));
        }
    }

    private void weaponTurret(BaseItemType baseItemType, Shape3D shape3D) {
        if (baseItemType.getWeaponType() == null) {
            return;
        }
        if (baseItemType.getWeaponType().getTurretType() == null) {
            alarmService.riseAlarm(INVALID_BASE_ITEM, "no turretType in WeaponType", baseItemType.getId());
            return;
        }

        String turretMaterialId = getTurretMaterialId(baseItemType);
        createShape3DRenderTasks(shape3D,
                timeStamp -> baseItemUiService.provideTurretModelMatrices(baseItemType),
                vertexContainer -> turretMaterialId != null && turretMaterialId.equals(vertexContainer.getVertexContainerMaterial().getMaterialId()),
                null);
    }


    private String getTurretMaterialId(BaseItemType baseItemType) {
        return (baseItemType.getWeaponType() != null && baseItemType.getWeaponType().getTurretType() != null)
                ? baseItemType.getWeaponType().getTurretType().getShape3dMaterialId()
                : null;
    }
}
