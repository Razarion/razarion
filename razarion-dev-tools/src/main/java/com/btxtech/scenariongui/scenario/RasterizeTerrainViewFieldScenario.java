package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.scenariongui.InstanceStringGenerator;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.utils.GeometricUtil;
import com.btxtech.uiservice.renderer.ViewField;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Beat
 * 26.11.2016.
 */
public class RasterizeTerrainViewFieldScenario extends Scenario {
    private double halfBottom = 50;
    private double halfTop = 100;
    private double halfHeight = 100;
    private boolean blockMouseMove;
    private Rectangle2D absAabbRect;
    private Polygon2D viewPolygon;
    private Collection<Index> display;

    private DecimalPosition position = new DecimalPosition(0, 0);

    @Override
    public void render(ExtendedGraphicsContext extendedGraphicsContext) {
        ViewField viewField = new ViewField(0);
        viewField.setBottomLeft(position.add(-halfBottom, -halfHeight));
        viewField.setBottomRight(position.add(halfBottom, -halfHeight));
        viewField.setTopLeft(position.add(-halfTop, halfHeight));
        viewField.setTopRight(position.add(halfTop, halfHeight));


        absAabbRect = viewField.calculateAabbRectangle();
        viewPolygon = viewField.toPolygon();

        display = GeometricUtil.rasterizeTerrainViewField(absAabbRect, viewPolygon);
        for (Index index : display) {
            Rectangle2D rectangle = TerrainUtil.toAbsoluteRectangle(index);
            Rectangle2D displayRect = new Rectangle2D(rectangle.startX(), rectangle.startY(), rectangle.width() - 2, rectangle.height() - 2);
            extendedGraphicsContext.fillRectangle(displayRect, 0.1, Color.GREEN);
        }

        extendedGraphicsContext.strokePolygon(viewPolygon, 1, Color.RED, false);

    }

    @Override
    public boolean onMouseMove(DecimalPosition position) {
        if (!blockMouseMove) {
            this.position = position;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onMouseDown(DecimalPosition position) {
        blockMouseMove = !blockMouseMove;
        return false;
    }

    @Override
    public void onGenerate() {
        System.out.println("------------------------------------------------------------");
        System.out.println("    @Test");
        System.out.println("    public void rasterizeTerrainPolygon() throws Exception {");
        System.out.println("        Rectangle2D absAabbRect = " + absAabbRect.testString() + ";");
        System.out.println("        Polygon2D viewPolygon = " + viewPolygon.testString() + ";");
        System.out.println("        Collection<Index> actual = GeometricUtil.rasterizeTerrainViewField(absAabbRect, viewPolygon);");
        System.out.println("        " + InstanceStringGenerator.generateIndexList(new ArrayList<>(display)));
        System.out.println("        GeometricUtilTest.assertIndices(positions, actual);");
        System.out.println("    }");
    }
}
