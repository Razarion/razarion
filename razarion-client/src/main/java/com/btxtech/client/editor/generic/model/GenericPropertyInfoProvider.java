package com.btxtech.client.editor.generic.model;

import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.editor.CollectionReferenceInfo;
import com.btxtech.shared.dto.editor.CustomEditorInfo;
import com.btxtech.shared.dto.editor.GenericPropertyInfo;
import com.btxtech.shared.rest.GenericPropertyEditorController;
import org.jboss.errai.common.client.api.Caller;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class GenericPropertyInfoProvider {
    // private static Logger logger = Logger.getLogger(GenericPropertyInfoProvider.class.getName());
    @Inject
    private Caller<GenericPropertyEditorController> genericPropertyEditorController;
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    private Map<String, Map<String, String>> listElementTypes;
    private Map<String, Map<String, CollectionReferenceInfo>> typesWithCollectionReference;
    private Map<String, Map<String, CustomEditorInfo>> typesWithCustomEditor;

    public void load() {
        if(listElementTypes != null) {
            return;
        }
        genericPropertyEditorController.call(response -> setup((GenericPropertyInfo) response),
                exceptionHandler.restErrorHandler("GenericPropertyEditorController.getListTypeArguments()")).getGenericPropertyInfo();
    }

    public CollectionReferenceInfo scanForCollectionReference(Class<?> type, String propertyName) {
        if (typesWithCollectionReference == null) {
            throw new IllegalArgumentException("No typesWithCollectionReference received");
        }

        Map<String, CollectionReferenceInfo> collectionReferenceProperties = typesWithCollectionReference.get(type.getName());
        if (collectionReferenceProperties != null && collectionReferenceProperties.containsKey(propertyName)) {
            return collectionReferenceProperties.get(propertyName);
        }
        if (type.getSuperclass() == Object.class) {
            return null;
        }
        return scanForCollectionReference(type.getSuperclass(), propertyName);
    }

    public CustomEditorInfo scanForCustomEditor(Class<?> type, String propertyName) {
        if (typesWithCustomEditor == null) {
            throw new IllegalArgumentException("No typesWithCustomEditor received");
        }

        Map<String, CustomEditorInfo> customEditorProperties = typesWithCustomEditor.get(type.getName());
        if (customEditorProperties != null && customEditorProperties.containsKey(propertyName)) {
            return customEditorProperties.get(propertyName);
        }
        if (type.getSuperclass() == Object.class) {
            return null;
        }
        return scanForCustomEditor(type.getSuperclass(), propertyName);
    }

    private void setup(GenericPropertyInfo genericPropertyInfo) {
        listElementTypes = genericPropertyInfo.getListElementTypes();
        typesWithCollectionReference = new HashMap<>();
        typesWithCustomEditor = new HashMap<>();
        genericPropertyInfo.getCollectionReferenceInfos().forEach(collectionReferenceInfo -> {
            typesWithCollectionReference.computeIfAbsent(collectionReferenceInfo.getJavaParentPropertyClass(), s -> new HashMap<>())
                    .put(collectionReferenceInfo.getJavaPropertyName(), collectionReferenceInfo);
        });
        genericPropertyInfo.getCustomEditorInfos().forEach(customEditorInfo -> {
            typesWithCustomEditor.computeIfAbsent(customEditorInfo.getJavaParentPropertyClass(), s -> new HashMap<>())
                    .put(customEditorInfo.getJavaPropertyName(), customEditorInfo);
        });
    }
}
