package com.btxtech;

import com.btxtech.client.terrain.GroundMesh;
import com.btxtech.client.terrain.GroundSlopeConnector;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.client.terrain.Water;
import com.btxtech.client.terrain.slope.AbstractBorder;
import com.btxtech.client.terrain.slope.Slope;
import com.btxtech.client.terrain.slope.SlopeWater;
import com.btxtech.client.terrain.slope.VerticalSegment;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.SlopeSkeleton;
import com.btxtech.shared.primitives.Polygon2I;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 19.03.2016.
 */
public class DrawScenes {
    public static final int MESH_NODES = 64;
    public static final int MESH_NODE_EDGE_LENGTH = 64;

    public static void drawFromFile(ExtendedGraphicsContext context) {
        TerrainSurface terrainSurface = GameMock.startTerrainSurface("/SlopeSkeletonSlope.json", "/SlopeSkeletonBeach.json", "/GroundSkeleton.json", "/TerrainSlopePositions.json");

        Slope slope = terrainSurface.getSlope(0);

        context.strokeCurve(slope.getGroundPlateauConnector().getOuterGroundEdges(), 1, Color.GREEN, true);
        context.strokeCurve(slope.getGroundPlateauConnector().getOuterSlopeEdges(), 1, Color.RED, true);

        context.strokeCurve(slope.getGroundPlateauConnector().getInnerGroundEdges(), 1, Color.GREEN, true);
        context.strokeCurve(slope.getGroundPlateauConnector().getInnerSlopeEdges(), 1, Color.RED, true);

        // context.strokeCurveIndex(terrainSlopePosition.getPolygon(), 1.0, Color.BLACK, true);

        context.fillVertexList(slope.getGroundPlateauConnector().getInnerConnectionVertexList().getVertices(), 2, Color.color(1.0F, 0.078431375F, 0.5764706F, 0.3));
        context.fillVertexList(slope.getGroundPlateauConnector().getOuterConnectionVertexList().getVertices(), 2, Color.RED);

        List<Index> innerBorder = new ArrayList<>();
        List<Index> outerBorder = new ArrayList<>();
        for (AbstractBorder abstractBorder : slope.getBorders()) {
            for (VerticalSegment verticalSegment : abstractBorder.getVerticalSegments()) {
                innerBorder.add(verticalSegment.getInner());
                outerBorder.add(verticalSegment.getOuter());
            }
        }
        context.strokeCurveIndex(innerBorder, 1, Color.BROWN, true);
        context.strokeCurveIndex(outerBorder, 1, Color.DARKCYAN, true);


        context.strokeVertexList(terrainSurface.getGroundVertexList().getVertices(), 1, Color.BLUE);

//        GroundMesh groundMesh = GroundModeler.generateGroundMesh(groundSkeleton, MESH_NODES, MESH_NODES);
//        groundMesh.setupNorms();
//
//        // Slope plateau = new Slope(slopeSkeletonBeach, Arrays.asList(new Index(580, 500), new Index(1000, 500), new Index(1000, 1120)));
//        Slope plateau = new Slope(slopeSkeletonBeach, Arrays.asList(new Index(908, 980), new Index(880, 1036), new Index(836, 1080), new Index(807, 1095), new Index(795, 1118), new Index(751, 1162), new Index(695, 1190), new Index(633, 1200), new Index(571, 1190), new Index(515, 1162), new Index(471, 1118), new Index(443, 1062), new Index(440, 1042), new Index(402, 1004), new Index(374, 948), new Index(368, 910), new Index(363, 905), new Index(335, 849), new Index(325, 787), new Index(335, 725), new Index(363, 669), new Index(388, 644), new Index(411, 598), new Index(455, 554), new Index(460, 551), new Index(479, 514), new Index(523, 470), new Index(579, 442), new Index(641, 432), new Index(703, 442), new Index(759, 470), new Index(803, 514), new Index(829, 567), new Index(838, 571), new Index(882, 615), new Index(907, 665), new Index(935, 693), new Index(963, 749), new Index(973, 811), new Index(963, 873), new Index(935, 929), new Index(913, 951)));
//
//        try {
//            plateau.wrap(groundMesh);
//            plateau.setupGroundConnection(groundMesh);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        // Ground
//        context.strokeVertexList(groundMesh.provideVertexList().getVertices(), 1, Color.BLUE);
//        context.strokeVertexList(plateau.getGroundPlateauConnector().getTopMesh().provideVertexList().getVertices(), 1, Color.BROWN);
////        // Slope line
////
////        List<Index> inners = new ArrayList<>();
////        List<Index> outers = new ArrayList<>();
////        for (AbstractBorder abstractBorder : plateau.getBorders()) {
////            for (VerticalSegment verticalSegment : abstractBorder.getVerticalSegments()) {
////                inners.add(verticalSegment.getInner());
////                outers.add(verticalSegment.getOuter());
////            }
////        }
////
////        context.strokeCurveIndex(inners, 3, Color.RED, true);
////        context.strokeCurveIndex(outers, 3, Color.BLUE, true);
//
//        context.fillVertexList(plateau.getMesh().getVertices(), 1, Color.BLACK);
//        context.strokeCurve(plateau.getInnerLine(), 1, Color.YELLOW, true);
//        context.strokeCurve(plateau.getCorner3d(), 1, Color.GREEN, false);
//        context.strokeCurve(plateau.getOuterLine(), 3, Color.BLUE, false);
////
//        context.strokeCurve(plateau.getGroundPlateauConnector().getInnerGroundEdges(), 3, Color.GREEN, true);
////
//        context.fillVertexList(plateau.getGroundPlateauConnector().getInnerConnectionVertexList().getVertices(), 2, Color.color(1.0F, 0.078431375F, 0.5764706F, 0.3));
//        context.fillVertexList(plateau.getGroundPlateauConnector().getOuterConnectionVertexList().getVertices(), 2, Color.RED);
//
    }

