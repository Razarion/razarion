package com.btxtech.client.renderer.shaders.library.shaders;

import com.google.gwt.resources.client.ClientBundleWithLookup;
import com.google.gwt.resources.client.TextResource;

public interface ShaderLibrary extends ClientBundleWithLookup {
    @Source("Phong.frag")
    TextResource phong();

    @Source("Ground.frag")
    TextResource ground();

}
