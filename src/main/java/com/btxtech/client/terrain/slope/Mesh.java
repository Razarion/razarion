package com.btxtech.client.terrain.slope;

import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.primitives.Vertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class Mesh {
    private MeshEntry[][] nodes;
    private int xCount;
    private int yCount;
    private List<Vertex> vertices;
    private List<Vertex> barycentric;
    private List<Vertex> norms;
    private List<Vertex> tangents;
    private List<Float> slopeFactors;
    private List<Float> splatting;

    public Mesh(int xCount, int yCount) {
        this.xCount = xCount;
        this.yCount = yCount;
        nodes = new MeshEntry[xCount][yCount];
    }

    public void addVertex(int x, int y, Vertex vertex, float slopeFactor, float splatting) {
        nodes[x][y] = new MeshEntry(vertex, slopeFactor, splatting);
    }

    public void setupValues() {
        setupNormAndTangent();
        setupResultList();
    }

    private void setupNormAndTangent() {
        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                Vertex center = getVertexSave(x, y);
                Vertex top = getVertexSave(x, y + 1);
                Vertex right = getVertexSave(x + 1, y);
                Vertex bottom = getVertexSave(x, y - 1);
                Vertex left = getVertexSave(x - 1, y);
                Collection<Vertex> norms = new ArrayList<>();
                appendNorm(norms, center, right, top);
                appendNorm(norms, center, bottom, right);
                appendNorm(norms, center, left, bottom);
                appendNorm(norms, center, top, left);
                Vertex norm = sum(norms);
                double normMagnitude = norm.magnitude();
                if (normMagnitude == 0.0) {
                    if (y == 0) {
                        nodes[x][y].setNorm(new Vertex(0, 0, 1)); // TODO take norm from Ground. Only in corners.
                    } else if (y == yCount - 1) {
                        nodes[x][y].setNorm(new Vertex(0, 0, 1)); // TODO take norm from Ground. Only in corners.
                    } else {
                        nodes[x][y].setNorm(getNorm(x - 1, y));
                    }
                } else {
                    nodes[x][y].setNorm(norm.divide(normMagnitude));
                }

                Vertex tangent = setupTangent(center, left, right);
                double tangentMagnitude = tangent.magnitude();
                if (tangentMagnitude == 0.0) {
                    if (y == 0) {
                        nodes[x][y].setTangent(new Vertex(1, 0, 0)); // TODO take tangent from Ground. Only in corners.
                    } else if (y == yCount - 1) {
                        nodes[x][y].setTangent(new Vertex(1, 0, 0)); // TODO take tangent from Ground. Only in corners.
                    } else {
                        nodes[x][y].setTangent(getTangent(x - 1, y));
                    }
                } else {
                    nodes[x][y].setTangent(tangent.divide(tangentMagnitude));
                }
            }
        }
    }

    private void setupResultList() {
        vertices = new ArrayList<>();
        barycentric = new ArrayList<>();
        norms = new ArrayList<>();
        tangents = new ArrayList<>();
        slopeFactors = new ArrayList<>();
        splatting = new ArrayList<>();
        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount - 1; y++) {
                MeshEntry bottomLeft = getMeshEntrySave(x, y);
                MeshEntry bottomRight = getMeshEntrySave(x + 1, y);
                MeshEntry topLeft = getMeshEntrySave(x, y + 1);
                MeshEntry topRight = getMeshEntrySave(x + 1, y + 1);
                appendTriangle(bottomLeft, bottomRight, topLeft);
                appendTriangle(bottomRight, topRight, topLeft);
            }
        }
    }

    private void appendTriangle(MeshEntry a, MeshEntry b, MeshEntry c) {
        appendTriangleCorner(a);
        appendTriangleCorner(b);
        appendTriangleCorner(c);
        barycentric.add(new Vertex(1, 0, 0));
        barycentric.add(new Vertex(0, 1, 0));
        barycentric.add(new Vertex(0, 0, 1));
    }

    private void appendTriangleCorner(MeshEntry corner) {
        vertices.add(corner.getVertex());
        norms.add(corner.getNorm());
        tangents.add(corner.getTangent());
        slopeFactors.add(corner.getSlopeFactor());
        splatting.add(corner.getSplatting());
    }

    private Vertex setupTangent(Vertex center, Vertex left, Vertex right) {
        Vertex v1 = center.sub(left);
        Vertex vz = right.sub(center);
        return v1.add(vz);
    }

    private void appendNorm(Collection<Vertex> norms, Vertex center, Vertex point1, Vertex point2) {
        if (point1 != null && point2 != null) {
            Vertex v1 = point1.sub(center);
            Vertex v2 = point2.sub(center);
            norms.add(v1.cross(v2));
        }
    }

    private Vertex sum(Collection<Vertex> vectors) {
        Vertex sum = new Vertex(0, 0, 0);
        for (Vertex vector : vectors) {
            sum = sum.add(vector);
        }
        return sum;
    }

    private MeshEntry getMeshEntrySave(int x, int y) {
        if (y > yCount - 1) {
            return null;
        } else if (y < 0) {
            return null;
        }
        int correctedX = x;
        if (x < 0) {
            correctedX = x + xCount;
        } else if (x > xCount - 1) {
            correctedX = x - xCount;
        }
        return nodes[correctedX][y];
    }

    private Vertex getNorm(int x, int y) {
        MeshEntry meshEntry = getMeshEntrySave(x, y);
        if (meshEntry == null) {
            throw new IllegalArgumentException("No entry for: x=" + x + " y=" + y);
        }
        Vertex norm = meshEntry.getNorm();
        if (norm == null) {
            throw new IllegalArgumentException("No norm for: x=" + x + " y=" + y);
        }
        return norm;
    }

    private Vertex getTangent(int x, int y) {
        MeshEntry meshEntry = getMeshEntrySave(x, y);
        if (meshEntry == null) {
            throw new IllegalArgumentException("No entry for: x=" + x + " y=" + y);
        }
        Vertex tangent = meshEntry.getTangent();
        if (tangent == null) {
            throw new IllegalArgumentException("No tanget for: x=" + x + " y=" + y);
        }
        return tangent;
    }

    private Vertex getVertexSave(int x, int y) {
        MeshEntry meshEntry = getMeshEntrySave(x, y);
        if (meshEntry != null) {
            return meshEntry.getVertex();
        } else {
            return null;
        }
    }

    public Vertex getVertexSave(Index index) {
        return getVertexSave(index.getX(), index.getY());
    }

    public Vertex getNormSave(Index index) {
        return getNorm(index.getX(), index.getY());
    }

    public Vertex getTangentSave(Index index) {
        return getTangent(index.getX(), index.getY());
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public List<Vertex> getBarycentric() {
        return barycentric;
    }

    public List<Vertex> getNorms() {
        return norms;
    }

    public List<Vertex> getTangents() {
        return tangents;
    }

    public List<Float> getSlopeFactors() {
        return slopeFactors;
    }

    public List<Float> getSplatting() {
        return splatting;
    }

    public MeshEntry pick(Vertex pointOnGround) {
        double minDistance = Double.MAX_VALUE;
        MeshEntry nearestMeshEntry = null;
        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                MeshEntry meshEntry = getMeshEntrySave(x, y);
                if (meshEntry != null) {
                    double distance = meshEntry.getVertex().toXY().getDistance(pointOnGround.toXY());
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearestMeshEntry = meshEntry;
                    }
                }
            }
        }
        return nearestMeshEntry;
    }
}
