package com.btxtech.server.systemtests;

import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.rest.GroundEditorController;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;

public class GroundEditorControllerTest extends EmptyDockerContainer {
    private GroundEditorController groundEditorController;

    @Before
    public void setup() {
        groundEditorController = setupRestAccess(GroundEditorController.class);
    }

    @After
    public void cleanImages() {
        cleanTableNative("GROUND_CONFIG");
    }

    @Test
    public void groundGround() {
        login("admin@admin.com", "1234");
        ObjectMapper mapper = new ObjectMapper();

        List<ObjectNameId> objectNameIds = groundEditorController.getObjectNameIds();
        Assert.assertEquals(0, objectNameIds.size());
        GroundConfig groundConfig1 = mapper.convertValue(groundEditorController.create(), GroundConfig.class);

        objectNameIds = groundEditorController.getObjectNameIds();
        Assert.assertEquals(1, objectNameIds.size());
        Assert.assertEquals(objectNameIds.get(0).getId(), groundConfig1.getId());

        GroundConfig groundConfig2 = mapper.convertValue(groundEditorController.read(groundConfig1.getId()), GroundConfig.class);
        assertThat(groundConfig2, is(samePropertyValuesAs(groundConfig1)));

        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, GroundConfig.class);
        List<GroundConfig> groundConfigs = mapper.convertValue(groundEditorController.read(), type);
        Assert.assertEquals(1, groundConfigs.size());
        assertThat(groundConfigs.get(0), is(samePropertyValuesAs(groundConfig1)));

        groundEditorController.delete(groundConfig1.getId());
        Assert.assertEquals(0, groundEditorController.getObjectNameIds().size());

    }

}
