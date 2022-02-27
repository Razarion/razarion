package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.uiservice.renderer.ViewField;
import jsinterop.annotations.JsType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@JsType
@ApplicationScoped
public class InputService {
    @Inject
    private TerrainUiService terrainUiService;

    @SuppressWarnings("unused") // Called by Angular
    public void onViewFieldChanged(
            double bottomLeftX, double bottomLeftY,
            double bottomRightX, double bottomRightY,
            double topRightX, double topRightY,
            double topLeftX, double topLeftY) {
        ViewField viewField = new ViewField(0);
        viewField.setBottomLeft(new DecimalPosition(bottomLeftX, bottomLeftY));
        viewField.setBottomRight(new DecimalPosition(bottomRightX, bottomRightY));
        viewField.setTopRight(new DecimalPosition(topRightX, topRightY));
        viewField.setTopLeft(new DecimalPosition(topLeftX, topLeftY));
        terrainUiService.onViewChanged(viewField);
    }

}
