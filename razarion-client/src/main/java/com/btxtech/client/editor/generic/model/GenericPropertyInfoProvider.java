package com.btxtech.client.editor.generic.model;

import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.editor.CollectionReferenceInfo;
import com.btxtech.shared.dto.editor.CustomEditorInfo;
import com.btxtech.shared.dto.editor.GenericPropertyInfo;
import com.btxtech.shared.rest.GenericPropertyEditorController;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.databinding.client.BindableProxyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class GenericPropertyInfoProvider {
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

    public Object provideListElement(Class type, String listPropertyName) {
        if (listElementTypes == null) {
            throw new IllegalArgumentException("No listElementTypes received");
        }

        Map<String, String> listProperties = listElementTypes.get(type.getName());
        if (listProperties == null) {
            throw new IllegalArgumentException("No list properties in '" + type + "'");
        }
        String typeArgument = listProperties.get(listPropertyName);
        if (typeArgument == null) {
            throw new IllegalArgumentException("No type argument found for list property '" + listPropertyName + "'  in '" + type + "'");
        }

        if (Double.class.getName().equals(typeArgument)) {
            return (double) 0;
        } else if (Integer.class.getName().equals(typeArgument)) {
            return 0;
        } else if (String.class.getName().equals(typeArgument)) {
            return "";
        }

        return BindableProxyFactory.getBindableProxy(typeArgument);
    }

    public CollectionReferenceInfo scanForCollectionReference(Class<?> type, String propertyName) {
        if (typesWithCollectionReference == null) {
            throw new IllegalArgumentException("No typesWithCollectionReference received");
        }

        Map<String, CollectionReferenceInfo> collectionReferenceProperties = typesWithCollectionReference.get(type.getName());
        if (collectionReferenceProperties == null) {
            return null;
        }
        return collectionReferenceProperties.get(propertyName);
    }

    public CustomEditorInfo scanForCustomEditor(Class<?> type, String propertyName) {
        if (typesWithCustomEditor == null) {
            throw new IllegalArgumentException("No typesWithCustomEditor received");
        }

        Map<String, CustomEditorInfo> customEditorProperties = typesWithCustomEditor.get(type.getName());
        if (customEditorProperties == null) {
            return null;
        }
        return customEditorProperties.get(propertyName);
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
