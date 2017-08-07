package com.btxtech.client.editor.framework;

import com.btxtech.client.editor.sidebar.LeftSideBarStackContent;
import com.btxtech.shared.dto.ObjectNameId;

/**
 * Created by Beat
 * on 06.08.2017.
 */
public abstract class ObjectNamePropertyPanel extends LeftSideBarStackContent {
    public abstract void setObjectNameId(ObjectNameId objectNameId);

    public abstract Object getConfigObject();

    public Object getPredecessorConfigObject() {
        Object panel = getPredecessor().getContent();
        if (panel instanceof AbstractCrudeParentSidebar) {
            return ((AbstractCrudeParentSidebar) panel).getConfigObject();
        } else if (panel instanceof ObjectNamePropertyPanel) {
            return ((ObjectNamePropertyPanel) panel).getConfigObject();
        } else {
            throw new IllegalStateException("ObjectNamePropertyPanel.getPredecessorConfigObject() unknown panel: " + panel);
        }
    }


}
