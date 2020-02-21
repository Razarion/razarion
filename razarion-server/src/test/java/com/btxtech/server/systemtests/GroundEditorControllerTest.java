package com.btxtech.server.systemtests;

import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.rest.GroundEditorController;
import org.junit.After;

public class GroundEditorControllerTest extends AbstractCrudTest<GroundEditorController, GroundConfig> {
    public GroundEditorControllerTest() {
        super(GroundEditorController.class, GroundConfig.class);
    }

    @After
    public void cleanImages() {
        cleanTableNative("GROUND_CONFIG");
    }

}
