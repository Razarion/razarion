package com.btxtech.client.menu;

import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.client.utils.GradToRadConverter;
import com.btxtech.shared.datatypes.Vertex;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.PropertyChangeEvent;
import org.jboss.errai.databinding.client.api.PropertyChangeHandler;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
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
    @Inject
    @DataField
    private Label directionLabel;
    @Inject
    @DataField
    private DoubleBox ambientIntensity;
    @Inject
    @DataField
    private DoubleBox diffuseIntensity;
    @Inject
    @Bound
    @DataField
    private DoubleBox shadowAlpha;

    @PostConstruct
    public void init() {
        shadowUiServiceDataBinder.setModel(shadowUiService);
        shadowUiServiceDataBinder.addPropertyChangeHandler(new PropertyChangeHandler<Object>() {
            @Override
            public void onPropertyChange(PropertyChangeEvent<Object> event) {
                displayLightDirectionLabel();
            }
        });
        displayLightDirectionLabel();
        ambientIntensity.setValue(shadowUiService.getAmbientIntensity());
        diffuseIntensity.setValue(shadowUiService.getDiffuseIntensity());
    }

    private void displayLightDirectionLabel() {
        Vertex lightDirection = shadowUiService.getLightDirection();
        directionLabel.setText("Light Direction (" + DisplayUtils.NUMBER_FORMATTER_X_XX.format(lightDirection.getX()) + ":" + DisplayUtils.NUMBER_FORMATTER_X_XX.format(lightDirection.getY()) + ":" + DisplayUtils.NUMBER_FORMATTER_X_XX.format(lightDirection.getZ()) + ")");
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
