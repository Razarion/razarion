package com.btxtech.client.renderer.subtask;

import com.btxtech.client.renderer.engine.Activator;
import com.btxtech.client.renderer.engine.ClientRenderServiceImpl;
import com.btxtech.client.renderer.engine.LightUniforms;
import com.btxtech.client.renderer.engine.TextureIdHandler;
import com.btxtech.client.renderer.engine.UniformLocation;
import com.btxtech.client.renderer.engine.WebGlGroundMaterial;
import com.btxtech.client.renderer.engine.WebGlPhongMaterial;
import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.engine.shaderattribute.AbstractShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.DecimalPositionShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.Vec2Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.VertexShaderAttribute;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.ShapeTransform;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.PhongMaterialConfig;
import com.btxtech.shared.nativejs.NativeMatrix;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.ProgressAnimation;
import com.btxtech.uiservice.renderer.RenderService;
import com.btxtech.uiservice.renderer.ViewService;
import com.btxtech.uiservice.renderer.WebGlRenderTask;
import elemental2.core.Float32Array;
import elemental2.webgl.WebGLRenderingContext;
import elemental2.webgl.WebGLTexture;
import elemental2.webgl.WebGLUniformLocation;
import jsinterop.base.Js;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.btxtech.client.renderer.shaders.SkeletonDefines.MODEL_MATRIX;
import static elemental2.webgl.WebGLRenderingContext.SAMPLE_ALPHA_TO_COVERAGE;

public abstract class AbstractWebGlRenderTask<T> implements WebGlRenderTask<T> {
    // private Logger logger = Logger.getLogger(AbstractWebGlRenderTask.class.getName());
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private ClientRenderServiceImpl renderService;
    @Inject
    private ViewService viewService;
    @Inject
    private VisualUiService visualUiService;
    @Inject
    private NativeMatrixFactory nativeMatrixFactory;
    private WebGlFacadeConfig webGlFacadeConfig;
    private NativeMatrix staticShapeTransformCache;

    private Collection<AbstractShaderAttribute> arrays = new ArrayList<>();
    private int elementCount;
    private boolean active = true;
    private Function<Long, List<ModelMatrices>> modelMatricesSupplier;
    private LightUniforms lightUniforms;
    private Collection<WebGlPhongMaterial> materials = new ArrayList<>();
    private Collection<WebGlGroundMaterial> webGlGroundMaterials = new ArrayList<>();
    private Collection<UniformLocation> uniforms = new ArrayList<>();
    private Collection<UniformLocation> modelMatrixUniforms = new ArrayList<>();
    private Collection<Activator> activators = new ArrayList<>();
    private Collection<WebGlUniformTexture> uniformTextures = new ArrayList<>();
    // Transformation
    private WebGLUniformLocation viewMatrixUniformLocation;
    private WebGLUniformLocation viewNormMatrixUniformLocation;
    private WebGLUniformLocation perspectiveMatrixUniformLocation;
    private WebGLUniformLocation receiveShadowMatrixUniformLocation;
    private WebGLUniformLocation modelMatrixUniformLocation;
    private WebGLUniformLocation modelNormMatrixUniformLocation;
    // Shadow lookup
    private TextureIdHandler.WebGlTextureId shadowWebGlTextureId;
    private WebGLUniformLocation uShadowAlpha;
    private WebGLUniformLocation uShadowTexture;
    private ShapeTransform shapeTransform;
    private Collection<ProgressAnimation> progressAnimations;

    protected abstract WebGlFacadeConfig getWebGlFacadeConfig(T t);

    protected abstract void setup(T t);

    public final void init(T t) {
        webGlFacadeConfig = getWebGlFacadeConfig(t);
        if (webGlFacadeConfig.isOESStandardDerivatives()) {
            webGlFacade.enableOESStandardDerivatives();
        }
        webGlFacade.init(webGlFacadeConfig, glslVertexDefines(t), glslFragmentDefines(t), webGlFacadeConfig.isOESStandardDerivatives());
        setupTransformation();
        setupReceiveShadow();
        if (webGlFacadeConfig.isLight()) {
            lightUniforms = new LightUniforms(webGlFacade);
        }
        setup(t);
    }

    private List<String> glslVertexDefines(T t) {
        List<String> vertexDefines = new ArrayList<>();
        if (modelMatricesSupplier != null) {
            vertexDefines.add(MODEL_MATRIX);
        }
        glslVertexCustomDefines(vertexDefines, t);
        return vertexDefines;
    }

