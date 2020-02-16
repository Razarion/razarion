package com.btxtech.client.editor.generic;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import java.util.logging.Logger;

@Templated("GenericPropertyPanel.html#genericPropertyPanel")
public class GenericPropertyPanel extends AbstractPropertyPanel<ObjectNameIdProvider> {
    private Logger logger = Logger.getLogger(GenericPropertyPanel.class.getName());
    private ObjectNameIdProvider genericObjectNameIdProvider;

    @Override
    public void init(ObjectNameIdProvider genericObjectNameIdProvider) {
        this.genericObjectNameIdProvider = genericObjectNameIdProvider;
        // DomGlobal.console.error(JsObject.getOwnPropertyNames(Js.cast((genericObjectNameIdProvider))));
    }

    @Override
    public ObjectNameIdProvider getConfigObject() {
        return genericObjectNameIdProvider;
    }
}
