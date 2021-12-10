package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.ShapeTransform;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;
import com.btxtech.shared.nativejs.NativeMatrix;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

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
    @Inject
    private NativeMatrixFactory nativeMatrixFactory;

    @PostConstruct
    public void postConstruct() {
        Map<Shape3DElementKey, MapList<BaseItemType, ShapeTransform>> baseItemsTransforms = new HashMap<>();
        baseItemUiService.getBaseItemTypes().forEach(baseItemType -> setupBaseItemType(baseItemType, baseItemsTransforms));

        baseItemsTransforms.forEach((shape3DElement, baseItemTransformations) -> createMeshRenderTask(
                shape3DUiService.getShape3D(shape3DElement.getShape3DId()),
                shape3DElement.getElement3DId(),
                createModelMatricesProvider(baseItemTransformations),
                null));
    }

    private Function<Long, List<ModelMatrices>> createModelMatricesProvider(MapList<BaseItemType, ShapeTransform> baseItemTransforms) {
        NativeMatrix unityShapeTransform = nativeMatrixFactory.createFromColumnMajorArray(Matrix4.createFromAxisAndTranslation(
                new Vertex(0, 1, 0),
                new Vertex(0, 0, 1),
                new Vertex(1, 0, 0),
                new Vertex(0, 0, 0)
        ).toWebGlArray());
        return timestamp -> {
            List<ModelMatrices> resultModelMatrices = new ArrayList<>();
            baseItemTransforms.getMap().forEach((baseItemType, shapeTransforms) -> {
                List<ModelMatrices> itemModelMatrices = baseItemUiService.provideAliveModelMatrices(baseItemType);
                if (itemModelMatrices != null) {
                    itemModelMatrices.forEach(baseItemModelMatrices -> {
                        shapeTransforms.forEach(shapeTransform -> {
                            resultModelMatrices.add(baseItemModelMatrices.multiplyStaticShapeTransform(unityShapeTransform).multiplyShapeTransform(shapeTransform));
                        });
                    });
                }

            });
            return resultModelMatrices;
        };
    }

    @Override
    protected double setupInterpolationFactor(long timeStamp) {
        return baseItemUiService.setupInterpolationFactor(timeStamp);
    }

    public void onEditorBaseItemTypeChanged() {
        destroyRenderAllTasks();
        postConstruct();
    }

    private void setupBaseItemType(BaseItemType baseItemType, Map<Shape3DElementKey, MapList<BaseItemType, ShapeTransform>> baseItemsTransforms) {
        if (baseItemType.getShape3DId() != null || baseItemType.getMeshContainerId() != null) {
            if (baseItemType.getMeshContainerId() != null) {
                MeshContainer rootMeshContainer = mashService.getMeshContainer(baseItemType.getMeshContainerId());
                recursiveFillBaseItemShape3DElements(baseItemType, rootMeshContainer, baseItemsTransforms);
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

    private void recursiveFillBaseItemShape3DElements(BaseItemType baseItemType, MeshContainer meshContainer, Map<Shape3DElementKey, MapList<BaseItemType, ShapeTransform>> baseItemsTransforms) {
        if (meshContainer.getMesh() != null) {
            Shape3DElementKey key = new Shape3DElementKey(meshContainer.getMesh().getShape3DId(), meshContainer.getMesh().getElement3DId());
            MapList<BaseItemType, ShapeTransform> baseItemTransforms = baseItemsTransforms.get(key);
            if (baseItemTransforms == null) {
                baseItemTransforms = new MapList<>();
                baseItemsTransforms.put(key, baseItemTransforms);
            }
            if (meshContainer.getMesh().getShapeTransform() != null) {
                baseItemTransforms.put(baseItemType, meshContainer.getMesh().getShapeTransform());
            } else {
                ShapeTransform normTransform = new ShapeTransform();
                normTransform.setScaleX(1);
                normTransform.setScaleY(1);
                normTransform.setScaleZ(1);
                baseItemTransforms.put(baseItemType, normTransform);
            }
        }
        if (meshContainer.getChildren() != null) {
            meshContainer.getChildren().forEach(child -> recursiveFillBaseItemShape3DElements(baseItemType, child, baseItemsTransforms));
        }
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

    public static class Shape3DElementKey {
        private final int shape3DId;
        private final String element3DId;

        public Shape3DElementKey(int shape3DId, String element3DId) {
            this.shape3DId = shape3DId;
            this.element3DId = element3DId;
        }

        public int getShape3DId() {
            return shape3DId;
        }

        public String getElement3DId() {
            return element3DId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Shape3DElementKey that = (Shape3DElementKey) o;
            return shape3DId == that.shape3DId && element3DId.equals(that.element3DId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(shape3DId, element3DId);
        }
    }
}
