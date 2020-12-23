package com.btxtech.client.editor.generic.model;

import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.editor.GenericPropertyInfo;
import com.btxtech.shared.dto.editor.OpenApi3Schema;
import com.btxtech.shared.rest.GenericPropertyEditorController;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.databinding.client.BindableProxy;
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
    private Map<String, Map<String, OpenApi3Schema>> typesWithOpenApi3Schema;

    public void load() {
        genericPropertyEditorController.call(response -> setup((GenericPropertyInfo) response),
                exceptionHandler.restErrorHandler("GenericPropertyEditorController.getListTypeArguments()")).getGenericPropertyInfo();
    }

    public BindableProxy provideListElementType(Class type, String listPropertyName) {
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
        return BindableProxyFactory.getBindableProxy(typeArgument);
    }

    public OpenApi3Schema scanForOpenApiScheme(Class type, String propertyName) {
        if (typesWithOpenApi3Schema == null) {
            throw new IllegalArgumentException("No typesWithOpenApi3Schema received");
        }

        Map<String, OpenApi3Schema> schemaProperties = typesWithOpenApi3Schema.get(type.getName());
        if (schemaProperties == null) {
            return null;
        }
        return schemaProperties.get(propertyName);
    }

    private void setup(GenericPropertyInfo genericPropertyInfo) {
        listElementTypes = genericPropertyInfo.getListElementTypes();
        typesWithOpenApi3Schema = new HashMap<>();
        genericPropertyInfo.getOpenApi3Schemas().forEach(openApi3Schema -> {
            Map<String, OpenApi3Schema> propertySchemas = typesWithOpenApi3Schema.computeIfAbsent(openApi3Schema.getJavaParentPropertyClass(), s -> new HashMap<>());
            propertySchemas.put(openApi3Schema.getJavaPropertyName(), openApi3Schema);
        });
    }
}
