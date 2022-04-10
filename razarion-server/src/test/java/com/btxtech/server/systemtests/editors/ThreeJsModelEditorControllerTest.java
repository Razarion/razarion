package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.ThreeJsModelConfigEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.shape.ThreeJsModelConfig;
import com.btxtech.shared.rest.ThreeJsModelController;
import com.btxtech.shared.rest.ThreeJsModelEditorController;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ThreeJsModelEditorControllerTest extends AbstractCrudTest<ThreeJsModelEditorController, ThreeJsModelConfig> {
    public ThreeJsModelEditorControllerTest() {
        super(ThreeJsModelEditorController.class, ThreeJsModelConfig.class);
    }

    @After
    public void cleanTables() {
        cleanTable(ThreeJsModelConfigEntity.class);
    }

    @Override
    protected void setupUpdate() {
    }

    @Test
    public void testUpload() throws URISyntaxException, IOException {
        ThreeJsModelConfig threeJsModelConfig = getCrudToBeTested().create();

        byte[] data = Files.readAllBytes(Paths.get(getClass().getResource("bin.rar").toURI()));

        Response response = getDefaultRestConnection()
                .getTarget()
                .path(CommonUrl.THREE_JS_MODEL_EDITOR_PATH)
                .path("upload/" + threeJsModelConfig.getId())
                .request()
                .put(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));

        Assert.assertEquals(204, response.getStatus());

        Response response1 = getDefaultRestConnection().getTarget().proxy(ThreeJsModelController.class).getThreeJsModel(threeJsModelConfig.getId());
        Assert.assertEquals(200, response1.getStatus());
        Assert.fail("Verify content of response1");

    }
}
