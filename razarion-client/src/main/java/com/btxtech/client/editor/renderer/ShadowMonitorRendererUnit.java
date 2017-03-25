package com.btxtech.client.editor.renderer;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.ClientRenderServiceImpl;
import com.btxtech.client.renderer.engine.shaderattribute.ShaderTextureCoordinateAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.VertexShaderAttribute;
import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.TextureCoordinate;
import com.btxtech.shared.datatypes.Triangle;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.VertexList;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 11.09.2015.
 */
@ColorBufferRenderer
@Dependent
public class ShadowMonitorRendererUnit extends AbstractRenderUnit<Void> {
    private static final int SIDE_LENGTH = 256;
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private ClientRenderServiceImpl renderService;
    @Inject
    private MonitorRenderTask monitorRenderTask;
    private VertexShaderAttribute positions;
    private ShaderTextureCoordinateAttribute textureCoordinates;
    private WebGlUniformTexture textureColor;
    private WebGlUniformTexture textureDepth;

    @PostConstruct
    public void init() {
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.monitorVertexShader(), Shaders.INSTANCE.monitorFragmentShader()));
        positions = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        textureCoordinates = webGlFacade.createShaderTextureCoordinateAttribute(WebGlFacade.A_TEXTURE_COORDINATE);

        textureColor = webGlFacade.createEmptyWebGLTexture("uColorSampler");
        textureDepth = webGlFacade.createEmptyWebGLTexture("uDeepSampler");
    }

    @Override
    public void setupImages() {

    }

    @Override
    public void fillBuffers(Void aVoid) {
        VertexList vertexList = new VertexList();
        double monitorWidth = 2.0 * SIDE_LENGTH / (double) gameCanvas.getWidth();
        double monitorHeight = 2.0 * SIDE_LENGTH / (double) gameCanvas.getHeight();
        Triangle triangle = new Triangle(new Vertex(0, 0, 0), new TextureCoordinate(0, 0),
                new Vertex(monitorWidth, 0, 0), new TextureCoordinate(1, 0),
                new Vertex(0, monitorHeight, 0), new TextureCoordinate(0, 1));
        vertexList.add(triangle);
        triangle = new Triangle(new Vertex(monitorWidth, monitorHeight, 0), new TextureCoordinate(1, 1),
                new Vertex(0, monitorHeight, 0), new TextureCoordinate(0, 1),
                new Vertex(monitorWidth, 0, 0), new TextureCoordinate(1, 0));
        vertexList.add(triangle);

        positions.fillBuffer(vertexList.getVertices());
        textureCoordinates.fillBuffer(vertexList.getTextureCoordinates());

        setElementCount(vertexList);
    }

    @Override
    protected void prepareDraw() {

    }

    @Override
    public void draw(ModelMatrices modelMatrices) {
        webGlFacade.useProgram();

        webGlFacade.uniform1b("uDeepMap", monitorRenderTask.isShowDeep());

        positions.activate();
        textureCoordinates.activate();


        textureColor.setWebGLTexture(renderService.getColorTexture());
        textureColor.activate();
        textureDepth.setWebGLTexture(renderService.getDepthTexture());
        textureDepth.activate();

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }
}
