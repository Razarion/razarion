package com.btxtech.client.editor.terrain.renderer;

import com.btxtech.client.editor.terrain.ModifiedSlope;
import com.btxtech.client.renderer.engine.shaderattribute.VertexShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import elemental2.webgl.WebGLRenderingContext;
import elemental2.webgl.WebGLUniformLocation;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 04.05.2016.
 */
@ColorBufferRenderer
@Dependent
public class TerrainEditorSlopeDrivewayRenderUnit extends AbstractRenderUnit<ModifiedSlope> {
    private static final Color COLOR_HOVER = new Color(1.0, 1.0, 0.5, 1.0);
    private static final Color COLOR_SLOPE_HOVER = new Color(0.5, 0.0, 1.0, 1.0);
    private static final Color COLOR_NORMAL = new Color(0.5, 1.0, 0.0, 1.0);

    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private WebGlFacade webGlFacade;
    private VertexShaderAttribute vertices;
    private ModifiedSlope modifiedSlope;
    private WebGLUniformLocation uColor;

    @Override
    public void init() {
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.terrainEditorVertexShader(), Shaders.INSTANCE.terrainEditorFragmentShader()).enableTransformation(false));
        vertices = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        uColor = webGlFacade.getUniformLocation("uColor");
    }

    @Override
    public void setupImages() {
        // Ignore
    }

    public void update() {
        fillBuffers();
    }

    @Override
    public void fillBuffers(ModifiedSlope modifiedSlope) {
        this.modifiedSlope = modifiedSlope;
        fillBuffers();
    }

    public void fillBuffers() {
        List<Vertex> corners = new ArrayList<>();
        for (int i = 0; i < modifiedSlope.getPolygon().getCorners().size(); i++) {
            DecimalPosition current = modifiedSlope.getPolygon().getCorners().get(i);
            DecimalPosition next = CollectionUtils.getCorrectedElement(i + 1, modifiedSlope.getPolygon().getCorners());
            if(modifiedSlope.isPositionInDriveway(current) && (modifiedSlope.isPositionInDriveway(next))) {
                corners.add(new Vertex(current, 0));
                corners.add(new Vertex(next, 0));
            }
        }
        vertices.fillBuffer(corners);
        setElementCount(corners.size());
    }

    @Override
    protected void prepareDraw() {

    }

    @Override
    public void draw(ModelMatrices modelMatrices) {
        webGlFacade.useProgram();

        if (modifiedSlope.isHover()) {
            webGlFacade.uniform4f(uColor, COLOR_SLOPE_HOVER);
        } else {
            webGlFacade.uniform4f(uColor, COLOR_NORMAL);
        }

        vertices.activate();

        webGlFacade.drawArrays(WebGLRenderingContext.LINES);
    }
}
