package com.btxtech.client.editor.terrainobject;

import com.btxtech.client.editor.framework.AbstractCrudeParent;
import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.ValueListBox;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 16.08.2016.
 */
@Templated("../framework/AbstractCrudeParent.html#abstract-crud-parent")
public class TerrainObjectCrudSidebar extends AbstractCrudeParent<TerrainObjectConfig, TerrainObjectPropertyPanel> {
    @Inject
    private TerrainObjectCrud terrainObjectCrud;
    @Inject
    private Instance<TerrainObjectPropertyPanel> configPanelInstance;

    @Override
    protected TerrainObjectCrud getCrudEditor() {
        return terrainObjectCrud;
    }

    @Override
    protected TerrainObjectPropertyPanel createPropertyPanel() {
        return configPanelInstance.get();
    }
}
