package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.webgl.WebGlFacade;

public class WebGlStruct {
    private final WebGlFacade webGlFacade;
    private String variableName;

    public WebGlStruct(WebGlFacade webGlFacade, String variableName) {
        this.webGlFacade = webGlFacade;
        this.variableName = variableName;
    }

    protected WebGlFacade getWebGlFacade() {
        return webGlFacade;
    }

    protected String variableName(String name) {
        if (variableName != null && variableName.trim().length() > 0) {
            return variableName + "." + name;
        } else {
            return name;
        }
    }

    protected double defaultIfNull(Double value) {
        if (value != null) {
            return value;
        } else {
            return 0.0;
        }
    }

}
