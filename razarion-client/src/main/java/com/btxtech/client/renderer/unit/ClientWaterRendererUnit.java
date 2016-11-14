package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.VertexShaderAttribute;
import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.dto.VisualConfig;
import com.btxtech.shared.gameengine.planet.terrain.Water;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.task.water.AbstractWaterRendererUnit;
import com.btxtech.uiservice.terrain.TerrainUiService;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.09.2015.
 */
@ColorBufferRenderer
@Dependent
public class ClientWaterRendererUnit extends AbstractWaterRendererUnit {
    // private Logger logger = Logger.getLogger(ClientWaterRendererUnit.class.getName());
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private VisualUiService visualUiService;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private WebGlFacade webGlFacade;
    private VertexShaderAttribute positions;
    private VertexShaderAttribute norms;
    private VertexShaderAttribute tangents;
    private WebGlUniformTexture bumpMap;

    @PostConstruct
    public void init() {
        webGlFacade.setAbstractRenderUnit(this);
        webGlFacade.createProgram(Shaders.INSTANCE.waterVertexShader(), Shaders.INSTANCE.waterFragmentShader());
        positions = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        norms = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_NORMAL);
        tangents = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_TANGENT);
    }

    @Override
    public void setupImages() {
    }

    @Override
    protected void fillInternalBuffers(Water water, VisualConfig visualConfig) {
        positions.fillBuffer(water.getVertices());
        norms.fillBuffer(water.getNorms());
        tangents.fillBuffer(water.getTangents());
        bumpMap = webGlFacade.createWebGLBumpMapTexture(visualConfig.getWaterBmId(), "uBm", "uBmScale", visualConfig.getWaterBmScale(), "uBmOnePixel");
    }

    @Override
    public void draw(ModelMatrices modelMatrices) {
        webGlFacade.useProgram();

        webGlFacade.setLightUniforms(null, visualUiService.getVisualConfig().getWaterLightConfig());

        webGlFacade.uniformMatrix4fv(WebGlFacade.U_PERSPECTIVE_MATRIX, projectionTransformation.getMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_MATRIX, camera.getMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_MODEL_NORM_MATRIX, camera.getNormMatrix());
        webGlFacade.uniform1f("uTransparency", visualUiService.getVisualConfig().getWaterTransparency());
        webGlFacade.uniform1f("uBmDepth", visualUiService.getVisualConfig().getWaterBmDepth());
        webGlFacade.uniform1f("animation", terrainUiService.getWaterAnimation());
        webGlFacade.uniform1f("animation2", terrainUiService.getWaterAnimation2());

        positions.activate();
        norms.activate();
        tangents.activate();

        bumpMap.activate();

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }
}
