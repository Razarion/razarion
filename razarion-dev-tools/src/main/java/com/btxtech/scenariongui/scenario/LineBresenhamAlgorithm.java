package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.scenariongui.InstanceStringGenerator;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.utils.GeometricUtil;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * Created by Beat
 * 21.01.2017.
 */
public class LineBresenhamAlgorithm extends Scenario {
    private static final int TILE_SIZE = 8;
    private DecimalPosition mouseStart;
    private DecimalPosition mouseEnd;
    private boolean drawMode = false;

    @Override
    public void render(ExtendedGraphicsContext extendedGraphicsContext) {
        if (mouseStart != null && mouseEnd != null) {

            try {
                List<Index> tiles = GeometricUtil.rasterizeLine(new Line(mouseStart, mouseEnd), TILE_SIZE);

                extendedGraphicsContext.getGc().setFill(Color.GREEN);
                for (Index tile : tiles) {
                    extendedGraphicsContext.getGc().fillRect(tile.getX() * TILE_SIZE, tile.getY() * TILE_SIZE, TILE_SIZE - 0.05, TILE_SIZE - 0.05);
                }

                extendedGraphicsContext.getGc().setStroke(Color.RED);
                extendedGraphicsContext.getGc().setLineWidth(0.1);
                extendedGraphicsContext.getGc().strokeLine(mouseStart.getX(), mouseStart.getY(), mouseEnd.getX(), mouseEnd.getY());
            } catch (Throwable t) {
                System.out.println("mouseStart: " + mouseStart + " mouseEnd: " + mouseEnd);
                t.printStackTrace();
            }
        }
    }

    @Override
    public boolean onMouseDown(DecimalPosition position) {
        if (!drawMode) {
            mouseStart = position;
        }
        drawMode = !drawMode;
        return false;
    }

    @Override
    public boolean onMouseMove(DecimalPosition position) {
        if (drawMode) {
            mouseEnd = position;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onGenerate() {
        Line line = new Line(mouseStart, mouseEnd);
        List<Index> expected = GeometricUtil.rasterizeLine(line, TILE_SIZE);
        System.out.println("------------------------------------------------------------");
        System.out.println("    @Test");
        System.out.println("    public void rasterizeLine() throws Exception {");
        System.out.println("        List<Index> actual = GeometricUtil.rasterizeLine(" + InstanceStringGenerator.generate(line) + ", " + TILE_SIZE + ");");
        System.out.println("        " + InstanceStringGenerator.generateIndexList(expected));
        System.out.println("        GeometricUtilTest.assertIndices(positions, actual);");
        System.out.println("    }");
    }

    private void verifyLoS(DecimalPosition start, DecimalPosition end, ExtendedGraphicsContext extendedGraphicsContext) {
        double deltaX = end.getX() - start.getX();
        double deltaY = end.getY() - start.getY();
        double distance = Math.abs(deltaX) + Math.abs(deltaY);

        double dx = deltaX / distance;
        double dy = deltaY / distance;

        for (int i = 0; i <= Math.ceil(distance); i++) {
            int x = (int) Math.floor(start.getX() + dx * i);
            int y = (int) Math.floor(start.getY() + dy * i);
            extendedGraphicsContext.getGc().fillRect(x, y, 1, 1);
        }
    }

    void raytrace(int xStart, int yStart, int xEnd, int yEnd, ExtendedGraphicsContext extendedGraphicsContext) {
        int dx = Math.abs(xEnd - xStart);
        int dy = Math.abs(yEnd - yStart);
        int x = xStart;
        int y = yStart;
        int n = 1 + dx + dy;
        int xInc = (xEnd > xStart) ? 1 : -1;
        int yInc = (yEnd > yStart) ? 1 : -1;
        int error = dx - dy;
        dx *= 2;
        dy *= 2;

        for (; n > 0; --n) {
            extendedGraphicsContext.getGc().fillRect(x, y, 1, 1);

            if (error > 0) {
                x += xInc;
                error -= dy;
            } else {
                y += yInc;
                error += dx;
            }
        }
    }

    public void line(int xStart, int yStart, int xEnd, int yEnd, int delta, ExtendedGraphicsContext extendedGraphicsContext) {
        int width = xEnd - xStart;
        int height = yEnd - yStart;
        int dx1 = 0;
        int dy1 = 0;
        int dx2 = 0;
        int dy2 = 0;
        if (width < 0) {
            dx1 = -delta;
            dx2 = -delta;
        } else if (width > 0) {
            dx1 = delta;
            dx2 = delta;
        }
        if (height < 0) {
            dy1 = -delta;
        } else if (height > 0) {
            dy1 = delta;
        }
        int longest = Math.abs(width);
        int shortest = Math.abs(height);
        if (longest <= shortest) {
            longest = Math.abs(height);
            shortest = Math.abs(width);
            if (height < 0) {
                dy2 = -delta;
            } else if (height > 0) {
                dy2 = delta;
            }
            dx2 = 0;
        }
        int numerator = (int) (longest / 2.0);
        for (int i = 0; i <= longest; i += delta) {
            //////////////////////////////////////
            extendedGraphicsContext.getGc().fillRect(xStart, yStart, delta, delta);
            //////////////////////////////////////
            numerator += shortest;
            if (numerator < longest) {
                xStart += dx2;
                yStart += dy2;
            } else {
                numerator -= longest;
                xStart += dx1;
                yStart += dy1;
            }
        }
    }

}
