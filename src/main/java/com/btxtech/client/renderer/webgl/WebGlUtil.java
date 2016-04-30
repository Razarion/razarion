package com.btxtech.client.renderer.webgl;

import com.google.gwt.dom.client.CanvasElement;
import elemental.html.Float32Array;
import elemental.html.WebGLRenderingContext;
import elemental.js.html.JsUint8Array;
import elemental.js.util.JsArrayOfNumber;

import java.util.List;

/**
 * Created by Beat
 * 01.05.2015.
 */
public class WebGlUtil {

    public static void checkLastWebGlError(String operation, WebGLRenderingContext ctx3d) {
        int lastError = ctx3d.getError();
        if (lastError != WebGLRenderingContext.NO_ERROR) {
            switch (lastError) {
                case WebGLRenderingContext.INVALID_OPERATION: {
                    throw new WebGlException(operation, "INVALID_OPERATION", lastError);
                }
                case WebGLRenderingContext.INVALID_VALUE: {
                    throw new WebGlException(operation, "INVALID_VALUE", lastError);
                }
                default:
                    throw new WebGlException(operation, lastError);
            }
        }
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

    public native static Float32Array createArrayBufferOfFloat32Doubles(double[] doubles) /*-{
        // TODO Why does this work?
        return doubles;
        // return new Float32Array(doubles);
    }-*/;

    public native static Float32Array createFloat32Array(JsArrayOfNumber vertices) /*-{
        return new Float32Array(vertices);
    }-*/;

    public native static JsUint8Array createUint8Array(int length) /*-{
        return new Uint8Array(length);
    }-*/;

    public native static elemental.dom.Element castElementToElement(com.google.gwt.dom.client.Element e) /*-{
        return e;
    }-*/;

    // http://in2gpu.com/2014/04/11/webgl-transparency/
    // {alpha:false}
    // http://stackoverflow.com/questions/7156971/webgl-readpixels-is-always-returning-0-0-0-0
    // {preserveDrawingBuffer: true}
    public native static WebGLRenderingContext getContext(CanvasElement canvasElement, String contextId) /*-{
        return canvasElement.getContext(contextId, {alpha:false, preserveDrawingBuffer: true});
    }-*/;


}
