package com.btxtech.client.editor.terrainobject;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.widgets.shape3dwidget.Shape3DReferenceFiled;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.uiservice.renderer.task.TerrainObjectRenderTask;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 16.08.2016.
 */
@Templated("TerrainObjectPropertyPanel.html#terrain-object-property-panel")
public class TerrainObjectPropertyPanel extends AbstractPropertyPanel<TerrainObjectConfig> {
    @Inject
    private TerrainObjectRenderTask terrainObjectRenderTask;
    @Inject
    @AutoBound
    private DataBinder<TerrainObjectConfig> terrainObjectConfigDataBinder;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Bound
    @DataField
    private Label id;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Bound
    @DataField
    private TextBox internalName;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Shape3DReferenceFiled shape3DReferenceFiled;

    public void init(TerrainObjectConfig terrainObjectConfig) {
        terrainObjectConfigDataBinder.setModel(terrainObjectConfig);
        shape3DReferenceFiled.init(terrainObjectConfig.getShape3DId(), shape3DId -> {
            terrainObjectConfigDataBinder.getModel().setShape3DId(shape3DId);
            terrainObjectRenderTask.onTerrainObjectChanged(terrainObjectConfigDataBinder.getModel());
        });
    }

    @Override
    public TerrainObjectConfig getConfigObject() {
        return terrainObjectConfigDataBinder.getModel();
    }
}
