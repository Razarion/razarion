package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.dto.PhongMaterialConfig;
import elemental2.webgl.WebGLUniformLocation;

public class WebGlPhongMaterial extends WebGlStruct {
    public static final String UNIFORM_LOCATION_TEXTURE = "texture";
    public static final String UNIFORM_LOCATION_SCALE = "scale";
    public static final String UNIFORM_LOCATION_BUMP_MAP = "bumpMap";
    public static final String UNIFORM_LOCATION_BUMP_MAP_DEPTH = "bumpMapDepth";
    public static final String UNIFORM_LOCATION_SHININESS = "shininess";
    public static final String UNIFORM_LOCATION_SPECULAR_STRENGTH = "specularStrength";
    private final PhongMaterialConfig phongMaterialConfig;
    private WebGlUniformTexture texture;
    private WebGLUniformLocation scale;
    private WebGlUniformTexture bumpMap;
    private WebGLUniformLocation bumpMapDepth;
    private WebGLUniformLocation shininess;
    private WebGLUniformLocation specularStrength;

    public WebGlPhongMaterial(WebGlFacade webGlFacade, PhongMaterialConfig phongMaterialConfig, String variableName) {
        super(webGlFacade, variableName);
        this.phongMaterialConfig = phongMaterialConfig;
        if (phongMaterialConfig.getTextureId() != null) {
            texture = webGlFacade.createWebGLTexture(phongMaterialConfig.getTextureId(), variableName(UNIFORM_LOCATION_TEXTURE));
        } else {
            texture = webGlFacade.createFakeWebGLTexture(variableName(UNIFORM_LOCATION_TEXTURE));
        }
        scale = webGlFacade.getUniformLocation(variableName(UNIFORM_LOCATION_SCALE));
        if (phongMaterialConfig.getBumpMapId() != null) {
            bumpMap = webGlFacade.createWebGLBumpMapTexture(phongMaterialConfig.getBumpMapId(), variableName(UNIFORM_LOCATION_BUMP_MAP));
        } else {
            bumpMap = webGlFacade.createFakeWebGLTexture(variableName(UNIFORM_LOCATION_BUMP_MAP));
        }
        bumpMapDepth = webGlFacade.getUniformLocation(variableName(UNIFORM_LOCATION_BUMP_MAP_DEPTH));
        shininess = webGlFacade.getUniformLocation(variableName(UNIFORM_LOCATION_SHININESS));
        specularStrength = webGlFacade.getUniformLocation(variableName(UNIFORM_LOCATION_SPECULAR_STRENGTH));
    }

    public void activate() {
        texture.activate();
        getWebGlFacade().uniform1f(scale, phongMaterialConfig.getScale());
        bumpMap.activate();
        getWebGlFacade().uniform1f(bumpMapDepth, defaultIfNull(phongMaterialConfig.getBumpMapDepth()));
        getWebGlFacade().uniform1f(shininess, defaultIfNull(phongMaterialConfig.getShininess()));
        getWebGlFacade().uniform1f(specularStrength, defaultIfNull(phongMaterialConfig.getSpecularStrength()));
    }

}
