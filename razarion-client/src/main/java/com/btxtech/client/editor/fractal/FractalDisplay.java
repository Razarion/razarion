package com.btxtech.client.editor.fractal;

import com.btxtech.shared.utils.InterpolationUtils;
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

    public void display(FractalDialogDto fractalDialogDto) {
        CanvasRenderingContext2D ctx = (CanvasRenderingContext2D) canvasElement.getContext("2d");

        ctx.clearRect(0, 0, canvasElement.getWidth(), canvasElement.getHeight());

        if (fractalDialogDto.getFractalField() == null) {
            return;
        }

        // find min & max
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (int x = 0; x < fractalDialogDto.getFractalFieldConfig().getXCount(); x++) {
            for (int y = 0; y < fractalDialogDto.getFractalFieldConfig().getYCount(); y++) {
                double f = fractalDialogDto.getFractalField()[x][y];
                min = Math.min(min, f);
                max = Math.max(max, f);
            }
        }

        ctx.save();
        int pixelCount = Math.max(fractalDialogDto.getFractalFieldConfig().getXCount(), fractalDialogDto.getFractalFieldConfig().getYCount());
        int dimension = Math.max(canvasElement.getWidth(), canvasElement.getHeight());
        float scale = (float) dimension / (float) pixelCount;
        ctx.scale(scale, scale);

        for (int x = 0; x < fractalDialogDto.getFractalFieldConfig().getXCount(); x++) {
            for (int y = 0; y < fractalDialogDto.getFractalFieldConfig().getYCount(); y++) {
                double f = fractalDialogDto.getFractalField()[x][y];
                int c = (int) InterpolationUtils.interpolate(0, 255, min, max, f);
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
