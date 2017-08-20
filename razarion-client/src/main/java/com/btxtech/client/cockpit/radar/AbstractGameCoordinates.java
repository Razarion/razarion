package com.btxtech.client.cockpit.radar;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.uiservice.control.GameUiControl;
import elemental.html.CanvasRenderingContext2D;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 20.08.2017.
 */
public abstract class AbstractGameCoordinates extends AbstractMiniMap {
    // private Logger logger = Logger.getLogger(AbstractGameCoordinates.class.getName());
    @Inject
    private GameUiControl gameUiControl;

    @Override
    protected void setupTransformation(ScaleStep scaleStep, CanvasRenderingContext2D ctx, int width, int height) {
        Rectangle2D playGround = gameUiControl.getPlanetConfig().getPlayGround();

        if (scaleStep == ScaleStep.WHOLE_MAP) {
            double scale = setupScale(width, height, playGround);
            ctx.scale((float) scale, (float) -scale);
            ctx.translate((float) -playGround.startX(), (float) (-playGround.startY() - playGround.height()));
        } else {
            throw new IllegalArgumentException("AbstractMiniMap.setScaleStep(): " + scaleStep);
        }
    }

    private double setupScale(int width, double height, Rectangle2D playGround) {
        return (float) Math.min(width / playGround.width(), height / playGround.height());
    }

    public DecimalPosition canvasToReal(DecimalPosition canvasPosition) {
        Rectangle2D playGround = gameUiControl.getPlanetConfig().getPlayGround();
        double scale = setupScale(getWidth(), getHeight(), playGround);
        DecimalPosition real = canvasPosition.divide(scale, -scale);
        real = real.add(playGround.startX(), playGround.startY() + playGround.height());
        return real;
    }


}
