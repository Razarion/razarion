package com.btxtech.client.renderer.webgl;

import com.btxtech.shared.nativejs.NativeMatrix;
import elemental.js.util.JsArrayOfNumber;
import elemental2.core.Float32Array;
import elemental2.webgl.WebGLRenderingContext;

import java.util.List;

/**
 * Created by Beat
 * 01.05.2015.
 */
public class WebGlUtil {

    public static void checkLastWebGlError(String operation, WebGLRenderingContext ctx3d) {
        // TODO find faster solution here
        double lastError = ctx3d.getError();
        if (lastError == WebGLRenderingContext.NO_ERROR) {
            return;
        }
        if (lastError == WebGLRenderingContext.INVALID_OPERATION) {
            throw new WebGlException(operation, "INVALID_OPERATION", lastError);
        }
        if (lastError == WebGLRenderingContext.INVALID_VALUE) {
            throw new WebGlException(operation, "INVALID_VALUE", lastError);
        }
        throw new WebGlException(operation, lastError);
    }

    public static Float32Array createArrayBufferOfFloat32Doubles(List<Double> doubleList) {
        JsArrayOfNumber vertices = JsArrayOfNumber.create();
        for (double d : doubleList) {
            vertices.push(d);
        }
        return createFloat32Array(vertices);
    }

    public static Float32Array createArrayBufferOfFloat32(List<Float> floatList) {
        JsArrayOfNumber vertices = JsArrayOfNumber.create();
        for (double f : floatList) {
            vertices.push(f);
        }
        return createFloat32Array(vertices);
    }

    public native static Float32Array toFloat32Array(NativeMatrix jsFloat32Array) /*-{
        return jsFloat32Array.float32Array;
    }-*/;

    public native static Float32Array doublesToFloat32Array(double[] doubles) /*-{
        return doubles;
    }-*/;

    public native static Float32Array createFloat32Array(JsArrayOfNumber vertices) /*-{
        return new Float32Array(vertices);
    }-*/;
}
