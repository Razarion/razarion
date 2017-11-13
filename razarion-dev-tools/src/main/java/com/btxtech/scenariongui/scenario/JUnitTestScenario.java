package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.scenariongui.InstanceStringGenerator;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Polygon2DRasterizer;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 19.03.2016.
 */
public class JUnitTestScenario extends Scenario {
    private List<DecimalPosition> corners = new ArrayList<>();
    private List<Rectangle2D> absolutePiercedTiles = new ArrayList<>();
    private List<Rectangle2D> absoluteInnerTiles = new ArrayList<>();

    @Override
    public void render(ExtendedGraphicsContext context) {
        context.strokePolygon(corners, 0.2, Color.RED, true);
        absolutePiercedTiles.forEach(rectangle2D -> context.strokeRectangle(rectangle2D, 0.2, Color.DARKGREEN));
        absoluteInnerTiles.forEach(rectangle2D -> context.strokeRectangle(rectangle2D, 0.2, Color.YELLOW));
    }

    @Override
    public boolean onMouseDown(DecimalPosition position) {
        corners.add(position);
        if (corners.size() >= 3) {
            absolutePiercedTiles.clear();
            Polygon2DRasterizer polygon2DRasterizer = Polygon2DRasterizer.create(new Polygon2D(corners), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);
            absolutePiercedTiles = polygon2DRasterizer.getPiercedTiles().stream().map(TerrainUtil::toAbsoluteNodeRectangle).collect(Collectors.toList());
            absoluteInnerTiles= polygon2DRasterizer.getInnerTiles().stream().map(TerrainUtil::toAbsoluteNodeRectangle).collect(Collectors.toList());

        }
        return true;
    }

    @Override
    public void onGenerate() {
        System.out.println("------------------------------------------------------------");
        System.out.println("    @Test");
        System.out.println("    public void test() throws Exception {");
        System.out.println("        Polygon2DRasterizer polygon2DRasterizer = Polygon2DRasterizer.create(new Polygon2D(" + InstanceStringGenerator.generateSimpleDecimalPositionList(corners) + "), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);");
//        System.out.println("        " + InstanceStringGenerator.generateIndexList(expected));
//        System.out.println("        GeometricUtilTest.assertIndices(positions, actual);");
        System.out.println("    }");
    }
}
