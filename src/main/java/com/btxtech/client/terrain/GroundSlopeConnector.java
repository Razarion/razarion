package com.btxtech.client.terrain;

import com.btxtech.client.renderer.model.GroundMesh;
import com.btxtech.client.renderer.model.VertexData;
import com.btxtech.client.terrain.slope.Plateau;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.ConvexHull;
import com.btxtech.shared.primitives.Polygon2I;
import com.btxtech.shared.primitives.Triangulator;
import com.btxtech.shared.primitives.Vertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Beat
 * 05.03.2016.
 */
public class GroundSlopeConnector {
    private final GroundMesh groundMesh;
    private final GroundMesh groundMeshOriginal;
    private final Plateau plateau;
    private Collection<Vertex> stampedOut = new ArrayList<>();
    private GroundMesh topMesh;
    private List<VertexDataObject> innerEdges;
    private List<VertexDataObject> outerEdges;
    private VertexList connectionVertexList;
    private VertexList outerConnectionVertexList;
    private List<VertexDataObject> totalLine;
    private List<VertexDataObject> totalOuterLine;
    private List<Index> topIndices;
    private List<Index> bottomIndices;

    public GroundSlopeConnector(GroundMesh groundMesh, Plateau plateau) {
        this.groundMesh = groundMesh;
        this.plateau = plateau;
        groundMeshOriginal = groundMesh.copy();
    }

