package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gui.AbstractTestGuiRenderer;
import com.btxtech.shared.gui.TestGuiDisplay;
import javafx.scene.paint.Color;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SlopeGroundConnectionFactoryTest {

    @Test
    public void test1() {
        Rectangle2D absoluteRect = new Rectangle2D(96.0, 96.0, 8.0, 8.0);
        List<List<DecimalPosition>> piercingLines = Collections.singletonList(
                Arrays.asList(new DecimalPosition(100.3, 95.0),
                        new DecimalPosition(100.3, 100.0),
                        new DecimalPosition(100.3, 105.0)));
        List<List<Vertex>> slopeGroundConnections = SlopeGroundConnectionFactory.setupSlopeGroundConnection(
                absoluteRect,
                piercingLines,
                0.0,
                false,
                null,
                0);

        // showDisplay(absoluteRect, piercingLines, slopeGroundConnections);

        TestHelper.assertPolygonsIgnoreOrder(Collections.singletonList(
                Arrays.asList(
                        new Vertex(100.3, 96.0, 0.0),
                        new Vertex(104.0, 96.0, 0.0),
                        new Vertex(104.0, 104.0, 0.0),
                        new Vertex(100.3, 104.0, 0.0),
                        new Vertex(100.3, 100.0, 0.0))),
                slopeGroundConnections);
    }

    @Test
    public void test2() {
        Rectangle2D absoluteRect = new Rectangle2D(48.0, 80.0, 8.0, 8.0);
        List<List<DecimalPosition>> piercingLines = Collections.singletonList(
                Arrays.asList(new DecimalPosition(49.7, 90.0),
                        new DecimalPosition(49.7, 85.0),
                        new DecimalPosition(49.7, 80.0),
                        new DecimalPosition(49.7, 75.0)));
        List<List<Vertex>> slopeGroundConnections = SlopeGroundConnectionFactory.setupSlopeGroundConnection(
                absoluteRect,
                piercingLines,
                0.0,
                false,
                null,
                0);

        // showDisplay(absoluteRect, piercingLines, slopeGroundConnections);

        TestHelper.assertPolygonsIgnoreOrder(Collections.singletonList(
                Arrays.asList(
                        new Vertex(49.7, 88.0, 0.0),
                        new Vertex(48.0, 88.0, 0.0),
                        new Vertex(48.0, 80.0, 0.0),
                        new Vertex(49.7, 80.0, 0.0),
                        new Vertex(49.7, 85.0, 0.0))),
                slopeGroundConnections);
    }

    @Test
    public void test3() {
        Rectangle2D absoluteRect = new Rectangle2D(56.0, 120.0, 8.0, 8.0);
        List<List<DecimalPosition>> piercingLines = Collections.singletonList(
                Arrays.asList(new DecimalPosition(61.142857142857146, 118.57142857142857),
                        new DecimalPosition(64.85714285714286, 121.42857142857143)));
        List<List<Vertex>> slopeGroundConnections = SlopeGroundConnectionFactory.setupSlopeGroundConnection(
                absoluteRect,
                piercingLines,
                20.0,
                false,
                null,
                0);

        // showDisplay(absoluteRect, piercingLines, slopeGroundConnections);

        TestHelper.assertPolygonsIgnoreOrder(Collections.singletonList(
                Arrays.asList(
                        new Vertex(63.00000000000001, 120.0, 20.0),
                        new Vertex(64.0, 120.0, 20.0),
                        new Vertex(64.0, 120.76923076923077, 20.0))),
                slopeGroundConnections);
    }

    @Test
    public void test4() {
        Rectangle2D absoluteRect = new Rectangle2D(56.0, 120.0, 8.0, 8.0);
        List<List<DecimalPosition>> piercingLines = Collections.singletonList(
                Arrays.asList(new DecimalPosition(64.67422962888796, 121.6663586253028),
                        new DecimalPosition(60.959943914602235, 118.80921576815994)));
        List<List<Vertex>> slopeGroundConnections = SlopeGroundConnectionFactory.setupSlopeGroundConnection(
                absoluteRect,
                piercingLines,
                0.0,
                false,
                null,
                0);

        // showDisplay(absoluteRect, piercingLines, slopeGroundConnections);

        TestHelper.assertPolygonsIgnoreOrder(Collections.singletonList(
                Arrays.asList(
                        new Vertex(64.0, 121.14772044923515, 0.0),
                        new Vertex(64.0, 128.0, 0.0),
                        new Vertex(56.0, 128.0, 0.0),
                        new Vertex(56.0, 120.0, 0.0),
                        new Vertex(62.50796341599431, 120.0, 0.0))),
                slopeGroundConnections);
    }

    @Test
    public void test5() {
        Rectangle2D absoluteRect = new Rectangle2D(72.0, 128.0, 8.0, 8.0);
        List<List<DecimalPosition>> piercingLines = Collections.singletonList(
                Arrays.asList(new DecimalPosition(80.0, 126.0),
                        new DecimalPosition(75.0, 130.0),
                        new DecimalPosition(72.0, 127.0)));
        List<List<Vertex>> slopeGroundConnections = SlopeGroundConnectionFactory.setupSlopeGroundConnection(
                absoluteRect,
                piercingLines,
                0.0,
                false,
                null,
                0);

        // showDisplay(absoluteRect, piercingLines, slopeGroundConnections);

        TestHelper.assertPolygonsIgnoreOrder(Collections.singletonList(
                Arrays.asList(
                        new Vertex(73.0, 128.0, 0.0),
                        new Vertex(75.0, 130.0, 0.0),
                        new Vertex(77.5, 128.0, 0.0),
                        new Vertex(80.0, 128.0, 0.0),
                        new Vertex(80.0, 136.0, 0.0),
                        new Vertex(72.0, 136.0, 0.0),
                        new Vertex(72.0, 128.0, 0.0))),
                slopeGroundConnections);
    }

    @Test
    public void test6() {
        Rectangle2D absoluteRect = new Rectangle2D(72.0, 128.0, 8.0, 8.0);
        List<List<DecimalPosition>> piercingLines = Collections.singletonList(
                Arrays.asList(new DecimalPosition(72.0, 127.0),
                        new DecimalPosition(75.0, 130.0),
                        new DecimalPosition(80.0, 126.0)));
        List<List<Vertex>> slopeGroundConnections = SlopeGroundConnectionFactory.setupSlopeGroundConnection(
                absoluteRect,
                piercingLines,
                0.0,
                false,
                null,
                0);

        // showDisplay(absoluteRect, piercingLines, slopeGroundConnections);

        TestHelper.assertPolygonsIgnoreOrder(Collections.singletonList(
                Arrays.asList(
                        new Vertex(77.5, 128.0, 0.0),
                        new Vertex(75.0, 130.0, 0.0),
                        new Vertex(73.0, 128.0, 0.0))),
                slopeGroundConnections);
    }

    @Test
    public void test7() {
        Rectangle2D absoluteRect = new Rectangle2D(72.0, 128.0, 8.0, 8.0);
        List<List<DecimalPosition>> piercingLines = Collections.singletonList(
                Arrays.asList(new DecimalPosition(72.28571428571429, 127.14285714285714),
                        new DecimalPosition(76.0, 130.0),
                        new DecimalPosition(80.0, 126.66666666666667))
        );
        List<List<Vertex>> slopeGroundConnections = SlopeGroundConnectionFactory.setupSlopeGroundConnection(
                absoluteRect,
                piercingLines,
                20.0,
                false,
                null,
                0);

        // showDisplay(absoluteRect, piercingLines, slopeGroundConnections);

        TestHelper.assertPolygonsIgnoreOrder(Collections.singletonList(
                Arrays.asList(
                        new Vertex(73.40000000000002, 128.0, 20.0),
                        new Vertex(78.40000000000002, 128.0, 20.0),
                        new Vertex(76.0, 130.0, 20.0))),
                slopeGroundConnections);
    }

    @Test
    public void test8() {
        Rectangle2D absoluteRect = new Rectangle2D(48.0, 32.0, 8.0, 8.0);
        List<List<DecimalPosition>> piercingLines = Collections.singletonList(
                Arrays.asList(new DecimalPosition(49.7, 45.0),
                        new DecimalPosition(49.7, 40.0),
                        new DecimalPosition(50.0, 39.7),
                        new DecimalPosition(55.0, 39.7),
                        new DecimalPosition(60.0, 39.7))
        );
        List<List<Vertex>> slopeGroundConnections = SlopeGroundConnectionFactory.setupSlopeGroundConnection(
                absoluteRect,
                piercingLines,
                0.0,
                false,
                null,
                0);

        // showDisplay(absoluteRect, piercingLines, slopeGroundConnections);

        TestHelper.assertPolygonsIgnoreOrder(Collections.singletonList(
                Arrays.asList(
                        new Vertex(49.7, 40.0, 0.0),
                        new Vertex(48.0, 40.0, 0.0),
                        new Vertex(48.0, 32.0, 0.0),
                        new Vertex(56.0, 32.0, 0.0),
                        new Vertex(56.0, 39.7, 0.0),
                        new Vertex(55.0, 39.7, 0.0),
                        new Vertex(50.0, 39.7, 0.0))),
                slopeGroundConnections);
    }

    @Test
    public void test9() {
        Rectangle2D absoluteRect = new Rectangle2D(8.0, 16.0, 8.0, 8.0);
        List<List<DecimalPosition>> piercingLines = Collections.singletonList(
                Arrays.asList(new DecimalPosition(6.0, 20.0),
                        new DecimalPosition(18.0, 20.0))
        );
        List<List<Vertex>> slopeGroundConnections = SlopeGroundConnectionFactory.setupSlopeGroundConnection(
                absoluteRect,
                piercingLines,
                0.0,
                false,
                null,
                0);

        // showDisplay(absoluteRect, piercingLines, slopeGroundConnections);

        TestHelper.assertPolygonsIgnoreOrder(Collections.singletonList(
                Arrays.asList(
                        new Vertex(8.0, 20.0, 0.0),
                        new Vertex(8.0, 16.0, 0.0),
                        new Vertex(16.0, 16.0, 0.0),
                        new Vertex(16.0, 20.0, 0.0))),
                slopeGroundConnections);
    }

    @Test
    public void test10() {
        Rectangle2D absoluteRect = new Rectangle2D(8.0, 16.0, 8.0, 8.0);
        List<List<DecimalPosition>> piercingLines = Collections.singletonList(
                Arrays.asList(new DecimalPosition(6.0, 20.0),
                        new DecimalPosition(12.0, 26.0))
        );
        List<List<Vertex>> slopeGroundConnections = SlopeGroundConnectionFactory.setupSlopeGroundConnection(
                absoluteRect,
                piercingLines,
                0.0,
                false,
                null,
                0);

        // showDisplay(absoluteRect, piercingLines, slopeGroundConnections);

        TestHelper.assertPolygonsIgnoreOrder(Collections.singletonList(
                Arrays.asList(
                        new Vertex(8.0, 22.0, 0.0),
                        new Vertex(8.0, 16.0, 0.0),
                        new Vertex(16.0, 16.0, 0.0),
                        new Vertex(16.0, 24.0, 0.0),
                        new Vertex(10.0, 24.0, 0.0))),
                slopeGroundConnections);
    }

    @Test
    public void test11() {
        Rectangle2D absoluteRect = new Rectangle2D(8.0, 16.0, 8.0, 8.0);
        List<List<DecimalPosition>> piercingLines = Arrays.asList(
                Arrays.asList(
                        new DecimalPosition(6.0, 20.0),
                        new DecimalPosition(12.0, 26.0)),
                Arrays.asList(
                        new DecimalPosition(13.0, 26.0),
                        new DecimalPosition(17.0, 20.0))
        );
        List<List<Vertex>> slopeGroundConnections = SlopeGroundConnectionFactory.setupSlopeGroundConnection(
                absoluteRect,
                piercingLines,
                0.0,
                false,
                null,
                0);

        // showDisplay(absoluteRect, piercingLines, slopeGroundConnections);

        TestHelper.assertPolygonsIgnoreOrder(Collections.singletonList(
                Arrays.asList(
                        new Vertex(10.0, 24.0, 0.0),
                        new Vertex(8.0, 22.0, 0.0),
                        new Vertex(8.0, 16.0, 0.0),
                        new Vertex(16.0, 16.0, 0.0),
                        new Vertex(16.0, 21.5, 0.0),
                        new Vertex(14.333333333333334, 24.0, 0.0))),
                slopeGroundConnections);
    }

    @Test
    public void test12() {
        Rectangle2D absoluteRect = new Rectangle2D(8.0, 16.0, 8.0, 8.0);
        List<List<DecimalPosition>> piercingLines = Arrays.asList(
                Arrays.asList(
                        new DecimalPosition(12.0, 26.0),
                        new DecimalPosition(6.0, 20.0)),
                Arrays.asList(
                        new DecimalPosition(17.0, 20.0),
                        new DecimalPosition(13.0, 26.0))
        );
        List<List<Vertex>> slopeGroundConnections = SlopeGroundConnectionFactory.setupSlopeGroundConnection(
                absoluteRect,
                piercingLines,
                0.0,
                false,
                null,
                0);

//        showDisplay(absoluteRect, piercingLines, slopeGroundConnections);

//        String assertCode;
//        List<Vertex> slopeGroundConnection = slopeGroundConnections.get(1);
//        if (slopeGroundConnection != null) {
//            String resultString = slopeGroundConnection
//                    .stream()
//                    .map(vertes -> "new Vertex(" + vertes.getX() + ", " + vertes.getY() + ", " + vertes.getZ() + ")")
//                    .collect(Collectors.joining(",\n"));
//            assertCode = "        TestHelper.assertVertices(\n" +
//                    "                Arrays.asList(\n" +
//                    resultString +
//                    "                ),\n" +
//                    "                slopeGroundConnections);\n";
//        } else {
//            assertCode = "        Assert.assertNull(slopeGroundConnections);\n";
//        }
//        System.out.println(assertCode);

        TestHelper.assertPolygonsIgnoreOrder(Arrays.asList(
                Arrays.asList(
                        new Vertex(10.0, 24.0, 0.0),
                        new Vertex(8.0, 24.0, 0.0),
                        new Vertex(8.0, 22.0, 0.0)),
                Arrays.asList(
                        new Vertex(16.0, 21.5, 0.0),
                        new Vertex(16.0, 24.0, 0.0),
                        new Vertex(14.333333333333334, 24.0, 0.0))
                ),
                slopeGroundConnections);
    }

    private void showDisplay(Rectangle2D absoluteRect, List<List<DecimalPosition>> piercingLines, List<List<Vertex>> slopeGroundConnections) {
        TestGuiDisplay.show(new AbstractTestGuiRenderer() {
            @Override
            protected void doRender() {
                strokePolygon(absoluteRect.toCorners(), 0.1, Color.BLUE, false);
                piercingLines.forEach(piercingLine -> strokeLine(piercingLine, 0.1, Color.GREEN, true));
                slopeGroundConnections.forEach(slopeGroundConnection -> strokeVertexPolygon(slopeGroundConnection, 0.2, Color.RED, true));
            }
        });
    }


}