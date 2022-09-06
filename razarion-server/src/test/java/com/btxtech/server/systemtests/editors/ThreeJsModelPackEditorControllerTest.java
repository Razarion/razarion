package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.ThreeJsModelPackConfigEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.ThreeJsModelPackConfig;
import com.btxtech.shared.rest.ThreeJsModelPackEditorController;
import org.junit.After;

import java.util.Arrays;

public class ThreeJsModelPackEditorControllerTest extends AbstractCrudTest<ThreeJsModelPackEditorController, ThreeJsModelPackConfig> {
    public ThreeJsModelPackEditorControllerTest() {
        super(ThreeJsModelPackEditorController.class, ThreeJsModelPackConfig.class);
    }

    @After
    public void cleanTables() {
        cleanTable(ThreeJsModelPackConfigEntity.class);
    }

    @Override
    protected void setupUpdate() {
        registerUpdate(drivewayConfig -> drivewayConfig.namePath(Arrays.asList("aaa", "bbb", "ccc") ));
        registerUpdate(drivewayConfig -> drivewayConfig.namePath(Arrays.asList("xxx", "yyy")));
        registerUpdate(drivewayConfig -> drivewayConfig.scale(new Vertex(0.1, 0.2, 0.3)).rotation(new Vertex(0.12, 0.23, 0.45)));
        registerUpdate(drivewayConfig -> drivewayConfig.position(new Vertex(11, 22, 33)));
    }

}