    public static void drawWater(ExtendedGraphicsContext context) {
        // setup
        final GroundMesh groundMesh = new GroundMesh();
        groundMesh.reset(MESH_NODE_EDGE_LENGTH, MESH_NODES, MESH_NODES, 0);

        SlopeSkeleton slopeSkeleton = new SlopeSkeleton();
        slopeSkeleton.setBumpMapDepth(0.5);
        slopeSkeleton.setSpecularHardness(0.2);
        slopeSkeleton.setSpecularIntensity(1.0);
        slopeSkeleton.setVerticalSpace(100);
        slopeSkeleton.setSegments(100);
        List<SlopeShape> shape = new ArrayList<>();
        shape.add(new SlopeShape(new Index(400, -8), 1));
        shape.add(new SlopeShape(new Index(350, -7), 1));
        shape.add(new SlopeShape(new Index(300, -6), 1));
        shape.add(new SlopeShape(new Index(250, -5), 1));
        shape.add(new SlopeShape(new Index(200, -4), 1));
        shape.add(new SlopeShape(new Index(150, -3), 1));
        shape.add(new SlopeShape(new Index(100, -2), 1));
        shape.add(new SlopeShape(new Index(50, -1), 0.5f));
        shape.add(new SlopeShape(new Index(0, 0), 0));
        // TODO slopeSkeleton.setShape(shape);
        // TODO SlopeModeler.sculpt(slopeSkeleton);

        Water water = new Water(-4, -8);
        SlopeWater beach = new SlopeWater(water, slopeSkeleton, Arrays.asList(new Index(2000, 1000), new Index(3000, 1000), new Index(3000, 1500), new Index(2000, 1500)));
        beach.wrap(groundMesh);
        GroundSlopeConnector groundBeachConnector = new GroundSlopeConnector(groundMesh, beach);
        groundBeachConnector.stampOut(false);

        // Draw
        context.strokeVertexList(water.getVertices(), 1, Color.BLUE);
        // context.fillVertexList(groundBeachConnector.getOuterConnectionVertexList().getVertices(), 2, Color.RED);
        // context.fillVertexList(beach.getMesh().getVertices(), 2, Color.RED);
    }

