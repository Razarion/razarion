package com.btxtech.client.dialogs;

import com.btxtech.client.editor.PlateauEditor;
import com.btxtech.client.editor.ShapeEditor;
import com.btxtech.client.math3d.TriangleRenderManager;
import com.btxtech.client.terrain.Terrain2;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created by Beat
 * 18.04.2015.
 */
public class Control extends Composite {
    interface ControlUiBinder extends UiBinder<Widget, Control> {
    }

    private static ControlUiBinder ourUiBinder = GWT.create(ControlUiBinder.class);
    @UiField(provided = true)
    ProjectionControl projectionControl;
    @UiField(provided = true)
    ModelControl modelControl;
    @UiField(provided = true)
    ViewControl viewControl;
    @UiField
    Button shapeEditorButton;
    @UiField
    Button plateauEditorButton;
    @UiField
    ListBox renderMode;
    @UiField(provided = true)
    LightingControl lightingControl;
    @UiField
    HorizontalInputRangeNumber roughness;
    private TriangleRenderManager triangleRenderManager;

    public Control(final TriangleRenderManager triangleRenderManager) {
        this.triangleRenderManager = triangleRenderManager;
        modelControl = new ModelControl(triangleRenderManager.getModelTransformation());
        viewControl = new ViewControl(triangleRenderManager.getViewTransformation());
        projectionControl = new ProjectionControl(triangleRenderManager.getProjectionTransformation());
        lightingControl = new LightingControl(triangleRenderManager.getLighting());
        initWidget(ourUiBinder.createAndBindUi(this));
        for (TriangleRenderManager.Mode mode : TriangleRenderManager.Mode.values()) {
            renderMode.addItem(mode.name());
        }
        renderMode.setSelectedIndex(triangleRenderManager.getMode().ordinal());
        roughness.setValue(Terrain2.getInstance().getRoughness());
    }

    @UiHandler("shapeEditorButton")
    void onShapeEditorButtonClick(ClickEvent clickEvent) {
        ShapeEditor.showEditor();
    }

    @UiHandler("plateauEditorButton")
    void onPlateauEditorButtonClick(ClickEvent clickEvent) {
        PlateauEditor.showEditor();
    }

    @UiHandler("renderMode")
    void onRenderModeChanged(ChangeEvent changeEvent) {
        triangleRenderManager.setMode(TriangleRenderManager.Mode.values()[renderMode.getSelectedIndex()]);
        triangleRenderManager.fillBuffers();
    }

    @UiHandler("roughness")
    void onRoughnessChanged(ValueChangeEvent<Double> valueChangeEvent) {
        Terrain2.getInstance().setRoughness(valueChangeEvent.getValue());
        triangleRenderManager.fillBuffers();
    }

}