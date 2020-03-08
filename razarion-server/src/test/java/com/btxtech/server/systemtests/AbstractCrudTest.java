package com.btxtech.server.systemtests;

import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.rest.CrudController;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.assertEquals;

public abstract class AbstractCrudTest<Controller extends CrudController<ConfigObject>, ConfigObject extends Config> extends AbstractSystemTest {
    private final Class<Controller> controllerClass;
    private final Class<ConfigObject> configClass;
    private Controller controller;
    private List<Consumer<ConfigObject>> configModifiers = new ArrayList<>();

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

    protected void registerUpdate(Consumer<ConfigObject> configModifier) {
        configModifiers.add(configModifier);
    }

    @Test
    public void testCrud() {
        login("admin@admin.com", "1234");

        // Create
        List<ObjectNameId> objectNameIds = controller.getObjectNameIds();
        assertEquals(0, objectNameIds.size());
        ConfigObject config1 = controller.create();
        // Read all
        objectNameIds = controller.getObjectNameIds();
        assertEquals(1, objectNameIds.size());
        assertEquals(objectNameIds.get(0).getId(), config1.getId());
        // Read
        ConfigObject config2 = controller.read(config1.getId());
        assertThat(config2, is(samePropertyValuesAs(config1)));
        // Update internal name
        config2.setInternalName("Internal 2");
        controller.update(config2);
        ConfigObject config3 = controller.read(config2.getId());
        assertThat(config3, is(samePropertyValuesAs(config2)));
        List<ConfigObject> configs = controller.read();
        assertEquals(1, configs.size());
        assertThat(configs.get(0), is(samePropertyValuesAs(config3)));
        // Additional Update
        configModifiers.forEach(configObjectConsumer -> {
            configObjectConsumer.accept(config3);
            controller.update(config3);
            ConfigObject configRead = controller.read(config2.getId());
            assertViaJson(config3, configRead);
        });
        // Delete
        controller.delete(config3.getId());
        assertEquals(0, controller.getObjectNameIds().size());
    }

}
