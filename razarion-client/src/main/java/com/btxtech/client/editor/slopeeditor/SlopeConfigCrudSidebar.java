package com.btxtech.client.editor.slopeeditor;

import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.shared.TerrainElementEditorProvider;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
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
 * 22.11.2015.
 */
@Templated("SlopeConfigCrudSidebar.html#slope-config-crud-panel")
public class SlopeConfigCrudSidebar extends LeftSideBarContent {
    private Logger logger = Logger.getLogger(SlopeConfigCrudSidebar.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private Caller<TerrainElementEditorProvider> provider;
    @Inject
    private Instance<SlopeConfigPanel> slopeConfigPanelInstance;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private SimplePanel content;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private ValueListBox<ObjectNameId> slopeSelection;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button newSlope;
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

    @PostConstruct
    public void init() {
        updateSlopeSelection();
        slopeSelection.addValueChangeHandler(event -> loadSlopeConfig(slopeSelection.getValue()));
    }

    private void updateSlopeSelection() {
        slopeSelection.setValue(null);
        provider.call(new RemoteCallback<Collection<ObjectNameId>>() {
            @Override
            public void callback(Collection<ObjectNameId> objectNameIds) {
                slopeSelection.setAcceptableValues(objectNameIds);
                SlopeConfig slopeConfig = getSlopeConfig();
                if (slopeConfig != null && slopeConfig.hasId()) {
                    slopeSelection.setValue(slopeConfig.createSlopeNameId());
                }

            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "getSlopeNameIds failed: " + message, throwable);
            return false;
        }).getSlopeNameIds();
    }

    @EventHandler("newSlope")
    private void newSlopeButtonClick(ClickEvent event) {
        SlopeConfig slopeConfig = new SlopeConfig();
        slopeConfig.setSlopeSkeletonConfig(new SlopeSkeletonConfig());
        slopeConfig.getSlopeSkeletonConfig().setSegments(1);
        slopeConfig.getSlopeSkeletonConfig().setVerticalSpace(30);
        List<SlopeShape> slopeShapes = new ArrayList<>();
        slopeShapes.add(new SlopeShape(new DecimalPosition(0, 0), 0));
        slopeShapes.add(new SlopeShape(new DecimalPosition(20, 20), 0));
        slopeConfig.setShape(slopeShapes);
        initEditor(slopeConfig);
        slopeSelection.setValue(null);
    }

    @EventHandler("delete")
    private void deleteButtonClick(ClickEvent event) {
        // TODO are you sure dialog
        SlopeConfig slopeConfig = getSlopeConfig();
        hideEditor();
        if (slopeConfig != null && slopeConfig.hasId()) {
            provider.call(response -> updateSlopeSelection(), (message, throwable) -> {
                logger.log(Level.SEVERE, "deleteSlopeConfig failed: " + message, throwable);
                return false;
            }).deleteSlopeConfig(slopeConfig);
        }
    }

    @EventHandler("save")
    private void saveButtonClick(ClickEvent event) {
        provider.call(new RemoteCallback<SlopeConfig>() {
            @Override
            public void callback(SlopeConfig slopeConfig) {
                initEditor(slopeConfig);
                updateSlopeSelection();
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "saveSlopeConfig failed: " + message, throwable);
            return false;
        }).saveSlopeConfig(getSlopeConfig());
    }

    private void loadSlopeConfig(ObjectNameId value) {
        loadingLabel.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        provider.call(new RemoteCallback<SlopeConfig>() {
            @Override
            public void callback(SlopeConfig slopeConfig) {
                loadingLabel.getElement().getStyle().setDisplay(Style.Display.NONE);
                initEditor(slopeConfig);
            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "loadSlopeConfig failed: " + message, throwable);
            return false;
        }).loadSlopeConfig(value.getId());

    }

    private void initEditor(SlopeConfig slopeConfig) {
        try {
            Double zoom = null;
            if (content.getWidget() != null) {
                zoom = ((SlopeConfigPanel) content.getWidget()).getZoom();
            }

            SlopeConfigPanel slopeConfigPanel = slopeConfigPanelInstance.get();
            slopeConfigPanel.init(slopeConfig, zoom);
            content.setWidget(slopeConfigPanel);
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

    private SlopeConfig getSlopeConfig() {
        if (content.getWidget() == null) {
            return null;
        }
        return ((SlopeConfigPanel) content.getWidget()).getSlopeConfig();
    }
}
