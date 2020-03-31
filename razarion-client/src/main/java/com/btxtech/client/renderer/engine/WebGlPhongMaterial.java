package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.dto.PhongMaterialConfig;
import elemental2.webgl.WebGLUniformLocation;

public class WebGlPhongMaterial {
    public static final String UNIFROM_LOCATION_TEXTURE = "texture";
    public static final String UNIFROM_LOCATION_SCALE = "scale";
    public static final String UNIFROM_LOCATION_BUMP_MAP = "bumpMap";
    public static final String UNIFROM_LOCATION_BUMP_MAP_DEPTH = "bumpMapDepth";
    public static final String UNIFROM_LOCATION_SHININESS = "shininess";
    public static final String UNIFROM_LOCATION_SPECULAR_STRENGTH = "specularStrength";
    private final WebGlFacade webGlFacade;
    private final PhongMaterialConfig phongMaterialConfig;
    private WebGlUniformTexture texture;
    private WebGLUniformLocation scale;
    private WebGlUniformTexture bumpMap;
    private WebGLUniformLocation bumpMapDepth;
    private WebGLUniformLocation shininess;
    private WebGLUniformLocation specularStrength;

    public WebGlPhongMaterial(WebGlFacade webGlFacade, PhongMaterialConfig phongMaterialConfig, String prefix) {
        this.webGlFacade = webGlFacade;
        this.phongMaterialConfig = phongMaterialConfig;
        texture = webGlFacade.createWebGLTexture(phongMaterialConfig.getTextureId(), prefix + UNIFROM_LOCATION_TEXTURE);
        scale = webGlFacade.getUniformLocation(prefix + UNIFROM_LOCATION_SCALE);
        bumpMap = webGlFacade.createWebGLTexture(phongMaterialConfig.getBumpMapId(), prefix + UNIFROM_LOCATION_BUMP_MAP);
        bumpMapDepth = webGlFacade.getUniformLocation(prefix + UNIFROM_LOCATION_BUMP_MAP_DEPTH);
        shininess = webGlFacade.getUniformLocation(prefix + UNIFROM_LOCATION_SHININESS);
        specularStrength = webGlFacade.getUniformLocation(prefix + UNIFROM_LOCATION_SPECULAR_STRENGTH);
    }

    public void activate() {
        texture.activate();
        webGlFacade.uniform1f(scale, phongMaterialConfig.getScale());
        bumpMap.activate();
        webGlFacade.uniform1f(bumpMapDepth, phongMaterialConfig.getBumpMapDepth());
        webGlFacade.uniform1f(shininess, phongMaterialConfig.getShininess());
        webGlFacade.uniform1f(specularStrength, phongMaterialConfig.getSpecularStrength());
    }
}
