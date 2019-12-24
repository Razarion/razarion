package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.terrain.container.SlopeGeometry;
import com.btxtech.shared.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class SlopeGeometryContext {
    private List<Vertex> positions = new ArrayList<>();
    private List<Vertex> norms = new ArrayList<>();
    private List<DecimalPosition> uvs = new ArrayList<>();
    private List<Double> slopeFactors = new ArrayList<>();

    public void addTriangleCorner(Vertex vertex, Vertex norm, DecimalPosition uv, double slopeFactor) {
        positions.add(vertex);
        norms.add(norm);
        uvs.add(uv);
        slopeFactors.add(slopeFactor);
    }

    public boolean isEmpty() {
        return positions.isEmpty();
    }

    public SlopeGeometry generate() {
        SlopeGeometry slopeGeometry = new SlopeGeometry();
        slopeGeometry.setPositions(Vertex.toArray(positions));
        slopeGeometry.setNorms(Vertex.toArray(norms));
        slopeGeometry.setUvs(DecimalPosition.toArray(uvs));
        slopeGeometry.setSlopeFactors(CollectionUtils.toArray(slopeFactors));
        return slopeGeometry;
    }
}
