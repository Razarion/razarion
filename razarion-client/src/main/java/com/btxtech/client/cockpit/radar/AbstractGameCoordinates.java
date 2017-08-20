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
        ctx.scale((float) scale, (float) -scale);
        ctx.translate((float) -playGround.startX(), (float) (-playGround.startY() - playGround.height()));
    }

    public DecimalPosition canvasToReal(DecimalPosition canvasPosition) {
        Rectangle2D playGround = gameUiControl.getPlanetConfig().getPlayGround();
        double scale = setupGameScale();
        DecimalPosition real = canvasPosition.divide(scale, -scale);
        real = real.add(playGround.startX(), playGround.startY() + playGround.height());
        return real;
    }


}
