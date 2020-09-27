package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.datatypes.Color;
import elemental2.webgl.WebGLUniformLocation;
import jsinterop.base.Js;

import java.util.function.Supplier;

public class UniformLocation<T> {
    private final String name;
    private final Type type;
    private WebGlFacade webGlFacade;
    private Supplier<T> valueSupplier;
    private WebGLUniformLocation webGLUniformLocation;

    public enum Type {
        I, // Integer
        B, // Boolean
        F,  // Float
        COLOR // Color
    }

    public UniformLocation(String name, Type type, WebGlFacade webGlFacade, Supplier<T> valueSupplier) {
        this.name = name;
        this.type = type;
        this.webGlFacade = webGlFacade;
        this.valueSupplier = valueSupplier;

        webGLUniformLocation = webGlFacade.getUniformLocationAlarm(name);
    }

    public void uniform() {
        switch (type) {
            case I:
                webGlFacade.uniform1i(webGLUniformLocation, Js.uncheckedCast(valueSupplier.get()));
                return;
            case B:
                webGlFacade.uniform1b(webGLUniformLocation, Js.uncheckedCast(valueSupplier.get()));
                return;
            case F:
                webGlFacade.uniform1f(webGLUniformLocation, Js.uncheckedCast(valueSupplier.get()));
                return;
            case COLOR:
                webGlFacade.uniform4f(webGLUniformLocation, (Color)valueSupplier.get());
                return;
            default:
                throw new IllegalStateException("No webGLUniformLocation.uniformXX() method found for type '" + type + " for uniform location '" + name + "'");
        }
    }
}
