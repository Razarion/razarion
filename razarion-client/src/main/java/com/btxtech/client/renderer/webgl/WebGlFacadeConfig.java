package com.btxtech.client.renderer.webgl;

import com.google.gwt.resources.client.TextResource;

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


    public WebGlFacadeConfig(TextResource vertexShaderCode, TextResource fragmentShaderCode) {
        this.vertexShaderCode = vertexShaderCode;
        this.fragmentShaderCode = fragmentShaderCode;
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

    public TextResource getVertexShaderCode() {
        return vertexShaderCode;
    }

    public TextResource getFragmentShaderCode() {
        return fragmentShaderCode;
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
}
