package com.btxtech.uiservice.datatypes;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Vertex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 12.09.2017.
 */
public interface InGameItemVisualization {
    List<ModelMatrices> provideCornerModelMatrices(long timeStamp);

    List<ModelMatrices> provideOutOfViewShape3DModelMatrices();

    Integer getOutOfViewShape3DId();

    List<Vertex> getCornerVertices();

    Color getCornerColor();


    default void preRender() {

    }

    default Integer getShape3DId() {
        return null;
    }

    default List<ModelMatrices> provideShape3DModelMatrices() {
        return null;
    }

    default List<Vertex> setupCornerVertices(double cornerLength) {
        List<Vertex> cornerVertices = new ArrayList<>();
        // Leg 1
        cornerVertices.add(new Vertex(-cornerLength, 0, 0));
        cornerVertices.add(new Vertex(0, 0, 0));
        // Leg 2
        cornerVertices.add(new Vertex(0, 0, 0));
        cornerVertices.add(new Vertex(0, cornerLength, 0));

        return cornerVertices;
    }

}
