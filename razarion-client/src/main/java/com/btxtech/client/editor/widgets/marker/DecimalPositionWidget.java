package com.btxtech.client.editor.widgets.marker;

import com.btxtech.client.guielements.CommaDoubleBox;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasValue;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 13.08.2017.
 */
@Deprecated
@Templated("DecimalPositionWidget.html#decimalPositionWidget")
public class DecimalPositionWidget implements HasValue<DecimalPosition> {
    private static final String SHOW = "Show";
    private static final String HIDE = "Hide";
    // private Logger logger = Logger.getLogger(DecimalPositionWidget.class.getName());
    @Inject
    private MarkerEditor markerEditor;
    @Inject
    @DataField
    private Button showHideButton;
    @Inject
    @DataField
    private Button topViewButton;
    @Inject
    @DataField
    private CommaDoubleBox xField;
    @Inject
    @DataField
    private CommaDoubleBox yField;
    @Inject
    @DataField
    private Div decimalPositionWidget;
    private Collection<ValueChangeHandler<DecimalPosition>> handlers = new ArrayList<>();
    private Consumer<DecimalPosition> decimalPositionListener;

    @PostConstruct
    public void postConstruct() {
        showHideButton.setText(SHOW);
        topViewButton.setEnabled(false);
        decimalPositionWidget.addEventListener("DOMNodeRemovedFromDocument", event -> {
            if (showHideButton.getText().equalsIgnoreCase(HIDE)) {
                markerEditor.deactivate();
            }
        }, false);
    }

    public void init(DecimalPosition decimalPosition, Consumer<DecimalPosition> decimalPositionListener) {
        setValue(decimalPosition);
        this.decimalPositionListener = decimalPositionListener;
    }

    @Override
    public DecimalPosition getValue() {
        if (xField.getValue() != null && yField.getValue() != null) {
            return new DecimalPosition(xField.getValue(), yField.getValue());
        } else {
            return null;
        }
    }

    @Override
    public void setValue(DecimalPosition decimalPosition) {
        if (decimalPosition != null) {
            xField.setValue(decimalPosition.getX());
            yField.setValue(decimalPosition.getY());
        } else {
            xField.setValue(null);
            yField.setValue(null);
        }
    }

    @EventHandler("xField")
    public void xFieldChanged(ChangeEvent e) {
        fireEvent(null);
    }

    @EventHandler("yField")
    public void yFieldChanged(ChangeEvent e) {
        fireEvent(null);
    }

    @EventHandler("topViewButton")
    private void topViewButtonClick(ClickEvent event) {
        markerEditor.topView();
    }

    @Override
    public void setValue(DecimalPosition decimalPosition, boolean fireEvents) {
        setValue(decimalPosition);
        if (fireEvents) {
            fireEvent(null);
        }
    }

    @EventHandler("showHideButton")
    private void showHideButtonClicked(ClickEvent event) {
        if (showHideButton.getText().equalsIgnoreCase(SHOW)) {
            markerEditor.activate(getValue(), decimalPosition -> {
                xField.setValue(decimalPosition.getX());
                yField.setValue(decimalPosition.getY());
                fireEvent(null);
            }, () -> {
                showHideButton.setText(SHOW);
                topViewButton.setEnabled(false);
            });
            showHideButton.setText(HIDE);
            topViewButton.setEnabled(true);
        } else {
            markerEditor.deactivate();
            showHideButton.setText(SHOW);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<DecimalPosition> handler) {
        handlers.add(handler);
        return () -> handlers.remove(handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        ValueChangeEvent<DecimalPosition> valueChangeEvent = new ValueChangeEvent<DecimalPosition>(getValue()) {
        };

        for (ValueChangeHandler<DecimalPosition> handler : handlers) {
            handler.onValueChange(valueChangeEvent);
        }

        if (decimalPositionListener != null) {
            decimalPositionListener.accept(getValue());
        }
    }

    public void dispose() {
        markerEditor.deactivate();
        showHideButton.setText(SHOW);
        xField.setValue(null);
        yField.setValue(null);
        decimalPositionListener = null;
    }

}
