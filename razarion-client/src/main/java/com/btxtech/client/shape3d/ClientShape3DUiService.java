package com.btxtech.client.shape3d;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import elemental2.core.Float32Array;
import elemental2.dom.XMLHttpRequest;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMapOfAny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 07.03.2017.
 */
@ApplicationScoped
@Deprecated // Use ThreeJsModel
public class ClientShape3DUiService extends Shape3DUiService {
    // private Logger logger = Logger.getLogger(ClientShape3DUiService.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    private final Map<String, Shape3DBuffer> buffer = new HashMap<>();

    public Float32Array getVertexFloat32Array(VertexContainer vertexContainer) {
        return getShape3DBuffer(vertexContainer).getVertex();
    }

    public Float32Array getNormFloat32Array(VertexContainer vertexContainer) {
        return getShape3DBuffer(vertexContainer).getNorm();
    }

    public Float32Array getTextureCoordinateFloat32Array(VertexContainer vertexContainer) {
        return getShape3DBuffer(vertexContainer).getTextureCoordinate();
    }

    public Float32Array getVertexColorFloat32Array(VertexContainer vertexContainer) {
        return getShape3DBuffer(vertexContainer).getVertexColor();
    }

    private Shape3DBuffer getShape3DBuffer(VertexContainer vertexContainer) {
        Shape3DBuffer shape3DBuffer = buffer.get(vertexContainer.getKey());
        if (shape3DBuffer == null) {
            throw new IllegalArgumentException("No Shape3DBuffer for key: " + vertexContainer.getKey());
        }
        return shape3DBuffer;
    }

    public void editorOverrideShape3DBuffer(Map<String, Shape3DBuffer> shape3DBuffers) {
        buffer.putAll(shape3DBuffers);
    }

    public void loadBuffer(DeferredStartup deferredStartup) {
        XMLHttpRequest xmlHttpRequest = new XMLHttpRequest();
        xmlHttpRequest.onload = progressEvent -> {
            try {
                if (xmlHttpRequest.readyState == 4 && xmlHttpRequest.status == 200) {
                    buffer.clear();
                    JsPropertyMapOfAny[] vertexContainerBuffers = Js.cast(xmlHttpRequest.response);
                    if (vertexContainerBuffers == null) {
                        deferredStartup.failed("Shape3DEditorController no buffer returned");
                        return;
                    }
                    Arrays.stream(vertexContainerBuffers).forEach(vertexContainerBuffer -> {
                        // GWT compiler issue. Can not be inlined. Must be double[] explicit declaration.
                        double[] vertexData = Js.uncheckedCast(vertexContainerBuffer.get("vertexData"));
                        double[] normData = Js.uncheckedCast(vertexContainerBuffer.get("normData"));
                        double[] textureCoordinate = Js.uncheckedCast(vertexContainerBuffer.get("textureCoordinate"));
                        double[] vertexColor = Js.uncheckedCast(vertexContainerBuffer.get("vertexColor"));
                        Float32Array vertexColorArray = null;
                        if (vertexColor != null) {
                            vertexColorArray = new Float32Array(vertexColor);
                        }
                        buffer.put(
                                vertexContainerBuffer.getAny("key").asString(),
                                new Shape3DBuffer(
                                        new Float32Array(vertexData),
                                        new Float32Array(normData),
                                        new Float32Array(textureCoordinate),
                                        vertexColorArray));
                    });
                    deferredStartup.finished();
                } else {
                    deferredStartup.failed("Shape3DEditorController onload error. Status: '" + xmlHttpRequest.status + "' StatusText: '" + xmlHttpRequest.statusText + "'");
                }
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
                deferredStartup.failed(t);
            }
        };
        xmlHttpRequest.addEventListener("error", evt -> {
            try {
                deferredStartup.failed("Shape3DEditorController error. Status: '" + xmlHttpRequest.status + "' StatusText: '" + xmlHttpRequest.statusText + "'");
            } catch (Throwable throwable) {
                exceptionHandler.handleException(throwable);
                deferredStartup.failed(throwable);
            }
        });
        xmlHttpRequest.onabort = progressEvent -> {
            try {
                deferredStartup.failed("Shape3DEditorController abort. Status: '" + xmlHttpRequest.status + "' StatusText: '" + xmlHttpRequest.statusText + "'");
            } catch (Throwable throwable) {
                exceptionHandler.handleException(throwable);
                deferredStartup.failed(throwable);
            }
        };
        xmlHttpRequest.open("GET", CommonUrl.loadShape3dBufferUrl());
        xmlHttpRequest.responseType = "json";
        xmlHttpRequest.send();
    }

    @Override
    public double getMaxZ(VertexContainer vertexContainer) {
        double maxZ = Double.MIN_VALUE;

//        Float32Array vertices = getShape3DBuffer(vertexContainer).getVertex();
//        Matrix4 matrix = vertexContainer.getShapeTransform().setupMatrix();
//
//        for (int i = 0; i < vertices.length; i += 3) {
//            Vertex vertex = new Vertex(vertices.getAt(i), vertices.getAt(i + 1), vertices.getAt(i + 2));
//            maxZ = Math.max(matrix.multiply(vertex, 1.0).getZ(), maxZ);
//        }

        return maxZ;
    }
}
