package com.btxtech.client.editor.widgets.placeconfig;

import com.btxtech.client.editor.widgets.marker.DecimalPositionWidget;
import com.btxtech.client.editor.widgets.marker.PolygonField;
import com.btxtech.client.guielements.CommaDoubleBox;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ValueListBox;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 08.08.2017.
 */
@Templated("PlaceConfigWidget.html#widget")
public class PlaceConfigWidget implements HasValue<PlaceConfig> {
    private static final String POSITION = "Position";
    private static final String POLYGON = "Polygon";
    // private Logger logger = Logger.getLogger(PlaceConfigWidget.class.getName());
    @Inject
    @DataField
    private ValueListBox<String> selector;
    @Inject
    @DataField
    private TableRow positionTr;
    @Inject
    @DataField
    private TableRow polygonTr;
    @Inject
    @DataField
    private PolygonField polygonField;
    @Inject
    @DataField
    private DecimalPositionWidget positionFiled;
    @Inject
    @DataField
    private CommaDoubleBox radiusField;
    private PlaceConfig placeConfig;
    private Consumer<PlaceConfig> placeConfigCallback;
    private Collection<ValueChangeHandler<PlaceConfig>> handlers = new ArrayList<>();

    @PostConstruct
    public void postConstruct() {
        selector.setAcceptableValues(Arrays.asList(POSITION, POLYGON));
        selector.addValueChangeHandler(event -> {
            placeConfig = null;
            switch (event.getValue()) {
                case POSITION:
                    toPosition();
                    break;
                case POLYGON:
                    toPolygon();
                    break;
                default:
                    toNone();
            }
            fireEvent(null);
        });
    }

    public void init(PlaceConfig placeConfig, Consumer<PlaceConfig> placeConfigCallback) {
        this.placeConfigCallback = placeConfigCallback;
        setValue(placeConfig);
    }

    @Override
    public PlaceConfig getValue() {
        return placeConfig;
    }

    @Override
    public void setValue(PlaceConfig placeConfig) {
        setValue(placeConfig, false);
    }

    @Override
    public void setValue(PlaceConfig placeConfig, boolean fireEvents) {
        this.placeConfig = placeConfig;
        if (placeConfig != null) {
            if (placeConfig.getPolygon2D() != null) {
                selector.setValue(POLYGON);
                toPolygon();
            } else if (placeConfig.getPosition() != null) {
                selector.setValue(POSITION);
                toPosition();
            } else {
                selector.setValue(null);
                toNone();
                if (this.placeConfig != null) {
                    this.placeConfig = null;
                    fireEvent(null);
                }
            }
        } else {
            selector.setValue(null);
            toNone();
        }
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

        if (placeConfigCallback != null) {
            placeConfigCallback.accept(getValue());
        }
    }

    @EventHandler("radiusField")
    public void radiusFieldChanged(ChangeEvent e) {
        if (this.placeConfig == null) {
            this.placeConfig = new PlaceConfig();
            this.placeConfig.setPosition(new DecimalPosition(0, 0));
        }
        placeConfig.setRadius(radiusField.getValue());
        fireEvent(null);
    }

    private void toPosition() {
        positionTr.getStyle().setProperty("display", "table-row");
        polygonTr.getStyle().setProperty("display", "none");
        DecimalPosition position = null;
        Double radius = null;
        if (placeConfig != null) {
            position = placeConfig.getPosition();
            radius = placeConfig.getRadius();
        }
        positionFiled.init(position, decimalPosition -> {
            if (this.placeConfig == null) {
                this.placeConfig = new PlaceConfig();
            }
            placeConfig.setPosition(decimalPosition);
            fireEvent(null);
        });
        radiusField.setValue(radius);
        polygonField.dispose();
    }

    private void toPolygon() {
        positionTr.getStyle().setProperty("display", "none");
        polygonTr.getStyle().setProperty("display", "table-row");
        Polygon2D polygon2D = null;
        if (placeConfig != null) {
            polygon2D = placeConfig.getPolygon2D();
        }
        polygonField.init(polygon2D, newPolygon2D -> {
            if (this.placeConfig == null) {
                this.placeConfig = new PlaceConfig();
            }
            placeConfig.setPolygon2D(newPolygon2D);
            fireEvent(null);
        });
        positionFiled.dispose();
        radiusField.setValue(null);
    }

    private void toNone() {
        positionTr.getStyle().setProperty("display", "none");
        polygonTr.getStyle().setProperty("display", "none");
        polygonField.dispose();
        positionFiled.dispose();
        radiusField.setValue(null);
    }

}
