package com.btxtech.client.editor.widgets.placeconfig;

import com.btxtech.client.editor.widgets.marker.PolygonField;
import com.btxtech.client.guielements.CommaDoubleBox;
import com.btxtech.client.guielements.DecimalPositionBox;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import org.jboss.errai.common.client.dom.RadioInput;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * on 08.08.2017.
 */
@Templated("PlaceConfigWidget.html#widget")
public class PlaceConfigWidget implements HasValue<PlaceConfig> {
    // private Logger logger = Logger.getLogger(PlaceConfigWidget.class.getName());
    @Inject
    @DataField
    private RadioInput polygonRadio;
    @Inject
    @DataField
    private RadioInput positionRadiusRadio;
    @Inject
    @DataField
    private PolygonField polygonField;
    @Inject
    @DataField
    private DecimalPositionBox positionFiled;
    @Inject
    @DataField
    private CommaDoubleBox radiusField;
    private PlaceConfig placeConfig;
    private Collection<ValueChangeHandler<PlaceConfig>> handlers = new ArrayList<>();

    @Override
    public PlaceConfig getValue() {
        return placeConfig;
    }

    @Override
    public void setValue(PlaceConfig placeConfig) {
        this.placeConfig = placeConfig;
        if (placeConfig != null) {
            if (placeConfig.getPolygon2D() != null) {
                polygonRadio.setChecked(true);
                positionRadiusRadio.setChecked(false);
                polygonField.init(placeConfig.getPolygon2D(), polygon2D -> {
                    placeConfig.setPolygon2D(polygon2D);
                    fireEvent(null);
                });
            } else if (placeConfig.getPosition() != null) {
                polygonRadio.setChecked(false);
                positionRadiusRadio.setChecked(true);
                positionFiled.setValue(placeConfig.getPosition());
                radiusField.setValue(placeConfig.getRadius());
            } else {
                polygonRadio.setChecked(false);
                positionRadiusRadio.setChecked(false);
            }
        } else {
            polygonRadio.setChecked(false);
            positionRadiusRadio.setChecked(false);
        }
    }

    @Override
    public void setValue(PlaceConfig placeConfig, boolean fireEvents) {
        this.placeConfig = placeConfig;
        if (fireEvents) {
            fireEvent(null);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<PlaceConfig> handler) {
        handlers.add(handler);
        return () -> handlers.remove(handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        ValueChangeEvent<PlaceConfig> valueChangeEvent = new ValueChangeEvent<PlaceConfig>(getValue()) {
        };

        for (ValueChangeHandler<PlaceConfig> handler : handlers) {
            handler.onValueChange(valueChangeEvent);
        }
    }

    @EventHandler("polygonRadio")
    private void polygonRadioClick(ClickEvent event) {
        if (placeConfig == null) {
            placeConfig = new PlaceConfig();
        }
        polygonField.init(placeConfig.getPolygon2D(), polygon2D -> {
            placeConfig.setPolygon2D(polygon2D);
            fireEvent(null);
        });
        placeConfig.setPosition(null);
        placeConfig.setRadius(null);
        positionFiled.setValue(null);
        radiusField.setValue(null);
        fireEvent(null);
    }

    @EventHandler("positionRadiusRadio")
    private void positionRadiusRadioClick(ClickEvent event) {
        if (placeConfig == null) {
            placeConfig = new PlaceConfig();
        }
        placeConfig.setPolygon2D(null);
        polygonField.dispose();
        fireEvent(null);
    }

    @EventHandler("positionFiled")
    public void positionFiledChanged(ChangeEvent e) {
        if (positionRadiusRadio.getChecked()) {
            placeConfig.setPosition(positionFiled.getValue());
            fireEvent(null);
        }
    }

    @EventHandler("radiusField")
    public void radiusFieldChanged(ChangeEvent e) {
        if (positionRadiusRadio.getChecked()) {
            placeConfig.setRadius(radiusField.getValue());
            fireEvent(null);
        }
    }

}
