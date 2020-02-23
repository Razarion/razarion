package com.btxtech.server.systemtests;

import com.btxtech.shared.dto.DoubleSplattingConfig;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.PhongMaterialConfig;
import com.btxtech.shared.rest.GroundEditorController;
import org.junit.After;
import org.junit.Before;

public class GroundEditorControllerTest extends AbstractCrudTest<GroundEditorController, GroundConfig> {
    public GroundEditorControllerTest() {
        super(GroundEditorController.class, GroundConfig.class);
        registerUpdate(groundConfig -> groundConfig.setTopMaterial(new PhongMaterialConfig().textureId(IMAGE_1_ID).scale(2.1).bumpMapId(IMAGE_2_ID).bumpMapDepth(1.0).shininess(90.0).specularStrength(0.9)));
        registerUpdate(groundConfig -> groundConfig.setBottomMaterial(new PhongMaterialConfig().textureId(IMAGE_2_ID).scale(2.1).bumpMapId(IMAGE_3_ID).bumpMapDepth(0.5).shininess(80.0).specularStrength(0.4)));
        registerUpdate(groundConfig -> groundConfig.setSplatting((DoubleSplattingConfig) new DoubleSplattingConfig().scale2(20.2).imageId(IMAGE_1_ID).scale(100.0).blur(0.5).amplitude(1.5).offset(13.8)));
        registerUpdate(groundConfig -> groundConfig.setTopMaterial(null));
        registerUpdate(groundConfig -> groundConfig.setBottomMaterial(null));
        registerUpdate(groundConfig -> groundConfig.setSplatting(null));
    }

    @Before
    public void fillTables() {
        setupImages();
    }

    @After
    public void cleanTables() {
        cleanTableNative("GROUND_CONFIG");
        cleanImages();
    }

}
