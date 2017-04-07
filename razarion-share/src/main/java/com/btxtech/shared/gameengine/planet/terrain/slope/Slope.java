package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.InterpolatedTerrainTriangle;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.Line3d;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleContainer;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleSlope;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.ground.GroundMesh;
import com.btxtech.shared.gameengine.planet.terrain.ground.GroundSlopeConnector;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.GeometricUtil;
import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class Slope {
    // private Logger logger = Logger.getLogger(Slope.class.getName());
    private int slopeId;
    private SlopeSkeletonConfig slopeSkeletonConfig;
    private List<DecimalPosition> corners;
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

    public Slope(int slopeId, SlopeSkeletonConfig slopeSkeletonConfig, List<DecimalPosition> corners) {
        this.slopeId = slopeId;
        this.slopeSkeletonConfig = slopeSkeletonConfig;
        this.corners = new ArrayList<>(corners);

        if (slopeSkeletonConfig.getWidth() > 0.0) {
            setupSlopingBorder(this.corners);
        } else {
            setupStraightBorder(this.corners);
        }

        // Setup vertical segments
        xVertices = 0;
        for (AbstractBorder border : borders) {
            xVertices += border.setupVerticalSegments(this, slopeSkeletonConfig.getVerticalSpace());
        }
        // Set VerticalSegment predecessor and successor
        VerticalSegment last = null;
        VerticalSegment first = null;
        for (AbstractBorder border : borders) {
            for (VerticalSegment verticalSegment : border.getVerticalSegments()) {
                if (first == null) {
                    first = verticalSegment;
                }
                if (last != null) {
                    last.setSuccessor(verticalSegment);
                    verticalSegment.setPredecessor(last);
                }
                last = verticalSegment;
            }
        }
        if (last != null) {
            last.setSuccessor(first);
            first.setPredecessor(last);
        }
    }

    public int getSlopeId() {
        return slopeId;
    }

    public List<AbstractBorder> getBorders() {
        return borders;
    }

    private void setupStraightBorder(List<DecimalPosition> corners) {
        for (int i = 0; i < corners.size(); i++) {
            DecimalPosition current = corners.get(i);
            DecimalPosition next = corners.get(CollectionUtils.getCorrectedIndex(i + 1, corners.size()));
            borders.add(new LineBorder(current, next));
        }
    }

    private void setupSlopingBorder(List<DecimalPosition> corners) {
        // Correct the borders. Outer corners can not be too close to other corners. It needs some safety distance
        boolean violationsFound = true;
        while (violationsFound) {
            violationsFound = false;
            for (int i = 0; i < corners.size(); i++) {
                DecimalPosition previous = corners.get(CollectionUtils.getCorrectedIndex(i - 1, corners.size()));
                DecimalPosition current = corners.get(i);
                DecimalPosition next = corners.get(CollectionUtils.getCorrectedIndex(i + 1, corners.size()));
                double innerAngle = current.angle(next, previous);
                if (innerAngle > MathHelper.HALF_RADIANT) {
                    double safetyDistance = slopeSkeletonConfig.getWidth() / Math.tan((MathHelper.ONE_RADIANT - innerAngle) / 2.0);
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

                    DecimalPosition afterNext = corners.get(CollectionUtils.getCorrectedIndex(i + 2, corners.size()));
                    double innerAngleNext = next.angle(afterNext, current);
                    if (innerAngleNext > MathHelper.HALF_RADIANT) {
                        double safetyDistanceNext = slopeSkeletonConfig.getWidth() / Math.tan((MathHelper.ONE_RADIANT - innerAngleNext) / 2.0);
                        if (current.getDistance(next) < safetyDistance + safetyDistanceNext) {
                            violationsFound = true;
                            corners.remove(i);
                            break;
                        }
                    }
                }
            }
        }
        // Setup inner and outer corner
        List<AbstractCornerBorder> cornerBorders = new ArrayList<>();
        for (int i = 0; i < corners.size(); i++) {
            DecimalPosition previous = corners.get(CollectionUtils.getCorrectedIndex(i - 1, corners.size()));
            DecimalPosition current = corners.get(i);
            DecimalPosition next = corners.get(CollectionUtils.getCorrectedIndex(i + 1, corners.size()));
            if (current.angle(next, previous) > MathHelper.HALF_RADIANT) {
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

    public Polygon2D getOuterPolygon() {
        return outerPolygon;
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

    public List<DecimalPosition> getCorner2d() {
        return corners;
    }

    public List<Vertex> getCorner3d() {
        return this.corners.stream().map(corner -> new Vertex(corner.getX(), corner.getY(), 0)).collect(Collectors.toList());
    }

    public void updateSlopeSkeleton(SlopeSkeletonConfig slopeSkeletonConfig) {
        this.slopeSkeletonConfig = slopeSkeletonConfig;
    }

    public void fillObstacleContainer(ObstacleContainer obstacleContainer) {
        fillObstacle(innerLine, obstacleContainer, false);
        fillObstacle(outerLine, obstacleContainer, true);
        for (AbstractBorder border : borders) {
            for (VerticalSegment verticalSegment : border.getVerticalSegments()) {
                obstacleContainer.addSlopeSegment(verticalSegment);
            }
        }
    }

    private void fillObstacle(List<Vertex> polygon, ObstacleContainer obstacleContainer, boolean isOuter) {
        DecimalPosition last = polygon.get(0).toXY();
        Index lastNodeIndex = null;
        for (int i = 0; i < polygon.size(); i++) {
            DecimalPosition next = polygon.get(CollectionUtils.getCorrectedIndex(i + 1, polygon.size())).toXY();
            if (last.equals(next)) {
                continue;
            }
            obstacleContainer.addObstacleSlope(new ObstacleSlope(new Line(last, next)));
            if (isOuter) {
                DecimalPosition absolute = polygon.get(i).toXY();
                Index nodeIndex = TerrainUtil.toNode(absolute);
                obstacleContainer.addSlopeGroundConnector(polygon, i, absolute);
                if (lastNodeIndex != null) {
                    // Check if some node are left out
                    if (nodeIndex.getX() != lastNodeIndex.getX() && nodeIndex.getY() != lastNodeIndex.getY()) {
                        Vertex predecessor = polygon.get(i - 1);
                        Vertex successor = polygon.get(i);
                        List<Index> leftOut = GeometricUtil.rasterizeLine(new Line(predecessor.toXY() ,successor.toXY()), TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH);
                        leftOut.remove(0);
                        leftOut.remove(leftOut.size() - 1);
                        for (Index leftOutNodeIndex : leftOut) {
                            obstacleContainer.addLeftOutSlopeGroundConnector(leftOutNodeIndex, predecessor, successor);

                        }
                    }
                }
                lastNodeIndex = nodeIndex;
            }
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

    public Vertex calculatePositionOnSlope(Line3d worldPickRay) {
        Vertex groundPlateauConnectorPosition = groundPlateauConnector.calculatePositionOnGroundPlateauConnector(worldPickRay);
        if (groundPlateauConnectorPosition != null) {
            return groundPlateauConnectorPosition;
        }

        return mesh.getCrossPositionOnMesh(worldPickRay);
    }

    public boolean isInSlope(DecimalPosition position) {
        return outerPolygon.isInside(position);
    }

    public boolean isInSlope(DecimalPosition position, double radius) {
        // TODO This is not enough. Radius is ignored. Also check the innerPolygon and the water
        return outerPolygon.isInside(position);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Slope slope = (Slope) o;

        return slopeId == slope.slopeId;
    }

    @Override
    public int hashCode() {
        return slopeId;
    }
}