    private List<String> glslFragmentDefines(T t) {
        List<String> fragmentDefines = new ArrayList<>();
        if (modelMatricesSupplier != null) {
            fragmentDefines.add(MODEL_MATRIX);
        }
        if (webGlFacadeConfig.isReceiveShadow()) {
            fragmentDefines.add("RECEIVE_SHADOW");
        }
        glslFragmentCustomDefines(fragmentDefines, t);
        return fragmentDefines;
    }

    protected WebGlFacade getWebGlFacade() {
        return webGlFacade;
    }

    @Override
    public void setModelMatricesSupplier(Function<Long, List<ModelMatrices>> modelMatricesSupplier) {
        this.modelMatricesSupplier = modelMatricesSupplier;
    }

    protected void setupTransformation() {
        if (webGlFacadeConfig.isTransformation()) {
            viewMatrixUniformLocation = webGlFacade.getUniformLocation(WebGlFacade.U_VIEW_MATRIX);
            perspectiveMatrixUniformLocation = webGlFacade.getUniformLocation(WebGlFacade.U_PROJECTION_MATRIX);
            if (webGlFacadeConfig.isNormTransformation()) {
                viewNormMatrixUniformLocation = webGlFacade.getUniformLocation(WebGlFacade.U_VIEW_NORM_MATRIX);
            }
        }
        if (modelMatricesSupplier != null) {
            modelMatrixUniformLocation = webGlFacade.getUniformLocation(WebGlFacade.U_MODEL_MATRIX);
            if (webGlFacadeConfig.isNormTransformation()) {
                modelNormMatrixUniformLocation = webGlFacade.getUniformLocation(WebGlFacade.U_MODEL_NORM_MATRIX);
            }
        }
    }

    protected void setupReceiveShadow() {
        if (webGlFacadeConfig.isReceiveShadow()) {
            receiveShadowMatrixUniformLocation = webGlFacade.getUniformLocation(WebGlFacade.U_SHADOW_MATRIX);
            shadowWebGlTextureId = webGlFacade.createWebGlTextureId();
            uShadowAlpha = webGlFacade.getUniformLocation("uShadowAlpha");
            uShadowTexture = webGlFacade.getUniformLocation("uDepthTexture");
        }
    }

    protected void setupVec3Array(String name, Float32ArrayEmu float32ArrayEmu) {
        Vec3Float32ArrayShaderAttribute array = webGlFacade.createVec3Float32ArrayShaderAttribute(name);
        array.fillFloat32Array(Js.uncheckedCast(float32ArrayEmu));
        arrays.add(array);
    }

    protected void setupVec3Array_(String name, Float32Array float32Array) {
        Vec3Float32ArrayShaderAttribute array = webGlFacade.createVec3Float32ArrayShaderAttribute(name);
        array.fillFloat32Array(float32Array);
        arrays.add(array);
    }

    protected void setupVec3VertexArray(String name, List<Vertex> vertexes) {
        VertexShaderAttribute array = webGlFacade.createVertexShaderAttribute(name);
        array.fillBuffer(vertexes);
        arrays.add(array);
    }

    protected void setupVec3DoubleArray(String name, List<Double> vertexes) {
        VertexShaderAttribute array = webGlFacade.createVertexShaderAttribute(name);
        array.fillDoubleBuffer(vertexes);
        arrays.add(array);
    }

    public void setupVec2Array(String name, Float32ArrayEmu float32ArrayEmu) {
        Vec2Float32ArrayShaderAttribute array = webGlFacade.createVec2Float32ArrayShaderAttribute(name);
        array.fillFloat32Array(Js.uncheckedCast(float32ArrayEmu));
        arrays.add(array);
    }

    public void setupDecimalPositionArray(String name, List<DecimalPosition> decimalPositions) {
        DecimalPositionShaderAttribute array = webGlFacade.createDecimalPositionShaderAttribute(name);
        array.fillBuffer(decimalPositions);
        arrays.add(array);
    }

    public void setupVec1Array(String name, Float32ArrayEmu float32ArrayEmu) {
        Float32ArrayShaderAttribute array = webGlFacade.createFloat32ArrayShaderAttribute(name);
        array.fillFloat32Array(Js.uncheckedCast(float32ArrayEmu));
        arrays.add(array);
    }

    public void setupVec1Array(String name, List<Double> doubles) {
        Float32ArrayShaderAttribute array = webGlFacade.createFloat32ArrayShaderAttribute(name);
        array.fillDoubleBuffer(doubles);
        arrays.add(array);
    }

