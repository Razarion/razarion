package com.btxtech.client.editor.renderer;

import com.btxtech.client.renderer.engine.shaderattribute.VertexShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Triangulator;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLUniformLocation;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 11.09.2015.
 */
@ColorBufferRenderer
@Dependent
public class TerrainMarkerRendererUnit extends AbstractRenderUnit<List<Vertex>> {
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private MonitorRenderTask monitorRenderTask;
    private VertexShaderAttribute positions;
    private WebGLUniformLocation colorUniformLocation;

    @PostConstruct
    public void init() {
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.rgbaVpVertexShader(), Shaders.INSTANCE.rgbaVpFragmentShader()).enableTransformation(false));
        positions = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        colorUniformLocation = webGlFacade.getUniformLocation(WebGlFacade.U_COLOR);
    }

    @Override
    public void setupImages() {

    }

    @Override
    public void fillBuffers(List<Vertex> polygon) {
        List<Vertex> vertices = new ArrayList<>();

        Triangulator.calculate(polygon, (vertex1, vertex2, vertex3) -> {
            vertices.add(vertex1);
            vertices.add(vertex2);
            vertices.add(vertex3);
        });

        positions.fillBuffer(vertices);
        setElementCount(vertices);
    }

    @Override
    protected void prepareDraw() {

    }

    @Override
    public void draw(ModelMatrices modelMatrices) {
        webGlFacade.useProgram();

        webGlFacade.uniform4f(colorUniformLocation, new Color(0, 1, 0, 0.5));

        positions.activate();

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }
}
