package com.btxtech.client.editor.generic;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import com.btxtech.shared.rest.CrudController;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GenericCrudControllerEditor extends AbstractCrudeEditor<ObjectNameIdProvider> {
    // private Logger logger = Logger.getLogger(GenericCrudControllerEditor.class.getName());
    @Inject
    private ClientExceptionHandlerImpl clientExceptionHandler;
    private Class<? extends CrudController> crudControllerClass;
    private List<ObjectNameId> objectNameIds = new ArrayList<>(); // Needs to be initialized due to frameworks restriction

    public void init(Class<? extends CrudController> crudControllerClass) {
        this.crudControllerClass = crudControllerClass;
        loadObjectNameId(crudControllerClass);
    }

    private void loadObjectNameId(Class<? extends CrudController> crudControllerClass) {
        MessageBuilder.createCall((RemoteCallback<List<ObjectNameId>>) objectNameIds -> {
                    GenericCrudControllerEditor.this.objectNameIds = objectNameIds;
                    fire();
                }, clientExceptionHandler.busErrorCallback("GenericCrudControllerEditor.loadObjectNameId() " + crudControllerClass),
                crudControllerClass).getObjectNameIds();
    }

    @Override
    public void create() {
        MessageBuilder.createCall((RemoteCallback<ObjectNameIdProvider>) objectNameIdProvider -> {
                    objectNameIds.add(objectNameIdProvider.createObjectNameId());
                    fire();
                    fireSelection(objectNameIdProvider.createObjectNameId());
                },
                clientExceptionHandler.busErrorCallback("GenericCrudControllerEditor.create() " + crudControllerClass),
                crudControllerClass).create();
    }

    @Override
    public void delete(ObjectNameIdProvider objectNameIdProvider) {
        MessageBuilder.createCall(ignore -> loadObjectNameId(crudControllerClass),
                clientExceptionHandler.busErrorCallback("GenericCrudControllerEditor.delete() " + crudControllerClass),
                crudControllerClass).delete(objectNameIdProvider.createObjectNameId().getId());
    }

    @Override
    public void save(ObjectNameIdProvider objectNameIdProvider) {
        MessageBuilder.createCall(ignore -> fire(),
                clientExceptionHandler.busErrorCallback("GenericCrudControllerEditor.objectNameIdProvider() " + crudControllerClass),
                crudControllerClass).update((Config) objectNameIdProvider);
    }

    @Override
    public void reload() {
        loadObjectNameId(crudControllerClass);
    }

    @Override
    public void getInstance(ObjectNameId id, Consumer<ObjectNameIdProvider> callback) {
        MessageBuilder.createCall((RemoteCallback<ObjectNameIdProvider>) callback::accept,
                clientExceptionHandler.busErrorCallback("GenericCrudControllerEditor.objectNameIdProvider() " + crudControllerClass),
                crudControllerClass).read(id.getId());
    }

    @Override
    protected List<ObjectNameId> setupObjectNameIds() {
        return objectNameIds;
    }
}
