package com.btxtech.client.renderer.shaders.library;

import com.btxtech.client.renderer.shaders.library.shaders.ShaderLibrary;
import com.google.gwt.core.client.GWT;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

@Singleton
public class GlslLibrarianProducer {
    private GlslLibrarian glslLibrarian;

    @PostConstruct
    public void postConstruct() {
        ShaderLibrary shaderLibrary = GWT.create(ShaderLibrary.class);
        glslLibrarian = new GlslLibrarian(shaderLibrary, "\n");
    }

    @Produces
    public GlslLibrarian getGlslLibrarian() {
        return glslLibrarian;
    }

}
