package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.item.BoxUiService;
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
public class BoxItemRenderTask extends AbstractRenderTask<BoxItemType> {
    private Logger logger = Logger.getLogger(BoxItemRenderTask.class.getName());
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private BoxUiService boxUiService;
    @Inject
    private Shape3DUiService shape3DUiService;

    @PostConstruct
    public void postConstruct() {
        itemTypeService.getBoxItemTypes().forEach(boxItemType -> setupBoxItemType(boxItemType, false));
    }

    public void onBoxItemTypeChanged(BoxItemType boxItemType) {
        removeAll(boxItemType);
        setupBoxItemType(boxItemType, true);
    }

    private void setupBoxItemType(BoxItemType boxItemType, boolean fillBuffer) {
        if (boxItemType.getShape3DId() != null) {
            ModelRenderer<BoxItemType, CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer>, AbstractVertexContainerRenderUnit, VertexContainer> modelRenderer = create();
            modelRenderer.init(boxItemType, timeStamp -> boxUiService.provideModelMatrices(boxItemType));
            Shape3D shape3D = shape3DUiService.getShape3D(boxItemType.getShape3DId());
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
            logger.warning("BoxItemRenderTask: no shape3DId for BoxItemType: " + boxItemType);
        }
    }

}
