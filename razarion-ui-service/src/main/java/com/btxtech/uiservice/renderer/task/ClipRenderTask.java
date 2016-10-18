package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.dto.ClipConfig;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.clip.ClipServiceImpl;
import com.btxtech.uiservice.renderer.AbstractLoopUpVertexContainerRenderUnit;
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
 * 14.10.2016.
 */
@ApplicationScoped
public class ClipRenderTask extends AbstractRenderTask<ClipConfig> {
    private Logger logger = Logger.getLogger(ClipRenderTask.class.getName());
    @Inject
    private ClipServiceImpl clipService;
    @Inject
    private Shape3DUiService shape3DUiService;

    @PostConstruct
    public void postConstruct() {
        clipService.getClipConfigs().forEach(clipConfig -> setupClip(clipConfig, false));
    }

    public void changeClip(ClipConfig clipConfig) {
        removeAll(clipConfig);
        setupClip(clipConfig, true);
    }

    private void setupClip(ClipConfig clipConfig, boolean fillBuffer) {
        if (clipConfig.getShape3DId() != null) {
            ModelRenderer<ClipConfig, CommonRenderComposite<AbstractLoopUpVertexContainerRenderUnit, VertexContainer>, AbstractLoopUpVertexContainerRenderUnit, VertexContainer> modelRenderer = create();
            modelRenderer.init(clipConfig, timeStamp -> clipService.provideModelMatrices(clipConfig, timeStamp));
            Shape3D shape3D = shape3DUiService.getShape3D(clipConfig.getShape3DId());
            for (Element3D element3D : shape3D.getElement3Ds()) {
                for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                    if (vertexContainer.hasLookUpTextureId()) {
                        CommonRenderComposite<AbstractLoopUpVertexContainerRenderUnit, VertexContainer> compositeRenderer = modelRenderer.create();
                        compositeRenderer.init(vertexContainer);
                        compositeRenderer.setRenderUnit(AbstractLoopUpVertexContainerRenderUnit.class);
                        // TODO compositeRenderer.setDepthBufferRenderUnit(AbstractLoopUpVertexContainerRenderUnit.class);
                        // TODO compositeRenderer.setNormRenderUnit(AbstractLoopUpVertexContainerRenderUnit.class);
                        compositeRenderer.setupAnimation(shape3D, element3D, vertexContainer.getShapeTransform());
                        modelRenderer.add(RenderUnitControl.SEMI_TRANSPARENT, compositeRenderer);
                        if (fillBuffer) {
                            compositeRenderer.fillBuffers();
                        }
                    } else {
                        CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer> compositeRenderer = modelRenderer.create();
                        compositeRenderer.init(vertexContainer);
                        compositeRenderer.setRenderUnit(AbstractVertexContainerRenderUnit.class);
                        compositeRenderer.setDepthBufferRenderUnit(AbstractVertexContainerRenderUnit.class);
                        compositeRenderer.setNormRenderUnit(AbstractVertexContainerRenderUnit.class);
                        compositeRenderer.setupAnimation(shape3D, element3D, vertexContainer.getShapeTransform());
                        modelRenderer.add(RenderUnitControl.NORMAL, compositeRenderer);
                        if (fillBuffer) {
                            compositeRenderer.fillBuffers();
                        }
                    }
                }
            }
            add(modelRenderer);
        } else {
            logger.warning("ClipRenderTask: no shape3DId for ClipConfig: " + clipConfig);
        }
    }
}
