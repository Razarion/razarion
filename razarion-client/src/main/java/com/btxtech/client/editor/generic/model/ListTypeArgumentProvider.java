package com.btxtech.client.editor.generic.model;

import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.rest.GenericPropertyEditorController;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.databinding.client.BindableProxy;
import org.jboss.errai.databinding.client.BindableProxyFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class ListTypeArgumentProvider {
    @Inject
    private Caller<GenericPropertyEditorController> genericPropertyEditorController;
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    private Map<String, Map<String, String>> allListProperties;

    @PostConstruct
    public void postConstruct() {
        genericPropertyEditorController.call(response -> this.allListProperties = (Map<String, Map<String, String>>) response,
                exceptionHandler.restErrorHandler("GenericPropertyEditorController.getListTypeArguments()")).getListTypeArguments();
    }

    public BindableProxy provide(Class type, String listPropertyName) {
        if (allListProperties == null) {
            throw new IllegalArgumentException("No allListProperties received");
        }

        Map<String, String> listProperties = allListProperties.get(type.getName());
        if (listProperties == null) {
            throw new IllegalArgumentException("No list properties in '" + type + "'");
        }
        String typeArgument = listProperties.get(listPropertyName);
        if (typeArgument == null) {
            throw new IllegalArgumentException("No type argument found for list property '" + listPropertyName + "'  in '" + type + "'");
        }
        return BindableProxyFactory.getBindableProxy(typeArgument);
    }
}
