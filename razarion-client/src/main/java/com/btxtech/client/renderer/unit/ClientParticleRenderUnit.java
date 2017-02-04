package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.VertexShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.task.particle.AbstractParticleRenderUnit;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 03.02.2017.
 */
@ColorBufferRenderer
@Dependent
public class ClientParticleRenderUnit extends AbstractParticleRenderUnit {
    // private Logger logger = Logger.getLogger(ClientParticleRenderUnit.class.getName());
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    private VertexShaderAttribute positions;
    private VertexShaderAttribute vertexFadeouts;

    @PostConstruct
    public void init() {
        webGlFacade.setAbstractRenderUnit(this);
        webGlFacade.createProgram(Shaders.INSTANCE.particleVertexShader(), Shaders.INSTANCE.particleFragmentShader());
        positions = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        vertexFadeouts = webGlFacade.createVertexShaderAttribute("aVertexFadeout");
    }

    @Override
    protected void fillBuffers(List<Vertex> vertices, List<Vertex> vertexFadeouts) {
        positions.fillBuffer(vertices);
        this.vertexFadeouts.fillBuffer(vertexFadeouts);
    }

    @Override
    protected void prepareDraw() {
        webGlFacade.useProgram();

        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_MATRIX, camera.getMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_PERSPECTIVE_MATRIX, projectionTransformation.getMatrix());

        positions.activate();
        vertexFadeouts.activate();
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_MODEL_MATRIX, modelMatrices.getModel());
        webGlFacade.uniform1f("uProgress", modelMatrices.getProgress());

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }
}