    public static void drawBeach(ExtendedGraphicsContext context) {
        // setup
        final GroundMesh groundMesh = new GroundMesh();
        groundMesh.reset(MESH_NODE_EDGE_LENGTH, MESH_NODES, MESH_NODES, 0);

//        SlopeSkeleton beachSlopeConfigEntity = new SlopeSkeleton();
//        beachSlopeConfigEntity.setBumpMapDepth(0.5);
//        beachSlopeConfigEntity.setFractalRoughness(0);
//        beachSlopeConfigEntity.setFractalShift(0);
//        beachSlopeConfigEntity.setSpecularHardness(0.2);
//        beachSlopeConfigEntity.setSpecularIntensity(1.0);
//        beachSlopeConfigEntity.setVerticalSpace(100);
//        beachSlopeConfigEntity.setSegments(100);
//        List<SlopeShape> shape = new ArrayList<>();
//        shape.add(new SlopeShape(new Index(400, -8), 1));
//        shape.add(new SlopeShape(new Index(350, -7), 1));
//        shape.add(new SlopeShape(new Index(300, -6), 1));
//        shape.add(new SlopeShape(new Index(250, -5), 1));
//        shape.add(new SlopeShape(new Index(200, -4), 1));
//        shape.add(new SlopeShape(new Index(150, -3), 1));
//        shape.add(new SlopeShape(new Index(100, -2), 1));
//        shape.add(new SlopeShape(new Index(50, -1), 0.5f));
//        shape.add(new SlopeShape(new Index(0, 0), 0));
//        beachSlopeConfigEntity.setShape(shape);
//        SlopeModeler.sculpt(beachSlopeConfigEntity);
//
//        Slope beach = new Slope(beachSlopeConfigEntity.getSlopeSkeletonEntity(), Arrays.asList(new Index(2000, 1000), new Index(3000, 1000), new Index(3000, 1500), new Index(2000, 1500)));
//        beach.wrap(groundMesh);
//        GroundSlopeConnector groundBeachConnector = new GroundSlopeConnector(groundMesh, beach);
//        groundBeachConnector.stampOut(false);
//
//        // Draw
//        // context.strokeVertexList(groundMesh.provideVertexList().getVertices(), 1, Color.BLUE);
//        // context.fillVertexList(groundBeachConnector.getOuterConnectionVertexList().getVertices(), 2, Color.RED);
//        context.fillVertexList(beach.getMesh().getVertices(), 2, Color.RED);
    }

    public static void drawPlateau(ExtendedGraphicsContext context) {
        final GroundMesh groundMesh = new GroundMesh();
        groundMesh.reset(MESH_NODE_EDGE_LENGTH, MESH_NODES, MESH_NODES, 0);
        groundMesh.setupNorms();

//        SlopeSkeleton plateauSlopeConfigEntity = new SlopeSkeleton();
//        plateauSlopeConfigEntity.setBumpMapDepth(7.5);
//        plateauSlopeConfigEntity.setFractalRoughness(1);
//        plateauSlopeConfigEntity.setFractalShift(12);
//        plateauSlopeConfigEntity.setSpecularHardness(1);
//        plateauSlopeConfigEntity.setSpecularIntensity(0.0);
//        plateauSlopeConfigEntity.setVerticalSpace(100);
//        plateauSlopeConfigEntity.setSegments(100);
//        List<SlopeShape> shape = new ArrayList<>();
//        shape.add(new SlopeShape(new Index(295, 205), 0f));
//        shape.add(new SlopeShape(new Index(271, 206), 0.1f));
//        shape.add(new SlopeShape(new Index(242, 207), 0.6f));
//        shape.add(new SlopeShape(new Index(217, 208), 1f));
//        shape.add(new SlopeShape(new Index(200, 210), 1f));
//        shape.add(new SlopeShape(new Index(184, 204), 1f));
//        shape.add(new SlopeShape(new Index(161, 188), 1f));
//        shape.add(new SlopeShape(new Index(151, 164), 1f));
//        shape.add(new SlopeShape(new Index(118, 147), 1f));
//        shape.add(new SlopeShape(new Index(111, 125), 1f));
//        shape.add(new SlopeShape(new Index(100, 110), 1f));
//        shape.add(new SlopeShape(new Index(104, 88), 1f));
//        shape.add(new SlopeShape(new Index(98, 68), 1f));
//        shape.add(new SlopeShape(new Index(111, 45), 1f));
//        shape.add(new SlopeShape(new Index(105, 28), 1f));
//        shape.add(new SlopeShape(new Index(98, 12), 1f));
//        shape.add(new SlopeShape(new Index(88, 10), 1f));
//        shape.add(new SlopeShape(new Index(65, 7), 1f));
//        shape.add(new SlopeShape(new Index(46, 7), 0.6f));
//        shape.add(new SlopeShape(new Index(21, 0), 0.1f));
//        shape.add(new SlopeShape(new Index(0, 0), 0f));
//        shape.add(new SlopeShape(new Index(100, 100), 0f));
//        shape.add(new SlopeShape(new Index(50, 50), 0f));
//        shape.add(new SlopeShape(new Index(0, 0), 0f));
//
//        plateauSlopeConfigEntity.setShape(shape);
//
//        SlopeModeler.sculpt(plateauSlopeConfigEntity);
//        Slope plateau = new Slope(plateauSlopeConfigEntity.getSlopeSkeletonEntity(), Arrays.asList(new Index(580, 500), new Index(1030, 500), new Index(1000, 1120)));
//        plateau.wrap(groundMesh);
//
//        GroundSlopeConnector groundSlopeConnector = new GroundSlopeConnector(groundMesh, plateau);
//        try {
//            groundSlopeConnector.stampOut(true);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        context.strokeVertexList(groundMesh.provideVertexList().getVertices(), 1, Color.BLUE);
//        context.strokeVertexList(groundSlopeConnector.getTopMesh().provideVertexList().getVertices(), 1, Color.BLUE);
//        context.fillVertexList(plateau.getMesh().getVertices(), 1, Color.BLACK);
//        context.strokeCurve(plateau.getInnerLine(), 3, Color.YELLOW, true);
//        context.strokeCurve(plateau.getOuterLine(), 3, Color.YELLOW, true);
//
//        context.strokeCurve(groundSlopeConnector.getInnerGroundEdges(), 3, Color.GREEN, true);
//        context.strokeCurve(groundSlopeConnector.getOuterGroundEdges(), 3, Color.GREEN);
        //  context.strokeCurve(groundSlopeConnector.getTotalOuterLine(), 3, Color.GREEN);
//
//        context.strokeVertices(groundSlopeConnector.getStampedOut(), Color.color(0.0, 0.0, 0.0, 0.3));

//        context.drawPositions(groundSlopeConnector.getValidTopIndices(), 20, Color.color(1.0, 0.0, 0.0, 0.3));
//
//        context.strokeCurve(groundSlopeConnector.getTotalLine(), 3, Color.BLACK);
//
//        context.fillVertexList(groundSlopeConnector.getInnerConnectionVertexList().getVertices(), 3, Color.RED);
//

//        context.fillVertexList(groundSlopeConnector.getInnerConnectionVertexList().getVertices(), 2, Color.color(1.0F, 0.078431375F, 0.5764706F, 0.3));
//
//        context.fillVertexList(groundSlopeConnector.getOuterConnectionVertexList().getVertices(), 2, Color.RED);
    }

