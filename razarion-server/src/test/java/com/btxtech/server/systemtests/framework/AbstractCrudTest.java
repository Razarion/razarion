package com.btxtech.server.systemtests.framework;

import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.rest.CrudController;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.assertEquals;

public abstract class AbstractCrudTest<Controller extends CrudController<ConfigObject>, ConfigObject extends Config> extends AbstractSystemTest {
    private List<Consumer<ConfigObject>> configModifiers = new ArrayList<>();
    private final Class<Controller> controllerClass;
    private final Class<ConfigObject> configClass;
    private RestConnection connection;
    private Controller crudToBeTested;

    // TODO test unregistered and none admin user

    public AbstractCrudTest(Class<Controller> controllerClass, Class<ConfigObject> configClass) {
        this.controllerClass = controllerClass;
        this.configClass = configClass;
        connection = new RestConnection(new ObjectMapperResolver(() -> configClass));
        crudToBeTested = connection.proxy(controllerClass);
    }

    /**
     * Override in subclasses
     */
    protected void setupUpdate() {
    }

    protected void registerUpdate(Consumer<ConfigObject> configModifier) {
        configModifiers.add(configModifier);
    }

    @Test
    public void testCrud() {
        connection.loginAdmin();

        // Create
        List<ObjectNameId> objectNameIds = crudToBeTested.getObjectNameIds();
        assertEquals(0, objectNameIds.size());
        ConfigObject config1 = crudToBeTested.create();
        // Read all
        objectNameIds = crudToBeTested.getObjectNameIds();
        assertEquals(1, objectNameIds.size());
        assertEquals(objectNameIds.get(0).getId(), config1.getId());
        // Read
        ConfigObject config2 = crudToBeTested.read(config1.getId());
        assertThat(config2, is(samePropertyValuesAs(config1)));
        // Update internal name
        config2.setInternalName("Internal 2");
        crudToBeTested.update(config2);
        ConfigObject config3 = crudToBeTested.read(config2.getId());
        assertThat(config3, is(samePropertyValuesAs(config2)));
        List<ConfigObject> configs = crudToBeTested.read();
        assertEquals(1, configs.size());
        assertThat(configs.get(0), is(samePropertyValuesAs(config3)));
        // Additional Update
        setupUpdate();
        configModifiers.forEach(configObjectConsumer -> {
            configObjectConsumer.accept(config3);
            crudToBeTested.update(config3);
            ConfigObject configRead = crudToBeTested.read(config2.getId());
            assertViaJson(config3, configRead);
        });
        // Delete
        crudToBeTested.delete(config3.getId());
        assertEquals(0, crudToBeTested.getObjectNameIds().size());
    }
}
