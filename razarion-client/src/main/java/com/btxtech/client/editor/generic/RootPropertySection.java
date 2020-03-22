package com.btxtech.client.editor.generic;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.generic.propertyeditors.PropertySection;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

// TODO Rename to GenericPropertyBook -> RootPropertySection
// TODO Rename to PropertyPage -> PropertySection
@Templated("RootPropertySection.html#rootPropertySection")
public class RootPropertySection extends AbstractPropertyPanel<ObjectNameIdProvider> {
    // private Logger logger = Logger.getLogger(GenericPropertyBook.class.getName());
    @Inject
    @DataField
    private PropertySection rootPropertySection;
    @Inject
    private PropertyModel rootPropertyModel;

    @Override
    public void init(ObjectNameIdProvider objectNameIdProvider) {
        rootPropertyModel.initAsRoot(objectNameIdProvider);
        rootPropertySection.init(rootPropertyModel);
    }

    @Override
    public ObjectNameIdProvider getConfigObject() {
        return (ObjectNameIdProvider) rootPropertyModel.getPropertyValue();
    }
}
