package com.btxtech.webglemulator.razarion.renderer;

import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.datatypes.Vertex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 14.03.2017.
 */
public class DevToolFloat32ArrayEmu extends Float32ArrayEmu {
    private List<Double> doubles;

    public DevToolFloat32ArrayEmu setBufferFromVertex(List<Vertex> vertices) {
        doubles = new ArrayList<>();
        for (Vertex vertex : vertices) {
            doubles.add(vertex.getX());
            doubles.add(vertex.getY());
            doubles.add(vertex.getZ());
        }
        return this;
    }

    public DevToolFloat32ArrayEmu setBufferDoubles(List<Double> doubles) {
        this.doubles = doubles;
        return this;
    }

    public DevToolFloat32ArrayEmu setBufferFloats(List<Float> floats) {
        this.doubles = new ArrayList<>();
        for (Float aFloat : floats) {
            doubles.add(aFloat.doubleValue());
        }
        return this;
    }

    public List<Double> getDoubles() {
        return doubles;
    }
}
