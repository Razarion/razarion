package com.btxtech.client.editor.widgets;

import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.client.utils.GradToRadConverter;
import com.btxtech.client.utils.HtmlColor2ColorConverter;
import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.datatypes.Vertex;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.handler.property.PropertyChangeEvent;
import org.jboss.errai.databinding.client.api.handler.property.PropertyChangeHandler;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 28.05.2016.
 */
@Templated("LightWidget.html#lightWidget")
public class LightWidget extends Composite {
    @Inject
    @AutoBound
    private DataBinder<LightConfig> lightConfigDataBinder;
    @Inject
    @Bound(converter = GradToRadConverter.class, property = "rotationX")
    @DataField
    private DoubleBox xRotationSlider;
    @Inject
    @Bound(converter = GradToRadConverter.class, property = "rotationX")
    @DataField
    private DoubleBox xRotationBox;
    @Inject
    @Bound(converter = GradToRadConverter.class, property = "rotationY")
    @DataField
    private DoubleBox yRotationSlider;
    @Inject
    @Bound(converter = GradToRadConverter.class, property = "rotationY")
    @DataField
    private DoubleBox yRotationBox;
    @Inject
    @DataField
    private Label directionLabel;
    @Inject
    @Bound(converter = HtmlColor2ColorConverter.class)
    @DataField
    private TextBox diffuse;
    @Inject
    @Bound(converter = HtmlColor2ColorConverter.class)
    @DataField
    private TextBox ambient;
    @Inject
    @Bound
    @DataField
    private DoubleBox specularIntensity;
    @Inject
    @Bound
    @DataField
    private DoubleBox specularHardness;

    public void setModel(LightConfig lightConfig) {
        lightConfigDataBinder.setModel(lightConfig);
        lightConfigDataBinder.addPropertyChangeHandler(new PropertyChangeHandler<Object>() {
            @Override
            public void onPropertyChange(PropertyChangeEvent<Object> event) {
                displayLightDirectionLabel();
            }
        });
        displayLightDirectionLabel();
    }

    private void displayLightDirectionLabel() {
        Vertex lightDirection = lightConfigDataBinder.getModel().setupDirection();
        directionLabel.setText(DisplayUtils.formatVertex(lightDirection));
    }

}
