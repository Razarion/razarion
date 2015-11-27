package com.btxtech.client.terrain;

import com.btxtech.client.renderer.model.Mesh;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.Rectangle;
import com.btxtech.shared.PlateauConfigEntity;
import com.btxtech.shared.primitives.Vertex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 17.10.2015.
 */
public class Plateau {
    private static final int PLANE_TOP_HEIGHT = 80;
    private static final Rectangle INDEX_RECT = new Rectangle(30, 20, 30, 15);
    private Mesh mesh;
    private PlateauConfigEntity plateauConfigEntity;
    private final List<Index> SLOP_INDEX = new ArrayList<>(Arrays.asList(
            new Index(TerrainSurface.MESH_EDGE_LENGTH, PLANE_TOP_HEIGHT),
            new Index(TerrainSurface.MESH_EDGE_LENGTH * 2, 60),
            new Index(TerrainSurface.MESH_EDGE_LENGTH * 3, 50),
            new Index(TerrainSurface.MESH_EDGE_LENGTH * 4, 40),
            new Index(TerrainSurface.MESH_EDGE_LENGTH * 5, 30)));
    private Logger logger = Logger.getLogger(Plateau.class.getName());

    public Plateau(Mesh mesh) {
        this.mesh = mesh;
    }

    public void sculpt() {
        final int slopeSize = SLOP_INDEX.size();
        int doubleSlopeSize = slopeSize * 2;
        double fractal = plateauConfigEntity != null ? plateauConfigEntity.getFractal() : 0;
        final FractalField fractalField = FractalField.createSaveFractalField(INDEX_RECT.getWidth() + doubleSlopeSize, INDEX_RECT.getHeight() + doubleSlopeSize, fractal, -fractal, 1.0);

        // Model slope
        final Collection<Index> slopeIndexes = new ArrayList<>();
        mesh.iterate(new Mesh.VertexVisitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                if (INDEX_RECT.contains2(new DecimalPosition(index))) {
                    mesh.setVertex(index, vertex.add(0, 0, PLANE_TOP_HEIGHT));
                } else {
                    double distance = INDEX_RECT.getNearestPointInclusive(new DecimalPosition(index)).getDistance(new DecimalPosition(index));
                    if (distance < slopeSize) {
                        mesh.getVertexDataSafe(index).addZValue(interpolateHeight(distance));
                        slopeIndexes.add(index);
                    }
                }
            }
        });

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

    private double interpolateHeight(double distance) {
        int x1 = (int) distance;
        if (x1 == distance) {
            return getSlopIndexY(x1);
        }
        int x2 = (int) Math.ceil(distance);
        double y1 = getSlopIndexY(x1);
        double y2 = getSlopIndexY(x2);
        return (y2 - y1) / (x2 - x1) * (distance - x1) + y1;
    }

    private double getSlopIndexY(int x) {
        if (x < 0) {
            return PLANE_TOP_HEIGHT;
        }
        if (x + 1 > SLOP_INDEX.size()) {
            return 0;
        }
        return SLOP_INDEX.get(x).getY();
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
