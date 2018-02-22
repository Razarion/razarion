package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;
import com.btxtech.shared.utils.Shape3DUtils;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.renderer.AbstractBuildupVertexContainerRenderUnit;
import com.btxtech.uiservice.renderer.AbstractDemolitionVertexContainerRenderUnit;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.AbstractVertexContainerRenderUnit;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderUnitControl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 31.08.2016.
 */
@ApplicationScoped
public class BaseItemRenderTask extends AbstractRenderTask<BaseItemType> {
    private Logger logger = Logger.getLogger(BaseItemRenderTask.class.getName());
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private Shape3DUiService shape3DUiService;
    @Inject
    private VisualUiService visualUiService;

    @PostConstruct
    public void postConstruct() {
        baseItemUiService.getBaseItemTypes().forEach(baseItemType -> setupBaseItemType(baseItemType, false));
    }

    public void onBaseItemTypeChanged(BaseItemType baseItemType) {
        removeAll(baseItemType);
        setupBaseItemType(baseItemType, true);
    }

    @Override
    protected double setupInterpolationFactor() {
        return baseItemUiService.setupInterpolationFactor();
    }

    private void setupBaseItemType(BaseItemType baseItemType, boolean fillBuffer) {
        spawn(baseItemType, fillBuffer);
        build(baseItemType, fillBuffer);
        alive(baseItemType, fillBuffer);
        demolition(baseItemType, fillBuffer);
        harvest(baseItemType, fillBuffer);
        buildBeam(baseItemType, fillBuffer);
        weaponTurret(baseItemType, fillBuffer);
    }

