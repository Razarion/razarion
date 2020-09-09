package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.renderer.AbstractModelRenderTask;
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
public class ResourceItemRenderTask extends AbstractModelRenderTask<ResourceItemType> {
    private Logger logger = Logger.getLogger(ResourceItemRenderTask.class.getName());
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private ResourceUiService resourceUiService;
    @Inject
    private Shape3DUiService shape3DUiService;

    @PostConstruct
    public void postConstruct() {
        itemTypeService.getResourceItemTypes().forEach(resourceItemType -> setupResourceItemType(resourceItemType, false));
    }

    public void onResourceItemTypeChanged(ResourceItemType resourceItemType) {
        removeAll(resourceItemType);
        setupResourceItemType(resourceItemType, true);
    }

    private void setupResourceItemType(ResourceItemType resourceItemType, boolean fillBuffer) {
        if (resourceItemType.getShape3DId() != null) {
            ModelRenderer<ResourceItemType> modelRenderer = create();
            modelRenderer.init(resourceItemType, timeStamp -> resourceUiService.provideModelMatrices(resourceItemType));
            Shape3D shape3D = shape3DUiService.getShape3D(resourceItemType.getShape3DId());
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
            logger.warning("ResourceItemRenderTask: no shape3DId for ResourceItemType: " + resourceItemType);
        }
    }

}
