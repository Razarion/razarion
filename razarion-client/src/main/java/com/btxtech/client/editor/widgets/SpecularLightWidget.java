package com.btxtech.client.editor.widgets;

import com.btxtech.common.DisplayUtils;
import com.btxtech.client.utils.GradToRadConverter;
import com.btxtech.client.utils.HtmlColor2ColorConverter;
import com.btxtech.shared.dto.SpecularLightConfig;
import com.btxtech.shared.datatypes.Vertex;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.common.client.dom.Input;
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
@Templated("SpecularLightWidget.html#specularLightWidget")
public class SpecularLightWidget extends Composite {
    @Inject
    @AutoBound
    private DataBinder<SpecularLightConfig> lightConfigDataBinder;
    @Inject
    @Bound
    @DataField
    private Input specularIntensity;
    @Inject
    @Bound
    @DataField
    private Input specularHardness;

    public void setModel(SpecularLightConfig specularLightConfig) {
        lightConfigDataBinder.setModel(specularLightConfig);
    }
}
