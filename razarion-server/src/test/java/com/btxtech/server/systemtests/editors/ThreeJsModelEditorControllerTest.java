package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.ThreeJsModelConfigEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.shape.ThreeJsModelConfig;
import com.btxtech.shared.rest.ThreeJsModelEditorController;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    public void testUpload() {
        ThreeJsModelConfig threeJsModelConfig = getCrudToBeTested().create();

        MultipartFormDataOutput multipartFormDataOutput = new MultipartFormDataOutput();
        multipartFormDataOutput.addFormData("http_form_data_model", getClass().getResourceAsStream("bin.rar"), MediaType.APPLICATION_OCTET_STREAM_TYPE);

        Response response = getDefaultRestConnection()
                .getTarget()
                .path(CommonUrl.THREE_JS_MODEL_EDITOR_PATH)
                .path("upload/" + threeJsModelConfig.getId())
                .request()
                .put(Entity.entity(multipartFormDataOutput, MediaType.MULTIPART_FORM_DATA));

        Assert.assertEquals(204, response.getStatus());
    }
}
