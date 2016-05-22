package com.btxtech.client.dialog.content.fractal;

import com.btxtech.shared.dto.FractalFieldConfig;
import com.btxtech.shared.MathHelper2;
import com.google.gwt.dom.client.Element;
import elemental.html.CanvasElement;
import elemental.html.CanvasRenderingContext2D;

/**
 * Created by Beat
 * 20.05.2016.
 */
public class FractalDisplay {
    // private Logger logger = Logger.getLogger(FractalDisplay.class.getName());
    private CanvasElement canvasElement;

    public FractalDisplay(Element canvasElement) {
        this.canvasElement = (CanvasElement) canvasElement;
    }

    public void display(FractalFieldConfig fractalFieldConfig) {
        CanvasRenderingContext2D ctx = (CanvasRenderingContext2D) canvasElement.getContext("2d");

        ctx.clearRect(0, 0, canvasElement.getWidth(), canvasElement.getHeight());

        if (fractalFieldConfig.getClampedFractalField() == null) {
            return;
        }

        ctx.save();
        int pixelCount = Math.max(fractalFieldConfig.getXCount(), fractalFieldConfig.getYCount());
        int dimension = Math.max(canvasElement.getWidth(), canvasElement.getHeight());
        float scale = (float) dimension / (float) pixelCount;
        ctx.scale(scale, scale);

        for (int x = 0; x < fractalFieldConfig.getXCount(); x++) {
            for (int y = 0; y < fractalFieldConfig.getYCount(); y++) {
                double f = fractalFieldConfig.getClampedFractalField()[x][y];
                int c = (int) MathHelper2.interpolate(0, 255, fractalFieldConfig.getFractalMin(), fractalFieldConfig.getFractalMax(), f);
                if (c < 0 || c > 255) {
                    throw new IllegalArgumentException("f=" + f + " c=" + c);
                }
                ctx.setFillStyle("rgb(" + c + "," + c + "," + c + ")");
                ctx.fillRect(x, y, 1, 1);
            }
        }
        ctx.restore();
    }
}
