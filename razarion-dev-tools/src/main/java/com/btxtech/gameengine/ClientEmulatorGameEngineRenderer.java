package com.btxtech.gameengine;

import com.btxtech.Abstract2dRenderer;
import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Beat
 * 13.10.2016.
 */
public class ClientEmulatorGameEngineRenderer extends Abstract2dRenderer {
    private static final long delay = 16;
    @Inject
    private ClientEmulator clientEmulator;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private List<Vertex> itemNullPosition = Arrays.asList(new Vertex(-1, 0, 0), new Vertex(1, 0, 0));

    public void init(Canvas canvas, double scale) {
        super.init(canvas, scale);
        scheduler.scheduleWithFixedDelay(() -> Platform.runLater(this::render), delay, delay, TimeUnit.MILLISECONDS);
    }

    private void render() {
        try {
            preRender();
            ExtendedGraphicsContext extendedGraphicsContext = createExtendedGraphicsContext();

            double factor = (double) (System.currentTimeMillis() - clientEmulator.getLastUpdateTimeStamp()) / 1000.0;

            for (ModelMatrices modelMatrices : clientEmulator.getAliveModelMatrices()) {
                throw new UnsupportedOperationException("Don't know how to solve");
//                Matrix4 model = modelMatrices.interpolateVelocity(factor).getModel();
//                List<Vertex> transformed = new ArrayList<>();
//                for (Vertex vertex : itemNullPosition) {
//                    transformed.add(model.multiply(vertex, 1.0));
//                }
//                extendedGraphicsContext.strokeCurve(transformed, 0.5, Color.RED, false);
            }


            postRender();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
