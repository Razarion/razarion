package com.btxtech.client.renderer.webgl;

import com.btxtech.client.renderer.shaders.library.GlslLibrarian;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 13.03.2017.
 */
@ApplicationScoped
public class WebGlProgramService {
    // private Logger logger = Logger.getLogger(WebGlProgramService.class.getName());
    @Inject
    private Instance<WebGlProgram> webGlProgramInstance;
    @Inject
    private GlslLibrarian glslLibrarian;
    private Map<String, WebGlProgram> webGlProgramCache = new HashMap<>();

    public WebGlProgram getWebGlProgram(WebGlFacadeConfig webGlFacadeConfig) {
        String key = glslLibrarian.link(webGlFacadeConfig.getVertexShaderCode(), webGlFacadeConfig.getAbstractRenderUnit().getGlslVertexDefines()) +
                glslLibrarian.link(webGlFacadeConfig.getFragmentShaderCode(), webGlFacadeConfig.getAbstractRenderUnit().getGlslFragmentDefines());
        WebGlProgram webGlProgram = webGlProgramCache.get(key);
        if (webGlProgram != null) {
            return webGlProgram;
        }

        webGlProgram = webGlProgramInstance.get();
        webGlProgram.createProgram(webGlFacadeConfig);
        webGlProgramCache.put(key, webGlProgram);
        return webGlProgram;
    }
}
