package com.btxtech.client.cockpit.radar;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.uiservice.control.GameUiControl;
import elemental.html.CanvasRenderingContext2D;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 20.08.2017.
 */
public abstract class AbstractGameCoordinates extends AbstractMiniMap {
    // private Logger logger = Logger.getLogger(AbstractGameCoordinates.class.getName());
    @Inject
    private GameUiControl gameUiControl;

    @Override
    protected void setupTransformation(double zoom, CanvasRenderingContext2D ctx, int width, int height) {
        DecimalPosition planetSize = gameUiControl.getPlanetConfig().getSize();

        double scale = setupGameScale();
        DecimalPosition centerOffset = getViewField().calculateCenter();

        float xShift = setupXShift(width, planetSize.getX(), scale, centerOffset);
        float yShift = setupYShift(height, planetSize.getY(), scale, centerOffset);

        ctx.scale((float) scale, (float) -scale);
        ctx.translate(-xShift, -yShift);
    }

    public DecimalPosition canvasToReal(DecimalPosition canvasPosition) {
        DecimalPosition planetSize = gameUiControl.getPlanetConfig().getSize();

        double scale = setupGameScale();
        DecimalPosition real = canvasPosition.divide(scale, -scale);
        DecimalPosition centerOffset = getViewField().calculateCenter();
        real = real.add(setupXShift(getWidth(), planetSize.getX(), scale, centerOffset), setupYShift(getHeight(), planetSize.getY(), scale, centerOffset));
        return real;
    }

    protected Rectangle2D getVisibleField() {
        return new Rectangle2D(canvasToReal(new DecimalPosition(0, getHeight())), canvasToReal(new DecimalPosition(getWidth(), 0)));
    }

    protected float toCanvasPixel(double pixels) {
        return (float) (pixels / setupGameScale());
    }

    private float setupXShift(int width, double playWidth, double scale, DecimalPosition centerOffset) {
        float xDownerLimit = (float) (width / scale / 2.0);
        float xUpperLimit = (float) (playWidth - xDownerLimit);
        float xShift;
        if (centerOffset.getX() < xDownerLimit) {
            xShift = 0.0f;
        } else if (centerOffset.getX() > xUpperLimit) {
            xShift = xUpperLimit - xDownerLimit;
        } else {
            xShift = (float) (centerOffset.getX() - xDownerLimit);
        }
        return xShift;
    }

    private float setupYShift(int height, double playHeight, double scale, DecimalPosition centerOffset) {
        float yDownerLimit = (float) (height / scale / 2.0);
        float yUpperLimit = (float) (playHeight - yDownerLimit);
        float yShift;
        if (centerOffset.getY() < yDownerLimit) {
            yShift = (float) (playHeight - yUpperLimit + yDownerLimit);
        } else if (centerOffset.getY() > yUpperLimit) {
            yShift = (float) playHeight;
        } else {
            yShift = (float) (centerOffset.getY() + yDownerLimit);
        }
        return yShift;
    }
}
