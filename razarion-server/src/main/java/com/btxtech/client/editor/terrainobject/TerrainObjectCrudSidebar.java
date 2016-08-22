package com.btxtech.client.editor.terrainobject;

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
@Templated("TerrainObjectCrudSidebar.html#terrain-object-crude-sidebar")
public class TerrainObjectCrudSidebar extends Composite implements LeftSideBarContent {
    // private Logger logger = Logger.getLogger(TerrainObjectCrudSidebar.class.getName());
    @Inject
    private TerrainObjectCrud terrainObjectCrud;
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private Instance<TerrainObjectPropertyPanel> configPanelInstance;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private ValueListBox<ObjectNameId> terrainObjectSelection;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button createButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button deleteButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button saveButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button reloadButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private SimplePanel content;

    @PostConstruct
    public void init() {
        terrainObjectCrud.monitor(this::updateTerrainObjectSelector);
        terrainObjectSelection.addValueChangeHandler(event -> displayPropertyBook(terrainObjectSelection.getValue()));
    }

    @EventHandler("createButton")
    private void newTerrainObjectButtonClick(ClickEvent event) {
        terrainObjectCrud.create();
    }

    @EventHandler("deleteButton")
    private void deleteButtonClick(ClickEvent event) {
        TerrainObjectConfig terrainObjectConfig = getTerrainObjectConfig();
        if (terrainObjectConfig != null) {
            terrainObjectCrud.delete(terrainObjectConfig);
        }
//        // TODO are you sure dialog
//        TerrainObjectConfig terrainObjectConfig = getTerrainObjectConfig();
//        hideEditor();
//        if (terrainObjectConfig != null && terrainObjectConfig.hasId()) {
//            // TODO let TerrainService handle this
//            provider.call(response -> updateTerrainObjectSelector(), (message, throwable) -> {
//                logger.log(Level.SEVERE, "deleteTerrainObjectConfig failed: " + message, throwable);
//                return false;
//            }).deleteTerrainObjectConfig(terrainObjectConfig);
//        }
    }

    @EventHandler("saveButton")
    private void saveButtonClick(ClickEvent event) {
        TerrainObjectConfig terrainObjectConfig = getTerrainObjectConfig();
        if (terrainObjectConfig != null) {
            terrainObjectCrud.save(terrainObjectConfig);
        }

//        // TODO let TerrainService handle this
//        provider.call(new RemoteCallback<TerrainObjectConfig>() {
//            @Override
//            public void callback(TerrainObjectConfig terrainObjectConfig) {
//                displayPropertyBook(terrainObjectConfig);
//                updateTerrainObjectSelector();
//            }
//        }, (message, throwable) -> {
//            logger.log(Level.SEVERE, "saveTerrainObjectConfig failed: " + message, throwable);
//            return false;
//        }).saveTerrainObjectConfig(getTerrainObjectConfig());
    }

    @EventHandler("reloadButton")
    private void reloadButtonClick(ClickEvent event) {
        terrainObjectCrud.reload();
    }

    private void updateTerrainObjectSelector(List<ObjectNameId> objectNameIds) {
        terrainObjectSelection.setAcceptableValues(objectNameIds);
        TerrainObjectConfig terrainObjectConfig = getTerrainObjectConfig();
        if (terrainObjectConfig != null) {
            terrainObjectSelection.setValue(terrainObjectConfig.createSlopeNameId());
        } else {
            terrainObjectSelection.setValue(null);
        }
//
//
//        provider.call(objectNameIds1 -> {
//
//        }, (message, throwable) -> {
//            logger.log(Level.SEVERE, "getTerrainObjectNameIds failed: " + message, throwable);
//            return false;
//        }).getTerrainObjectNameIds();
    }

//    private void displayTerrainObjectProperties(ObjectNameId objectNameId) {
//        loadingLabel.getElement().getStyle().setDisplay(Style.Display.BLOCK);
//        provider.call(new RemoteCallback<TerrainObjectConfig>() {
//            @Override
//            public void callback(TerrainObjectConfig terrainObjectConfig) {
//                loadingLabel.getElement().getStyle().setDisplay(Style.Display.NONE);
//                displayPropertyBook(terrainObjectConfig);
//            }
//        }, (message, throwable) -> {
//            logger.log(Level.SEVERE, "displayTerrainObjectProperties failed: " + message, throwable);
//            return false;
//        }).readTerrainObjectConfig(objectNameId.getId());
//    }

    private TerrainObjectConfig getTerrainObjectConfig() {
        if (content.getWidget() == null) {
            return null;
        }
        return ((TerrainObjectPropertyPanel) content.getWidget()).getTerrainObjectConfig();
    }

    private void displayPropertyBook(ObjectNameId objectNameId) {
        TerrainObjectPropertyPanel terrainObjectPropertyPanel = configPanelInstance.get();
        terrainObjectPropertyPanel.init(terrainTypeService.getTerrainObjectConfig(objectNameId.getId()));
        content.setWidget(terrainObjectPropertyPanel);
        deleteButton.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        saveButton.getElement().getStyle().setDisplay(Style.Display.BLOCK);
    }

    @Override
    public void onClose() {
        terrainObjectCrud.removeMonitor(this::updateTerrainObjectSelector);
    }
}
