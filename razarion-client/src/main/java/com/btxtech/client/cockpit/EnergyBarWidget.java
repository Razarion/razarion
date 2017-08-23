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
    private static final String SVG_BAR_RED = "energyBarWidgetBarRed";
    private static final String SVG_BAR_GREEN = "energyBarWidgetBarGreen";
    private static final String SVG_TEXT = "energyBarWidgetText";
    private static final double WIDTH = 100;
    // private Logger logger = Logger.getLogger(EnergyBarWidget.class.getName());

    public void setEnergy(int consuming, int generating) {
        SVGTextElement svgTextElement = (SVGTextElement) DOM.getElementById(SVG_TEXT);
        SVGRectElement svgRectElementGreen = (SVGRectElement) DOM.getElementById(SVG_BAR_GREEN);
        SVGRectElement svgRectElementRed = (SVGRectElement) DOM.getElementById(SVG_BAR_RED);
        if (consuming == 0 && generating == 0) {
            svgTextElement.setTextContent("");
            svgRectElementGreen.setAttribute("width", "0");
            svgRectElementRed.setAttribute("width", "0");
        } else {
            svgTextElement.setTextContent(consuming + "/" + generating);
            double usage = MathHelper.clamp((double) consuming / (double) generating, 0.0, 1.0);
            svgRectElementGreen.setAttribute("width", Integer.toString((int) WIDTH));
            svgRectElementRed.setAttribute("width", Integer.toString((int) (WIDTH * usage)));
        }
    }
}
