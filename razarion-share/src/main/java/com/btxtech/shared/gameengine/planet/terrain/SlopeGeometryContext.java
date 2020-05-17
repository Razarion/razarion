package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.terrain.container.SlopeGeometry;
import com.btxtech.shared.system.JsInteropObjectFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Dependent
public class SlopeGeometryContext {
    @Inject
    private JsInteropObjectFactory jsInteropObjectFactory;
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
        slopeGeometry.setPositions(jsInteropObjectFactory.newFloat32Array4Vertices(positions));
        slopeGeometry.setNorms(jsInteropObjectFactory.newFloat32Array4Vertices(norms));
        slopeGeometry.setUvs(jsInteropObjectFactory.newFloat32Array4DecimalPositions(uvs));
        slopeGeometry.setSlopeFactors(jsInteropObjectFactory.newFloat32Array4Doubles(slopeFactors));
        return slopeGeometry;
    }
}
