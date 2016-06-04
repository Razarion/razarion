package com.btxtech.client.menu;

import com.btxtech.client.renderer.model.ShadowUiService;
import com.btxtech.shared.primitives.Vertex;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 07.11.2015.
 */
@Templated("ShadowMenu.html#menu-shadow")
public class ShadowMenu extends Composite {
    @Inject
    private ShadowUiService shadowUiService;
    @Inject
    @DataField
    private DoubleBox lightRotateX;
    @Inject
    @DataField
    private Label lightRotateXDisplay;
    @Inject
    @DataField
    private DoubleBox lightRotateY;
    @Inject
    @DataField
    private Label lightRotateYDisplay;
    @Inject
    @DataField
    private Label lightLabel;
    @Inject
    @DataField
    private DoubleBox ambientIntensity;
    @Inject
    @DataField
    private DoubleBox diffuseIntensity;
    @Inject
    @DataField
    private DoubleBox shadowAlpha;

    @PostConstruct
    public void init() {
        displayLightDirectionLabel();
        lightRotateX.setValue(Math.toDegrees(shadowUiService.getXAngle()));
        lightRotateXDisplay.setText(Double.toString(Math.toDegrees(shadowUiService.getXAngle())));
        lightRotateY.setValue(Math.toDegrees(shadowUiService.getYAngle()));
        lightRotateYDisplay.setText(Double.toString(Math.toDegrees(shadowUiService.getYAngle())));
        shadowAlpha.setValue(shadowUiService.getShadowAlpha());
        ambientIntensity.setValue(shadowUiService.getAmbientIntensity());
        diffuseIntensity.setValue(shadowUiService.getDiffuseIntensity());
    }

    private void displayLightDirectionLabel() {
        Vertex lightDirection = shadowUiService.getLightDirection();
        NumberFormat decimalFormat = NumberFormat.getFormat("#.##");
        lightLabel.setText("Light Direction (" + decimalFormat.format(lightDirection.getX()) + ":" + decimalFormat.format(lightDirection.getY()) + ":" + decimalFormat.format(lightDirection.getZ()) + ")");
    }

    @EventHandler("lightRotateX")
    public void lightRotateXChanged(ChangeEvent e) {
        shadowUiService.setXAngle(Math.toRadians(lightRotateX.getValue()));
        lightRotateXDisplay.setText(Double.toString(Math.toDegrees(shadowUiService.getXAngle())));
        displayLightDirectionLabel();
    }

    @EventHandler("lightRotateY")
    public void lightRotateYChanged(ChangeEvent e) {
        shadowUiService.setYAngle(Math.toRadians(lightRotateY.getValue()));
        lightRotateYDisplay.setText(Double.toString(Math.toDegrees(shadowUiService.getYAngle())));
        displayLightDirectionLabel();
    }

    @EventHandler("shadowAlpha")
    public void shadowAlphaChanged(ChangeEvent e) {
        shadowUiService.setShadowAlpha(shadowAlpha.getValue());
    }

    @EventHandler("ambientIntensity")
    public void ambientIntensityChanged(ChangeEvent e) {
        shadowUiService.setAmbientIntensity(ambientIntensity.getValue());
    }

    @EventHandler("diffuseIntensity")
    public void diffuseIntensityChanged(ChangeEvent e) {
        shadowUiService.setDiffuseIntensity(diffuseIntensity.getValue());
    }

}
