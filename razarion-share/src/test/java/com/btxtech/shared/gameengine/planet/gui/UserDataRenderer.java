package com.btxtech.shared.gameengine.planet.gui;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.planet.gui.userobject.PositionMarker;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.DifferenceCollector;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * on 19.10.2017.
 */
public class UserDataRenderer {
    private final WeldTestRenderer weldTestRenderer;
    private final Object[] userObjects;
    public boolean simplePathColorToggle;

    public UserDataRenderer(WeldTestRenderer weldTestRenderer, Object[] userObjects) {
        this.weldTestRenderer = weldTestRenderer;
        this.userObjects = userObjects;
    }

    public void render() {
        Arrays.stream(userObjects).forEach(this::renderUserObject);
    }

    private void renderUserObject(Object userObject) {
        if (userObject instanceof SimplePath) {
            render((SimplePath) userObject);
        } else if (userObject instanceof PositionMarker) {
            render((PositionMarker) userObject);
        } else if (userObject instanceof DifferenceCollector) {
            render((DifferenceCollector) userObject);
        } else {
            throw new IllegalArgumentException("Unknown userObject: " + userObject);
        }
    }

    private void render(PositionMarker positionMarker) {
        positionMarker.getCircles().forEach(circle -> weldTestRenderer.strokeCircle(circle, WeldTestRenderer.FAT_LINE_WIDTH, Color.RED));
        weldTestRenderer.drawPositions(positionMarker.getPositions(), WeldTestRenderer.FAT_LINE_WIDTH, Color.PINK);
        positionMarker.getLines().forEach(lineColor -> {
            weldTestRenderer.getGc().setStroke(lineColor.getColor());
            weldTestRenderer.getGc().setLineWidth(0.1);
            weldTestRenderer.getGc().strokeLine(lineColor.getLine().getPoint1().getX(), lineColor.getLine().getPoint1().getY(), lineColor.getLine().getPoint2().getX(), lineColor.getLine().getPoint2().getY());
        });
        positionMarker.getSyncItems().forEach(syncItem -> {
            weldTestRenderer.fillCircle(syncItem.getCircle2D(), syncItem.getColor());
        });
        positionMarker.getPolygon2Ds().forEach(polygon2D -> weldTestRenderer.strokePolygon(polygon2D.getCorners(), WeldTestRenderer.FAT_LINE_WIDTH, Color.GREEN, true));
        positionMarker.getRectangle2Ds().forEach(rectangle -> weldTestRenderer.fillRectangle(rectangle.getRectangle2D(), rectangle.getColor()));
        positionMarker.getCircleColors().forEach(circle -> weldTestRenderer.fillCircle(circle.getCircle2D(), circle.getColor()));
        positionMarker.getPathColors().forEach(pathColor -> weldTestRenderer.strokeCurveDecimalPosition(pathColor.getPath(), 0.1, pathColor.getColor(), true));
    }

    private void render(SimplePath simplePath) {
        weldTestRenderer.strokeLine(simplePath.getWayPositions(), WeldTestRenderer.LINE_WIDTH, simplePathColorToggle ? Color.DARKGREEN : Color.DEEPPINK, false);
        simplePathColorToggle = !simplePathColorToggle;
    }

    private void render(List<DecimalPosition> userObject) {
        weldTestRenderer.strokeLine(userObject, WeldTestRenderer.FAT_LINE_WIDTH, Color.DEEPPINK, true);
    }

    private void render(DifferenceCollector differenceCollector) {
        differenceCollector.getDifferenceTriangleElements().forEach(diffTriangleElement -> {
            weldTestRenderer.showDifference(diffTriangleElement);
        });
    }

}
