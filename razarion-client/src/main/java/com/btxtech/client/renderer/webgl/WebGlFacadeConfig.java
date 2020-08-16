package com.btxtech.client.renderer.webgl;

import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.google.gwt.resources.client.TextResource;

/**
 * Created by Beat
 * 25.03.2017.
 */
public class WebGlFacadeConfig {
    private AbstractRenderUnit abstractRenderUnit;
    private TextResource vertexShaderCode;
    private TextResource fragmentShaderCode;
    private boolean transformation;
    private boolean normTransformation;
    private boolean receiveShadow;
    private boolean castShadow;

    public WebGlFacadeConfig(AbstractRenderUnit abstractRenderUnit, TextResource vertexShaderCode, TextResource fragmentShaderCode) {
        this.abstractRenderUnit = abstractRenderUnit;
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

    public AbstractRenderUnit getAbstractRenderUnit() {
        return abstractRenderUnit;
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
}
