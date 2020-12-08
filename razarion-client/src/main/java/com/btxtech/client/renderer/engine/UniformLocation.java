package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import elemental2.webgl.WebGLUniformLocation;
import jsinterop.base.Js;

import java.util.function.Function;
import java.util.function.Supplier;

public class UniformLocation<T> {
    private final String name;
    private final Type type;
    private WebGlFacade webGlFacade;
    private Supplier<T> valueSupplier;
    private Function<ModelMatrices, T> modelMatricesSupplier;
    private WebGLUniformLocation webGLUniformLocation;

    public enum Type {
        I, // Integer
        B, // Boolean
        F,  // Float
        COLOR, // Color
        MATRIX_4 // Matrix 4 as double array
    }

    public UniformLocation(String name, Type type, WebGlFacade webGlFacade, Supplier<T> valueSupplier) {
        this(name, type, webGlFacade);
        this.valueSupplier = valueSupplier;
    }

    public UniformLocation(String name, Type type, WebGlFacade webGlFacade, Function<ModelMatrices, T> modelMatricesSupplier) {
        this(name, type, webGlFacade);
        this.modelMatricesSupplier = modelMatricesSupplier;
    }

    private UniformLocation(String name, Type type, WebGlFacade webGlFacade) {
        this.name = name;
        this.type = type;
        this.webGlFacade = webGlFacade;
        webGLUniformLocation = webGlFacade.getUniformLocationAlarm(name);
    }

    public void uniform(ModelMatrices modelMatrices) {
        uniform(modelMatricesSupplier.apply(modelMatrices));
    }

    public void uniform() {
        uniform(valueSupplier.get());
    }

    private void uniform(T t) {
        switch (type) {
            case I:
                webGlFacade.uniform1i(webGLUniformLocation, Js.uncheckedCast(t));
                return;
            case B:
                webGlFacade.uniform1b(webGLUniformLocation, Js.uncheckedCast(t));
                return;
            case F:
                webGlFacade.uniform1f(webGLUniformLocation, Js.uncheckedCast(t));
                return;
            case COLOR:
                webGlFacade.uniform4f(webGLUniformLocation, (Color) t);
                return;
            case MATRIX_4:
                webGlFacade.uniformMatrix4fv(webGLUniformLocation, (double[]) t);
                return;
            default:
                throw new IllegalStateException("No webGLUniformLocation.uniformXX() method found for type '" + type + " for uniform location '" + name + "'");
        }
    }
}
