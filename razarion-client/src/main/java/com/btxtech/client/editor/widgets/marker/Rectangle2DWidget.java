package com.btxtech.client.editor.widgets.marker;

import com.btxtech.client.guielements.CommaDoubleBox;
import com.btxtech.shared.datatypes.Rectangle2D;
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
 * on 15.08.2017.
 */
@Deprecated
@Templated("Rectangle2DWidget.html#rectangle2DWidget")
public class Rectangle2DWidget implements HasValue<Rectangle2D> {
    private static final String SHOW = "Show";
    private static final String HIDE = "Hide";
    // private Logger logger = Logger.getLogger(Rectangle2DWidget.class.getName());
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
    private CommaDoubleBox wField;
    @Inject
    @DataField
    private CommaDoubleBox hField;
    @Inject
    @DataField
    private Div rectangle2DWidget;
    private Collection<ValueChangeHandler<Rectangle2D>> handlers = new ArrayList<>();
    private Consumer<Rectangle2D> rectangle2DListener;

    @PostConstruct
    public void postConstruct() {
        showHideButton.setText(SHOW);
        topViewButton.setEnabled(false);
        rectangle2DWidget.addEventListener("DOMNodeRemovedFromDocument", event -> {
            if (showHideButton.getText().equalsIgnoreCase(HIDE)) {
                markerEditor.deactivate();
            }
        }, false);
    }

    public void init(Rectangle2D rectangle2D, Consumer<Rectangle2D> rectangle2DListener) {
        setValue(rectangle2D);
        this.rectangle2DListener = rectangle2DListener;
    }

    @Override
    public Rectangle2D getValue() {
        if (xField.getValue() != null && yField.getValue() != null && wField.getValue() != null && hField.getValue() != null) {
            return new Rectangle2D(xField.getValue(), yField.getValue(), wField.getValue(), hField.getValue());
        } else {
            return null;
        }
    }

    @Override
    public void setValue(Rectangle2D rectangle2D) {
        if (rectangle2D != null) {
            xField.setValue(rectangle2D.getStart().getX());
            yField.setValue(rectangle2D.getStart().getY());
            wField.setValue(rectangle2D.height());
            hField.setValue(rectangle2D.width());
        } else {
            xField.setValue(null);
            yField.setValue(null);
            wField.setValue(null);
            hField.setValue(null);
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

    @EventHandler("wField")
    public void wFieldChanged(ChangeEvent e) {
        fireEvent(null);
    }

    @EventHandler("hField")
    public void hFieldChanged(ChangeEvent e) {
        fireEvent(null);
    }

    @EventHandler("topViewButton")
    private void topViewButtonClick(ClickEvent event) {
        markerEditor.topView();
    }

    @Override
    public void setValue(Rectangle2D rectangle2D, boolean fireEvents) {
        setValue(rectangle2D);
        if (fireEvents) {
            fireEvent(null);
        }
    }

    @EventHandler("showHideButton")
    private void showHideButtonClicked(ClickEvent event) {
        if (showHideButton.getText().equalsIgnoreCase(SHOW)) {
            markerEditor.activate(getValue(), newRectangle -> {
                setValue(newRectangle);
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
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Rectangle2D> handler) {
        handlers.add(handler);
        return () -> handlers.remove(handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        ValueChangeEvent<Rectangle2D> valueChangeEvent = new ValueChangeEvent<Rectangle2D>(getValue()) {
        };

        for (ValueChangeHandler<Rectangle2D> handler : handlers) {
            handler.onValueChange(valueChangeEvent);
        }

        if (rectangle2DListener != null) {
            rectangle2DListener.accept(getValue());
        }
    }

}
