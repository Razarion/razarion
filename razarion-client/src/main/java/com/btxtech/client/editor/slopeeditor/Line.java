package com.btxtech.client.editor.slopeeditor;

import com.btxtech.shared.datatypes.DecimalPosition;
import elemental.client.Browser;
import elemental.dom.Node;
import elemental.events.MouseEvent;
import elemental.svg.SVGLineElement;

/**
 * Created by Beat
 * 06.02.2016.
 */
public class Line {
    private SVGLineElement line;

    public Line(final Corner previous, DecimalPosition next, final Model model) {
        line = Browser.getDocument().createSVGLineElement();
        line.getX1().getBaseVal().setValue((float) previous.getSlopeShape().getPosition().getX());
        line.getY1().getBaseVal().setValue((float) previous.getSlopeShape().getPosition().getY());
        line.getX2().getBaseVal().setValue((float) next.getX());
        line.getY2().getBaseVal().setValue((float) next.getY());
        line.getStyle().setProperty("stroke", "blue");
        line.getStyle().setProperty("stroke-width", "0.2");
        line.getStyle().setCursor("copy");
        line.addEventListener("mousedown", evt -> model.createCorner(model.convertMouseToSvg((MouseEvent) evt), previous), false);

    }

    public Node getSvgElement() {
        return line;
    }

    public void moveStart(DecimalPosition position) {
        line.getX1().getBaseVal().setValue((float) position.getX());
        line.getY1().getBaseVal().setValue((float) position.getY());
    }

    public void moveEnd(DecimalPosition position) {
        line.getX2().getBaseVal().setValue((float) position.getX());
        line.getY2().getBaseVal().setValue((float) position.getY());
    }
}
