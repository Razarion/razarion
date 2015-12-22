package com.btxtech.client.terrain;

import com.btxtech.client.renderer.model.Mesh;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.Rectangle;
import com.btxtech.shared.MathHelper2;
import com.btxtech.shared.PlateauConfigEntity;
import com.btxtech.shared.TerrainMeshVertex;
import com.btxtech.shared.primitives.Vertex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 17.10.2015.
 */
public class Plateau {
    private static final Rectangle INDEX_RECT = new Rectangle(45, 60, 30, 15);
    private Mesh mesh;
    private PlateauConfigEntity plateauConfigEntity;
    private final List<Index> SLOP_INDEX = new ArrayList<>(Arrays.asList(
            new Index(TerrainSurface.MESH_NODE_EDGE_LENGTH, 70),
            new Index(TerrainSurface.MESH_NODE_EDGE_LENGTH * 2, 60),
            new Index(TerrainSurface.MESH_NODE_EDGE_LENGTH * 3, 50),
            new Index(TerrainSurface.MESH_NODE_EDGE_LENGTH * 4, 40),
            new Index(TerrainSurface.MESH_NODE_EDGE_LENGTH * 5, 30)));
    // private Logger logger = Logger.getLogger(Plateau.class.getName());

    public Plateau(Mesh mesh) {
        this.mesh = mesh;
    }

    public void sculpt() {
        final int slopeSize = SLOP_INDEX.size();
        int doubleSlopeSize = slopeSize * 2;
        double fractal = plateauConfigEntity != null ? plateauConfigEntity.getFractal() : 0;

        final int top = plateauConfigEntity != null ? plateauConfigEntity.getTop() : 100;
        final List<Double> slopeForm = new ArrayList<>();
        slopeForm.add((double) top);
        for (Index index : SLOP_INDEX) {
            slopeForm.add((double) index.getY());
        }
        slopeForm.add(0.0);

        // Model slope
        final Collection<Index> slopeIndexes = new ArrayList<>();
        mesh.iterate(new Mesh.VertexVisitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                Mesh.VertexData vertexData = mesh.getVertexDataSafe(index);
                if (isInside(index)) {
                    vertexData.setVertex(new Vertex(vertex.getX(), vertex.getY(), top));
                    vertexData.setSlopeFactor(0);
                } else {
                    double distance = INDEX_RECT.getNearestPointInclusive(new DecimalPosition(index)).getDistance(new DecimalPosition(index));
                    if (distance < slopeSize + 1) {
                        vertexData.addZValue(MathHelper2.interpolate(distance, slopeForm));
                        slopeIndexes.add(index);
                        vertexData.setSlopeFactor(1.0);
                        vertexData.setType(TerrainMeshVertex.Type.PLATEAU);
                    }
                }
            }
        });

        FractalField fractalField = FractalField.createSaveFractalField(INDEX_RECT.getWidth() + doubleSlopeSize, INDEX_RECT.getHeight() + doubleSlopeSize, fractal, -fractal, 1.0);
        // Calculate fractal
        Index origin = INDEX_RECT.getStart().sub(slopeSize, slopeSize);
        Map<Index, Vertex> displacements = new HashMap<>();
        for (Index slopeIndex : slopeIndexes) {
            Vertex norm = mesh.getVertexNormSafe(slopeIndex);
            displacements.put(slopeIndex, norm.multiply(fractalField.get(slopeIndex.sub(origin))));
        }

        // Apply fractal
        for (Map.Entry<Index, Vertex> entry : displacements.entrySet()) {
            mesh.getVertexDataSafe(entry.getKey()).add(entry.getValue());
        }

    }

    public boolean isInside(Index index) {
        return INDEX_RECT.contains2(new DecimalPosition(index));
    }

    public void setPlateauConfigEntity(PlateauConfigEntity plateauConfigEntity) {
        this.plateauConfigEntity = plateauConfigEntity;
    }

    public PlateauConfigEntity getPlateauConfigEntity() {
        return plateauConfigEntity;
    }

    public List<Index> getSlopeIndexes() {
        return SLOP_INDEX;
    }

    public void setSlopeIndexes(List<Index> indexes) {
        SLOP_INDEX.clear();
        SLOP_INDEX.addAll(indexes);
    }
}
