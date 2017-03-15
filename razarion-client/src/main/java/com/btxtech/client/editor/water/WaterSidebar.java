package com.btxtech.client.editor.water;

import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.client.editor.widgets.LightWidget;
import com.btxtech.client.editor.widgets.image.ImageItemWidget;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import org.jboss.errai.common.client.dom.NumberInput;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 15.03.2017.
 */
@Templated("WaterSidebar.html#water")
public class WaterSidebar extends LeftSideBarContent {
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private VisualUiService visualUiService;
    @Inject
    @AutoBound
    private DataBinder<WaterConfig> waterDataBinder;
    @Inject
    @DataField
    private LightWidget lightConfig;
    @Inject
    @Bound
    @DataField
    private NumberInput transparency;
    @Inject
    @DataField
    private ImageItemWidget bmId;
    @Inject
    @Bound
    @DataField
    private NumberInput bmScale;
    @Inject
    @Bound
    @DataField
    private NumberInput bmDepth;
    @Inject
    @Bound
    @DataField
    private NumberInput groundLevel;

    @PostConstruct
    public void init() {
        waterDataBinder.setModel(visualUiService.getVisualConfig().getWaterConfig());
        lightConfig.setModel(visualUiService.getVisualConfig().getWaterConfig().getLightConfig());
        terrainUiService.enableEditMode(visualUiService.getVisualConfig().getWaterConfig());
        bmId.setImageId(visualUiService.getVisualConfig().getWaterConfig().getBmId(), imageId -> visualUiService.getVisualConfig().getWaterConfig().setBmId(imageId));
    }

}
