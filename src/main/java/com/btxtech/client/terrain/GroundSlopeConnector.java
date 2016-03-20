package com.btxtech.client.terrain;

import com.btxtech.client.renderer.model.GridRect;
import com.btxtech.client.renderer.model.GroundMesh;
import com.btxtech.client.terrain.slope.Plateau;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.Line;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Line3d;
import com.btxtech.shared.primitives.Polygon2d;
import com.btxtech.shared.primitives.Vertex;

import java.util.ArrayList;
import java.util.Arrays;
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
    private final Plateau plateau;
    private Collection<Vertex> stampedOut = new ArrayList<>();
    private GroundMesh topMesh;
    private List<Vertex> innerEdges;
    private VertexList connectionVertexList;
    private List<DecimalPosition> totalLine;

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

        setupInnerEdges(topIndices);
        totalLine = new ArrayList<>();
        totalLine.addAll(Vertex.toXY(plateau.getInnerLine()));
        totalLine.add(plateau.getInnerLine().get(0).toXY());
        totalLine.add(innerEdges.get(0).toXY());
        List<Vertex> innerVertices = new ArrayList<>(innerEdges);
        Collections.reverse(innerVertices);
        totalLine.addAll(Vertex.toXY(innerVertices));
        System.out.println(DecimalPosition.testString(totalLine));







