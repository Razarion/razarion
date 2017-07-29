package com.btxtech.client.editor.server.startregion;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.widgets.level.LevelField;
import com.btxtech.shared.dto.StartRegionConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
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
 * on 28.07.2017.
 */
@Templated("StartRegionPropertyPanel.html#propertyPanel")
public class StartRegionPropertyPanel extends AbstractPropertyPanel<StartRegionConfig> {
    @Inject
    @AutoBound
    private DataBinder<StartRegionConfig> dataBinder;
    @Inject
    @Bound
    @DataField
    private Label id;
    @Inject
    @Bound
    @DataField
    private TextBox internalName;
    @Inject
    @DataField
    private LevelField minimalLevelId;


    @Override
    public void init(StartRegionConfig startRegionConfig) {
        dataBinder.setModel(startRegionConfig);
        minimalLevelId.init(startRegionConfig.getMinimalLevelId(), startRegionConfig::setMinimalLevelId);
    }

    @Override
    public StartRegionConfig getConfigObject() {
        return dataBinder.getModel();
    }
}
