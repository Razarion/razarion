package com.btxtech.uiservice.renderer.task.visualization;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.datatypes.InGameItemVisualization;
import com.btxtech.uiservice.questvisualization.QuestInGamePlaceVisualization;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.AbstractVertexContainerRenderUnit;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderUnitControl;
import com.btxtech.uiservice.tip.visualization.InGameDirectionVisualization;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 05.12.2016.
 */
@ApplicationScoped
public class ItemVisualizationRenderTask extends AbstractRenderTask<InGameItemVisualization> {
    private Logger logger = Logger.getLogger(ItemVisualizationRenderTask.class.getName());
    @Inject
    private Shape3DUiService shape3DUiService;
    private boolean active;
    private InGameItemVisualization gameItemVisualization;
    private InGameDirectionVisualization inGameDirectionVisualization;
    private QuestInGamePlaceVisualization questInGameOutOfViewVisualization;

    @Override
    public boolean isActive() {
        return active;
    }

    public void activate(InGameItemVisualization inGameItemVisualization) {
        deactivate();
        this.gameItemVisualization = inGameItemVisualization;
        setupCorners();
        if (inGameItemVisualization.hasShape3DId()) {
            setupShape3D();
        }
        setupOutOfViewShape3D();
        active = true;
    }

    public void activate(InGameDirectionVisualization inGameDirectionVisualization) {
        deactivate();
        this.inGameDirectionVisualization = inGameDirectionVisualization;
        setupDirectionShape3D();
        active = true;
    }

    public void activate(QuestInGamePlaceVisualization questInGameOutOfViewVisualization) {
        deactivate();
        this.questInGameOutOfViewVisualization = questInGameOutOfViewVisualization;
        setupQuestInGameOutOfViewShape3D();
        active = true;
    }

    public void deactivate() {
        active = false;
        clear();
        gameItemVisualization = null;
        inGameDirectionVisualization = null;
        questInGameOutOfViewVisualization = null;
    }

    @Override
    protected void preRender(long timeStamp) {
        if (gameItemVisualization != null) {
            gameItemVisualization.preRender();
        }
    }

    private void setupCorners() {
        ModelRenderer<InGameItemVisualization, CommonRenderComposite<AbstractInGameItemCornerRendererUnit, InGameItemVisualization>, AbstractInGameItemCornerRendererUnit, InGameItemVisualization> modelRenderer = create();
        modelRenderer.init(gameItemVisualization, timeStamp -> gameItemVisualization.provideCornerModelMatrices(timeStamp));
        CommonRenderComposite<AbstractInGameItemCornerRendererUnit, InGameItemVisualization> compositeRenderer = modelRenderer.create();
        compositeRenderer.init(gameItemVisualization);
        compositeRenderer.setRenderUnit(AbstractInGameItemCornerRendererUnit.class);
        modelRenderer.add(RenderUnitControl.TERRAIN_ITEM_VISUALIZATION_CORNERS, compositeRenderer);
        add(modelRenderer);
        compositeRenderer.fillBuffers();
    }

    private void setupShape3D() {
        if (gameItemVisualization.getShape3DId() == null) {
            logger.warning("ItemVisualizationRenderTask: no shape3DId for GameItemVisualization: " + gameItemVisualization);
            return;
        }

        ModelRenderer<InGameItemVisualization, CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer>, AbstractVertexContainerRenderUnit, VertexContainer> modelRenderer = create();
        modelRenderer.init(gameItemVisualization, timeStamp -> gameItemVisualization.provideShape3DModelMatrices());

        setupRenderer(modelRenderer, gameItemVisualization.getShape3DId());
    }


    private void setupOutOfViewShape3D() {
        if (gameItemVisualization.getOutOfViewShape3DId() == null) {
            logger.warning("ItemVisualizationRenderTask: no getOutOfViewShape3DId for GameItemVisualization: " + gameItemVisualization);
            return;
        }

        ModelRenderer<InGameItemVisualization, CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer>, AbstractVertexContainerRenderUnit, VertexContainer> modelRenderer = create();
        modelRenderer.init(gameItemVisualization, timeStamp -> gameItemVisualization.provideOutOfViewShape3DModelMatrices());

        setupRenderer(modelRenderer, gameItemVisualization.getOutOfViewShape3DId());
    }


    private void setupDirectionShape3D() {
        if (inGameDirectionVisualization.getShape3DId() == null) {
            logger.warning("ItemVisualizationRenderTask: no getOutOfViewShape3DId for InGameDirectionVisualization: " + inGameDirectionVisualization);
            return;
        }

        ModelRenderer<InGameDirectionVisualization, CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer>, AbstractVertexContainerRenderUnit, VertexContainer> modelRenderer = create();
        modelRenderer.init(inGameDirectionVisualization, timeStamp -> inGameDirectionVisualization.provideDModelMatrices());

        setupRenderer(modelRenderer, inGameDirectionVisualization.getShape3DId());
    }

    private void setupQuestInGameOutOfViewShape3D() {
        if (questInGameOutOfViewVisualization.getOutOfViewShape3DId() == null) {
            logger.warning("ItemVisualizationRenderTask: no getOutOfViewShape3DId for QuestInGamePlaceVisualization: " + questInGameOutOfViewVisualization);
            return;
        }

        ModelRenderer<QuestInGamePlaceVisualization, CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer>, AbstractVertexContainerRenderUnit, VertexContainer> modelRenderer = create();
        modelRenderer.init(questInGameOutOfViewVisualization, timeStamp -> questInGameOutOfViewVisualization.provideOutOfViewModelMatrices());

        setupRenderer(modelRenderer, questInGameOutOfViewVisualization.getOutOfViewShape3DId());
    }

    private void setupRenderer(ModelRenderer<?, CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer>, AbstractVertexContainerRenderUnit, VertexContainer> modelRenderer, int shape3DId) {
        Shape3D shape3D = shape3DUiService.getShape3D(shape3DId);
        for (Element3D element3D : shape3D.getElement3Ds()) {
            for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer> renderComposite = modelRenderer.create();
                renderComposite.init(vertexContainer);
                renderComposite.setRenderUnit(AbstractVertexContainerRenderUnit.class);
                renderComposite.setupAnimation(shape3D, element3D, vertexContainer.getShapeTransform());
                renderComposite.setNormRenderUnit(AbstractVertexContainerRenderUnit.class);
                modelRenderer.add(RenderUnitControl.TERRAIN_ITEM_VISUALIZATION_IMAGE, renderComposite);
                renderComposite.fillBuffers();
            }
        }
        add(modelRenderer);
    }

}