    protected void setupVec3PositionArray(Float32ArrayEmu float32ArrayEmu) {
        setupVec3Array(WebGlFacade.A_VERTEX_POSITION, float32ArrayEmu);
        elementCount = (int) (((Float32Array) (Js.uncheckedCast(float32ArrayEmu))).length / Vertex.getComponentsPerVertex());
    }

    protected void setupVec3PositionArray_(Float32Array float32Array) {
        setupVec3Array_(WebGlFacade.A_VERTEX_POSITION, float32Array);
        elementCount = (int) (float32Array.length / Vertex.getComponentsPerVertex());
    }

    protected void setupVec3VertexPositionArray(List<Vertex> vertexes) {
        setupVec3VertexArray(WebGlFacade.A_VERTEX_POSITION, vertexes);
        elementCount = vertexes.size();
    }

    protected <R> void setupUniform(String name, UniformLocation.Type type, Supplier<R> valueSupplier) {
        uniforms.add(new UniformLocation<>(name, type, webGlFacade, valueSupplier));
    }

    protected <R> void setupModelMatrixUniform(String name, UniformLocation.Type type, Function<ModelMatrices, R> modelMatricesSupplier) {
        modelMatrixUniforms.add(new UniformLocation<>(name, type, webGlFacade, modelMatricesSupplier));
    }

    protected void addActivator(Activator activator) {
        activators.add(activator);
    }

    protected void createWebGLTexture(String uniformName, Supplier<WebGLTexture> webGLTextureSupplier) {
        uniformTextures.add(webGlFacade.createWebGLTexture(uniformName, webGLTextureSupplier));
    }

    protected void createWebGLTexture(String uniformName, Integer imageId) {
        if (imageId != null) {
            uniformTextures.add(webGlFacade.createWebGLTexture(imageId, uniformName));
        } else {
            uniformTextures.add(webGlFacade.createFakeWebGLTexture(uniformName));
        }
    }

    protected void setupPhongMaterial(PhongMaterialConfig material, String variableName) {
        materials.add(new WebGlPhongMaterial(webGlFacade, material, variableName));
    }

    protected void setupGroundMaterial(GroundConfig groundConfig) {
        webGlGroundMaterials.add(new WebGlGroundMaterial(webGlFacade, groundConfig));
    }

    @Override
    public final void draw(double interpolationFactor) {
        if (!active) {
            return;
        }
        if (canBeSkipped()) {
            return;
        }
        webGlFacade.useProgram();
        transformationUniformValues();

        if (lightUniforms != null) {
            lightUniforms.setLightUniforms(webGlFacade);
        }

        activateReceiveShadow();

        arrays.forEach(AbstractShaderAttribute::activate);

        uniforms.forEach(UniformLocation::uniform);
        materials.forEach(WebGlPhongMaterial::activate);
        webGlGroundMaterials.forEach(WebGlGroundMaterial::activate);
        uniformTextures.forEach(WebGlUniformTexture::activate);
        activators.forEach(Activator::activate);

//        if (inGameQuestVisualizationService.isQuestInGamePlaceVisualization()) {
//            terrainMarkerTexture.activate();
//            webGlFacade.uniform4f(terrainMarker2DPoints, inGameQuestVisualizationService.getQuestInGamePlaceVisualization().getPlaceConfigBoundary());
//            webGlFacade.uniform1f(terrainMarkerAnimation, inGameQuestVisualizationService.getQuestInGamePlaceVisualization().getAnimation());
//        } else {
//            webGlFacade.uniform4f(terrainMarker2DPoints, 0, 0, 0, 0);
//        }
        configureRenderer();
        if (modelMatricesSupplier != null) {
            drawModels(interpolationFactor);
        } else {
            webGlFacade.drawArrays(webGlFacadeConfig.getDrawMode(), elementCount, getHelperString());
            WebGlUtil.checkLastWebGlError("drawArrays", webGlFacade.getCtx3d());
        }

        arrays.forEach(AbstractShaderAttribute::deactivate);
    }

