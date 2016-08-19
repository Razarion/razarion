package com.btxtech.client.editor.terrainobject;

import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.client.editor.slopeeditor.SlopeConfigPanel;
import com.btxtech.shared.TerrainElementEditorProvider;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.ValueListBox;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 16.08.2016.
 */
@Templated("TerrainObjectConfigCrudSidebar.html#terrain-object-config-crud-sidebar")
public class TerrainObjectConfigCrudSidebar extends Composite implements LeftSideBarContent {
    private Logger logger = Logger.getLogger(TerrainObjectConfigCrudSidebar.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private Caller<TerrainElementEditorProvider> provider;
    @Inject
    private Instance<TerrainObjectConfigPanel> configPanelInstance;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private ValueListBox<ObjectNameId> terrainObjectSelection;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button newTerrainObject;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button delete;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button save;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label loadingLabel;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private SimplePanel content;

    @PostConstruct
    public void init() {
        updateSlopeSelection();
        terrainObjectSelection.addValueChangeHandler(event -> loadTerrainObjectConfig(terrainObjectSelection.getValue()));
    }

    @EventHandler("newTerrainObject")
    private void newTerrainObjectButtonClick(ClickEvent event) {
        TerrainObjectConfig terrainObjectConfig = new TerrainObjectConfig();
        initEditor(terrainObjectConfig);
        terrainObjectSelection.setValue(null);
    }

    @EventHandler("delete")
    private void deleteButtonClick(ClickEvent event) {
        // TODO are you sure dialog
        TerrainObjectConfig terrainObjectConfig = getTerrainObjectConfig();
        hideEditor();
        if (terrainObjectConfig != null && terrainObjectConfig.hasId()) {
            // TODO let TerrainService handle this
            provider.call(response -> updateSlopeSelection(), (message, throwable) -> {
                logger.log(Level.SEVERE, "deleteTerrainObjectConfig failed: " + message, throwable);
                return false;
            }).deleteTerrainObjectConfig(terrainObjectConfig);
        }
    }

    @EventHandler("save")
    private void saveButtonClick(ClickEvent event) {
        // TODO let TerrainService handle this
        provider.call(new RemoteCallback<TerrainObjectConfig>() {
            @Override
            public void callback(TerrainObjectConfig terrainObjectConfig) {
                initEditor(terrainObjectConfig);
                updateSlopeSelection();
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "saveTerrainObjectConfig failed: " + message, throwable);
            return false;
        }).saveTerrainObjectConfig(getTerrainObjectConfig());
    }

    private void updateSlopeSelection() {
        terrainObjectSelection.setValue(null);
        provider.call(new RemoteCallback<Collection<ObjectNameId>>() {
            @Override
            public void callback(Collection<ObjectNameId> objectNameIds) {
                terrainObjectSelection.setAcceptableValues(objectNameIds);
                TerrainObjectConfig terrainObjectConfig = getTerrainObjectConfig();
                if (terrainObjectConfig != null && terrainObjectConfig.hasId()) {
                    terrainObjectSelection.setValue(terrainObjectConfig.createSlopeNameId());
                }

            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "getTerrainObjectNameIds failed: " + message, throwable);
            return false;
        }).getTerrainObjectNameIds();
    }

    private void loadTerrainObjectConfig(ObjectNameId value) {
        loadingLabel.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        provider.call(new RemoteCallback<TerrainObjectConfig>() {
            @Override
            public void callback(TerrainObjectConfig terrainObjectConfig) {
                loadingLabel.getElement().getStyle().setDisplay(Style.Display.NONE);
                initEditor(terrainObjectConfig);
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "loadTerrainObjectConfig failed: " + message, throwable);
            return false;
        }).loadTerrainObjectConfig(value.getId());
    }

    private TerrainObjectConfig getTerrainObjectConfig() {
        if (content.getWidget() == null) {
            return null;
        }
        return ((TerrainObjectConfigPanel) content.getWidget()).getTerrainObjectConfig();
    }

    private void initEditor(TerrainObjectConfig terrainObjectConfig) {
        try {
            TerrainObjectConfigPanel terrainObjectConfigPanel = configPanelInstance.get();
            terrainObjectConfigPanel.init(terrainObjectConfig);
            content.setWidget(terrainObjectConfigPanel);
            delete.getElement().getStyle().setDisplay(Style.Display.BLOCK);
            save.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "initEditor failed: " + e.getMessage(), e);
        }
    }

    private void hideEditor() {
        content.clear();
        delete.getElement().getStyle().setDisplay(Style.Display.NONE);
        save.getElement().getStyle().setDisplay(Style.Display.NONE);
    }

    @Override
    public void onClose() {

    }
}
