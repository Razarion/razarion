package com.btxtech.server.systemtests;

import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.PhongMaterialConfig;
import com.btxtech.shared.rest.GroundEditorController;
import org.junit.After;
import org.junit.Before;

public class GroundEditorControllerTest extends AbstractCrudTest<GroundEditorController, GroundConfig> {
    public GroundEditorControllerTest() {
        super(GroundEditorController.class, GroundConfig.class);
        registerUpdate(groundConfig -> groundConfig.setTopMaterial(new PhongMaterialConfig().textureId(IMAGE_1_ID)));
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
