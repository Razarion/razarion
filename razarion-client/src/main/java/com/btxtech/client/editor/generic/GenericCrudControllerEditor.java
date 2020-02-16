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

public class GenericCrudControllerEditor extends AbstractCrudeEditor<ObjectNameIdProvider> {
    // private Logger logger = Logger.getLogger(GenericCrudControllerEditor.class.getName());
    private Class<? extends CrudController> crudControllerClass;
    private List<ObjectNameId> objectNameIds = new ArrayList<>(); // Needs to be initialized due to frameworks restriction

    public void init(Class<? extends CrudController> crudControllerClass) {
        this.crudControllerClass = crudControllerClass;
        MessageBuilder.createCall((RemoteCallback<List<ObjectNameId>>) objectNameIds -> {
            GenericCrudControllerEditor.this.objectNameIds = objectNameIds;
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
    public void delete(ObjectNameIdProvider objectNameIdProvider) {

    }

    @Override
    public void save(ObjectNameIdProvider objectNameIdProvider) {

    }

    @Override
    public void reload() {

    }

    @Override
    public void getInstance(ObjectNameId id, Consumer<ObjectNameIdProvider> callback) {
        MessageBuilder.createCall((RemoteCallback<ObjectNameIdProvider>) callback::accept, crudControllerClass).read(id.getId());
    }

    @Override
    protected List<ObjectNameId> setupObjectNameIds() {
        return objectNameIds;
    }
}
