package com.btxtech.client.editor;

import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.client.editor.widgets.LightWidget;
import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.client.utils.GradToRadConverter;
import com.btxtech.shared.dto.VisualConfig;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 14.08.2016.
 */
@Templated("VisualConfigPanel.html#visual-panel")
public class VisualConfigPanel extends LeftSideBarContent {
    @Inject
    private ShadowUiService shadowUiService;
    @Inject
    private VisualUiService visualUiService;
    @Inject
    @AutoBound
    private DataBinder<VisualConfig> visualConfigDataBinder;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label shadowDirectionLabel;
    @Inject
    @Bound(property = "shadowRotationX", converter = GradToRadConverter.class)
    @DataField
    private DoubleBox shadowRotationXSlider;
    @Inject
    @Bound(property = "shadowRotationX", converter = GradToRadConverter.class)
    @DataField
    private DoubleBox shadowRotationXBox;
    @Inject
    @Bound(property = "shadowRotationY", converter = GradToRadConverter.class)
    @DataField
    private DoubleBox shadowRotationYSlider;
    @Inject
    @Bound(property = "shadowRotationY", converter = GradToRadConverter.class)
    @DataField
    private DoubleBox shadowRotationYBox;
    @Inject
    @Bound
    @DataField
    private DoubleBox shadowAlpha;
    @Inject
    @DataField
    private LightWidget waterLightConfig;
    @Inject
    @Bound
    @DataField
    private DoubleBox waterTransparency;
    @Inject
    @Bound
    @DataField
    private DoubleBox waterBmDepth;
    @Inject
    @Bound
    @DataField
    private DoubleBox waterGroundLevel;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label shape3DDirectionLabel;
    @Inject
    @Bound(property = "shape3DLightRotateX", converter = GradToRadConverter.class)
    @DataField
    private DoubleBox shape3DLightRotationXSlider;
    @Inject
    @Bound(property = "shape3DLightRotateX", converter = GradToRadConverter.class)
    @DataField
    private DoubleBox shape3DLightRotationXBox;
    @Inject
    @Bound(property = "shape3DLightRotateZ", converter = GradToRadConverter.class)
    @DataField
    private DoubleBox shape3DLightRotationZSlider;
    @Inject
    @Bound(property = "shape3DLightRotateZ", converter = GradToRadConverter.class)
    @DataField
    private DoubleBox shape3DLightRotationZBox;

    @PostConstruct
    public void init() {
        // Shadow
        visualConfigDataBinder.setModel(visualUiService.getVisualConfig());
        visualConfigDataBinder.addPropertyChangeHandler(event -> displayLightDirectionLabels());
        waterLightConfig.setModel(visualUiService.getVisualConfig().getWaterLightConfig());
        displayLightDirectionLabels();
    }

    private void displayLightDirectionLabels() {
        shadowDirectionLabel.setText(DisplayUtils.formatVertex(shadowUiService.getLightDirection()));
        shape3DDirectionLabel.setText(DisplayUtils.formatVertex(visualUiService.getShape3DLightDirection()));
    }
}
