package com.btxtech.server.persistence.asset;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.shape.ShapeTransform;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.Embeddable;
import javax.persistence.Lob;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Embeddable
public class ShapeTransformEmbeddable {
    @Lob
    private String shapeTransformMatrixJson;

    public ShapeTransform toShapeTransform() {
        try {
            List<Double> list = new ObjectMapper().readValue(shapeTransformMatrixJson, new TypeReference<List<Double>>() {

            });
            return new ShapeTransform()
                    .setStaticMatrix(
                            Matrix4.fromColumnMajorOrder(
                                    list.stream().mapToDouble(value -> value).toArray()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void fromShapeTransform(ShapeTransform shapeTransform) {
        try {
            shapeTransformMatrixJson = new ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(Arrays.stream(shapeTransform.getStaticMatrix().toWebGlArray())
                            .boxed()
                            .collect(Collectors.toList()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
