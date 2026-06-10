package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.questvisualization.InGameQuestVisualizationService;
import com.btxtech.uiservice.renderer.ViewField;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class InputService {
    private final TerrainUiService terrainUiService;
    private final BaseItemUiService baseItemUiService;
    private final ResourceUiService resourceUiService;
    private final BoxUiService boxUiService;
    private final InGameQuestVisualizationService inGameQuestVisualizationService;
    private Runnable moveCommandAckCallback;

    @Inject
    public InputService(InGameQuestVisualizationService inGameQuestVisualizationService,
                        BoxUiService boxUiService,
                        ResourceUiService resourceUiService,
                        BaseItemUiService baseItemUiService,
                        TerrainUiService terrainUiService) {
        this.inGameQuestVisualizationService = inGameQuestVisualizationService;
        this.boxUiService = boxUiService;
        this.resourceUiService = resourceUiService;
        this.baseItemUiService = baseItemUiService;
        this.terrainUiService = terrainUiService;
    }

    @SuppressWarnings("unused") // Called by Angular
    public void onViewFieldChanged(double bottomLeftX, double bottomLeftY, double bottomRightX, double bottomRightY, double topRightX, double topRightY, double topLeftX, double topLeftY) {
        ViewField viewField = new ViewField(0)
                .bottomLeft(new DecimalPosition(bottomLeftX, bottomLeftY))
                .bottomRight(new DecimalPosition(bottomRightX, bottomRightY))
                .topRight(new DecimalPosition(topRightX, topRightY))
                .topLeft(new DecimalPosition(topLeftX, topLeftY));
        Rectangle2D viewFieldAabb = viewField.calculateAabbRectangle();
        terrainUiService.onViewChanged(viewField, viewFieldAabb);
        baseItemUiService.onViewChanged(viewField, viewFieldAabb);
        resourceUiService.onViewChanged(viewField, viewFieldAabb);
        boxUiService.onViewChanged(viewField, viewFieldAabb);
        inGameQuestVisualizationService.onViewChanged(viewField, viewFieldAabb);
    }

    public void onMoveCommandAck() {
        if (moveCommandAckCallback != null) {
            moveCommandAckCallback.run();
        }
    }

    public void setMoveCommandAckCallback(Runnable callback) {
        this.moveCommandAckCallback = callback;
    }
}
