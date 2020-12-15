package com.btxtech.client.renderer.webgl;

import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.shaders.library.GlslLibrarian;
import com.btxtech.client.utils.DomConstants;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 13.03.2017.
 */
@ApplicationScoped
public class WebGlProgramService {
    private Logger logger = Logger.getLogger(WebGlProgramService.class.getName());
    @Inject
    private Instance<WebGlProgramFacade> webGlProgramInstance;
    private Map<String, WebGlProgramFacade> webGlProgramCache = new HashMap<>();

    public WebGlProgramFacade getWebGlProgram(WebGlFacadeConfig webGlFacadeConfig, List<String> glslVertexDefines, List<String> glslFragmentDefines, boolean oESStandardDerivatives) {
        String vertexSkeletonShader = Shaders.SHADERS.skeletonVertexShader().getText();
        String fragmentSkeletonShader = Shaders.SHADERS.skeletonFragmentShader().getText();
        if (webGlFacadeConfig.isSkeletonShader()) {
            GlslLibrarian glslCustomLibrarian = new GlslLibrarian(webGlFacadeConfig.getSkeletonCustomLib() != null ? webGlFacadeConfig.getSkeletonCustomLib().getText() : null,
                    DomConstants.JAVASCRIPT_LINE_SEPARATOR);
            vertexSkeletonShader = glslCustomLibrarian.link(vertexSkeletonShader, glslVertexDefines, oESStandardDerivatives);
            fragmentSkeletonShader = glslCustomLibrarian.link(fragmentSkeletonShader, glslFragmentDefines, oESStandardDerivatives);
        }
        String key = vertexSkeletonShader + fragmentSkeletonShader;
        WebGlProgramFacade webGlProgram = webGlProgramCache.get(key);
        if (webGlProgram != null) {
            return webGlProgram;
        }

        webGlProgram = webGlProgramInstance.get();
        try {
            webGlProgram.createProgram(vertexSkeletonShader, fragmentSkeletonShader);
        } catch (Throwable t) {
            logger.warning("VertexShaderCode: " + WebGlProgramFacade.addLineNumbers(vertexSkeletonShader));
            logger.warning("FragmentShaderCode: " + WebGlProgramFacade.addLineNumbers(fragmentSkeletonShader));
            throw t;
        }
        webGlProgramCache.put(key, webGlProgram);
        return webGlProgram;
    }
}
