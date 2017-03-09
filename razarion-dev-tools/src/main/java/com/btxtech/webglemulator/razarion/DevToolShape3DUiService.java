package com.btxtech.webglemulator.razarion;

import com.btxtech.persistence.JsonProviderEmulator;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
import com.btxtech.uiservice.Shape3DUiService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 08.03.2017.
 */
@ApplicationScoped
public class DevToolShape3DUiService extends Shape3DUiService {
    @Inject
    private JsonProviderEmulator jsonProviderEmulator;
    private Map<String, VertexContainerBuffer> buffer = new HashMap<>();

    public List<Double> getVertexArray(VertexContainer vertexContainer) {
        return floats2Doubles(getVertexContainerBuffer(vertexContainer).getVertexData());
    }

    public List<Double> getNormFloat32Array(VertexContainer vertexContainer) {
        return floats2Doubles(getVertexContainerBuffer(vertexContainer).getNormData());
    }

    public void loadBuffer() {
        buffer.clear();
        for (VertexContainerBuffer vertexContainerBuffer : jsonProviderEmulator.readVertexContainerBuffersFromFile()) {
            buffer.put(vertexContainerBuffer.getKey(), vertexContainerBuffer);
        }
    }

    private VertexContainerBuffer getVertexContainerBuffer(VertexContainer vertexContainer) {
        VertexContainerBuffer vertexContainerBuffer = buffer.get(vertexContainer.getKey());
        if (vertexContainerBuffer == null) {
            throw new IllegalArgumentException("No VertexContainerBuffer for key: " + vertexContainer.getKey());
        }
        return vertexContainerBuffer;
    }

    private List<Double> floats2Doubles(List<Float> floats) {
        List<Double> doubles = new ArrayList<>();
        for (Float floatValue : floats) {
            doubles.add(floatValue.doubleValue());
        }
        return doubles;
    }

    @Override
    public double getMaxZ(VertexContainer vertexContainer) {
        return 1.0; // Not used in DevTool
    }
}
