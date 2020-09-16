package com.btxtech.client.renderer.webgl;

import com.btxtech.client.renderer.shaders.library.GlslLibrarian;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 13.03.2017.
 */
@ApplicationScoped
public class WebGlProgramService {
    // private Logger logger = Logger.getLogger(WebGlProgramService.class.getName());
    @Inject
    private Instance<WebGlProgramFacade> webGlProgramInstance;
    @Inject
    private GlslLibrarian glslLibrarian;
    private Map<String, WebGlProgramFacade> webGlProgramCache = new HashMap<>();

    public WebGlProgramFacade getWebGlProgram(WebGlFacadeConfig webGlFacadeConfig, List<String> glslVertexDefines, List<String> glslFragmentDefines) {
        String vertexShaderCodee = glslLibrarian.link(webGlFacadeConfig.getVertexShaderCode(), glslVertexDefines);
        String fragmentShaderCode = glslLibrarian.link(webGlFacadeConfig.getFragmentShaderCode(), glslFragmentDefines);

        String key = vertexShaderCodee + fragmentShaderCode;
        WebGlProgramFacade webGlProgram = webGlProgramCache.get(key);
        if (webGlProgram != null) {
            return webGlProgram;
        }

        webGlProgram = webGlProgramInstance.get();
        webGlProgram.createProgram(vertexShaderCodee, fragmentShaderCode);
        webGlProgramCache.put(key, webGlProgram);
        return webGlProgram;
    }
}
