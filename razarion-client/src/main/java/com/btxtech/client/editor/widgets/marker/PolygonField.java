package com.btxtech.client.editor.widgets.marker;

import com.btxtech.shared.datatypes.Polygon2D;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.dom.Div;
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
    private MarkerEditor markerEditor;
    @Inject
    @DataField
    private Button showHideButton;
    @Inject
    @DataField
    private Button clearButton;
    @Inject
    @DataField
    private Button topViewButton;
    @Inject
    @DataField
    private Div field;
    private Polygon2D polygon2D;
    private Consumer<Polygon2D> polygonListener;

    public void init(Polygon2D polygon2D, Consumer<Polygon2D> polygonListener) {
        this.polygon2D = polygon2D;
        this.polygonListener = polygonListener;
        showHideButton.setText(SHOW);
        clearButton.setEnabled(false);
        topViewButton.setEnabled(false);
        field.addEventListener("DOMNodeRemovedFromDocument", event -> {
            if (showHideButton.getText().equalsIgnoreCase(HIDE)) {
                markerEditor.deactivate();
            }
        }, false);
    }

    @EventHandler("showHideButton")
    private void selectorButtonClicked(ClickEvent event) {
        if (showHideButton.getText().equalsIgnoreCase(SHOW)) {
            showHideButton.setText(HIDE);
            markerEditor.activate(polygon2D, decimalPositions -> {
                if (polygonListener != null) {
                    if (decimalPositions != null) {
                        polygonListener.accept(new Polygon2D(decimalPositions));
                    } else {
                        polygonListener.accept(null);
                    }
                }
            }, () -> {
                polygonListener = null;
                showHideButton.setText(SHOW);
                clearButton.setEnabled(false);
                topViewButton.setEnabled(false);
            });
            clearButton.setEnabled(true);
            topViewButton.setEnabled(true);
        } else {
            markerEditor.deactivate();
            showHideButton.setText(SHOW);
            clearButton.setEnabled(false);
            topViewButton.setEnabled(false);
        }
    }

    @EventHandler("topViewButton")
    private void topViewButtonClick(ClickEvent event) {
        markerEditor.topView();
    }

    @EventHandler("clearButton")
    private void clearButtonClick(ClickEvent event) {
        markerEditor.clear();
    }

    @Override
    protected void onUnload() {
        markerEditor.deactivate();
        super.onUnload();
    }

    public void dispose() {
        markerEditor.deactivate();
        polygonListener = null;
        showHideButton.setText(SHOW);
        clearButton.setEnabled(false);
        topViewButton.setEnabled(false);
    }
}
