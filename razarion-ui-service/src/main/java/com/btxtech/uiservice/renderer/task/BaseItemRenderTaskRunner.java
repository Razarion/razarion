package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.renderer.task.progress.BuildupState;
import com.btxtech.uiservice.renderer.task.progress.DemolitionState;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.NoSuchElementException;

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
    private AlarmService alarmService;

    @PostConstruct
    public void postConstruct() {
        baseItemUiService.getBaseItemTypes().forEach(this::setupBaseItemType);
    }

    public void onBaseItemTypeChanged(BaseItemType baseItemType) {
        // TODO removeAll(baseItemType);
        setupBaseItemType(baseItemType);
    }

    @Override
    protected double setupInterpolationFactor() {
        return baseItemUiService.setupInterpolationFactor();
    }

    private void setupBaseItemType(BaseItemType baseItemType) {
        if (baseItemType.getShape3DId() != null) {
            Shape3D shape3D = shape3DUiService.getShape3D(baseItemType.getShape3DId());
            build(baseItemType, shape3D);
            alive(baseItemType, shape3D);
            demolition(baseItemType, shape3D);
            weaponTurret(baseItemType, shape3D);
        } else {
            alarmService.riseAlarm(INVALID_BASE_ITEM, "No shape3DId", baseItemType.getId());
        }

        // harvest(baseItemType);
        spawn(baseItemType);
        buildBeam(baseItemType);
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

//    private void harvest(BaseItemType baseItemType, boolean fillBuffer) {
//        if (baseItemType.getHarvesterType() != null) {
//            HarvesterType harvesterType = baseItemType.getHarvesterType();
//            if (harvesterType.getAnimationShape3dId() == null) {
//                logger.warning("BaseItemRenderTask: no AnimationShape3dId for harvester BaseItemType: " + baseItemType);
//                return;
//            }
//            if (harvesterType.getAnimationOrigin() == null) {
//                logger.warning("BaseItemRenderTask: no AnimationOrigin for harvester BaseItemType: " + baseItemType);
//                return;
//            }
//
//            ModelRenderer<BaseItemType> modelRenderer = create();
//            modelRenderer.init(baseItemType, timeStamp -> baseItemUiService.provideHarvestAnimationModelMatrices(baseItemType));
//            Shape3D shape3D = shape3DUiService.getShape3D(harvesterType.getAnimationShape3dId());
//            for (Element3D element3D : shape3D.getElement3Ds()) {
//                for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
//                    CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer> compositeRenderer = modelRenderer.create();
//                    compositeRenderer.init(vertexContainer);
//                    compositeRenderer.setRenderUnit(AbstractVertexContainerRenderUnit.class);
//                    compositeRenderer.setDepthBufferRenderUnit(AbstractVertexContainerRenderUnit.class);
//                    compositeRenderer.setNormRenderUnit(AbstractVertexContainerRenderUnit.class);
//                    compositeRenderer.setupAnimation(shape3D, element3D, vertexContainer.getShapeTransform());
//                    modelRenderer.add(RenderUnitControl.ITEMS, compositeRenderer);
//                    if (fillBuffer) {
//                        compositeRenderer.fillBuffers();
//                    }
//                }
//            }
//            add(modelRenderer);
//        }
//    }

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
