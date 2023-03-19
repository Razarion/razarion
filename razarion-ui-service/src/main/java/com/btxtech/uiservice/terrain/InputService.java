package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.mouse.TerrainMouseHandler;
import com.btxtech.uiservice.renderer.ViewField;
import jsinterop.annotations.JsType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@JsType
@ApplicationScoped
public class InputService {
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private TerrainMouseHandler terrainMouseHandler;

    @SuppressWarnings("unused") // Called by Angular
    public void onViewFieldChanged(double bottomLeftX, double bottomLeftY, double bottomRightX, double bottomRightY, double topRightX, double topRightY, double topLeftX, double topLeftY) {
        ViewField viewField = new ViewField(0);
        viewField.setBottomLeft(new DecimalPosition(bottomLeftX, bottomLeftY));
        viewField.setBottomRight(new DecimalPosition(bottomRightX, bottomRightY));
        viewField.setTopRight(new DecimalPosition(topRightX, topRightY));
        viewField.setTopLeft(new DecimalPosition(topLeftX, topLeftY));
        Rectangle2D viewFieldAabb = viewField.calculateAabbRectangle();
        terrainUiService.onViewChanged(viewField);
        baseItemUiService.onViewChanged(viewField, viewFieldAabb);
    }

    @SuppressWarnings("unused") // Called by Babylonjs
    public void onMouseMove(int x, int y, boolean primaryButtonDown) {
        terrainMouseHandler.onMouseMove(new DecimalPosition(x, y), primaryButtonDown);
    }

    @SuppressWarnings("unused") // Called by Babylonjs
    public void onMouseDown(int x, int y) {
        terrainMouseHandler.onMouseDown(new DecimalPosition(x, y));
    }

    @SuppressWarnings("unused") // Called by Babylonjs
    public void onMouseUp(int x, int y) {
        terrainMouseHandler.onMouseUp(new DecimalPosition(x, y));
    }

}
