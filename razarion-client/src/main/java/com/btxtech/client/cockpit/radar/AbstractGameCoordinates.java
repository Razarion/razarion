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
        Rectangle2D playGround = gameUiControl.getPlanetConfig().getPlayGround();

        double scale = setupGameScale();
        DecimalPosition centerOffset = getViewField().calculateCenter().sub(playGround.getStart());

        float xShift = setupXShift(width, playGround, scale, centerOffset);
        float yShift = setupYShift(height, playGround, scale, centerOffset);

        ctx.scale((float) scale, (float) -scale);
        ctx.translate(-xShift, -yShift);
    }

    public DecimalPosition canvasToReal(DecimalPosition canvasPosition) {
        double scale = setupGameScale();
        DecimalPosition real = canvasPosition.divide(scale, -scale);
        Rectangle2D playGround = gameUiControl.getPlanetConfig().getPlayGround();
        DecimalPosition centerOffset = getViewField().calculateCenter().sub(playGround.getStart());
        real = real.add(setupXShift(getWidth(), playGround, scale, centerOffset), setupYShift(getHeight(), playGround, scale, centerOffset));
        return real;
    }

    protected Rectangle2D getVisibleField() {
        return new Rectangle2D(canvasToReal(new DecimalPosition(0, getHeight())), canvasToReal(new DecimalPosition(getWidth(), 0)));
    }

    protected float toCanvasPixel(double pixels) {
        return (float) (pixels / setupGameScale());
    }

    private float setupXShift(int width, Rectangle2D playGround, double scale, DecimalPosition centerOffset) {
        float xDownerLimit = (float) (width / scale / 2.0);
        float xUpperLimit = (float) (playGround.width() - xDownerLimit);
        float xShift;
        if (centerOffset.getX() < xDownerLimit) {
            xShift = (float) playGround.startX();
        } else if (centerOffset.getX() > xUpperLimit) {
            xShift = (float) (playGround.startX() + xUpperLimit - xDownerLimit);
        } else {
            xShift = (float) (centerOffset.getX() + playGround.startX() - xDownerLimit);
        }
        return xShift;
    }

    private float setupYShift(int height, Rectangle2D playGround, double scale, DecimalPosition centerOffset) {
        float yDownerLimit = (float) (height / scale / 2.0);
        float yUpperLimit = (float) (playGround.height() - yDownerLimit);
        float yShift;
        if (centerOffset.getY() < yDownerLimit) {
            yShift = (float) (playGround.startY() + playGround.height() - yUpperLimit + yDownerLimit);
        } else if (centerOffset.getY() > yUpperLimit) {
            yShift = (float) (playGround.startY() + playGround.height());
        } else {
            yShift = (float) (centerOffset.getY() + playGround.startY() + yDownerLimit);
        }
        return yShift;
    }
}
