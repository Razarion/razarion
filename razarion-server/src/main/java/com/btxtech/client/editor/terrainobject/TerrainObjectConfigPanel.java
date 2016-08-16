package com.btxtech.client.editor.terrainobject;

import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.google.gwt.user.client.ui.Composite;
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
@Templated("TerrainObjectConfigPanel.html#terrain-object-config-panel")
public class TerrainObjectConfigPanel extends Composite {
    @Inject
    @AutoBound
    private DataBinder<TerrainObjectConfig> terrainObjectConfigDataBinder;
    @Inject
    @Bound
    @DataField
    private Label id;
    @Inject
    @Bound
    @DataField
    private TextBox internalName;

    public TerrainObjectConfig getTerrainObjectConfig() {
        return terrainObjectConfigDataBinder.getModel();
    }

    public void init(TerrainObjectConfig terrainObjectConfig) {
        terrainObjectConfigDataBinder.setModel(terrainObjectConfig);
    }
}
