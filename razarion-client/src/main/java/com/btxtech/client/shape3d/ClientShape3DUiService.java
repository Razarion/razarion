package com.btxtech.client.shape3d;

import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.Shape3DComposite;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
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
public class ClientShape3DUiService extends Shape3DUiService {
    // private Logger logger = Logger.getLogger(ClientShape3DUiService.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    private Map<String, Shape3DBuffer> buffer = new HashMap<>();


    public Float32Array getVertexFloat32Array(VertexContainer vertexContainer) {
        return getShape3DBuffer(vertexContainer).getVertex();
    }

    public Float32Array getNormFloat32Array(VertexContainer vertexContainer) {
        return getShape3DBuffer(vertexContainer).getNorm();
    }

    public Float32Array getTextureCoordinateFloat32Array(VertexContainer vertexContainer) {
        return getShape3DBuffer(vertexContainer).getTextureCoordinate();
    }

    private Shape3DBuffer getShape3DBuffer(VertexContainer vertexContainer) {
        Shape3DBuffer shape3DBuffer = buffer.get(vertexContainer.getKey());
        if (shape3DBuffer == null) {
            throw new IllegalArgumentException("No Shape3DBuffer for key: " + vertexContainer.getKey());
        }
        return shape3DBuffer;
    }

    public void override(Shape3DComposite shape3DComposite) {
        override(shape3DComposite.getShape3D());
        for (VertexContainerBuffer vertexContainerBuffer : shape3DComposite.getVertexContainerBuffers()) {
            buffer.put(vertexContainerBuffer.getKey(), new Shape3DBuffer(WebGlUtil.createArrayBufferOfFloat32(vertexContainerBuffer.getVertexData()),
                    WebGlUtil.createArrayBufferOfFloat32(vertexContainerBuffer.getNormData()),
                    WebGlUtil.createArrayBufferOfFloat32(vertexContainerBuffer.getTextureCoordinate())));
        }
    }

    public void loadBuffer(DeferredStartup deferredStartup) {
        XMLHttpRequest xmlHttpRequest = new XMLHttpRequest();
        xmlHttpRequest.onload = progressEvent -> {
            try {
                if (xmlHttpRequest.readyState == 4 && xmlHttpRequest.status == 200) {
                    buffer.clear();
                    JsPropertyMapOfAny[] vertexContainerBuffers = Js.cast(xmlHttpRequest.response);
                    Arrays.stream(vertexContainerBuffers).forEach(vertexContainerBuffer ->
                            buffer.put(
                                    Js.cast(vertexContainerBuffer.get("key")),
                                    new Shape3DBuffer(
                                            new Float32Array((double[]) Js.cast(vertexContainerBuffer.get("vertexData"))),
                                            new Float32Array((double[]) Js.cast(vertexContainerBuffer.get("normData"))),
                                            new Float32Array((double[]) Js.cast(vertexContainerBuffer.get("textureCoordinate"))))));
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

        Float32Array vertices = getShape3DBuffer(vertexContainer).getVertex();
        Matrix4 matrix = vertexContainer.getShapeTransform().setupMatrix();

        for (int i = 0; i < vertices.length; i += 3) {
            Vertex vertex = new Vertex(vertices.getAt(i), vertices.getAt(i + 1), vertices.getAt(i + 2));
            maxZ = Math.max(matrix.multiply(vertex, 1.0).getZ(), maxZ);
        }

        return maxZ;
    }
}
