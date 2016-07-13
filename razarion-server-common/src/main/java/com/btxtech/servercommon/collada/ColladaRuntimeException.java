package com.btxtech.servercommon.collada;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class ColladaRuntimeException extends RuntimeException {
    public ColladaRuntimeException(String s) {
        super(s);
    }

    public ColladaRuntimeException(String s, Throwable cause) {
        super(s, cause);
    }
}
