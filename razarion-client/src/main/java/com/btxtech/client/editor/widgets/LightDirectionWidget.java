package com.btxtech.client.editor.widgets;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.system.ExceptionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import elemental2.svg.SVGCircleElement;
import elemental2.svg.SVGSVGElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * on 23.11.2018.
 */
@Templated("LightDirectionWidget.html#div")
public class LightDirectionWidget implements HasValue<Vertex>, IsElement {
    // private Logger logger = Logger.getLogger(LightDirectionWidget.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    @DataField
    private HTMLDivElement div;
    private SVGCircleElement planetElement;
    private SVGCircleElement sunElement;
    private Vertex lightDirection;
    private Collection<ValueChangeHandler<Vertex>> handlers = new ArrayList<>();

    @PostConstruct
    public void postConstruct() {
        SVGSVGElement svgElement = (SVGSVGElement) div.firstElementChild;
        svgElement.addEventListener("mousemove", evt -> {
            try {
                drag((MouseEvent) evt);
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        });
        planetElement = (SVGCircleElement) svgElement.firstElementChild;
        sunElement = (SVGCircleElement) svgElement.lastElementChild;
    }

    @Override
    public HTMLElement getElement() {
        return div;
    }

    @Override
    public Vertex getValue() {
        return lightDirection;
    }

    @Override
    public void setValue(Vertex value) {
        setValue(value, false);
    }

    @Override
    public void setValue(Vertex value, boolean fireEvents) {
        lightDirection = value;
        if (lightDirection != null) {
            double planetRadius = planetElement.r.baseVal.value;

            sunElement.cx.baseVal.value = planetElement.cx.baseVal.value - value.getX() * planetRadius;
            sunElement.cy.baseVal.value = planetElement.cy.baseVal.value + value.getY() * planetRadius;
        }
        if (fireEvents) {
            fireEvent(null);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Vertex> handler) {
        handlers.add(handler);
        return () -> handlers.remove(handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        ValueChangeEvent<Vertex> valueChangeEvent = new ValueChangeEvent<Vertex>(getValue()) {
        };

        handlers.forEach(handler -> handler.onValueChange(valueChangeEvent));
    }

    private void drag(MouseEvent mouseEvent) {
        try {
            if (mouseEvent.buttons == 1) {
                mouseEvent.preventDefault();

                double dragX = mouseEvent.offsetX;
                double dragY = mouseEvent.offsetY;
                double planetCenterX = planetElement.cx.baseVal.value;
                double planetCenterY = planetElement.cy.baseVal.value;
                double xDiff = dragX - planetCenterX;
                double yDiff = dragY - planetCenterY;
                double distance = Math.sqrt(xDiff * xDiff + yDiff * yDiff);
                double planetRadius = planetElement.r.baseVal.value;
                if (distance > planetRadius) {
                    return;
                }

                sunElement.cx.baseVal.value = dragX;
                sunElement.cy.baseVal.value = dragY;

                // Setup light vector
                double lightDirectionX = Math.min(xDiff / planetRadius, 1.0);
                double lightDirectionY = -Math.min(yDiff / planetRadius, 1.0);
                double lightDirectionZ = Math.sqrt(1 - lightDirectionX * lightDirectionX - lightDirectionY * lightDirectionY);

                lightDirection = new Vertex(-lightDirectionX, -lightDirectionY, -lightDirectionZ);

                fireEvent(null);
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

}
