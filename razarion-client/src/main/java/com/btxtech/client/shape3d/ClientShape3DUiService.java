package com.btxtech.client.shape3d;

import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.Shape3DComposite;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.xhr.client.XMLHttpRequest;
import elemental2.core.Float32Array;
import elemental.js.util.Xhr;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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
        Xhr.get(CommonUrl.loadShape3dBufferUrl(), new Xhr.Callback() {
            @Override
            public void onFail(XMLHttpRequest xhr) {
                deferredStartup.failed("Calling Shape3D buffer failed: " + xhr.getStatus());
            }

            @Override
            public void onSuccess(XMLHttpRequest xhr) {
                try {
                    buffer.clear();
                    String responseText = xhr.getResponseText();
                    JsArray array = JsonUtils.safeEval(responseText);
                    for (int i = 0; i < array.length(); i++) {
                        JavaScriptObject jsonObject = array.get(i);
                        buffer.put(getKey(jsonObject), new Shape3DBuffer(getVertexData(jsonObject), getNormData(jsonObject), getTextureCoordinate(jsonObject)));
                    }
                    deferredStartup.finished();
                } catch (Throwable t) {
                    exceptionHandler.handleException(t);
                    deferredStartup.failed(t);
                }
            }
        });
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

    private native String getKey(JavaScriptObject jsonObject) /*-{
        return jsonObject.key;
    }-*/;

    private native Float32Array getVertexData(JavaScriptObject jsonObject) /*-{
        return new Float32Array(jsonObject.vertexData);
    }-*/;

    private native Float32Array getNormData(JavaScriptObject jsonObject) /*-{
        return new Float32Array(jsonObject.normData);
    }-*/;

    private native Float32Array getTextureCoordinate(JavaScriptObject jsonObject) /*-{
        return new Float32Array(jsonObject.textureCoordinate);
    }-*/;
}