//        totalLine.addAll(Vertex.toXY(innerEdges));
//        totalLine.add(innerEdges.get(0).toXY());
//        totalLine.add(plateau.getInnerLine().get(0).toXY());
//        List<Vertex> outerVertices = new ArrayList<>(plateau.getInnerLine());
//        Collections.reverse(outerVertices);
//        totalLine.addAll(Vertex.toXY(outerVertices));
//        System.out.println(DecimalPosition.testString(totalLine));

        // Make polygon with inner and outer line


        // setupConnectionTriangles();
    }

    private void setupInnerEdges(List<Index> topIndices) {
        List<Index> edgeIndices = new ArrayList<>();
        int count = 0;
        for (Line line : plateau.getInnerPolygon().getLines()) {
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
                if (!topIndices.contains(indexStart)) {
                    indexStart = null;
                }
                Index indexEnd = cross.getIndexEnd();
                if (!topIndices.contains(indexEnd)) {
                    indexEnd = null;
                }

                if (indexStart != null || indexEnd != null) {
                    if (indexStart != null && indexEnd != null) {
                        // Not on the edge
                        throw new IllegalStateException();
                    } else if (indexStart != null) {
                        if (edgeIndices.isEmpty() || !edgeIndices.get(edgeIndices.size() - 1).equals(indexStart)) {
                            edgeIndices.add(indexStart);
                        }
                    } else {
                        if (edgeIndices.isEmpty() || !edgeIndices.get(edgeIndices.size() - 1).equals(indexEnd)) {
                            edgeIndices.add(indexEnd);
                        }
                    }
                }
                ignore = cross;
                gridRect = groundMesh.getGridRect(cross, gridRect);
            }
        }

        innerEdges = new ArrayList<>();
        List<Index> correctedEdgeIndices = new ArrayList<>();
        for (int i = 0; i < edgeIndices.size(); i++) {
            Index start = edgeIndices.get(i);
            Index end = edgeIndices.get(i + 1 < edgeIndices.size() ? i + 1 : 0);

            correctedEdgeIndices.add(start);
            Index middlePoint = insertIfInvalidTriangle(start, end);
            if (middlePoint != null) {
                correctedEdgeIndices.add(middlePoint);
            }
        }
        for (Index index : correctedEdgeIndices) {
            innerEdges.add(topMesh.getVertexSafe(index));
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

    private void setupConnectionTriangles() {
        // TODO norms
        Vertex norm = new Vertex(0, 0, 1);
        connectionVertexList = new VertexList();
        Polygon2d innerPolygon = new Polygon2d(Vertex.toXY(innerEdges));

        int innerIndex = 0;
        List<Vertex> outerLine = plateau.getInnerLine();
        for (int outerIndex = 0; outerIndex < outerLine.size(); outerIndex++) {
            Vertex outer1 = outerLine.get(outerIndex);
            Vertex outer2 = outerLine.get(outerIndex + 1 < outerLine.size() ? outerIndex + 1 : outerIndex - outerLine.size() + 1);

            // TODO Triangle correct direction (corner order counter clock)
            if (checkIntersection(innerPolygon, outer1, outer2, innerEdges.get(innerIndex))) {
                while (true) {
                    Vertex inner1 = innerEdges.get(innerIndex);
                    innerIndex++;
                    if (innerIndex > innerEdges.size() - 1) {
                        innerIndex = 0;
                    }
                    Vertex inner2 = innerEdges.get(innerIndex);
                    if (checkIntersection(innerPolygon, inner1, inner2, outer1)) {
//                        System.out.println("-------------------");
//                        System.out.println("inner1: " + inner1);
//                        System.out.println("inner2: " + inner2);
//                        System.out.println("outer1: " + outer1);
//                        System.out.println("outer2: " + outer2);
                        Vertex insertOuter = projectOnLine(outer1, outer2, inner2);
                        outerLine.add(outerIndex + 1, insertOuter);
                        // System.out.println("new outer2: " + outer2);

                        if (!checkIntersection(innerPolygon, outer1, insertOuter, inner1)) {
                            connectionVertexList.add(outer1, norm, insertOuter, norm, inner1, norm);
                        } else {
                            System.out.println("Unknown 1 ???");
                            // TODO ???
                            return;
                        }

                        outer1 = insertOuter;
                        if(checkIntersection(innerPolygon, inner1, inner2, outer1)) {
                            System.out.println("Unknown 2 ???");
                            // TODO ???
                            return;
                        }

                    }

//                    if(!checkIntersection(innerPolygon, inner1, inner2, outer1)) {
//                        System.out.println("failed");
//                        System.out.println("inner1: " + inner1);
//                        System.out.println("inner2: " + inner2);
//                        System.out.println("outer1: " + outer1);
//                    }

                    connectionVertexList.add(inner1, norm, inner2, norm, outer1, norm);
                    if (!checkIntersection(innerPolygon, outer1, outer2, inner2)) {
                        System.out.println("Next outer");
                        break;
                    }
                }
            }
            connectionVertexList.add(outer1, norm, outer2, norm, innerEdges.get(innerIndex), norm);
        }
    }

    private Vertex projectOnLine(Vertex lineStart, Vertex lineEnd, Vertex source) {
        Line3d line = new Line3d(lineStart, lineEnd);
        return line.projectOnInfiniteLine(source);
    }

    /**
     * Ceck if trinagle violates inner polygon
     *
     * @param innerPolygon Polygon inner (2d)
     * @param cornerA triangle corner A
     * @param cornerB triangle corner B
     * @param cornerC triangle corner C
     * @return true if inner polygon is violated
     */
    private boolean checkIntersection(Polygon2d innerPolygon, Vertex cornerA, Vertex cornerB, Vertex cornerC) {
        Line line1 = new Line(cornerA.toXY(), cornerB.toXY());
        if (innerPolygon.isLineCrossing(line1)) {
            return true;
        }
        Line line2 = new Line(cornerB.toXY(), cornerC.toXY());
        if (innerPolygon.isLineCrossing(line2)) {
            return true;
        }
        Line line3 = new Line(cornerC.toXY(), cornerA.toXY());
        if (innerPolygon.isLineCrossing(line3)) {
            return true;
        }
        Polygon2d outerPolygon = new Polygon2d(Arrays.asList(cornerA.toXY(), cornerB.toXY(), cornerC.toXY()));
        for (DecimalPosition innerCorner : innerPolygon.getCorners()) {
            if (cornerA.toXY().equalsDelta(innerCorner)) {
                continue;
            }
            if (cornerB.toXY().equalsDelta(innerCorner)) {
                continue;
            }
            if (cornerC.toXY().equalsDelta(innerCorner)) {
                continue;
            }
            if (outerPolygon.isInside(innerCorner)) {
                return true;
            }
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

    public List<Vertex> getInnerEdges() {
        return innerEdges;
    }

    public VertexList getConnectionVertexList() {
        return connectionVertexList;
    }

    public List<DecimalPosition> getTotalLine() {
        return totalLine;
    }
}
