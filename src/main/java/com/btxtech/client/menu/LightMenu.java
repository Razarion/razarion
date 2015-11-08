package com.btxtech.client.menu;

import com.btxtech.client.renderer.model.Lighting;
import com.google.gwt.event.dom.client.ChangeEvent;
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
@Templated("LightMenu.html#menu-terrain")
public class LightMenu extends Composite {
    @Inject
    private Lighting lighting;
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
    @DataField()
    private Label lightRotateYDisplay;
    @Inject
    @DataField
    private DoubleBox ambientIntensity;
    @Inject
    @DataField
    private DoubleBox diffuseIntensity;
    @Inject
    @DataField
    private DoubleBox shadowProjectionTransformationZNear;
    @Inject
    @DataField()
    private DoubleBox shadowAlpha;

    @PostConstruct
    public void init() {
        lightRotateX.setValue(Math.toDegrees(lighting.getRotateX()));
        lightRotateXDisplay.setText(Double.toString(Math.toDegrees(lighting.getRotateX())));
        lightRotateY.setValue(Math.toDegrees(lighting.getRotateY()));
        lightRotateYDisplay.setText(Double.toString(Math.toDegrees(lighting.getRotateY())));
        shadowProjectionTransformationZNear.setValue(lighting.getZNear());
        shadowAlpha.setValue(lighting.getShadowAlpha());
        ambientIntensity.setValue(lighting.getAmbientIntensity());
        diffuseIntensity.setValue(lighting.getDiffuseIntensity());
    }

    @EventHandler("lightRotateX")
    public void lightRotateXChanged(ChangeEvent e) {
        lighting.setRotateX(Math.toRadians(lightRotateX.getValue()));
        lightRotateXDisplay.setText(Double.toString(Math.toDegrees(lighting.getRotateX())));
    }

    @EventHandler("lightRotateY")
    public void lightRotateYChanged(ChangeEvent e) {
        lighting.setRotateY(Math.toRadians(lightRotateY.getValue()));
        lightRotateYDisplay.setText(Double.toString(Math.toDegrees(lighting.getRotateY())));
    }

    @EventHandler("shadowProjectionTransformationZNear")
    public void shadowProjectionTransformationZNearChanged(ChangeEvent e) {
        lighting.setZNear(shadowProjectionTransformationZNear.getValue());
    }

    @EventHandler("shadowAlpha")
    public void shadowAlphaChanged(ChangeEvent e) {
        lighting.setShadowAlpha(shadowAlpha.getValue());
    }

    @EventHandler("ambientIntensity")
    public void ambientIntensityChanged(ChangeEvent e) {
        lighting.setAmbientIntensity(ambientIntensity.getValue());
    }

    @EventHandler("diffuseIntensity")
    public void diffuseIntensityChanged(ChangeEvent e) {
        lighting.setDiffuseIntensity(diffuseIntensity.getValue());
    }

}
