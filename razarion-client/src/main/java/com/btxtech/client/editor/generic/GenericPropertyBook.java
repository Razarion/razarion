package com.btxtech.client.editor.generic;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import org.jboss.errai.databinding.client.BindableProxyFactory;
import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

@Templated("GenericPropertyBook.html#propertyPage")
public class GenericPropertyBook extends AbstractPropertyPanel<ObjectNameIdProvider> {
    // private Logger logger = Logger.getLogger(GenericPropertyBook.class.getName());
    @Inject
    @DataField
    private PropertyPage propertyPage;
    private ObjectNameIdProvider genericObjectNameIdProvider;

    @Override
    public void init(ObjectNameIdProvider objectNameIdProvider) {
        this.genericObjectNameIdProvider = objectNameIdProvider;
        propertyPage.init((HasProperties) BindableProxyFactory.getBindableProxy(objectNameIdProvider));
    }

    @Override
    public ObjectNameIdProvider getConfigObject() {
        return genericObjectNameIdProvider;
    }
}
