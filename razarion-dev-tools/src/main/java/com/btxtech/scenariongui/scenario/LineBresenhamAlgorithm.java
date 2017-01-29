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
                    extendedGraphicsContext.getGc().fillRect(tile.getX() * TILE_SIZE, tile.getY() * TILE_SIZE, TILE_SIZE - 0.1, TILE_SIZE - 0.1);
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
}
