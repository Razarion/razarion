package com.btxtech.client.renderer.webgl;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.TextureIdHandler;
import com.btxtech.client.renderer.engine.WebGlPhongMaterial;
import com.btxtech.client.renderer.engine.WebGlSplatting;
import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.engine.shaderattribute.DecimalPositionShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.FloatShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.IntegerShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.ShaderTextureCoordinateAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.Vec2Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.VertexShaderAttribute;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.Vertex4;
import com.btxtech.shared.dto.GroundSplattingConfig;
import com.btxtech.shared.dto.PhongMaterialConfig;
import com.btxtech.shared.nativejs.NativeMatrix;
import com.btxtech.uiservice.VisualUiService;
import elemental2.webgl.WebGLRenderingContext;
import elemental2.webgl.WebGLTexture;
import elemental2.webgl.WebGLUniformLocation;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by Beat
 * 04.09.2015.
 */
@Dependent
public class WebGlFacade {
    // private Logger logger = Logger.getLogger(WebGlFacade.class.getName());
    // Attributes
    public static final String A_VERTEX_POSITION = "position";
    public static final String A_VERTEX_NORMAL = "objectNormal";
    public static final String A_VERTEX_UV = "uv";
    @Deprecated
    public static final String A_TEXTURE_COORDINATE = "aTextureCoord";
    // Uniform common
    public static final String U_COLOR = "uColor";
    // Uniform model matrix
    public static final String U_VIEW_MATRIX = "viewMatrix";
    public static final String U_PROJECTION_MATRIX = "projectionMatrix";
    public static final String U_VIEW_NORM_MATRIX = "normalMatrix";
    public static final String U_MODEL_MATRIX = "modelMatrix";
    public static final String U_MODEL_NORM_MATRIX = "modelMatrix???"; // ???
    public static final String U_SHADOW_MATRIX = "shadowMatrix";
    // Unifrom Editor
    public static final String U_CURSOR_TYPE = "uCursorType";

    // private Logger logger = Logger.getLogger(AbstractWebGlUnitRenderer.class.getName());
    private WebGlProgramFacade webGlProgram;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private VisualUiService visualUiService;
    @Inject
    private WebGlProgramService webGlProgramService;
    @Inject
    private WebGLTextureContainer textureContainer;
    private TextureIdHandler textureIdHandler = new TextureIdHandler();

    public void init(WebGlFacadeConfig webGlFacadeConfig, List<String> glslVertexDefines, List<String> glslFragmentDefines, boolean oESStandardDerivatives) {
        webGlProgram = webGlProgramService.getWebGlProgram(webGlFacadeConfig, glslVertexDefines, glslFragmentDefines, oESStandardDerivatives);
    }

    @Deprecated
    public void init(WebGlFacadeConfig webGlFacadeConfig) {
        webGlProgram = webGlProgramService.getWebGlProgram(webGlFacadeConfig, null, null, false);
    }

    public VertexShaderAttribute createVertexShaderAttribute(String attributeName) {
        return new VertexShaderAttribute(gameCanvas.getCtx3d(), webGlProgram, attributeName);
    }

    public Float32ArrayShaderAttribute createFloat32ArrayShaderAttribute(String attributeName) {
        return new Float32ArrayShaderAttribute(gameCanvas.getCtx3d(), webGlProgram, attributeName);
    }

    public Vec3Float32ArrayShaderAttribute createVec3Float32ArrayShaderAttribute(String attributeName) {
        return new Vec3Float32ArrayShaderAttribute(gameCanvas.getCtx3d(), webGlProgram, attributeName);
    }

    public Vec2Float32ArrayShaderAttribute createVec2Float32ArrayShaderAttribute(String attributeName) {
        return new Vec2Float32ArrayShaderAttribute(gameCanvas.getCtx3d(), webGlProgram, attributeName);
    }

    public DecimalPositionShaderAttribute createDecimalPositionShaderAttribute(String attributeName) {
        return new DecimalPositionShaderAttribute(gameCanvas.getCtx3d(), webGlProgram, attributeName);
    }

    public FloatShaderAttribute createFloatShaderAttribute(String attributeName) {
        return new FloatShaderAttribute(gameCanvas.getCtx3d(), webGlProgram, attributeName);
    }

    protected IntegerShaderAttribute createIntegerShaderAttribute(String attributeName) {
        return new IntegerShaderAttribute(gameCanvas.getCtx3d(), webGlProgram, attributeName);
    }

    public ShaderTextureCoordinateAttribute createShaderTextureCoordinateAttribute(String attributeName) {
        return new ShaderTextureCoordinateAttribute(gameCanvas.getCtx3d(), webGlProgram, attributeName);
    }

    protected double getAndEnableAttributeLocation(String attributeName) {
        return webGlProgram.getAttributeLocation(attributeName);
    }

    public WebGLUniformLocation getUniformLocation(String uniformName) {
        return webGlProgram.getUniformLocation(uniformName);
    }

    public WebGLUniformLocation getUniformLocationAlarm(String uniformName) {
        return webGlProgram.getUniformLocationAlarm(uniformName);
    }

    public void useProgram() {
        webGlProgram.useProgram();
    }

    @Deprecated
    // Do not use anymore -> slow. User: uniformMatrix4fv(String uniformName, Matrix4 matrix)
    public void uniformMatrix4fv(WebGLUniformLocation uniformLocation, Matrix4 matrix) {
        gameCanvas.getCtx3d().uniformMatrix4fv(uniformLocation, false, WebGlUtil.doublesToFloat32Array(matrix.toWebGlArray()));
        WebGlUtil.checkLastWebGlError("uniformMatrix4fv", gameCanvas.getCtx3d());
    }

