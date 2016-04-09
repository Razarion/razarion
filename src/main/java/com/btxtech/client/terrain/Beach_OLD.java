package com.btxtech.client.terrain;

import com.btxtech.client.renderer.model.GroundMesh;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.Rectangle;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.shared.MathHelper2;
import com.btxtech.shared.TerrainMeshVertex;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Vertex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 21.12.2015.
 */
@Deprecated
public class Beach_OLD {
    private static final Rectangle INDEX_RECT = new Rectangle(100, 30, 30, 15);
    private static final double BOTTOM = -8.0;
    private static final double WATER_LEVEL = -4.0;
    private final List<Double> SLOP_INDEX = Arrays.asList(-8.0, -7.0, -6.0, -5.0, -4.0, -3.0, -2.0, -1.0, -0.0);
    // private GroundMesh groundMesh;
    private double bumpMap = 0.5;
    private double fractal = 0.5;
    // Water, should not be in here
    private double waterTransparency = 0.5;
    private double waterBumpMap = 2.0;
    private double waterSpecularIntensity = 0.5;
    private double waterSpecularHardness = 2.0;
    // private Logger logger = Logger.getLogger(Beach.class.getName());

    public Beach_OLD(GroundMesh groundMesh) {
      //   this.groundMesh = groundMesh;
    }

    public void sculpt() {
//        final int slopeSize = SLOP_INDEX.size();
//
//        final List<Double> slopeForm = new ArrayList<>();
//        slopeForm.add(BOTTOM);
//        slopeForm.addAll(SLOP_INDEX);
//        slopeForm.add(0.0);
//
//        final Collection<Index> slopeIndexes = new ArrayList<>();
//
//        groundMesh.iterate(new GroundMesh.VertexVisitor() {
//            @Override
//            public void onVisit(Index index, Vertex vertex) {
//                GroundMesh.VertexData vertexData = groundMesh.getVertexDataSafe(index);
//                if (isInside(index)) {
//                    vertexData.setVertex(new Vertex(vertex.getX(), vertex.getY(), BOTTOM));
//                    vertexData.setSlopeFactor(0);
//                    vertexData.setType(TerrainMeshVertex.Type.UNDER_WATER);
//                } else {
//                    double distance = INDEX_RECT.getNearestPointInclusive(new DecimalPosition(index)).getDistance(new DecimalPosition(index));
//                    if (distance < slopeSize + 1) {
//                        vertexData.addZValue(MathHelper2.interpolate(distance, slopeForm));
//                        vertexData.setSlopeFactor(1.0);
//                        vertexData.setType(TerrainMeshVertex.Type.BEACH);
//                        slopeIndexes.add(index);
//                    }
//                }
//            }
//        });
//
//
//        FractalFieldOld fractalFieldOld = FractalFieldOld.createSaveFractalField(INDEX_RECT.getWidth() + slopeSize * 2, INDEX_RECT.getHeight() + slopeSize * 2, fractal, -fractal, 1.0);
//        // Calculate fractal
//        Index origin = INDEX_RECT.getStart().sub(slopeSize, slopeSize);
//        Map<Index, Vertex> displacements = new HashMap<>();
//        for (Index slopeIndex : slopeIndexes) {
//            Vertex norm = groundMesh.getVertexNormSafe(slopeIndex);
//            displacements.put(slopeIndex, norm.multiply(fractalFieldOld.get(slopeIndex.sub(origin))));
//        }
//
//        // Apply fractal
//        for (Map.Entry<Index, Vertex> entry : displacements.entrySet()) {
//            groundMesh.getVertexDataSafe(entry.getKey()).add(entry.getValue());
//        }

    }

    public boolean isInside(Index index) {
        return INDEX_RECT.contains2(new DecimalPosition(index));
    }

    public VertexList provideWaterVertexList() {
        int waterX = (INDEX_RECT.getStart().getX() - SLOP_INDEX.size() - 1) * TerrainSurface.MESH_NODE_EDGE_LENGTH;
        int waterY = (INDEX_RECT.getStart().getY() - SLOP_INDEX.size() - 1) * TerrainSurface.MESH_NODE_EDGE_LENGTH;

        int waterXSize = (INDEX_RECT.getWidth() + 2 * SLOP_INDEX.size() + 2) * TerrainSurface.MESH_NODE_EDGE_LENGTH;
        int waterYSize = (INDEX_RECT.getHeight() + 2 * SLOP_INDEX.size() + 2) * TerrainSurface.MESH_NODE_EDGE_LENGTH;

//        GroundMesh waterGroundMesh = new GroundMesh();
//        waterGroundMesh.reset(waterXSize, waterYSize, waterXSize, waterYSize, WATER_LEVEL);
//        waterGroundMesh.shift(new Index(waterX, waterY));
//        waterGroundMesh.generateAllTriangle();
//        waterGroundMesh.setupNorms();
//
//        return waterGroundMesh.provideVertexList();
        return new VertexList();
    }

    public double getWaterLevel() {
        return WATER_LEVEL;
    }

    public double getWaterGround() {
        return BOTTOM;
    }

    public double getWaterTransparency() {
        return waterTransparency;
    }

    public void setWaterTransparency(double waterTransparency) {
        this.waterTransparency = waterTransparency;
    }

    public double getWaterBumpMap() {
        return waterBumpMap;
    }

    public void setWaterBumpMap(double waterBumpMap) {
        this.waterBumpMap = waterBumpMap;
    }

    public double getWaterSpecularIntensity() {
        return waterSpecularIntensity;
    }

    public void setWaterSpecularIntensity(double waterSpecularIntensity) {
        this.waterSpecularIntensity = waterSpecularIntensity;
    }

    public double getWaterSpecularHardness() {
        return waterSpecularHardness;
    }

    public void setWaterSpecularHardness(double waterSpecularHardness) {
        this.waterSpecularHardness = waterSpecularHardness;
    }

    public double getWaterAnimation() {
        return getWaterAnimation(System.currentTimeMillis(), 2000, 0);
    }

    public double getWaterAnimation2() {
        return getWaterAnimation(System.currentTimeMillis(), 2000, 500);
    }

    public double getWaterAnimation(long millis, int durationMs, int offsetMs) {
        return Math.sin(((millis % durationMs) / (double) durationMs + ((double)offsetMs / (double)durationMs)) * MathHelper.ONE_RADIANT);
    }

    public double getBumpMap() {
        return bumpMap;
    }

    public void setBumpMap(double bumpMap) {
        this.bumpMap = bumpMap;
    }

    public double getFractal() {
        return fractal;
    }

    public void setFractal(double fractal) {
        this.fractal = fractal;
    }
}
