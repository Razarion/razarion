package com.btxtech.client.editor.generic.model;

import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.editor.GenericPropertyInfo;
import com.btxtech.shared.dto.editor.CollectionReferenceInfo;
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
            throw new IllegalArgumentException("No typesWithOpenApi3Schema received");
        }

        Map<String, CollectionReferenceInfo> schemaProperties = typesWithCollectionReference.get(type.getName());
        if (schemaProperties == null) {
            return null;
        }
        return schemaProperties.get(propertyName);
    }

    private void setup(GenericPropertyInfo genericPropertyInfo) {
        listElementTypes = genericPropertyInfo.getListElementTypes();
        typesWithCollectionReference = new HashMap<>();
        genericPropertyInfo.getCollectionReferenceInfos().forEach(collectionReferenceInfo -> {
            Map<String, CollectionReferenceInfo> propertySchemas = typesWithCollectionReference.computeIfAbsent(collectionReferenceInfo.getJavaParentPropertyClass(), s -> new HashMap<>());
            propertySchemas.put(collectionReferenceInfo.getJavaPropertyName(), collectionReferenceInfo);
        });
    }
}