    private void spawn(BaseItemType baseItemType, boolean fillBuffer) {
        if (baseItemType.getSpawnShape3DId() != null) {
            ModelRenderer<BaseItemType, CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer>, AbstractVertexContainerRenderUnit, VertexContainer> modelRenderer = create();
            modelRenderer.init(baseItemType, timeStamp -> baseItemUiService.provideSpawningModelMatrices(baseItemType));
            Shape3D shape3D = shape3DUiService.getShape3D(baseItemType.getSpawnShape3DId());
            for (Element3D element3D : shape3D.getElement3Ds()) {
                for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                    CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer> compositeRenderer = modelRenderer.create();
                    compositeRenderer.init(vertexContainer);
                    compositeRenderer.setRenderUnit(AbstractVertexContainerRenderUnit.class);
                    compositeRenderer.setDepthBufferRenderUnit(AbstractVertexContainerRenderUnit.class);
                    compositeRenderer.setNormRenderUnit(AbstractVertexContainerRenderUnit.class);
                    compositeRenderer.setupAnimation(shape3D, element3D, vertexContainer.getShapeTransform());
                    modelRenderer.add(RenderUnitControl.ITEMS, compositeRenderer);
                    if (fillBuffer) {
                        compositeRenderer.fillBuffers();
                    }
                }
            }
            add(modelRenderer);
        } else {
            logger.warning("BaseItemRenderTask: no spawnShape3DId for BaseItemType: " + baseItemType);
        }
    }

    private void build(BaseItemType baseItemType, boolean fillBuffer) {
        if (baseItemType.getPhysicalAreaConfig().fulfilledMovable()) {
            return; // Startup Performance
        }

        if (baseItemType.getShape3DId() != null) {

            Shape3D shape3D = shape3DUiService.getShape3D(baseItemType.getShape3DId());
            double maxZ = Double.MIN_VALUE;

            for (Element3D element3D : shape3D.getElement3Ds()) {
                for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                    maxZ = Math.max(maxZ, shape3DUiService.getMaxZ(vertexContainer));
                }
            }

            ModelRenderer<BaseItemType, CommonRenderComposite<AbstractBuildupVertexContainerRenderUnit, VertexContainer>, AbstractBuildupVertexContainerRenderUnit, VertexContainer> modelRenderer = create();
            modelRenderer.init(baseItemType, timeStamp -> baseItemUiService.provideBuildupModelMatrices(baseItemType));
            for (Element3D element3D : shape3D.getElement3Ds()) {
                for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                    CommonRenderComposite<AbstractBuildupVertexContainerRenderUnit, VertexContainer> compositeRenderer = modelRenderer.create();
                    compositeRenderer.init(vertexContainer);
                    compositeRenderer.setRenderUnit(AbstractBuildupVertexContainerRenderUnit.class).setMaxZ(maxZ).setBaseItemBuildupImageId(baseItemType.getBuildupTextureId());
                    compositeRenderer.setDepthBufferRenderUnit(AbstractBuildupVertexContainerRenderUnit.class).setMaxZ(maxZ).setBaseItemBuildupImageId(baseItemType.getBuildupTextureId());
                    compositeRenderer.setupNoAnimation(vertexContainer.getShapeTransform());
                    modelRenderer.add(RenderUnitControl.ITEMS, compositeRenderer);
                    if (fillBuffer) {
                        compositeRenderer.fillBuffers();
                    }
                }
            }
            add(modelRenderer);
        } else {
            logger.warning("BaseItemRenderTask: no spawnShape3DId for BaseItemType: " + baseItemType);
        }
    }

    private void alive(BaseItemType baseItemType, boolean fillBuffer) {
        if (baseItemType.getShape3DId() != null) {
            String turretMaterialId = null;
            if (baseItemType.getWeaponType() != null && baseItemType.getWeaponType().getTurretType() != null) {
                turretMaterialId = baseItemType.getWeaponType().getTurretType().getShape3dMaterialId();
            }
            ModelRenderer<BaseItemType, CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer>, AbstractVertexContainerRenderUnit, VertexContainer> modelRenderer = create();
            modelRenderer.init(baseItemType, timeStamp -> baseItemUiService.provideAliveModelMatrices(baseItemType));
            Shape3D shape3D = shape3DUiService.getShape3D(baseItemType.getShape3DId());
            for (Element3D element3D : shape3D.getElement3Ds()) {
                for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                    if (turretMaterialId != null && turretMaterialId.equals(vertexContainer.getMaterialId())) {
                        continue;
                    }
                    CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer> compositeRenderer = modelRenderer.create();
                    compositeRenderer.init(vertexContainer);
                    compositeRenderer.setRenderUnit(AbstractVertexContainerRenderUnit.class);
                    compositeRenderer.setDepthBufferRenderUnit(AbstractVertexContainerRenderUnit.class);
                    compositeRenderer.setNormRenderUnit(AbstractVertexContainerRenderUnit.class);
                    compositeRenderer.setupAnimation(shape3D, element3D, vertexContainer.getShapeTransform());
                    modelRenderer.add(RenderUnitControl.ITEMS, compositeRenderer);
                    if (fillBuffer) {
                        compositeRenderer.fillBuffers();
                    }
                }
            }
            add(modelRenderer);
        } else {
            logger.warning("BaseItemRenderTask: no shape3DId for BaseItemType: " + baseItemType);
        }
    }

    private void demolition(BaseItemType baseItemType, boolean fillBuffer) {
        if (baseItemType.getShape3DId() != null) {
            String turretMaterialId = null;
            if (baseItemType.getWeaponType() != null && baseItemType.getWeaponType().getTurretType() != null) {
                turretMaterialId = baseItemType.getWeaponType().getTurretType().getShape3dMaterialId();
            }
            ModelRenderer<BaseItemType, CommonRenderComposite<AbstractDemolitionVertexContainerRenderUnit, VertexContainer>, AbstractDemolitionVertexContainerRenderUnit, VertexContainer> modelRenderer = create();
            modelRenderer.init(baseItemType, timeStamp -> baseItemUiService.provideDemolitionModelMatrices(baseItemType));
            Shape3D shape3D = shape3DUiService.getShape3D(baseItemType.getShape3DId());
            for (Element3D element3D : shape3D.getElement3Ds()) {
                for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                    if (turretMaterialId != null && turretMaterialId.equals(vertexContainer.getMaterialId())) {
                        continue;
                    }
                    CommonRenderComposite<AbstractDemolitionVertexContainerRenderUnit, VertexContainer> compositeRenderer = modelRenderer.create();
                    compositeRenderer.init(vertexContainer);
                    compositeRenderer.setRenderUnit(AbstractDemolitionVertexContainerRenderUnit.class).setAdditionalData(baseItemType.getDemolitionImageId());
                    compositeRenderer.setDepthBufferRenderUnit(AbstractDemolitionVertexContainerRenderUnit.class).setAdditionalData(baseItemType.getDemolitionImageId());
                    compositeRenderer.setupAnimation(shape3D, element3D, vertexContainer.getShapeTransform());
                    modelRenderer.add(RenderUnitControl.ITEMS, compositeRenderer);
                    if (fillBuffer) {
                        compositeRenderer.fillBuffers();
                    }
                }
            }
            add(modelRenderer);
        } else {
            logger.warning("BaseItemRenderTask: no shape3DId for BaseItemType: " + baseItemType);
        }
    }

    private void harvest(BaseItemType baseItemType, boolean fillBuffer) {
        if (baseItemType.getHarvesterType() != null) {
            HarvesterType harvesterType = baseItemType.getHarvesterType();
            if (harvesterType.getAnimationShape3dId() == null) {
                logger.warning("BaseItemRenderTask: no AnimationShape3dId for harvester BaseItemType: " + baseItemType);
                return;
            }
            if (harvesterType.getAnimationOrigin() == null) {
                logger.warning("BaseItemRenderTask: no AnimationOrigin for harvester BaseItemType: " + baseItemType);
                return;
            }

            ModelRenderer<BaseItemType, CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer>, AbstractVertexContainerRenderUnit, VertexContainer> modelRenderer = create();
            modelRenderer.init(baseItemType, timeStamp -> baseItemUiService.provideHarvestAnimationModelMatrices(baseItemType));
            Shape3D shape3D = shape3DUiService.getShape3D(harvesterType.getAnimationShape3dId());
            for (Element3D element3D : shape3D.getElement3Ds()) {
                for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                    CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer> compositeRenderer = modelRenderer.create();
                    compositeRenderer.init(vertexContainer);
                    compositeRenderer.setRenderUnit(AbstractVertexContainerRenderUnit.class);
                    compositeRenderer.setDepthBufferRenderUnit(AbstractVertexContainerRenderUnit.class);
                    compositeRenderer.setNormRenderUnit(AbstractVertexContainerRenderUnit.class);
                    compositeRenderer.setupAnimation(shape3D, element3D, vertexContainer.getShapeTransform());
                    modelRenderer.add(RenderUnitControl.ITEMS, compositeRenderer);
                    if (fillBuffer) {
                        compositeRenderer.fillBuffers();
                    }
                }
            }
            add(modelRenderer);
        }
    }

    private void buildBeam(BaseItemType baseItemType, boolean fillBuffer) {
        if (baseItemType.getBuilderType() != null) {
            BuilderType builderType = baseItemType.getBuilderType();
            if (builderType.getAnimationShape3dId() == null) {
                logger.warning("BaseItemRenderTask: no AnimationShape3dId for build beam in BaseItemType: " + baseItemType);
                return;
            }
            if (builderType.getAnimationOrigin() == null) {
                logger.warning("BaseItemRenderTask: no AnimationOrigin for build beam in BaseItemType: " + baseItemType);
                return;
            }

            ModelRenderer<BaseItemType, CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer>, AbstractVertexContainerRenderUnit, VertexContainer> modelRenderer = create();
            modelRenderer.init(baseItemType, timeStamp -> baseItemUiService.provideBuildAnimationModelMatrices(baseItemType));
            Shape3D shape3D = shape3DUiService.getShape3D(builderType.getAnimationShape3dId());
            for (Element3D element3D : shape3D.getElement3Ds()) {
                for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                    CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer> compositeRenderer = modelRenderer.create();
                    compositeRenderer.init(vertexContainer);
                    compositeRenderer.setRenderUnit(AbstractVertexContainerRenderUnit.class);
                    compositeRenderer.setDepthBufferRenderUnit(AbstractVertexContainerRenderUnit.class);
                    compositeRenderer.setNormRenderUnit(AbstractVertexContainerRenderUnit.class);
                    compositeRenderer.setupAnimation(shape3D, element3D, vertexContainer.getShapeTransform());
                    modelRenderer.add(RenderUnitControl.ITEMS, compositeRenderer);
                    if (fillBuffer) {
                        compositeRenderer.fillBuffers();
                    }
                }
            }
            add(modelRenderer);
        }
    }

    private void weaponTurret(BaseItemType baseItemType, boolean fillBuffer) {
        if (baseItemType.getWeaponType() == null) {
            return;
        }
        if (baseItemType.getWeaponType().getTurretType() == null) {
            logger.warning("BaseItemRenderTask: no Turret for WeaponType in BaseItemType: " + baseItemType);
            return;
        }
        String shape3dMaterialId = baseItemType.getWeaponType().getTurretType().getShape3dMaterialId();
        if (shape3dMaterialId == null) {
            logger.warning("BaseItemRenderTask: no TurretMaterialId WeaponType beam in BaseItemType: " + baseItemType);
            return;
        }

        ModelRenderer<BaseItemType, CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer>, AbstractVertexContainerRenderUnit, VertexContainer> modelRenderer = create();
        modelRenderer.init(baseItemType, timeStamp -> baseItemUiService.provideTurretModelMatrices(baseItemType));
        Shape3D shape3D = shape3DUiService.getShape3D(baseItemType.getShape3DId());
        VertexContainer vertexContainer = Shape3DUtils.getVertexContainer4MaterialId(shape3D, shape3dMaterialId);
        CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer> compositeRenderer = modelRenderer.create();
        compositeRenderer.init(vertexContainer);
        compositeRenderer.setRenderUnit(AbstractVertexContainerRenderUnit.class);
        compositeRenderer.setDepthBufferRenderUnit(AbstractVertexContainerRenderUnit.class);
        compositeRenderer.setNormRenderUnit(AbstractVertexContainerRenderUnit.class);
        compositeRenderer.setupAnimation(shape3D, Shape3DUtils.getElement4MaterialId(shape3D, shape3dMaterialId), vertexContainer.getShapeTransform());
        modelRenderer.add(RenderUnitControl.ITEMS, compositeRenderer);
        if (fillBuffer) {
            compositeRenderer.fillBuffers();
        }

        add(modelRenderer);
    }
}
