package com.btxtech.client.editor;

import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.client.utils.GradToRadConverter;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.shared.rest.PlanetEditorProvider;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.uiservice.renderer.ViewService;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.dom.NumberInput;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 14.08.2016.
 */
@Templated("PlanetVisualConfigPanel.html#planet-panel")
public class PlanetVisualConfigPanel extends LeftSideBarContent {
    private Logger logger = Logger.getLogger(PlanetVisualConfigPanel.class.getName());
    @Inject
    private ShadowUiService shadowUiService;
    @Inject
    private VisualUiService visualUiService;
    @Inject
    private Shape3DUiService shape3DUiService;
    @Inject
    private ViewService viewService;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private Caller<PlanetEditorProvider> planetEditorProviderCaller;
    @Inject
    @AutoBound
    private DataBinder<PlanetVisualConfig> planetVisualConfigDataBinder;
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
    private NumberInput shadowAlpha;
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
    @Bound(property = "shape3DLightRotateY", converter = GradToRadConverter.class)
    @DataField
    private DoubleBox shape3DLightRotationYSlider;
    @Inject
    @Bound(property = "shape3DLightRotateY", converter = GradToRadConverter.class)
    @DataField
    private DoubleBox shape3DLightRotationYBox;

    @PostConstruct
    public void init() {
        planetVisualConfigDataBinder.setModel(visualUiService.getPlanetVisualConfig());
        planetVisualConfigDataBinder.addPropertyChangeHandler(event -> {
            shadowUiService.setupMatrices();
            shape3DUiService.updateLightDirection();
            viewService.onViewChanged();
            displayLightDirectionLabels();
            enableSaveButton(true);
        });
        displayLightDirectionLabels();
    }

    private void displayLightDirectionLabels() {
        shadowDirectionLabel.setText(DisplayUtils.formatVertex(shadowUiService.getLightDirection()));
        shape3DDirectionLabel.setText(DisplayUtils.formatVertex(shape3DUiService.getShape3DLightDirection()));
    }

    @Override
    protected void onConfigureDialog() {
        registerSaveButton(() -> {
            planetEditorProviderCaller.call(response -> {

            }, (message, throwable) -> {
                logger.log(Level.SEVERE, "getAudioItemConfigs failed: " + message, throwable);
                return false;
            }).updatePlanetVisualConfig(gameUiControl.getPlanetConfig().getPlanetId(), visualUiService.getPlanetVisualConfig());
        });
        enableSaveButton(false);
    }

}