    public void uniformMatrix4fv(WebGLUniformLocation uniformLocation, double[] data) {
        gameCanvas.getCtx3d().uniformMatrix4fv(uniformLocation, false, data);
        WebGlUtil.checkLastWebGlError("uniformMatrix4fv", gameCanvas.getCtx3d());
    }

    public void uniformMatrix4fv(WebGLUniformLocation uniformLocation, NativeMatrix matrix) {
        gameCanvas.getCtx3d().uniformMatrix4fv(uniformLocation, false, WebGlUtil.toFloat32Array(matrix));
        WebGlUtil.checkLastWebGlError("uniformMatrix4fv", gameCanvas.getCtx3d());
    }

    public void uniform3f(WebGLUniformLocation webGLUniformLocation, Vertex vertex) {
        if (webGLUniformLocation == null) {
            return;
        }
        gameCanvas.getCtx3d().uniform3f(webGLUniformLocation, (float) vertex.getX(), (float) vertex.getY(), (float) vertex.getZ());
        WebGlUtil.checkLastWebGlError("uniform3f", gameCanvas.getCtx3d());
    }

    public void uniform3fNoAlpha(WebGLUniformLocation uniformLocation, Color color) {
        if (uniformLocation == null) {
            return;
        }
        gameCanvas.getCtx3d().uniform3f(uniformLocation, (float) color.getR(), (float) color.getG(), (float) color.getB());
        WebGlUtil.checkLastWebGlError("uniform3f", gameCanvas.getCtx3d());
    }

    public void uniform1f(WebGLUniformLocation uniformLocation, double value) {
        gameCanvas.getCtx3d().uniform1f(uniformLocation, (float) value);
        WebGlUtil.checkLastWebGlError("uniform1f", gameCanvas.getCtx3d());
    }

    public void uniform1i(WebGLUniformLocation uniformLocation, int value) {
        gameCanvas.getCtx3d().uniform1i(uniformLocation, value);
        WebGlUtil.checkLastWebGlError("uniform1i", gameCanvas.getCtx3d());
    }

    public void uniform1b(WebGLUniformLocation uniformLocation, boolean value) {
        gameCanvas.getCtx3d().uniform1i(uniformLocation, value ? 1 : 0);
        WebGlUtil.checkLastWebGlError("uniform1b", gameCanvas.getCtx3d());
    }

    public void uniform4f(WebGLUniformLocation uniformLocation, double x, double y, double z, double w) {
        gameCanvas.getCtx3d().uniform4f(uniformLocation, (float) x, (float) y, (float) z, (float) w);
        WebGlUtil.checkLastWebGlError("uniform3f", gameCanvas.getCtx3d());
    }

    public void uniform4f(WebGLUniformLocation uniformLocation, Color color) {
        uniform4f(uniformLocation, color.getR(), color.getG(), color.getB(), color.getA());
    }

    public void uniform4f(WebGLUniformLocation uniformLocation, Vertex4 vertex4) {
        uniform4f(uniformLocation, vertex4.getX(), vertex4.getY(), vertex4.getZ(), vertex4.getW());
    }

    public WebGlUniformTexture createWebGLTexture(String uniformName, Supplier<WebGLTexture> webGLTextureSupplier) {
        return new WebGlUniformTexture(gameCanvas.getCtx3d(),
                this,
                uniformName,
                textureIdHandler.create(),
                webGLTextureSupplier);
    }

    public WebGlUniformTexture createWebGLTexture(int imageId, String uniformName) {
        WebGLTexture webGLTexture = textureContainer.getTexture(imageId);
        return createWebGLTexture(uniformName, () -> webGLTexture);
    }

    public WebGlUniformTexture createTerrainMarkerWebGLTexture(String uniformName) {
        WebGLTexture webGLTexture = textureContainer.getTerrainMarkerTexture();
        return createWebGLTexture(uniformName, () -> webGLTexture);
    }

    public WebGlUniformTexture createFakeWebGLTexture(String uniformName) {
        WebGLTexture webGLTexture = textureContainer.getFakeTexture();
        return createWebGLTexture(uniformName, () -> webGLTexture);
    }

    public WebGlPhongMaterial createPhongMaterial(PhongMaterialConfig phongMaterialConfig, String variableName) {
        if (phongMaterialConfig != null) {
            return new WebGlPhongMaterial(this, phongMaterialConfig, variableName);
        } else {
            return null;
        }
    }

    public WebGlSplatting createSplatting(GroundSplattingConfig splatting, String variableName) {
        return new WebGlSplatting(this, splatting, variableName);
    }


    public WebGlUniformTexture createSaveWebGLTexture(Integer imageId, String samplerUniformName) {
        if (imageId != null) {
            return createWebGLTexture(imageId, samplerUniformName);
        } else {
            return createFakeWebGLTexture(samplerUniformName);
        }
    }

    public TextureIdHandler.WebGlTextureId createWebGlTextureId() {
        return textureIdHandler.create();
    }

    public WebGLRenderingContext getCtx3d() {
        return gameCanvas.getCtx3d();
    }

    @Deprecated
    public void drawArrays(double mode) {
    }

    public void drawArrays(double mode, int elementCount, String helperString) {
        getCtx3d().drawArrays(mode, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays for " + helperString, getCtx3d());
    }

    public void enableOESStandardDerivatives() {
        Object extension = getCtx3d().getExtension("OES_standard_derivatives");
        if (extension == null) {
            throw new WebGlException("OES_standard_derivatives is no supported");
        }
    }

    public VisualUiService getVisualUiService() {
        return visualUiService;
    }
}
