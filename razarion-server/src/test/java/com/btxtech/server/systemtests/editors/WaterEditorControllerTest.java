package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.surface.WaterConfigEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.rest.WaterEditorController;
import org.junit.After;
import org.junit.Before;

public class WaterEditorControllerTest extends AbstractCrudTest<WaterEditorController, WaterConfig> {
    public WaterEditorControllerTest() {
        super(WaterEditorController.class, WaterConfig.class);
    }

    @Before
    public void fillTables() {
        setupImages();
    }

    @After
    public void cleanTables() {
        cleanTable(WaterConfigEntity.class);
    }

    @Override
    protected void setupUpdate() {
        registerUpdate(waterConfig -> waterConfig.waterLevel(0.9).groundLevel(-5).transparency(0.8).reflectionScale(1.5).normalMapDepth(0.45).normalMapDepth(21).distortionStrength(2.3).distortionAnimationSeconds(12));
        registerUpdate(waterConfig -> waterConfig.fresnelOffset(1.9).fresnelDelta(7.6).shininess(3.1).specularStrength(12));
        registerUpdate(waterConfig -> waterConfig.reflectionId(IMAGE_1_ID).normalMapId(IMAGE_2_ID).distortionId(IMAGE_3_ID));
        registerUpdate(waterConfig -> waterConfig.reflectionId(IMAGE_2_ID).normalMapId(IMAGE_3_ID).distortionId(IMAGE_1_ID));
        registerUpdate(waterConfig -> waterConfig.reflectionId(null).normalMapId(null).distortionId(null));
    }
}
