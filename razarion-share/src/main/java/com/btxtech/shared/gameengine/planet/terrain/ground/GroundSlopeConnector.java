package com.btxtech.shared.gameengine.planet.terrain.ground;

import com.btxtech.shared.dto.VertexList;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.InterpolatedTerrainTriangle;
import com.btxtech.shared.datatypes.Polygon2I;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 05.03.2016.
 */
public class GroundSlopeConnector {
    private final static Logger LOGGER = Logger.getLogger(GroundSlopeConnector.class.getName());
    private final GroundMesh groundMesh;
    private final GroundMesh groundMeshOriginal;
    private final Slope slope;
    private GroundMesh topMesh;
    private List<VertexDataObject> innerGroundEdges;
    private List<VertexDataObject> innerSlopeEdges;
    private List<VertexDataObject> outerGroundEdges;
    private List<VertexDataObject> outerSlopeEdges;
    private VertexList innerConnectionVertexList;
    private VertexList outerConnectionVertexList;
    private List<Index> topIndices;
    private List<Index> bottomIndices;

    public GroundSlopeConnector(GroundMesh groundMesh, Slope slope) {
        this.groundMesh = groundMesh;
        this.slope = slope;
        groundMeshOriginal = groundMesh.copy();
    }

    public void stampOut(final boolean hasTop) {
        outerConnectionVertexList = new VertexList();
        if (hasTop) {
            innerConnectionVertexList = new VertexList();
            topMesh = new GroundMesh();
            topMesh.setEdgeLength(groundMesh.getEdgeLength());
            topIndices = new ArrayList<>();
        }
        bottomIndices = new ArrayList<>();
        try {
            groundMesh.iterate(new GroundMesh.VertexVisitor() {
                @Override
                public void onVisit(Index index, Vertex vertex) {
                    if (hasTop && slope.isInsideInner(vertex)) {
                        topMesh.createVertexData(index, groundMesh);
                        topMesh.getVertexDataSafe(index).addZ(slope.getHeight());
                        topIndices.add(index);
                    }
                    if (slope.isInsideOuter(vertex)) {
                        bottomIndices.add(index);
                        groundMesh.remove(index);
                    }
                }
            });

            if (hasTop) {
                topMesh.setupNorms();
                setupInnerConnections();
            }

            setupOuterConnections();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void setupOuterConnections() {
        outerGroundEdges = setupGroundEdgeList(bottomIndices, groundMesh);
        outerSlopeEdges = setupSlopeEdgeList(slope.getOuterLineMeshIndex(), groundMeshOriginal, outerGroundEdges.get(0));

        GroundSlopeConnectorTriangulator triangulator = new GroundSlopeConnectorTriangulator(outerConnectionVertexList, outerGroundEdges, outerSlopeEdges, true);
        triangulator.triangulation();
    }

    private List<VertexDataObject> setupGroundEdgeList(List<Index> indices, GroundMesh groundMesh) {
        // Edge detection
        Index start = indices.get(0);
        boolean failed = false;
        while (!groundMesh.contains(start)) {
            if (start.getX() < 0) {
                failed = true;
                break;
            }
            start = start.sub(1, 0);
        }
        if (failed) {
            start = indices.get(0);
            while (!groundMesh.contains(start)) {
                start = start.add(1, 0);
            }
        }

        List<Index> edgeList = new ArrayList<>();
        edgeList.add(start);
        List<Index> connectingEdges = getConnectedEdges(start, groundMesh);
        Index last = connectingEdges.get(0);
        edgeList.add(last);

        Index secondToLast = start;
        while (!start.equals(last)) {
            connectingEdges = getConnectedEdges(last, groundMesh);
            connectingEdges.remove(secondToLast);
            if (connectingEdges.size() != 1) {
                throw new IllegalStateException();
            }
            secondToLast = last;
            last = connectingEdges.get(0);
            if (!start.equals(last)) {
                edgeList.add(last);
            }
        }

        // Setup the outer edges list
        if (Polygon2I.isCounterClock(edgeList)) {
            Collections.reverse(edgeList);
        }
        List<VertexDataObject> edges = new ArrayList<>();
        for (Index index : edgeList) {
            VertexData vertexData = groundMesh.getVertexDataSafe(index);
            edges.add(new VertexDataObject(vertexData.getVertex(), vertexData.getNorm(), vertexData.getTangent(), vertexData.getSplatting()));
        }

        return edges;
    }

    private List<VertexDataObject> setupSlopeEdgeList(List<Index> inputSlopeMeshIndices, GroundMesh groundMesh, Vertex referenceGroundVertex) {
        List<VertexDataObject> slopeLine = new ArrayList<>();
        for (Index index : inputSlopeMeshIndices) {
            Vertex vertex = slope.getMesh().getVertexSave(index);
            slopeLine.add(new VertexDataObject(vertex,
                    slope.getMesh().getNormSave(index),
                    slope.getMesh().getTangentSave(index),
                    groundMesh.getInterpolatedTerrainTriangle(vertex.toXY()).getSplatting()));
        }

        // Find nearest point and fix list
        double shortestDistance = Double.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < slopeLine.size(); i++) {
            VertexDataObject vertex = slopeLine.get(i);
            double distance = vertex.distance(referenceGroundVertex);
            if (shortestDistance > distance) {
                shortestDistance = distance;
                index = i;
            }
        }
        if (index == -1) {
            throw new IllegalStateException();
        }

        List<VertexDataObject> correctedSlopeLine = new ArrayList<>(slopeLine.subList(index, slopeLine.size()));
        correctedSlopeLine.addAll(slopeLine.subList(0, index));

        return correctedSlopeLine;
    }

    private List<Index> getConnectedEdges(Index start, GroundMesh groundMesh) {
        List<Index> connectingEdges = new ArrayList<>();
        // North
        if (isEdge(groundMesh, start, new Index(0, 1), new Index(-1, 0), new Index(-1, 1), new Index(1, 0), new Index(1, 1))) {
            connectingEdges.add(start.add(0, 1));
        }
        // East
        if (isEdge(groundMesh, start, new Index(1, 0), new Index(0, 1), new Index(1, 1), new Index(0, -1), new Index(1, -1))) {
            connectingEdges.add(start.add(1, 0));
        }
        // South
        if (isEdge(groundMesh, start, new Index(0, -1), new Index(-1, 0), new Index(-1, -1), new Index(1, 0), new Index(1, -1))) {
            connectingEdges.add(start.add(0, -1));
        }
        // West
        if (isEdge(groundMesh, start, new Index(-1, 0), new Index(0, 1), new Index(-1, 1), new Index(0, -1), new Index(-1, -1))) {
            connectingEdges.add(start.add(-1, 0));
        }
        if (connectingEdges.size() != 2) {
            throw new IllegalArgumentException();
        }

        return connectingEdges;
    }

    private boolean isEdge(GroundMesh groundMesh, Index start, Index destination, Index side1A, Index side1B, Index side2A, Index side2B) {
        if (!groundMesh.contains(start.add(destination))) {
            return false;
        }

        boolean side1Exists = groundMesh.contains(start.add(side1A)) && groundMesh.contains(start.add(side1B));
        boolean side2Exists = groundMesh.contains(start.add(side2A)) && groundMesh.contains(start.add(side2B));

        return side1Exists ^ side2Exists;
    }

    private void setupInnerConnections() {
        for (Iterator<Index> iterator = topIndices.iterator(); iterator.hasNext(); ) {
            Index topIndex = iterator.next();
            if (!isValidTriangle(topIndex, topIndices)) {
                iterator.remove();
            }
        }

        innerGroundEdges = setupGroundEdgeList(topIndices, topMesh);
        innerSlopeEdges = setupSlopeEdgeList(slope.getInnerLineMeshIndex(), groundMeshOriginal, innerGroundEdges.get(0));

        GroundSlopeConnectorTriangulator triangulator = new GroundSlopeConnectorTriangulator(innerConnectionVertexList, innerGroundEdges, innerSlopeEdges, false);

        triangulator.triangulation();
    }


    private boolean isValidTriangle(Index edge, List<Index> indices) {
        if (exist(indices, edge, 1, 0) && exist(indices, edge, 0, 1)) {
            return true;
        }
        if (exist(indices, edge, 0, 1) && exist(indices, edge, -1, 1)) {
            return true;
        }
        if (exist(indices, edge, -1, 1) && exist(indices, edge, -1, 0)) {
            return true;
        }
        if (exist(indices, edge, -1, 0) && exist(indices, edge, 0, -1)) {
            return true;
        }
        if (exist(indices, edge, 0, -1) && exist(indices, edge, 1, -1)) {
            return true;
        }
        if (exist(indices, edge, 1, -1) && exist(indices, edge, 1, 0)) {
            return true;
        }
        return false;
    }


    private boolean exist(List<Index> indices, Index index, int x, int y) {
        return indices.contains(index.add(x, y));
    }

    public GroundMesh getTopMesh() {
        return topMesh;
    }

    public List<VertexDataObject> getInnerGroundEdges() {
        return innerGroundEdges;
    }

    public List<VertexDataObject> getInnerSlopeEdges() {
        return innerSlopeEdges;
    }

    public VertexList getInnerConnectionVertexList() {
        return innerConnectionVertexList;
    }

    public List<VertexDataObject> getOuterGroundEdges() {
        return outerGroundEdges;
    }

    public List<VertexDataObject> getOuterSlopeEdges() {
        return outerSlopeEdges;
    }

    public VertexList getOuterConnectionVertexList() {
        return outerConnectionVertexList;
    }

    public InterpolatedTerrainTriangle getInterpolatedVertexData(DecimalPosition absoluteXY) {
        if (topMesh != null) {
            InterpolatedTerrainTriangle terrainTriangle = topMesh.getInterpolatedTerrainTriangle(absoluteXY);
            if (terrainTriangle != null) {
                return terrainTriangle;
            }
            terrainTriangle = innerConnectionVertexList.getInterpolatedTerrainTriangle(absoluteXY);
            if (terrainTriangle != null) {
                return terrainTriangle;
            }
        }
        InterpolatedTerrainTriangle terrainTriangle = outerConnectionVertexList.getInterpolatedTerrainTriangle(absoluteXY);
        if (terrainTriangle != null) {
            return terrainTriangle;
        }

        return null;
    }
}
