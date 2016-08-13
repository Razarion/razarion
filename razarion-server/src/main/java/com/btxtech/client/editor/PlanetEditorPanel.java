package com.btxtech.client.editor;

import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.shared.api.annotations.Templated;

/**
 * Created by Beat
 * 13.08.2016.
 */
@Templated("PlanetEditorPanel.html#planet-editor-panel")
public class PlanetEditorPanel extends Composite implements LeftSideBarContent {
    @Override
    public void onClose() {

    }
}
