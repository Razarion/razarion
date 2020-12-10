package com.btxtech.client.renderer.webgl;

import com.google.gwt.resources.client.TextResource;
import elemental2.webgl.WebGLRenderingContext;

/**
 * Created by Beat
 * 25.03.2017.
 */
public class WebGlFacadeConfig {
    public enum Blend {
        SOURCE_ALPHA,
        CONST_ALPHA
    }

    private TextResource vertexShaderCode;
    private TextResource fragmentShaderCode;
    private boolean skeletonShader;
    private TextResource skeletonCustomLib;
    private boolean transformation;
    private boolean normTransformation;
    private boolean receiveShadow;
    private boolean castShadow;
    private boolean oESStandardDerivatives;
    private boolean light;
    private boolean depthTest = true;
    private boolean writeDepthBuffer = true;
    private Blend blend;
    private double constAlpha;
    private double drawMode = WebGLRenderingContext.TRIANGLES;


    @Deprecated
    public WebGlFacadeConfig(TextResource vertexShaderCode, TextResource fragmentShaderCode) {
        this.vertexShaderCode = vertexShaderCode;
        this.fragmentShaderCode = fragmentShaderCode;
    }

    public WebGlFacadeConfig(TextResource skeletonCustomLib) {
        skeletonShader = true;
        this.skeletonCustomLib = skeletonCustomLib;
    }

    public WebGlFacadeConfig enableTransformation(boolean normTransformation) {
        transformation = true;
        this.normTransformation = normTransformation;
        return this;
    }

    @Deprecated
    public WebGlFacadeConfig enableShadowTransformation() {
        return this;
    }

    public WebGlFacadeConfig enableReceiveShadow() {
        receiveShadow = true;
        return this;
    }

    public WebGlFacadeConfig enableCastShadow() {
        castShadow = true;
        return this;
    }

    public WebGlFacadeConfig enableOESStandardDerivatives() {
        oESStandardDerivatives = true;
        return this;
    }

    public WebGlFacadeConfig enableLight() {
        light = true;
        return this;
    }

    public WebGlFacadeConfig depthTest(boolean dpDepthTest) {
        this.depthTest = dpDepthTest;
        return this;
    }

    public WebGlFacadeConfig writeDepthBuffer(boolean writeDepthBuffer) {
        this.writeDepthBuffer = writeDepthBuffer;
        return this;
    }

    public WebGlFacadeConfig blend(Blend blend) {
        this.blend = blend;
        return this;
    }

    public WebGlFacadeConfig constAlpha(double constAlpha) {
        this.constAlpha = constAlpha;
        return this;
    }

    public WebGlFacadeConfig drawMode(double drawMode) {
        this.drawMode = drawMode;
        return this;
    }

    public TextResource getVertexShaderCode() {
        return vertexShaderCode;
    }

    public TextResource getFragmentShaderCode() {
        return fragmentShaderCode;
    }

    public boolean isSkeletonShader() {
        return skeletonShader;
    }

    public TextResource getSkeletonCustomLib() {
        return skeletonCustomLib;
    }

    public boolean isTransformation() {
        return transformation;
    }

    public boolean isNormTransformation() {
        return normTransformation;
    }

    public boolean isReceiveShadow() {
        return receiveShadow;
    }

    public boolean isCastShadow() {
        return castShadow;
    }

    public boolean isOESStandardDerivatives() {
        return oESStandardDerivatives;
    }

    public boolean isLight() {
        return light;
    }

    public boolean isDepthTest() {
        return depthTest;
    }

    public boolean isWriteDepthBuffer() {
        return writeDepthBuffer;
    }

    public Blend getBlend() {
        return blend;
    }

    public double getConstAlpha() {
        return constAlpha;
    }

    public double getDrawMode() {
        return drawMode;
    }
}
