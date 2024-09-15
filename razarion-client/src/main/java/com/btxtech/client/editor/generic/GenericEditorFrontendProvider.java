package com.btxtech.client.editor.generic;

import com.btxtech.client.editor.generic.model.GenericPropertyInfoProvider;
import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import elemental2.core.JsArray;
import elemental2.promise.Promise;
import jsinterop.annotations.JsType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.logging.Logger;

@JsType
@ApplicationScoped
public class GenericEditorFrontendProvider {
    @Inject
    private GenericPropertyInfoProvider genericPropertyInfoProvider;
    private final Logger logger = Logger.getLogger(GenericEditorFrontendProvider.class.getName());

    @SuppressWarnings("unused") // Called by Angular
    public JsArray<String> collectionNames() {
        genericPropertyInfoProvider.load();
        JsArray<String> crudControllers = new JsArray<>();
        Arrays.stream(CollectionReferenceType.values())
                .filter(collectionReferenceType -> collectionReferenceType.getCrudControllerClass() != null)
                .forEach(crudControllerEntry -> crudControllers.push(crudControllerEntry.getCollectionName()));
        return crudControllers;
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<ObjectNameId[]> requestObjectNameIds(String collectionName) {
        return new Promise<>((resolve, reject) -> {
//            CollectionReferenceType collectionReferenceType = CollectionReferenceType.getType4CollectionName(collectionName);
//            MessageBuilder.createCall(
//                    (RemoteCallback<List<ObjectNameId>>) response -> resolve.onInvoke(response.toArray(new ObjectNameId[0])),
//                    (message, throwable) -> {
//                        logger.log(Level.SEVERE, "CrudController.getObjectNameIds() " + collectionReferenceType.getCrudControllerClass() + "\n" + message, throwable);
//                        reject.onInvoke(throwable.getMessage());
//                        return false;
//                    },
//                    collectionReferenceType.getCrudControllerClass()).getObjectNameIds();
        });
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<ObjectNameId> requestObjectNameId(String collectionName, int configId) {
        return new Promise<>((resolve, reject) -> {
//            CollectionReferenceType collectionReferenceType = CollectionReferenceType.getType4CollectionName(collectionName);
//            MessageBuilder.createCall(
//                    (RemoteCallback<ObjectNameId>) resolve::onInvoke,
//                    (message, throwable) -> {
//                        logger.log(Level.SEVERE, "CrudController.getObjectNameId() " + collectionReferenceType.getCrudControllerClass() + " id:" + configId + "\n" + message, throwable);
//                        reject.onInvoke(throwable.getMessage());
//                        return false;
//                    },
//                    collectionReferenceType.getCrudControllerClass()).getObjectNameId(configId);
        });
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<Config> createConfig(String collectionName) {
        CollectionReferenceType collectionReferenceType = CollectionReferenceType.getType4CollectionName(collectionName);

//        return new Promise<>((resolve, reject) -> MessageBuilder.createCall(
//                (RemoteCallback<Config>) resolve::onInvoke,
//                (message, throwable) -> {
//                    logger.log(Level.SEVERE, "CrudController.create() " + collectionReferenceType.getCrudControllerClass() + "\n" + message, throwable);
//                    reject.onInvoke(throwable.getMessage());
//                    return false;
//                },
//                collectionReferenceType.getCrudControllerClass()).create());
        return null;
    }

    @SuppressWarnings("unused") // Called by Angular
    public String getPathForCollection(String collectionName) {
        return CollectionReferenceType.getType4CollectionName(collectionName).getPath();
    }
}
