package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.surface.SlopeConfigEntity;
import com.btxtech.server.persistence.surface.SlopeShapeEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.PhongMaterialConfig;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.gameengine.datatypes.config.ShallowWaterConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeSplattingConfig;
import com.btxtech.shared.rest.SlopeEditorController;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.util.Arrays;

public class SlopeEditorControllerTest extends AbstractCrudTest<SlopeEditorController, SlopeConfig> {
    public SlopeEditorControllerTest() {
        super(SlopeEditorController.class, SlopeConfig.class);
    }

    @Before
    public void fillTables() {
        setupImages();
        setupGroundConfig();
        setupWaterConfig();
    }

    @After
    public void cleanTables() {
        cleanTable(SlopeShapeEntity.class);
        cleanTable(SlopeConfigEntity.class);
    }

    @Override
    protected void setupUpdate() {
        registerUpdate(slopeConfig -> slopeConfig.coastDelimiterLineGameEngine(10).outerLineGameEngine(5).innerLineGameEngine(20).horizontalSpace(5).interpolateNorm(true));
        registerUpdate(slopeConfig -> slopeConfig.coastDelimiterLineGameEngine(20).outerLineGameEngine(27).innerLineGameEngine(50).horizontalSpace(25).groundConfigId(GROUND_1_ID));
        // TODO registerUpdate(slopeConfig -> slopeConfig.material(new PhongMaterialConfig().textureId(IMAGE_2_ID).scale(2.1).bumpMapId(IMAGE_3_ID).bumpMapDepth(0.5).shininess(80.0).specularStrength(0.4)).interpolateNorm(false));
        // TODO registerUpdate(slopeConfig -> slopeConfig.material(null));
        registerUpdate(slopeConfig -> slopeConfig.groundConfigId(null));
        registerUpdate(slopeConfig -> slopeConfig.waterConfigId(WATER_1_ID));
        registerUpdate(slopeConfig -> slopeConfig.waterConfigId(WATER_2_ID));
        registerUpdate(slopeConfig -> slopeConfig.waterConfigId(null));
        registerUpdate(slopeConfig -> slopeConfig.slopeShapes(Arrays.asList(
                new SlopeShape().slopeFactor(0.5),
                new SlopeShape().position(new DecimalPosition(1.1, 2.3)).slopeFactor(0.5),
                new SlopeShape().position(new DecimalPosition(3.4, 4.5)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(5.7, 8.1)).slopeFactor(0.4))));
        registerUpdate(slopeConfig -> slopeConfig.slopeShapes(Arrays.asList(
                new SlopeShape().slopeFactor(0.4),
                new SlopeShape().position(new DecimalPosition(2.1, 5.3)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(7.7, 18.1)).slopeFactor(0.0))));
        registerUpdate(slopeConfig -> slopeConfig.slopeShapes(null));
//    TODO    registerUpdate(slopeConfig -> slopeConfig.setShallowWaterConfig(new ShallowWaterConfig().textureId(IMAGE_1_ID).scale(123).distortionId(IMAGE_2_ID).distortionStrength(12.5).stencilId(IMAGE_3_ID).durationSeconds(21)));
//    TODO    registerUpdate(slopeConfig -> slopeConfig.setShallowWaterConfig(new ShallowWaterConfig().textureId(IMAGE_3_ID).scale(34).distortionId(IMAGE_1_ID).distortionStrength(1.5).stencilId(IMAGE_2_ID).durationSeconds(10.4)));
//    TODO    registerUpdate(slopeConfig -> slopeConfig.setShallowWaterConfig(null));
        registerUpdate(slopeConfig -> slopeConfig.setOuterSlopeSplattingConfig(new SlopeSplattingConfig().textureId(IMAGE_1_ID).scale(1.2).impact(1.7).blur(5).offset(38)));
        registerUpdate(slopeConfig -> slopeConfig.setInnerSlopeSplattingConfig(new SlopeSplattingConfig().textureId(IMAGE_3_ID).scale(5.2).impact(7.7).blur(0.5).offset(12)));
        registerUpdate(slopeConfig -> slopeConfig.setOuterSlopeSplattingConfig(new SlopeSplattingConfig().textureId(IMAGE_2_ID).scale(3.2).impact(5.7).blur(80).offset(1)));
        registerUpdate(slopeConfig -> slopeConfig.setOuterSlopeSplattingConfig(null));
        registerUpdate(slopeConfig -> slopeConfig.setInnerSlopeSplattingConfig(new SlopeSplattingConfig().textureId(IMAGE_1_ID).scale(4.7).impact(9.6).blur(0.2).offset(122)));
        registerUpdate(slopeConfig -> slopeConfig.setInnerSlopeSplattingConfig(null));
        Assert.fail("Fix Material see TODOs");
    }
}