    public void stampOut() {
        topMesh = new GroundMesh();
        topIndices = new ArrayList<>();
        bottomIndices = new ArrayList<>();
        groundMesh.iterate(new GroundMesh.VertexVisitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                if (plateau.isInsideInner(vertex)) {
                    stampedOut.add(vertex);
                    topMesh.createVertexData(index, groundMesh);
                    topMesh.getVertexDataSafe(index).addZ(plateau.getZInner());
                    topIndices.add(index);
                }
                if (plateau.isInsideOuter(vertex)) {
                    bottomIndices.add(index);
                    groundMesh.remove(index);
                }
            }
        });
        topMesh.setupNorms();

        setupTopConnections();

        setupBottomConnections();
    }

    private void setupBottomConnections() {
        outerEdges = setupGroundEdgeList(bottomIndices, groundMesh);

        List<VertexDataObject> slopeOuterLine = setupSlopeEdgeList(plateau.getOuterLineMeshIndex(), groundMeshOriginal, outerEdges.get(0));

        outerConnectionVertexList = new VertexList();
        triangulation(outerConnectionVertexList, outerEdges, slopeOuterLine);
    }

    private List<VertexDataObject> setupGroundEdgeList(List<Index> indices, GroundMesh groundMesh) {
        // Edge detection
        Index start = indices.get(0);

        while (!groundMesh.contains(start)) {
            start = start.sub(1, 0);
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
            edges.add(new VertexDataObject(vertexData.getVertex(), vertexData.getNorm(), vertexData.getTangent(), vertexData.getEdge()));
        }

        return edges;
    }

    private List<VertexDataObject> setupSlopeEdgeList(List<Index> inputSlopeMeshIndices, GroundMesh groundMesh, Vertex referenceGroundVertex) {
        List<VertexDataObject> slopeLine = new ArrayList<>();
        for (Index index : inputSlopeMeshIndices) {
            Vertex vertex = plateau.getMesh().getVertexSave(index);
            slopeLine.add(new VertexDataObject(vertex,
                    plateau.getMesh().getNormSave(index),
                    plateau.getMesh().getTangentSave(index),
                    groundMesh.getInterpolatedSplatting(vertex.toXY())));
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

    private void triangulation(VertexList vertexList, List<VertexDataObject> groundLine, List<VertexDataObject> slopeLine) {
        int innerIndex = 0;
        int outerIndex = 0;

        groundLine.add(groundLine.get(0));
        slopeLine.add(slopeLine.get(0));

        while (outerIndex + 1 < groundLine.size() || innerIndex + 1 < slopeLine.size()) {
            VertexDataObject outerVertex = groundLine.get(outerIndex);
            VertexDataObject innerVertex = slopeLine.get(innerIndex);

            double distanceOuter = Double.MAX_VALUE;
            if (outerIndex + 1 < groundLine.size()) {
                distanceOuter = groundLine.get(outerIndex + 1).distance(innerVertex);
            }

            double distanceInner = Double.MAX_VALUE;
            if (innerIndex + 1 < slopeLine.size()) {
                distanceInner = slopeLine.get(innerIndex + 1).distance(outerVertex);
            }

            if (distanceOuter < distanceInner) {
                addOuterTriangle(vertexList, outerVertex, groundLine.get(outerIndex + 1), innerVertex);
                outerIndex++;
            } else {
                addOuterTriangle(vertexList, outerVertex, slopeLine.get(innerIndex + 1), innerVertex);
                innerIndex++;
            }
        }
    }

    private void addOuterTriangle(VertexList vertexList, VertexDataObject vertexA, VertexDataObject vertexB, VertexDataObject vertexC) {
        vertexList.add(vertexA, vertexA.getNorm(), vertexA.getTangent(), vertexA.getSplatting(),
                vertexB, vertexB.getNorm(), vertexB.getTangent(), vertexB.getSplatting(),
                vertexC, vertexC.getNorm(), vertexC.getTangent(), vertexC.getSplatting());
    }

    private void setupBottomConnections2() {
        // Edge detection
        Index start = bottomIndices.get(0);

        while (!groundMesh.contains(start)) {
            start = start.sub(1, 0);
        }

        List<Index> hull = new ArrayList<>();
        hull.add(start);
        List<Index> connectingEdges = getConnectedEdges(start, groundMesh);
        Index last = connectingEdges.get(0);
        hull.add(last);

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
                hull.add(last);
            }
        }

        // Setup the outer edges list
        if (Polygon2I.isCounterClock(hull)) {
            Collections.reverse(hull);
        }
        outerEdges = new ArrayList<>();
        for (Index index : hull) {
            VertexData vertexData = groundMeshOriginal.getVertexDataSafe(index);
            outerEdges.add(new VertexDataObject(vertexData.getVertex(), vertexData.getNorm(), vertexData.getTangent(), vertexData.getEdge()));
        }


        List<VertexDataObject> tmpInnerLine = new ArrayList<>();
        List<Vertex> outerLine = plateau.getOuterLine();
        for (int i = outerLine.size() - 1; i >= 0; i--) {
            Index index = plateau.getOuterLineMeshIndex().get(i);
            Vertex vertex = plateau.getMesh().getVertexSave(index);
            tmpInnerLine.add(new VertexDataObject(vertex,
                    plateau.getMesh().getNormSave(index),
                    plateau.getMesh().getTangentSave(index),
                    groundMeshOriginal.getInterpolatedSplatting(vertex.toXY())));
        }

        // Find nearest point and fix list
        double shortestDistance = Double.MAX_VALUE;
        int index = -1;
        VertexDataObject outerEdge = outerEdges.get(0);
        for (int i = 0; i < tmpInnerLine.size(); i++) {
            VertexDataObject vertex = tmpInnerLine.get(i);
            double distance = vertex.distance(outerEdge);
            if (shortestDistance > distance) {
                shortestDistance = distance;
                index = i;
            }
        }
        if (index == -1) {
            throw new IllegalStateException();
        }

        List<VertexDataObject> tmpInnerLine2 = new ArrayList<>(tmpInnerLine.subList(index, tmpInnerLine.size()));
        tmpInnerLine2.addAll(tmpInnerLine.subList(0, index));

        // Setup total outer line
        totalOuterLine = new ArrayList<>();
        totalOuterLine.addAll(outerEdges);
        totalOuterLine.add(outerEdges.get(0));
        totalOuterLine.addAll(tmpInnerLine2);
        totalOuterLine.add(tmpInnerLine2.get(0));

        outerConnectionVertexList = new VertexList();
        try {
            Triangulator.calculate(totalOuterLine, new Triangulator.Listener<VertexDataObject>() {
                @Override
                public void onTriangle(VertexDataObject vertex1, VertexDataObject vertex2, VertexDataObject vertex3) {
                    if (vertex1.cross(vertex2, vertex3).getZ() >= 0) {
                        outerConnectionVertexList.add(vertex1, vertex1.getNorm(), vertex1.getTangent(), vertex1.getSplatting(),
                                vertex2, vertex2.getNorm(), vertex2.getTangent(), vertex2.getSplatting(),
                                vertex3, vertex3.getNorm(), vertex3.getTangent(), vertex3.getSplatting());
                    } else {
                        outerConnectionVertexList.add(vertex1, vertex1.getNorm(), vertex1.getTangent(), vertex1.getSplatting(),
                                vertex3, vertex3.getNorm(), vertex3.getTangent(), vertex3.getSplatting(),
                                vertex2, vertex2.getNorm(), vertex2.getTangent(), vertex2.getSplatting());
                    }
                }
            });
        } catch (Throwable t) {
            t.printStackTrace();
        }
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

    private void setupTopConnections() {
        for (Iterator<Index> iterator = topIndices.iterator(); iterator.hasNext(); ) {
            Index topIndex = iterator.next();
            if (!isValidTriangle(topIndex, topIndices)) {
                iterator.remove();
            }
        }

        innerEdges = setupGroundEdgeList(topIndices, topMesh);

        List<VertexDataObject> slopeInnerLine = setupSlopeEdgeList(plateau.getInnerLineMeshIndex(), groundMeshOriginal, innerEdges.get(0));

        connectionVertexList = new VertexList();
        triangulation(connectionVertexList, innerEdges, slopeInnerLine);
    }

    private void setupTopConnections2() {
        for (Iterator<Index> iterator = topIndices.iterator(); iterator.hasNext(); ) {
            Index topIndex = iterator.next();
            if (!isValidTriangle(topIndex, topIndices)) {
                iterator.remove();
            }
        }

        setupInnerEdges(topIndices);
        totalLine = new ArrayList<>();
        List<Vertex> innerLine = plateau.getInnerLine();
        for (int i = 0; i < innerLine.size(); i++) {
            Index index = plateau.getInnerLineMeshIndex().get(i);
            totalLine.add(new VertexDataObject(plateau.getMesh().getVertexSave(index), plateau.getMesh().getNormSave(index), plateau.getMesh().getTangentSave(index), 1)); // TODO
        }

        totalLine.add(totalLine.get(0));
        totalLine.add(innerEdges.get(0));
        List<VertexDataObject> innerVertices = new ArrayList<>(innerEdges);
        Collections.reverse(innerVertices);
        totalLine.addAll(innerVertices);

        connectionVertexList = new VertexList();
        Triangulator.calculate(totalLine, new Triangulator.Listener<VertexDataObject>() {
            @Override
            public void onTriangle(VertexDataObject vertex1, VertexDataObject vertex2, VertexDataObject vertex3) {
                if (vertex1.cross(vertex2, vertex3).getZ() >= 0) {
                    connectionVertexList.add(vertex1, vertex1.getNorm(), vertex1.getTangent(), vertex1.getSplatting(),
                            vertex2, vertex2.getNorm(), vertex2.getTangent(), vertex2.getSplatting(),
                            vertex3, vertex3.getNorm(), vertex3.getTangent(), vertex3.getSplatting());
                } else {
                    connectionVertexList.add(vertex1, vertex1.getNorm(), vertex1.getTangent(), vertex1.getSplatting(),
                            vertex3, vertex3.getNorm(), vertex3.getTangent(), vertex3.getSplatting(),
                            vertex2, vertex2.getNorm(), vertex2.getTangent(), vertex2.getSplatting());
                }
            }
        });
    }

    private void setupInnerEdges(List<Index> topIndices) {
        List<Index> edgeIndices = ConvexHull.convexHull(topIndices);
        // fill missing indices
        List<Index> correctedEdgeIndices = new ArrayList<>();
        for (int i = 0; i < edgeIndices.size(); i++) {
            Index start = edgeIndices.get(i);
            Index end = edgeIndices.get(i + 1 < edgeIndices.size() ? i + 1 : 0);
            Index delta = end.sub(start);
            int deltaX = Math.abs(delta.getX());
            int deltaY = Math.abs(delta.getY());
            correctedEdgeIndices.add(start);
            if (deltaX + deltaY <= 1) {
                continue;
            }
            int stepCount = Math.max(deltaX, deltaY);
            for (int step = 1; step < stepCount; step++) {
                correctedEdgeIndices.add(start.add(step * delta.getX() / stepCount, step * delta.getY() / stepCount));
            }
        }

        List<Index> correctedEdgeIndices2 = new ArrayList<>();
        for (int i = 0; i < correctedEdgeIndices.size(); i++) {
            Index start = correctedEdgeIndices.get(i);
            Index end = correctedEdgeIndices.get(i + 1 < correctedEdgeIndices.size() ? i + 1 : 0);

            correctedEdgeIndices2.add(start);
            Index middlePoint = insertIfInvalidTriangle(start, end);
            if (middlePoint != null) {
                correctedEdgeIndices2.add(middlePoint);
            }
        }

        innerEdges = new ArrayList<>();
        for (Index index : correctedEdgeIndices2) {
            VertexData vertexData = topMesh.getVertexDataSafe(index);
            innerEdges.add(new VertexDataObject(vertexData.getVertex(), vertexData.getNorm(), vertexData.getTangent(), vertexData.getEdge()));
        }
    }

    private Index insertIfInvalidTriangle(Index start, Index end) {
        Index delta = end.sub(start);
        if (delta.getX() == 0 && Math.abs(delta.getY()) == 1) {
            return null;
        } else if (Math.abs(delta.getX()) == 1 && delta.getY() == 0) {
            return null;
        } else if (Math.abs(delta.getX()) == 1 && Math.abs(delta.getY()) == 1) {
            if (delta.getX() == -1 && delta.getY() == 1) {
                return null;
            } else if (delta.getX() == -1 && delta.getY() == -1) {
                return start.sub(0, 1);
            } else if (delta.getX() == 1 && delta.getY() == -1) {
                return null;
            } else if (delta.getX() == 1 && delta.getY() == 1) {
                return start.add(0, 1);
            } else {
                throw new IllegalArgumentException("Can not handle 1. start: " + start + " end: " + end);
            }
        } else {
            throw new IllegalArgumentException("Can not handle. start: " + start + " end: " + end);
        }
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

    public Collection<Vertex> getStampedOut() {
        return stampedOut;
    }

    public GroundMesh getTopMesh() {
        return topMesh;
    }

    public List<VertexDataObject> getInnerEdges() {
        return innerEdges;
    }

    public VertexList getConnectionVertexList() {
        return connectionVertexList;
    }

    public List<VertexDataObject> getTotalLine() {
        return totalLine;
    }

    public List<VertexDataObject> getOuterEdges() {
        return outerEdges;
    }

    public List<VertexDataObject> getTotalOuterLine() {
        return totalOuterLine;
    }

    public VertexList getOuterConnectionVertexList() {
        return outerConnectionVertexList;
    }
}
