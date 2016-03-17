package com.btxtech.client.terrain;

import com.btxtech.client.renderer.model.GridRect;
import com.btxtech.client.renderer.model.GroundMesh;
import com.btxtech.client.terrain.slope.Plateau;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.Line;
import com.btxtech.shared.primitives.Vertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Beat
 * 05.03.2016.
 */
public class GroundSlopeConnector {
    private final GroundMesh groundMesh;
    private final Plateau plateau;
    private Collection<Vertex> stampedOut = new ArrayList<>();
    private GroundMesh topMesh;
    List<Vertex> edges;

    public GroundSlopeConnector(GroundMesh groundMesh, Plateau plateau) {
        this.groundMesh = groundMesh;
        this.plateau = plateau;
    }

    public void stampOut() {
        topMesh = new GroundMesh();
        final List<Index> topIndices = new ArrayList<>();
        groundMesh.iterate(new GroundMesh.VertexVisitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                if (plateau.isInsideInner(vertex)) {
                    stampedOut.add(vertex);
                    topMesh.createVertexData(index, groundMesh);
                    topMesh.getVertexDataSafe(index).add(0, 0, plateau.getZInner());
                    topIndices.add(index);
                    // TODO groundMesh.remove(index); ???
                }
            }
        });
        topMesh.setupNorms();

        for (Iterator<Index> iterator = topIndices.iterator(); iterator.hasNext(); ) {
            Index topIndex = iterator.next();
            if (!isValidTriangle(topIndex, topIndices)) {
                iterator.remove();
            }
        }

        // Get all edge indices
        // GridRect gridRect = groundMesh.getGridRect(plateau.getInnerLine().get(0));
        //  for (Line line : plateau.getInnerPolygon().getLines()) {
        //Line line = plateau.getInnerPolygon().getLines().get(0);

        List<Index> edgeIndex = new ArrayList<>();
        int count = 0;
        for (Line line : plateau.getInnerPolygon().getLines()) {
            System.out.println("line: " + line);
            GridRect.Cross ignore = null;
            GridRect gridRect = groundMesh.getGridRect(plateau.getInnerLine().get(count).toXY());
            count++;
            while (gridRect != null) {
                GridRect.Cross cross = gridRect.getSingleCross(line, ignore);
                if (cross == null) {
                    gridRect = null;
                    continue;
                }

                Index indexStart = cross.getIndexStart();
                if (/*!plateau.getInnerPolygon().isInside(point1) || !line.isOnNormSide(point1) || */!topIndices.contains(indexStart)) {
                    indexStart = null;
                }
                Index indexEnd = cross.getIndexEnd();
                if (/*!plateau.getInnerPolygon().isInside(point2) || !line.isOnNormSide(point2) || */!topIndices.contains(indexEnd)) {
                    indexEnd = null;
                }

                if (indexStart == null && indexEnd == null) {
                    System.out.println("+++ 1");
                } else if (indexStart != null && indexEnd != null) {
                    System.out.println("+++ 2");
                    throw new IllegalStateException();
                } else if (indexStart != null) {
                    if (edgeIndex.isEmpty() || !edgeIndex.get(edgeIndex.size() - 1).equals(indexStart)) {
                        System.out.println("+++ 3: " + indexStart);
                        edgeIndex.add(indexStart);
                    }
                } else {
                    if (edgeIndex.isEmpty() || !edgeIndex.get(edgeIndex.size() - 1).equals(indexEnd)) {
                        System.out.println("+++ 4: " + indexEnd);
                        edgeIndex.add(indexEnd);
                    }
                }
                ignore = cross;
                gridRect = groundMesh.getGridRect(cross, gridRect);
            }

            edges = new ArrayList<>();
            for (Index index : edgeIndex) {
                edges.add(topMesh.getVertexSafe(index));
            }

        }

        //  }


//
//        // Get all edge indices
//        edges = new ArrayList<>();
//        for (Index topIndex : topIndices) {
//            if (isEdge(topIndex, topIndices) && isValidTriangle(topIndex, topIndices)) {
//                edges.add(topIndex);
//            }
//        }
//
//        // Order the edge list
//        Index start = null;
//        double minDistance = Double.MAX_VALUE;
//        Vertex innerPlateauStart = plateau.getInnerLine().get(0);
//        for (Index edge : edges) {
//            double distance = topMesh.getVertexDataSafe(edge).getVertex().distance(innerPlateauStart);
//            if (distance < minDistance) {
//                minDistance = distance;
//                start = edge;
//            }
//        }
//        if (start == null) {
//            throw new IllegalStateException();
//        }
//        Index current = start;
//        Index previous = getNextNode(current, edges, topIndices);
//        List<Index> sortedEdges = new ArrayList<>();
//
//
//        System.out.println("-----------------------------------------------------");
//        System.out.println("innerPlateauStart: " + innerPlateauStart);
//        System.out.println("start1: " + start);
//        System.out.println("start2: " + topMesh.getVertexDataSafe(start));
////        //
////        // Find start
////        Index start = topIndices.get(0);
////        List<Index> edges = new ArrayList<>();
////        while (topIndices.contains(start.sub(1, 0))) {
////            start = start.sub(1, 0);
////        }
////        edges.add(start);
////
////        // North
////        Index next = start.add(1, 0);
////        if (isEdge(next, edges)) {
////        }

    }

    private Index getNextNode(Index current, List<Index> edges, List<Index> topIndices) {
        // North
        if (!exist(topIndices, current, 0, 1)) {
            // move west
            if (exist(topIndices, current, -1, 0)) {
                return current.add(-1, 0);
            } else {

            }
        }
        // East
        if (!exist(topIndices, current, 1, 0)) {
            // move up
        }
        // South
        if (!exist(topIndices, current, 0, -1)) {
            // move right
        }
        // West
        if (!exist(topIndices, current, -1, 0)) {
            // move down
        }


        return null;
    }

    private boolean isEdge(Index index, List<Index> indices) {
        int count = 0;
        if (exist(indices, index, 0, 1)) {
            count++; // North
        }
        if (exist(indices, index, 1, 1)) {
            count++; // North east
        }
        if (exist(indices, index, 1, 0)) {
            count++; // East
        }
        if (exist(indices, index, 1, -1)) {
            count++; // South east
        }
        if (exist(indices, index, 0, -1)) {
            count++; // South
        }
        if (exist(indices, index, -1, -1)) {
            count++; // South west
        }
        if (exist(indices, index, -1, 0)) {
            count++; // West
        }
        if (exist(indices, index, -1, 1)) {
            count++; // North west
        }
        return count < 8;
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

    public List<Vertex> getEdges() {
        return edges;
    }
}
