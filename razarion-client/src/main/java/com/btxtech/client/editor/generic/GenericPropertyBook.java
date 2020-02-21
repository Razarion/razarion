package com.btxtech.client.editor.generic;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.BindableProxyFactory;
import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.stream.Collectors;

@Templated("GenericPropertyBook.html#genericPropertyBook")
public class GenericPropertyBook extends AbstractPropertyPanel<ObjectNameIdProvider> {
    // private Logger logger = Logger.getLogger(GenericPropertyBook.class.getName());
    @Inject
    @DataField
    @ListContainer("tbody")
    private ListComponent<PropertyModel, PropertyWidget> propertyTable;
    private ObjectNameIdProvider genericObjectNameIdProvider;

    @Override
    public void init(ObjectNameIdProvider objectNameIdProvider) {
        this.genericObjectNameIdProvider = objectNameIdProvider;
        DOMUtil.removeAllElementChildren(propertyTable.getElement()); // Remove placeholder table row from template.
        HasProperties hasProperties = (HasProperties) BindableProxyFactory.getBindableProxy(objectNameIdProvider);
        propertyTable.setValue(hasProperties.getBeanProperties().entrySet().stream().map(entry -> new PropertyModel(entry.getKey(), entry.getValue(), hasProperties)).collect(Collectors.toList()));
    }

    @Override
    public ObjectNameIdProvider getConfigObject() {
        return genericObjectNameIdProvider;
    }
}
