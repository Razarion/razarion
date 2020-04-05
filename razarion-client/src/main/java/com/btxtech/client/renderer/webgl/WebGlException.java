package com.btxtech.client.renderer.webgl;

/**
 * Created by Beat
 * 21.06.2015.
 */
public class WebGlException extends RuntimeException {
    /**
     * Satisfying GWT
     */
    public WebGlException() {
    }

    public WebGlException(String message) {
        super(message);
    }

    public WebGlException(String operation, double lastError) {
        super("Operation: " + operation + ". Last error: " + lastError + (" (0x" + Integer.toHexString((int)lastError) + ")"));
    }

    public WebGlException(String operation, String description, double lastError) {
        super("Operation: " + operation + " Description: " + description + ". Last error: " + lastError + (" (0x" + Integer.toHexString((int)lastError) + ")"));
    }
}
