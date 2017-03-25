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
    private boolean shadowTransformation;

    public WebGlFacadeConfig(AbstractRenderUnit abstractRenderUnit, TextResource vertexShaderCode, TextResource fragmentShaderCode) {
        this.abstractRenderUnit = abstractRenderUnit;
        this.vertexShaderCode = vertexShaderCode;
        this.fragmentShaderCode = fragmentShaderCode;
    }

    public WebGlFacadeConfig enableTransformation(boolean normTransformation) {
        transformation = true;
        shadowTransformation = false;
        this.normTransformation = normTransformation;
        return this;
    }

    public WebGlFacadeConfig enableShadowTransformation() {
        shadowTransformation = true;
        transformation = false;
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

    public boolean isShadowTransformation() {
        return shadowTransformation;
    }
}
