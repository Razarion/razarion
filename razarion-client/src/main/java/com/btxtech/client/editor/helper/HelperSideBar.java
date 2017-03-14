package com.btxtech.client.editor.helper;

import com.btxtech.client.editor.renderer.TerrainMarkerRenderTask;
import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.user.client.ui.DoubleBox;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 09.12.2016.
 */
@Templated("HelperSideBar.html#render-helper-side-bar")
public class HelperSideBar extends LeftSideBarContent {
    @Inject
    private TerrainMarkerRenderTask terrainMarkerRenderTask;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    @DataField
    private DoubleBox rectX;
    @Inject
    @DataField
    private DoubleBox rectY;
    @Inject
    @DataField
    private DoubleBox rectWidth;
    @Inject
    @DataField
    private DoubleBox rectHeight;

    @EventHandler("rectX")
    public void rectXChanged(ChangeEvent e) {
        markRectangle();
    }

    @EventHandler("rectY")
    public void rectYChanged(ChangeEvent e) {
        markRectangle();
    }

    @EventHandler("rectWidth")
    public void rectWidthChanged(ChangeEvent e) {
        markRectangle();
    }

    @EventHandler("rectHeight")
    public void rectHeightChanged(ChangeEvent e) {
        markRectangle();
    }

    private void markRectangle() {
        if (rectX.getValue() != null && rectY.getValue() != null && rectWidth.getValue() != null && rectHeight.getValue() != null && rectWidth.getValue() > 0.0 && rectHeight.getValue() > 0.0) {
            Rectangle2D rect = new Rectangle2D(rectX.getValue(), rectY.getValue(), rectWidth.getValue(), rectHeight.getValue());
            List<Vertex> polygon = new ArrayList<>();
            // TODO polygon.add(terrainUiService.getPosition3d(rect.cornerBottomLeft()));
            // TODO polygon.add(terrainUiService.getPosition3d(rect.cornerBottomRight()));
            // TODO polygon.add(terrainUiService.getPosition3d(rect.cornerTopRight()));
            // TODO polygon.add(terrainUiService.getPosition3d(rect.cornerTopLeft()));
            terrainMarkerRenderTask.showPolygon(polygon);
            throw new UnsupportedOperationException("FIXME: The required data is in the worker now");
        } else {
            terrainMarkerRenderTask.hidePolygon();
        }
    }

    @Override
    protected void onClose() {
        terrainMarkerRenderTask.hidePolygon();
    }
}
