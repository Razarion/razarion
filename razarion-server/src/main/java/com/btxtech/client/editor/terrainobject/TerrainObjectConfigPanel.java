package com.btxtech.client.editor.terrainobject;

import com.btxtech.client.editor.widgets.shape3dwidget.Shape3DWidget;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * Created by Beat
 * 16.08.2016.
 */
@Templated("TerrainObjectConfigPanel.html#terrain-object-config-panel")
public class TerrainObjectConfigPanel extends Composite {
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
    @Inject
    @DataField
    private Shape3DWidget shape3DWidget;
    @Inject
    private Event<TerrainObjectConfig> trigger;

    public TerrainObjectConfig getTerrainObjectConfig() {
        return terrainObjectConfigDataBinder.getModel();
    }

    public void init(TerrainObjectConfig terrainObjectConfig) {
        terrainObjectConfigDataBinder.setModel(terrainObjectConfig);
        shape3DWidget.init(terrainObjectConfig.getShape3DId(), shape3DId -> {
            terrainObjectConfigDataBinder.getModel().setShape3DId(shape3DId);
            trigger.fire(terrainObjectConfigDataBinder.getModel());
        });
    }
}
