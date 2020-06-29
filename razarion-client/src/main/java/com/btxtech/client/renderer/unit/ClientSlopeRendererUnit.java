package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.WebGlGroundMaterial;
import com.btxtech.client.renderer.engine.WebGlPhongMaterial;
import com.btxtech.client.renderer.engine.WebGlSlopeSplatting;
import com.btxtech.client.renderer.engine.shaderattribute.FloatShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.Vec2Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmRaiser;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.questvisualization.InGameQuestVisualizationService;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.task.slope.AbstractSlopeRendererUnit;
import com.btxtech.uiservice.terrain.UiTerrainSlopeTile;
import elemental2.core.Float32Array;
import elemental2.webgl.WebGLRenderingContext;
import jsinterop.base.Js;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 24.01.2016.
 */
@ColorBufferRenderer
@Dependent
public class ClientSlopeRendererUnit extends AbstractSlopeRendererUnit {
    // private static Logger logger = Logger.getLogger(ClientSlopeRendererUnit.class.getName());
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private InGameQuestVisualizationService inGameQuestVisualizationService;
    @Inject
    private GameUiControl gameUiControl;
    private Vec3Float32ArrayShaderAttribute positions;
    private Vec3Float32ArrayShaderAttribute normals;
    private Vec2Float32ArrayShaderAttribute uvs;
    private FloatShaderAttribute slopeFactor;
    private WebGlPhongMaterial material;
    private WebGlSlopeSplatting webGlSlopeSplatting;
    private WebGlGroundMaterial webGlGroundMaterial;

    private LightUniforms lightUniforms;

    @Override
    public void init() {
        webGlFacade.enableOESStandartDerivatives();
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.slopeVertexShader(), Shaders.INSTANCE.slopeFragmentShader()).enableTransformation(true).enableReceiveShadow());
        positions = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        normals = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_NORMAL);
        uvs = webGlFacade.createVec2Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_UV);
        slopeFactor = webGlFacade.createFloatShaderAttribute("slopeFactor");
        lightUniforms = new LightUniforms(webGlFacade);
    }

    @Override
    protected void fillBufferInternal(UiTerrainSlopeTile uiTerrainSlopeTile) {
        if (uiTerrainSlopeTile.getGroundConfig() != null) {
            webGlGroundMaterial = new WebGlGroundMaterial(webGlFacade, gameUiControl);
            webGlGroundMaterial.init(uiTerrainSlopeTile.getGroundConfig());
            if (uiTerrainSlopeTile.getSlopeConfig().getSlopeSplattingConfig() != null) {
                webGlSlopeSplatting = webGlFacade.createSlopeSplatting(uiTerrainSlopeTile.getSlopeConfig().getSlopeSplattingConfig(), "slopeSplatting");
            }
        }

        AlarmRaiser.onNull(uiTerrainSlopeTile.getSlopeConfig().getMaterial(), Alarm.Type.INVALID_SLOPE_CONFIG, "No Material in SlopeConfig: ", uiTerrainSlopeTile.getSlopeConfig().getId());
        material = webGlFacade.createPhongMaterial(uiTerrainSlopeTile.getSlopeConfig().getMaterial(), "material");

        Float32Array groundPositions = Js.uncheckedCast(uiTerrainSlopeTile.getSlopeGeometry().getPositions());
        positions.fillFloat32Array(groundPositions);
        normals.fillFloat32Array(Js.uncheckedCast(uiTerrainSlopeTile.getSlopeGeometry().getNorms()));
        uvs.fillFloat32Array(Js.uncheckedCast(uiTerrainSlopeTile.getSlopeGeometry().getUvs()));
        slopeFactor.fillFloat32Array(Js.uncheckedCast(uiTerrainSlopeTile.getSlopeGeometry().getSlopeFactors()));
        setElementCount((int) (groundPositions.length / Vertex.getComponentsPerVertex()));

    }


    @Override
    protected void draw(UiTerrainSlopeTile uiTerrainSlopeTile) {
        webGlFacade.useProgram();

        lightUniforms.setLightUniforms(webGlFacade);

        positions.activate();
        normals.activate();
        uvs.activate();
        slopeFactor.activate();
        material.activate();
        if (webGlGroundMaterial != null) {
            webGlGroundMaterial.activate();
            if (webGlSlopeSplatting != null) {
                webGlSlopeSplatting.activate();
            }
        }

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }

    @Override
    public void dispose() {
        positions.deleteBuffer();
        normals.deleteBuffer();
//        tangents.deleteBuffer();
//        groundSplatting.deleteBuffer();
//        slopeFactors.deleteBuffer();
    }

    @Override
    public List<String> getGlslFragmentDefines() {
        List<String> defines = new ArrayList<>();
        if (getRenderData().getGroundConfig() != null) {
            defines.add("RENDER_GROUND_TEXTURE");
            if (getRenderData().getGroundConfig().getBottomMaterial() != null && getRenderData().getGroundConfig().getSplatting() != null) {
                defines.add("RENDER_GROUND_BOTTOM_TEXTURE");
            }
            if(getRenderData().getSlopeConfig().getSlopeSplattingConfig() != null) {
                defines.add("RENDER_SPLATTING");
            }
        }
        return defines;
    }
}
