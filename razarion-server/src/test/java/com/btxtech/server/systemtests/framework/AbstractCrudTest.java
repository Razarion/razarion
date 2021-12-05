package com.btxtech.server.systemtests.framework;

import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.rest.CrudController;
import com.btxtech.test.JsonAssert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.assertEquals;

public abstract class AbstractCrudTest<Controller extends CrudController<ConfigObject>, ConfigObject extends Config> extends AbstractSystemTest {
    private List<ConfigModifier<ConfigObject>> configModifiers = new ArrayList<>();
    private RestConnection connection;
    private Controller crudToBeTested;
    private boolean ignoreInternalName;

    public static class ConfigModifier<ConfigObject> {
        private Consumer<ConfigObject> configModifier;
        private JsonAssert.IdSuppressor[] idSuppressors;

        public ConfigModifier(Consumer<ConfigObject> configModifier, JsonAssert.IdSuppressor[] idSuppressors) {
            this.configModifier = configModifier;
            this.idSuppressors = idSuppressors;
        }
    }

    // TODO test unregistered and none admin user

    public AbstractCrudTest(Class<Controller> controllerClass, Class<ConfigObject> configClass) {
        connection = new RestConnection(new ObjectMapperResolver(() -> configClass));
        crudToBeTested = connection.proxy(controllerClass);
    }

    /**
     * Override in subclasses
     */
    protected void setupUpdate() {
    }

    /**
     * Override in subclasses
     */
    protected void doFinalAssert() {
    }

    /**
     * Override in subclasses
     */
    protected void assertCreated(ConfigObject config1) {

    }

    protected void registerUpdate(Consumer<ConfigObject> configModifier, JsonAssert.IdSuppressor... idSuppressors) {
        configModifiers.add(new ConfigModifier<>(configModifier, idSuppressors));
    }

    protected void enabledIgnoreInternalName() {
        ignoreInternalName = true;
    }

    @Test
    public void testCrud() {
        connection.loginAdmin();

        // Create
        List<ObjectNameId> objectNameIds = crudToBeTested.getObjectNameIds();
        assertEquals(0, objectNameIds.size());
        ConfigObject config1 = crudToBeTested.create();
        assertCreated(config1);
        // Read all
        objectNameIds = crudToBeTested.getObjectNameIds();
        assertEquals(1, objectNameIds.size());
        assertEquals(objectNameIds.get(0).getId(), config1.getId());
        // Read
        ConfigObject config2 = crudToBeTested.read(config1.getId());
        assertThat(config2, is(samePropertyValuesAs(config1)));
        ConfigObject config3 = config2;
        if (!ignoreInternalName) {
            // Update internal name
            config2.setInternalName("Internal 2");
            crudToBeTested.update(config2);
            config3 = crudToBeTested.read(config2.getId());
            assertThat(config3, is(samePropertyValuesAs(config2)));
            List<ConfigObject> configs = crudToBeTested.read();
            assertEquals(1, configs.size());
            assertThat(configs.get(0), is(samePropertyValuesAs(config3)));
        }
        // Additional Update
        setupUpdate();
        SingleHolder<ConfigObject> holder = new SingleHolder<>(config3);
        SingleHolder<Integer> indexHolder = new SingleHolder<>(0);
        configModifiers.forEach(configModifier -> {
            try {
                configModifier.configModifier.accept(holder.getO());
                crudToBeTested.update(holder.getO());
                ConfigObject configRead = crudToBeTested.read(config2.getId());
                JsonAssert.assertViaJson(holder.getO(), configRead, configModifier.idSuppressors);
                holder.setO(configRead);
            } catch (AssertionError assertionError) {
                throw new AssertionError("ConfigModifier indexed with " + indexHolder.getO() + " failed." + assertionError);
            }
            indexHolder.setO(indexHolder.getO() + 1);
        });
        // Delete
        crudToBeTested.delete(config3.getId());
        assertEquals(0, crudToBeTested.getObjectNameIds().size());
        doFinalAssert();
    }

    protected <T> List<T> add(List<T> readonly, T entity) {
        List<T> list = new ArrayList<>(readonly);
        list.add(entity);
        return list;
    }

    protected <T> List<T> remove(List<T> readonly, int index) {
        List<T> list = new ArrayList<>(readonly);
        list.remove(index);
        return list;
    }

    protected <T> List<T> reverse(List<T> readonly) {
        List<T> list = new ArrayList<>(readonly);
        Collections.reverse(list);
        return list;
    }

}
