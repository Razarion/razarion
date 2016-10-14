package com.btxtech.gameengine;

import com.btxtech.Abstract2dRenderer;
import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.projectile.Projectile;
import com.btxtech.shared.gameengine.planet.projectile.ProjectileService;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Beat
 * 13.10.2016.
 */
public class OverlayGameEngineRenderer extends Abstract2dRenderer {
    private static final long delay = 16;
    @Inject
    private ProjectileService projectileService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void init(Canvas canvas, double scale) {
        super.init(canvas, scale);

        startRender();
    }

    public void startRender() {
        scheduler.scheduleWithFixedDelay(() -> Platform.runLater(this::render), delay, delay, TimeUnit.MILLISECONDS);
    }

    private void render() {
        try {
            long timeStamp = System.currentTimeMillis();

            preRender();
            ExtendedGraphicsContext extendedGraphicsContext = createExtendedGraphicsContext();

            for (Projectile projectile : new ArrayList<>(projectileService.getProjectiles())) {
                DecimalPosition position = projectile.getInterpolatedModelMatrices(timeStamp).getModel().multiply(Vertex.ZERO, 1.0).toXY();
                extendedGraphicsContext.drawPosition(position, 0.2, Color.RED);
            }

            postRender();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
