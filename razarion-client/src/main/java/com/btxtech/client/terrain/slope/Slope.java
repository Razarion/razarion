package com.btxtech.client.terrain.slope;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.terrain.GroundMesh;
import com.btxtech.client.terrain.GroundSlopeConnector;
import com.btxtech.game.jsre.client.common.CollectionUtils;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.Line2I;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.shared.dto.SlopeSkeleton;
import com.btxtech.shared.gameengine.pathing.Obstacle;
import com.btxtech.shared.primitives.Polygon2D;
import com.btxtech.shared.primitives.Vertex;

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
    private SlopeSkeleton slopeSkeleton;
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
    private ImageDescriptor slopeGroundSplattingImageDescriptor;
    private ImageDescriptor slopeImageDescriptor;
    private ImageDescriptor slopeBumpImageDescriptor;
    private GroundSlopeConnector groundPlateauConnector;

    public Slope(SlopeSkeleton slopeSkeleton, List<Index> corners) {
        this.slopeSkeleton = slopeSkeleton;
        this.corners = new ArrayList<>(corners);

        if (slopeSkeleton.getWidth() > 0) {
            setupSlopingBorder(this.corners);
        } else {
            setupStraightBorder(this.corners);
        }

        // Setup vertical segments
        xVertices = 0;
        for (AbstractBorder border : borders) {
            xVertices += border.setupVerticalSegments(slopeSkeleton.getVerticalSpace());
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
        // Correct the borders. Outer corners can not be too close to other corners. Id needs some safty distance
        boolean violationsFound = true;
        while (violationsFound) {
            violationsFound = false;
            for (int i = 0; i < corners.size(); i++) {
                Index previous = corners.get(CollectionUtils.getCorrectedIndex(i - 1, corners.size()));
                Index current = corners.get(i);
                Index next = corners.get(CollectionUtils.getCorrectedIndex(i + 1, corners.size()));
                double innerAngle = current.getAngle(next, previous);
                if (innerAngle > MathHelper.HALF_RADIANT) {
                    double safetyDistance = (double) slopeSkeleton.getWidth() / Math.tan((MathHelper.ONE_RADIANT - innerAngle) / 2.0);
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
                cornerBorders.add(new OuterCornerBorder(current, previous, next, slopeSkeleton.getWidth()));
            } else {
                cornerBorders.add(new InnerCornerBorder(current, previous, next, slopeSkeleton.getWidth()));
            }
        }
        // Setup whole contour
        for (int i = 0; i < cornerBorders.size(); i++) {
            AbstractCornerBorder current = cornerBorders.get(i);
            AbstractCornerBorder next = cornerBorders.get(CollectionUtils.getCorrectedIndex(i + 1, cornerBorders.size()));
            borders.add(current);
            borders.add(new LineBorder(current, next, slopeSkeleton.getWidth()));
        }
    }

    public void wrap(GroundMesh groundMesh) {
        mesh = new Mesh(xVertices, slopeSkeleton.getRows());
        innerLineMeshIndex = new ArrayList<>();
        outerLineMeshIndex = new ArrayList<>();
        SlopeModeler.generateMesh(mesh, slopeSkeleton, borders, innerLineMeshIndex, outerLineMeshIndex, groundMesh);
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
        return slopeSkeleton.getHeight();
    }

    public List<Vertex> getInnerLine() {
        return innerLine;
    }

    public List<Vertex> getOuterLine() {
        return outerLine;
    }

    public ImageDescriptor getSlopeImageDescriptor() {
        return slopeImageDescriptor;
    }

    public void setSlopeImageDescriptor(ImageDescriptor slopeImageDescriptor) {
        this.slopeImageDescriptor = slopeImageDescriptor;
    }

    public ImageDescriptor getSlopeBumpImageDescriptor() {
        return slopeBumpImageDescriptor;
    }

    public void setSlopeBumpImageDescriptor(ImageDescriptor slopeBumpImageDescriptor) {
        this.slopeBumpImageDescriptor = slopeBumpImageDescriptor;
    }

    public ImageDescriptor getSlopeGroundSplattingImageDescriptor() {
        return slopeGroundSplattingImageDescriptor;
    }

    public void setSlopeGroundSplattingImageDescriptor(ImageDescriptor slopeGroundSplattingImageDescriptor) {
        this.slopeGroundSplattingImageDescriptor = slopeGroundSplattingImageDescriptor;
    }

    public boolean hasWater() {
        return false;
    }

    public double getWaterLevel() {
        return 0;
    }

    public double getWaterGround() {
        return 0;
    }

    public SlopeSkeleton getSlopeSkeleton() {
        return slopeSkeleton;
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

    public void updateSlopeSkeleton(SlopeSkeleton slopeSkeleton) {
        this.slopeSkeleton = slopeSkeleton;
    }

    public Collection<Obstacle> generateObstacles() {
        Collection<Obstacle> obstacles = new ArrayList<>();

        fillObstacle(innerLine, obstacles);
        fillObstacle(outerLine, obstacles);

//        Index last = innerLine.get(0).toXY().getPositionRound();
//        for (int i = 0; i < innerLine.size(); i++) {
//            Index next = innerLine.get(CollectionUtils.getCorrectedIndex(i + 1, innerLine.size())).toXY().getPositionRound();;
//            if(last.equals(next)) {
//                continue;
//            }
//            obstacles.add(new Obstacle(new Line2I(last, next)));
//            last = next;
//        }
//        for (int i = 0; i < outerLine.size(); i++) {
//            Vertex current = outerLine.get(i);
//            Vertex next = outerLine.get(CollectionUtils.getCorrectedIndex(i + 1, outerLine.size()));
//            obstacles.add(new Obstacle(new Line2I(current.toXY().getPositionRound(), next.toXY().getPositionRound())));
//        }

        return obstacles;
    }

    private void fillObstacle(List<Vertex> polygon, Collection<Obstacle> obstacles) {
        Index last = polygon.get(0).toXY().getPositionRound();
        for (int i = 0; i < polygon.size(); i++) {
            Index next = polygon.get(CollectionUtils.getCorrectedIndex(i + 1, polygon.size())).toXY().getPositionRound();;
            if(last.equals(next)) {
                continue;
            }
            obstacles.add(new Obstacle(new Line2I(last, next)));
            last = next;
        }
    }
}
