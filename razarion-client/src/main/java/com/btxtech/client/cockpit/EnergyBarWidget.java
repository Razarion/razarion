package com.btxtech.client.cockpit;

import com.btxtech.shared.utils.MathHelper;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import elemental.svg.SVGRectElement;
import elemental.svg.SVGTextElement;
import org.jboss.errai.ui.shared.api.annotations.Templated;

/**
 * Created by Beat
 * on 21.08.2017.
 */
@Templated("EnergyBarWidget.html#energyBarWidget")
public class EnergyBarWidget extends Composite {
    private static final double WIDTH = 100;
    // private Logger logger = Logger.getLogger(EnergyBarWidget.class.getName());

    public void setEnergy(double generating, double consuming) {
        SVGRectElement svgRectElement = (SVGRectElement) DOM.getElementById("energyBarWidgetBar");
        double factor = MathHelper.clamp(consuming / generating, 0.0, 1.0);

        svgRectElement.setAttribute("width", Integer.toString((int) (WIDTH * factor)));
        if(factor < 1.0) {
            svgRectElement.setAttribute("style", "fill: green");
        } else {
            svgRectElement.setAttribute("style", "fill: red");
        }
        SVGTextElement svgTextElement = (SVGTextElement) DOM.getElementById("energyBarWidgetText");
        svgTextElement.setTextContent(consuming + "/" + generating);
    }
}
