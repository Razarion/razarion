package com.btxtech.client.editor.generic;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import com.btxtech.shared.rest.CrudController;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.common.client.api.RemoteCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GenericCrudController extends AbstractCrudeEditor<GenericObjectNameIdProvider> {
    private Class<? extends CrudController> crudControllerClass;
    private List<ObjectNameId> objectNameIds = new ArrayList<>(); // Needs to be initialized due to frameworks restriction

    public void init(Class<? extends CrudController> crudControllerClass) {
        this.crudControllerClass = crudControllerClass;
        MessageBuilder.createCall((RemoteCallback<List<ObjectNameId>>) objectNameIds -> {
            GenericCrudController.this.objectNameIds = objectNameIds;
            fire();
        }, crudControllerClass).getObjectNameIds();
    }

    @Override
    public void create() {
        MessageBuilder.createCall((RemoteCallback<ObjectNameIdProvider>) objectNameIdProvider -> {
            objectNameIds.add(objectNameIdProvider.createObjectNameId());
            fire();
            fireSelection(objectNameIdProvider.createObjectNameId());
        }, crudControllerClass).create();
    }

    @Override
    public void delete(GenericObjectNameIdProvider genericObjectNameIdProvider) {

    }

    @Override
    public void save(GenericObjectNameIdProvider genericObjectNameIdProvider) {

    }

    @Override
    public void reload() {

    }

    @Override
    public void getInstance(ObjectNameId id, Consumer<GenericObjectNameIdProvider> callback) {

    }

    @Override
    protected List<ObjectNameId> setupObjectNameIds() {
        return objectNameIds;
    }
}
