package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.uiservice.projectile.ProjectileUiService;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.item.BaseItemUiService;
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
public class ProjectileRenderTask extends AbstractRenderTask<BaseItemType> {
    private Logger logger = Logger.getLogger(ProjectileRenderTask.class.getName());
    @Inject
    private ProjectileUiService projectileUiService;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private Shape3DUiService shape3DUiService;

    @PostConstruct
    public void postConstruct() {
        baseItemUiService.getBaseItemTypes().stream().filter(baseItemType -> baseItemType.getWeaponType() != null).forEach(baseItemType -> setupBaseItemType(baseItemType, false));
    }

    public void onBaseItemTypeChanged(BaseItemType baseItemType) {
        removeAll(baseItemType);
        setupBaseItemType(baseItemType, true);
    }

    @Override
    protected void preRender(long timeStamp) {
        projectileUiService.preRender(timeStamp);
    }

    private void setupBaseItemType(BaseItemType baseItemType, boolean fillBuffer) {
        if (baseItemType.getWeaponType().getProjectileShape3DId() != null) {
            ModelRenderer<BaseItemType> modelRenderer = create();
            modelRenderer.init(baseItemType, timeStamp -> projectileUiService.getProjectiles(baseItemType));
            Shape3D shape3D = shape3DUiService.getShape3D(baseItemType.getWeaponType().getProjectileShape3DId());
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
            logger.warning("ProjectileRenderTask: no  projectileShape3DId for BaseItemType: " + baseItemType);
        }
    }
}
