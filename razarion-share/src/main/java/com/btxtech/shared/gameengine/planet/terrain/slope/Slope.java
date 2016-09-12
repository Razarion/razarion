package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.InterpolatedTerrainTriangle;
import com.btxtech.shared.datatypes.Line2I;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.terrain.ground.GroundMesh;
import com.btxtech.shared.gameengine.planet.terrain.ground.GroundSlopeConnector;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class Slope {
    // private Logger logger = Logger.getLogger(Slope.class.getName());
    private SlopeSkeletonConfig slopeSkeletonConfig;
    private List<Index> corners;
    private List<AbstractBorder> borders = new ArrayList<>();
    private Mesh mesh;
    private int xVertices;
    private List<Index> innerLineMeshIndex;
    private List<Vertex> innerLine;
    private Polygon2D innerPolygon;
    private List<Index> outerLineMeshIndex;
    private List<Vertex> outerLine;
    private Polygon2D outerPolygon;
    private GroundSlopeConnector groundPlateauConnector;

    public Slope(SlopeSkeletonConfig slopeSkeletonConfig, List<Index> corners) {
        this.slopeSkeletonConfig = slopeSkeletonConfig;
        this.corners = new ArrayList<>(corners);

        if (slopeSkeletonConfig.getWidth() > 0) {
            setupSlopingBorder(this.corners);
        } else {
            setupStraightBorder(this.corners);
        }

        // Setup vertical segments
        xVertices = 0;
        for (AbstractBorder border : borders) {
            xVertices += border.setupVerticalSegments(slopeSkeletonConfig.getVerticalSpace());
        }
    }

    public List<AbstractBorder> getBorders() {
        return borders;
    }

    private void setupStraightBorder(List<Index> corners) {
        for (int i = 0; i < corners.size(); i++) {
            Index current = corners.get(i);
            Index next = corners.get(CollectionUtils.getCorrectedIndex(i + 1, corners.size()));
            borders.add(new LineBorder(current, next));
        }
    }

    private void setupSlopingBorder(List<Index> corners) {
        // Correct the borders. Outer corners can not be too close to other corners. Id needs some safety distance
        boolean violationsFound = true;
        while (violationsFound) {
            violationsFound = false;
            for (int i = 0; i < corners.size(); i++) {
                Index previous = corners.get(CollectionUtils.getCorrectedIndex(i - 1, corners.size()));
                Index current = corners.get(i);
                Index next = corners.get(CollectionUtils.getCorrectedIndex(i + 1, corners.size()));
                double innerAngle = current.getAngle(next, previous);
                if (innerAngle > MathHelper.HALF_RADIANT) {
                    double safetyDistance = (double) slopeSkeletonConfig.getWidth() / Math.tan((MathHelper.ONE_RADIANT - innerAngle) / 2.0);
                    if (current.getDistance(previous) < safetyDistance) {
                        violationsFound = true;
                        corners.remove(i);
                        break;
                    }
                    if (current.getDistance(next) < safetyDistance) {
                        violationsFound = true;
                        corners.remove(i);
                        break;
                    }
                }
            }
        }
        // Setup inner and outer corner
        List<AbstractCornerBorder> cornerBorders = new ArrayList<>();
        for (int i = 0; i < corners.size(); i++) {
            Index previous = corners.get(CollectionUtils.getCorrectedIndex(i - 1, corners.size()));
            Index current = corners.get(i);
            Index next = corners.get(CollectionUtils.getCorrectedIndex(i + 1, corners.size()));
            if (current.getAngle(next, previous) > MathHelper.HALF_RADIANT) {
                cornerBorders.add(new OuterCornerBorder(current, previous, next, slopeSkeletonConfig.getWidth()));
            } else {
                cornerBorders.add(new InnerCornerBorder(current, previous, next, slopeSkeletonConfig.getWidth()));
            }
        }
        // Setup whole contour
        for (int i = 0; i < cornerBorders.size(); i++) {
            AbstractCornerBorder current = cornerBorders.get(i);
            AbstractCornerBorder next = cornerBorders.get(CollectionUtils.getCorrectedIndex(i + 1, cornerBorders.size()));
            borders.add(current);
            borders.add(new LineBorder(current, next, slopeSkeletonConfig.getWidth()));
        }
    }

    public void wrap(GroundMesh groundMesh) {
        mesh = new Mesh(xVertices, slopeSkeletonConfig.getRows());
        innerLineMeshIndex = new ArrayList<>();
        outerLineMeshIndex = new ArrayList<>();
        SlopeModeler.generateMesh(mesh, slopeSkeletonConfig, borders, innerLineMeshIndex, outerLineMeshIndex, groundMesh);
        mesh.setupValues(groundMesh);
        // Setup helper lists
        innerLine = new ArrayList<>();
        innerPolygon = correctAndCreateEdge(innerLineMeshIndex, innerLine);
        outerLine = new ArrayList<>();
        outerPolygon = correctAndCreateEdge(outerLineMeshIndex, outerLine);
    }

    private Polygon2D correctAndCreateEdge(List<Index> indices, List<Vertex> vertices) {
        Vertex last = mesh.getVertexSave(indices.get(indices.size() - 1));
        for (Iterator<Index> iterator = indices.iterator(); iterator.hasNext(); ) {
            Index index = iterator.next();
            Vertex current = mesh.getVertexSave(index);
            if (current.toXY().getDistance(last.toXY()) > 0.1) {
                vertices.add(current);
            } else {
                iterator.remove();
            }
            last = current;
        }
        return new Polygon2D(Vertex.toXY(vertices));
    }

    public Mesh getMesh() {
        return mesh;
    }

    public boolean isInsideInner(Vertex vertex) {
        return innerPolygon.isInside(vertex.toXY());
    }

    public boolean isInsideOuter(Vertex vertex) {
        return outerPolygon.isInside(vertex.toXY());
    }

    public Polygon2D getInnerPolygon() {
        return innerPolygon;
    }

    public List<Index> getInnerLineMeshIndex() {
        return innerLineMeshIndex;
    }

    public List<Index> getOuterLineMeshIndex() {
        return outerLineMeshIndex;
    }

    public double getHeight() {
        return slopeSkeletonConfig.getHeight();
    }

    public List<Vertex> getInnerLine() {
        return innerLine;
    }

    public List<Vertex> getOuterLine() {
        return outerLine;
    }

//    public ImageDescriptor getSlopeImageDescriptor() {
//        return slopeImageDescriptor;
//    }
//
//    public void setSlopeImageDescriptor(ImageDescriptor slopeImageDescriptor) {
//        this.slopeImageDescriptor = slopeImageDescriptor;
//    }
//
//    public ImageDescriptor getSlopeBumpImageDescriptor() {
//        return slopeBumpImageDescriptor;
//    }
//
//    public void setSlopeBumpImageDescriptor(ImageDescriptor slopeBumpImageDescriptor) {
//        this.slopeBumpImageDescriptor = slopeBumpImageDescriptor;
//    }
//
//    public ImageDescriptor getSlopeGroundSplattingImageDescriptor() {
//        return slopeGroundSplattingImageDescriptor;
//    }
//
//    public void setSlopeGroundSplattingImageDescriptor(ImageDescriptor slopeGroundSplattingImageDescriptor) {
//        this.slopeGroundSplattingImageDescriptor = slopeGroundSplattingImageDescriptor;
//    }

    public boolean hasWater() {
        return false;
    }

    public double getWaterLevel() {
        return 0;
    }

    public SlopeSkeletonConfig getSlopeSkeletonConfig() {
        return slopeSkeletonConfig;
    }

    public MeshEntry pick(Vertex pointOnGround) {
        return mesh.pick(pointOnGround);
    }

    public void setupGroundConnection(GroundMesh groundMesh) {
        groundPlateauConnector = new GroundSlopeConnector(groundMesh, this);
        groundPlateauConnector.stampOut(!hasWater());
    }

    public GroundSlopeConnector getGroundPlateauConnector() {
        return groundPlateauConnector;
    }

    public List<Index> getCorner2d() {
        return corners;
    }

    public List<Vertex> getCorner3d() {
        List<Vertex> corners = new ArrayList<>();
        for (Index corner : this.corners) {
            corners.add(new Vertex(corner.getX(), corner.getY(), 0));
        }
        return corners;
    }

    public void updateSlopeSkeleton(SlopeSkeletonConfig slopeSkeletonConfig) {
        this.slopeSkeletonConfig = slopeSkeletonConfig;
    }

    public Collection<Obstacle> generateObstacles() {
        Collection<Obstacle> obstacles = new ArrayList<>();

        fillObstacle(innerLine, obstacles);
        fillObstacle(outerLine, obstacles);

//        Index last = innerLine.get(0).toXY().toIndexRound();
//        for (int i = 0; i < innerLine.size(); i++) {
//            Index next = innerLine.get(CollectionUtils.getCorrectedIndex(i + 1, innerLine.size())).toXY().toIndexRound();;
//            if(last.equals(next)) {
//                continue;
//            }
//            obstacles.add(new Obstacle(new Line2I(last, next)));
//            last = next;
//        }
//        for (int i = 0; i < outerLine.size(); i++) {
//            Vertex current = outerLine.get(i);
//            Vertex next = outerLine.get(CollectionUtils.getCorrectedIndex(i + 1, outerLine.size()));
//            obstacles.add(new Obstacle(new Line2I(current.toXY().toIndexRound(), next.toXY().toIndexRound())));
//        }

        return obstacles;
    }

    private void fillObstacle(List<Vertex> polygon, Collection<Obstacle> obstacles) {
        Index last = polygon.get(0).toXY().toIndexRound();
        for (int i = 0; i < polygon.size(); i++) {
            Index next = polygon.get(CollectionUtils.getCorrectedIndex(i + 1, polygon.size())).toXY().toIndexRound();
            if (last.equals(next)) {
                continue;
            }
            obstacles.add(new Obstacle(new Line2I(last, next)));
            last = next;
        }
    }

    public InterpolatedTerrainTriangle getInterpolatedVertexData(DecimalPosition absoluteXY) {
        InterpolatedTerrainTriangle interpolatedTerrainTriangle = groundPlateauConnector.getInterpolatedVertexData(absoluteXY);
        if (interpolatedTerrainTriangle != null) {
            return interpolatedTerrainTriangle;
        }
        return mesh.getInterpolatedVertexData(absoluteXY);
    }

    public boolean isInSlope(DecimalPosition position, double radius) {
        // TODO This is not enough. Radius is ignored. Also check the innerPolygon and the water
        return outerPolygon.isInside(position);
    }
}
