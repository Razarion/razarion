package com.btxtech.client.editor.generic;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Templated("GenericPropertyPanel.html#genericPropertyPanel")
public class GenericPropertyPanel extends AbstractPropertyPanel<ObjectNameIdProvider> {
    private ObjectNameIdProvider genericObjectNameIdProvider;

    @Override
    public ObjectNameIdProvider getConfigObject() {
        return genericObjectNameIdProvider;
    }

    @Override
    public void init(ObjectNameIdProvider genericObjectNameIdProvider) {
        this.genericObjectNameIdProvider = genericObjectNameIdProvider;
    }
}
