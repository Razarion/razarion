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

        double scale = setupScale(width, height, playGround, zoom);
        ctx.scale((float) scale, (float) -scale);
        ctx.translate((float) -playGround.startX(), (float) (-playGround.startY() - playGround.height()));
    }

    private double setupScale(int width, double height, Rectangle2D playGround, double zoom) {
        double scale = (float) Math.min(width / playGround.width(), height / playGround.height());
        scale *= zoom;
        return scale;
    }

    public DecimalPosition canvasToReal(DecimalPosition canvasPosition) {
        Rectangle2D playGround = gameUiControl.getPlanetConfig().getPlayGround();
        double scale = setupScale(getWidth(), getHeight(), playGround, getZoom());
        DecimalPosition real = canvasPosition.divide(scale, -scale);
        real = real.add(playGround.startX(), playGround.startY() + playGround.height());
        return real;
    }


}
