package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.surface.GroundConfigEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.rest.GroundEditorController;
import org.junit.After;
import org.junit.Before;

public class GroundEditorControllerTest extends AbstractCrudTest<GroundEditorController, GroundConfig> {
    public GroundEditorControllerTest() {
        super(GroundEditorController.class, GroundConfig.class);
    }

    @Before
    public void fillTables() {
        setupImages();
    }

    @After
    public void cleanTables() {
        cleanTable(GroundConfigEntity.class);
    }

    @Override
    protected void setupUpdate() {
        // TODO registerUpdate(groundConfig -> groundConfig.setTopMaterial(new PhongMaterialConfig().textureId(IMAGE_1_ID).scale(2.1).bumpMapId(IMAGE_2_ID).bumpMapDepth(1.0).shininess(90.0).specularStrength(0.9)));
        // TODO registerUpdate(groundConfig -> groundConfig.setBottomMaterial(new PhongMaterialConfig().textureId(IMAGE_2_ID).scale(2.1).bumpMapId(IMAGE_3_ID).bumpMapDepth(0.5).shininess(80.0).specularStrength(0.4)));
        // TODO registerUpdate(groundConfig -> groundConfig.setSplatting(new GroundSplattingConfig().scale2(20.2).textureId(IMAGE_1_ID).scale1(100.0).scale2(30).blur(0.5).offset(13.8)));
        // TODO registerUpdate(groundConfig -> groundConfig.setTopMaterial(null));
        // TODO registerUpdate(groundConfig -> groundConfig.setBottomMaterial(null));
        // TODO registerUpdate(groundConfig -> groundConfig.setSplatting(null));
        throw new UnsupportedOperationException("FIX MEE");
    }
}
