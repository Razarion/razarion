package com.btxtech.client.editor.widgets.polygon;

import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 27.07.2017.
 */
@Templated("PolygonField.html#field")
public class PolygonField extends Composite {
    private static final String SHOW = "Show";
    private static final String HIDE = "Hide";
    @Inject
    private PolygonEditor polygonEditor;
    @Inject
    private Camera camera;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    @DataField
    private Button showHideButton;
    @Inject
    @DataField
    private Button clearButton;
    @Inject
    @DataField
    private Button topViewButton;
    private Polygon2D polygon2D;
    private Consumer<Polygon2D> polygonListener;

    public void init(Polygon2D polygon2D, Consumer<Polygon2D> polygonListener) {
        this.polygon2D = polygon2D;
        this.polygonListener = polygonListener;
        showHideButton.setText(SHOW);
        clearButton.setEnabled(false);
        topViewButton.setEnabled(false);
    }

    @EventHandler("showHideButton")
    private void selectorButtonClicked(ClickEvent event) {
        if (showHideButton.getText().equalsIgnoreCase(SHOW)) {
            showHideButton.setText(HIDE);
            polygonEditor.activate(polygon2D, decimalPositions -> {
                if (decimalPositions != null) {
                    polygonListener.accept(new Polygon2D(decimalPositions));
                } else {
                    polygonListener.accept(null);
                }
            });
            clearButton.setEnabled(true);
            topViewButton.setEnabled(true);
        } else {
            polygonEditor.deactivate();
            showHideButton.setText(SHOW);
            clearButton.setEnabled(false);
            topViewButton.setEnabled(false);
        }
    }

    @EventHandler("topViewButton")
    private void topViewButtonClick(ClickEvent event) {
        projectionTransformation.disableFovYConstrain();
        terrainScrollHandler.setPlayGround(null);
        terrainScrollHandler.setScrollDisabled(false, null);
        camera.setTop();
    }

    @EventHandler("clearButton")
    private void clearButtonClick(ClickEvent event) {
        polygonEditor.clear();
    }

    @Override
    protected void onUnload() {
        polygonEditor.deactivate();
        super.onUnload();
    }
}
