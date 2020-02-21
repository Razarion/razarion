package com.btxtech.server.systemtests;

import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.rest.CrudController;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;

public class AbstractCrudTest<Controller extends CrudController<ConfigObject>, ConfigObject extends Config> extends RestServerTestBase {
    private final Class<Controller> controllerClass;
    private final Class<ConfigObject> configClass;
    private Controller controller;

    // TODO test unregistered and none admin user

    public AbstractCrudTest(Class<Controller> controllerClass, Class<ConfigObject> configClass) {
        this.controllerClass = controllerClass;
        this.configClass = configClass;
    }

    @Before
    public void setup() {
        controller = setupRestAccess(controllerClass);
    }

    @Override
    protected void configureRestClient(Client client) {
        client.register(new ObjectMapperResolver(configClass));
    }

    @Test
    public void testCrud() {
        login("admin@admin.com", "1234");

        // Create
        List<ObjectNameId> objectNameIds = controller.getObjectNameIds();
        Assert.assertEquals(0, objectNameIds.size());
        ConfigObject config1 = controller.create();
        // Read all
        objectNameIds = controller.getObjectNameIds();
        Assert.assertEquals(1, objectNameIds.size());
        Assert.assertEquals(objectNameIds.get(0).getId(), config1.getId());
        // Read
        ConfigObject config2 = controller.read(config1.getId());
        assertThat(config2, is(samePropertyValuesAs(config1)));
        // Update internal name
        config2.setInternalName("Internal 2");
        controller.update(config2);
        ConfigObject config3 = controller.read(config2.getId());
        assertThat(config3, is(samePropertyValuesAs(config2)));
        List<ConfigObject> configs = controller.read();
        Assert.assertEquals(1, configs.size());
        assertThat(configs.get(0), is(samePropertyValuesAs(config3)));
        // Delete
        controller.delete(config3.getId());
        Assert.assertEquals(0, controller.getObjectNameIds().size());
    }

}
