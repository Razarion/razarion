package com.btxtech.server.systemtests;

import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.rest.GroundEditorController;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
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

    @Override
    protected void configureRestClient(Client client) {
        client.register(new ObjectMapperResolver(GroundConfig.class));
    }

    @Test
    public void groundGround() {
        login("admin@admin.com", "1234");

        List<ObjectNameId> objectNameIds = groundEditorController.getObjectNameIds();
        Assert.assertEquals(0, objectNameIds.size());
        GroundConfig groundConfig1 = groundEditorController.create();

        objectNameIds = groundEditorController.getObjectNameIds();
        Assert.assertEquals(1, objectNameIds.size());
        Assert.assertEquals(objectNameIds.get(0).getId(), groundConfig1.getId());

        GroundConfig groundConfig2 = groundEditorController.read(groundConfig1.getId());
        assertThat(groundConfig2, is(samePropertyValuesAs(groundConfig1)));
        groundConfig2.setInternalName("Internal 2");
        groundEditorController.update(groundConfig2);
        GroundConfig groundConfig3 = groundEditorController.read(groundConfig1.getId());
        assertThat(groundConfig3, is(samePropertyValuesAs(groundConfig2)));

        List<GroundConfig> groundConfigs = groundEditorController.read();
        Assert.assertEquals(1, groundConfigs.size());
        assertThat(groundConfigs.get(0), is(samePropertyValuesAs(groundConfig3)));

        groundEditorController.delete(groundConfig3.getId());
        Assert.assertEquals(0, groundEditorController.getObjectNameIds().size());
    }

}
