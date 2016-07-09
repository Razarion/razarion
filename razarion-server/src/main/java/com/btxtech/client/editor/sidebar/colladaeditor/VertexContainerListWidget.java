package com.btxtech.client.editor.sidebar.colladaeditor;

import com.btxtech.shared.dto.VertexContainer;
import org.jboss.errai.ui.client.widget.ListWidget;

/**
 * Created by Beat
 * 15.06.2016.
 */
public class VertexContainerListWidget extends ListWidget<VertexContainer, VertexContainerWidget> {
    @Override
    protected Class<VertexContainerWidget> getItemWidgetType() {
        return VertexContainerWidget.class;
    }
}
