package com.btxtech.client.shaders;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

/**
 * Created by  Beat
 * 04.04.2015.
 */
public interface Shaders extends ClientBundle {
    Shaders INSTANCE = GWT.create(Shaders.class);

    @Source("NormalVertexShader.shd")
    TextResource normalVertexShader();

    @Source("NormalFragmentShader.shd")
    TextResource normalFragmentShader();

    @Source("WireVertexShader.shd")
    TextResource wireVertexShader();

    @Source("WireFragmentShader.shd")
    TextResource wireFragmentShader();

    @Source("LightVertexShader.shd")
    TextResource LightVertexShader();

    @Source("LightFragmentShader.shd")
    TextResource LightFragmentShader();
}