    private void configureRenderer() {
        if (isAlphaToCoverage()) {
            webGlFacade.getCtx3d().enable(SAMPLE_ALPHA_TO_COVERAGE);
        } else {
            webGlFacade.getCtx3d().disable(SAMPLE_ALPHA_TO_COVERAGE);
        }
        if (webGlFacadeConfig.isDepthTest()) {
            webGlFacade.getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);
        } else {
            webGlFacade.getCtx3d().disable(WebGLRenderingContext.DEPTH_TEST);
        }
        if (renderService.getPass() != RenderService.Pass.SHADOW) {
            // WebGl behaves strange if during depth buffer render depth mask is set to false
            webGlFacade.getCtx3d().depthMask(webGlFacadeConfig.isWriteDepthBuffer());
        }
        webGlFacade.getCtx3d().enable(WebGLRenderingContext.CULL_FACE);
        webGlFacade.getCtx3d().cullFace(WebGLRenderingContext.BACK);
        if (webGlFacadeConfig.getBlend() != null) {
            webGlFacade.getCtx3d().enable(WebGLRenderingContext.BLEND);
            switch (webGlFacadeConfig.getBlend()) {
                case SOURCE_ALPHA:
                    webGlFacade.getCtx3d().blendFunc(WebGLRenderingContext.SRC_ALPHA, WebGLRenderingContext.ONE_MINUS_SRC_ALPHA);
                    break;
                case CONST_ALPHA:
                    webGlFacade.getCtx3d().blendColor(1f, 1f, 1f, webGlFacadeConfig.getConstAlpha());
                    webGlFacade.getCtx3d().blendFunc(WebGLRenderingContext.CONSTANT_ALPHA, WebGLRenderingContext.ONE_MINUS_CONSTANT_ALPHA);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown Blend mode: " + webGlFacadeConfig.getBlend());
            }
        } else {
            webGlFacade.getCtx3d().disable(WebGLRenderingContext.BLEND);
        }

    }

    private void drawModels(double interpolationFactor) {
        List<ModelMatrices> modelMatricesList = modelMatricesSupplier.apply(System.currentTimeMillis());
        if (modelMatricesList == null) {
            return;
        }
        modelMatricesList.forEach(modelMatrices -> {
            modelMatrixUniforms.forEach(uniformLocation -> uniformLocation.uniform(modelMatrices));
            ModelMatrices transformedModelMatrices = mixTransformation(modelMatrices, interpolationFactor);
            webGlFacade.uniformMatrix4fv(modelMatrixUniformLocation, transformedModelMatrices.getModel());
            if(modelNormMatrixUniformLocation != null) {
                webGlFacade.uniformMatrix4fv(modelNormMatrixUniformLocation, transformedModelMatrices.getNorm());
            }
            webGlFacade.drawArrays(webGlFacadeConfig.getDrawMode(), elementCount, getHelperString());
        });
    }

    protected void transformationUniformValues() {
        switch (renderService.getPass()) {
            case MAIN:
                if (viewMatrixUniformLocation != null) {
                    webGlFacade.uniformMatrix4fv(viewMatrixUniformLocation, viewService.getViewMatrix());
                    WebGlUtil.checkLastWebGlError("uniformMatrix4fv U_VIEW_MATRIX", webGlFacade.getCtx3d());
                }
                if (viewNormMatrixUniformLocation != null) {
                    webGlFacade.uniformMatrix4fv(viewNormMatrixUniformLocation, viewService.getViewNormMatrix());
                    WebGlUtil.checkLastWebGlError("uniformMatrix4fv U_VIEW_NORM_MATRIX", webGlFacade.getCtx3d());
                }
                if (perspectiveMatrixUniformLocation != null) {
                    // Perspective
                    webGlFacade.uniformMatrix4fv(perspectiveMatrixUniformLocation, viewService.getPerspectiveMatrix());
                    WebGlUtil.checkLastWebGlError("uniformMatrix4fv U_PROJECTION_MATRIX", webGlFacade.getCtx3d());
                }
                if (receiveShadowMatrixUniformLocation != null) {
                    webGlFacade.uniformMatrix4fv(receiveShadowMatrixUniformLocation, viewService.getShadowLookupMatrix());
                    WebGlUtil.checkLastWebGlError("uniformMatrix4fv uShadowMatrix", webGlFacade.getCtx3d());
                }
                break;
            case SHADOW:
                if (viewMatrixUniformLocation != null) {
                    webGlFacade.uniformMatrix4fv(viewMatrixUniformLocation, viewService.getViewShadowMatrix());
                    WebGlUtil.checkLastWebGlError("uniformMatrix4fv U_VIEW_MATRIX", webGlFacade.getCtx3d());
                }
                if (perspectiveMatrixUniformLocation != null) {
                    // Perspective
                    webGlFacade.uniformMatrix4fv(perspectiveMatrixUniformLocation, viewService.getPerspectiveShadowMatrix());
                    WebGlUtil.checkLastWebGlError("uniformMatrix4fv U_PROJECTION_MATRIX", webGlFacade.getCtx3d());
                }
                break;
            default:
                throw new IllegalStateException("Dont know how to setup transformation uniforms for render pass: " + renderService.getPass());
        }
    }

    public ModelMatrices mixTransformation(ModelMatrices modelMatrix, double interpolationFactor) {
        modelMatrix = modelMatrix.interpolateVelocity(interpolationFactor);

        if (shapeTransform == null) {
            return modelMatrix.calculateFromTurretAngle();
        }

        if (progressAnimations == null) {
            if (shapeTransform.getStaticMatrix() != null) {
                if (staticShapeTransformCache == null) {
                    staticShapeTransformCache = nativeMatrixFactory.createFromColumnMajorArray(shapeTransform.getStaticMatrix().toWebGlArray());
                }
                return modelMatrix.multiplyStaticShapeTransform(staticShapeTransformCache).calculateFromTurretAngle();
            } else {
                return modelMatrix.multiplyShapeTransform(shapeTransform).calculateFromTurretAngle();
            }
        } else {
            ShapeTransform shapeTransformTRS = new ShapeTransform();
            shapeTransformTRS.setScaleX(1);
            shapeTransformTRS.setScaleY(1);
            shapeTransformTRS.setScaleZ(1);
            for (ProgressAnimation progressAnimation : progressAnimations) {
                Objects.requireNonNull(progressAnimation.getAnimationTrigger(), "No animation trigger");
                switch (progressAnimation.getAnimationTrigger()) {
                    case ITEM_PROGRESS:
                        progressAnimation.dispatch(shapeTransformTRS, modelMatrix.getProgress());
                        break;
                    case CONTINUES:
                        progressAnimation.dispatch(shapeTransformTRS, setupContinuesAnimationProgress(progressAnimation));
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown animation trigger '" + progressAnimation.getAnimationTrigger());
                }
            }
            if (shapeTransform.getStaticMatrix() != null) {
                // Why must the static matrix from shapeTransform be ignored?
                return modelMatrix.multiplyShapeTransform(shapeTransformTRS).calculateFromTurretAngle();
            } else {
                return modelMatrix.multiplyShapeTransform(shapeTransform).multiplyShapeTransform(shapeTransformTRS).calculateFromTurretAngle();
            }
        }
    }

    protected void activateReceiveShadow() {
        if (shadowWebGlTextureId == null) {
            return;
        }
        webGlFacade.uniform1f(uShadowAlpha, (float) visualUiService.getPlanetVisualConfig().getShadowAlpha());
        webGlFacade.uniform1i(uShadowTexture, shadowWebGlTextureId.getUniformValue());
        webGlFacade.getCtx3d().activeTexture(shadowWebGlTextureId.getWebGlTextureId());
        if (renderService.getPass() == RenderService.Pass.SHADOW) {
            webGlFacade.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, null);
        } else {
            webGlFacade.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, renderService.getDepthTexture());
        }
    }

    protected boolean canBeSkipped() {
        return renderService.getPass() == RenderService.Pass.SHADOW && !webGlFacadeConfig.isCastShadow();
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void dispose() {
        arrays.forEach(AbstractShaderAttribute::deleteBuffer);
    }

    @Override
    public void setShapeTransform(ShapeTransform shapeTransform) {
        this.shapeTransform = shapeTransform;
    }

    @Override
    public void setProgressAnimations(Collection<ProgressAnimation> progressAnimations) {
        this.progressAnimations = progressAnimations;
    }

    /**
     * Override in subclasses
     *
     * @return true if alpha to coverage
     */
    protected boolean isAlphaToCoverage() {
        return false;
    }

    /**
     * Override in subclasses
     */
    protected void glslVertexCustomDefines(List<String> defines, T t) {

    }

    /**
     * Override in subclasses
     */
    protected void glslFragmentCustomDefines(List<String> defines, T t) {

    }

    protected String getHelperString() {
        return getClass().getName();
    }

    private double setupContinuesAnimationProgress(ProgressAnimation progressAnimation) {
        int millis = (int) (System.currentTimeMillis() % progressAnimation.getTotalTime());
        return (double) millis / (double) progressAnimation.getTotalTime();
    }
}