    public static void drawPolygon2DCombine(ExtendedGraphicsContext context, Index mouseDown) {
        Polygon2I poly1 = new Polygon2I(Arrays.asList(new Index(50, 0), new Index(25, 44), new Index(-25, 44), new Index(-50, 0), new Index(-25, -43), new Index(25, -43)));
        poly1 = poly1.translate(mouseDown);
        Polygon2I poly2 = new Polygon2I(Arrays.asList(new Index(580, 500), new Index(1000, 500), new Index(1000, 1120)));

        context.strokeCurveIndex(poly1.getCorners(), 1.0, Color.RED, true);
        context.strokeCurveIndex(poly2.getCorners(), 1.0, Color.BLUE, true);

        try {
            Polygon2I polyResult = poly1.combine(poly2);
            context.strokeCurveIndex(polyResult.getCorners(), 3.0, Color.PINK, true);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void drawPolygon(ExtendedGraphicsContext context) {
        Polygon2I polygon = new Polygon2I(Arrays.asList(new Index(908, 980), new Index(880, 1036), new Index(836, 1080), new Index(807, 1095), new Index(795, 1118), new Index(751, 1162), new Index(695, 1190), new Index(633, 1200), new Index(571, 1190), new Index(515, 1162), new Index(471, 1118), new Index(443, 1062), new Index(440, 1042), new Index(402, 1004), new Index(374, 948), new Index(368, 910), new Index(363, 905), new Index(335, 849), new Index(325, 787), new Index(335, 725), new Index(363, 669), new Index(388, 644), new Index(411, 598), new Index(455, 554), new Index(460, 551), new Index(479, 514), new Index(523, 470), new Index(579, 442), new Index(641, 432), new Index(703, 442), new Index(759, 470), new Index(803, 514), new Index(829, 567), new Index(838, 571), new Index(882, 615), new Index(907, 665), new Index(935, 693), new Index(963, 749), new Index(973, 811), new Index(963, 873), new Index(935, 929), new Index(913, 951)));
        context.strokeCurveIndex(polygon.getCorners(), 1.0, Color.RED, true);
    }

}
