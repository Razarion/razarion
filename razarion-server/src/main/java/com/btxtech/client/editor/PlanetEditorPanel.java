package com.btxtech.client.editor;

import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.client.editor.widgets.LightWidget;
import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.client.utils.GradToRadConverter;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.terrain.Water;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.handler.property.PropertyChangeEvent;
import org.jboss.errai.databinding.client.api.handler.property.PropertyChangeHandler;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 14.08.2016.
 */
@Templated("PlanetEditorPanel.html#planet-editor-panel")
public class PlanetEditorPanel extends Composite implements LeftSideBarContent {
    @Inject
    private ShadowUiService shadowUiService;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    @AutoBound
    private DataBinder<ShadowUiService> shadowUiServiceDataBinder;
    @Inject
    @Bound(property = "rotateX", converter = GradToRadConverter.class)
    @DataField
    private DoubleBox rotateXSlider;
    @Inject
    @Bound(property = "rotateX", converter = GradToRadConverter.class)
    @DataField
    private DoubleBox rotateXBox;
    @Inject
    @Bound(property = "rotateZ", converter = GradToRadConverter.class)
    @DataField
    private DoubleBox rotateZSlider;
    @Inject
    @Bound(property = "rotateZ", converter = GradToRadConverter.class)
    @DataField
    private DoubleBox rotateZBox;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label directionLabel;
    @Inject
    @Bound
    @DataField
    private DoubleBox shadowAlpha;
    @Inject
    @DataField
    private LightWidget lightWidget;
    @Inject
    @DataField
    private DoubleBox transparency;
    @Inject
    @DataField
    private DoubleBox bumpMap;
    @Inject
    @DataField
    private DoubleBox level;
    @Inject
    @DataField
    private DoubleBox ground;

    @PostConstruct
    public void init() {
        // Shadow
        shadowUiServiceDataBinder.setModel(shadowUiService);
        shadowUiServiceDataBinder.addPropertyChangeHandler(event -> displayLightDirectionLabel());
        displayLightDirectionLabel();
        // Water
        Water water = terrainUiService.getWater();
        transparency.setValue(water.getWaterTransparency());
        bumpMap.setValue(water.getWaterBumpMapDepth());
        level.setValue(water.getLevel());
        ground.setValue(water.getGround());
        lightWidget.setModel(water.getLightConfig());
    }

    @EventHandler("transparency")
    public void transparencyChanged(ChangeEvent e) {
        terrainUiService.getWater().setWaterTransparency(transparency.getValue());
    }

    @EventHandler("bumpMap")
    public void bumpMapChanged(ChangeEvent e) {
        terrainUiService.getWater().setWaterBumpMapDepth(bumpMap.getValue());
    }

    @EventHandler("level")
    public void levelChanged(ChangeEvent e) {
        terrainUiService.getWater().setLevel(level.getValue());
    }

    @EventHandler("ground")
    public void groundChanged(ChangeEvent e) {
        terrainUiService.getWater().setGround(ground.getValue());
    }

    private void displayLightDirectionLabel() {
        Vertex lightDirection = shadowUiService.getLightDirection();
        directionLabel.setText(DisplayUtils.NUMBER_FORMATTER_X_XX.format(lightDirection.getX()) + ":" + DisplayUtils.NUMBER_FORMATTER_X_XX.format(lightDirection.getY()) + ":" + DisplayUtils.NUMBER_FORMATTER_X_XX.format(lightDirection.getZ()));
    }

    @Override
    public void onClose() {

    }
}
