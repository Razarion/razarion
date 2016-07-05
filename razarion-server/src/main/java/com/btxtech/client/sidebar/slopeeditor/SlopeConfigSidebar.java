package com.btxtech.client.sidebar.slopeeditor;

import com.btxtech.client.sidebar.LeftSideBarContent;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.TerrainEditorService;
import com.btxtech.shared.dto.SlopeConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.SlopeSkeleton;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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
@Templated("SlopeConfigSidebar.html#slopeConfigSidebar")
public class SlopeConfigSidebar extends Composite implements LeftSideBarContent {
    private Logger logger = Logger.getLogger(SlopeConfigSidebar.class.getName());
    @Inject
    private Caller<TerrainEditorService> terrainEditorService;
    @Inject
    private Instance<SlopeConfigPanel> plateauPanelInstance;
    @Inject
    @DataField
    private SimplePanel content;
    @Inject
    @DataField
    private ValueListBox<ObjectNameId> slopeSelection;
    @Inject
    @DataField
    private Button newSlope;
    @Inject
    @DataField
    private Button delete;
    @Inject
    @DataField
    private Button save;
    @Inject
    @DataField
    private Label loadingLabel;

    @PostConstruct
    public void init() {
        updateSlopeSelection();
        slopeSelection.addValueChangeHandler(new ValueChangeHandler<ObjectNameId>() {
            @Override
            public void onValueChange(ValueChangeEvent<ObjectNameId> event) {
                loadSlopeConfig(slopeSelection.getValue());
            }
        });
    }

    @Override
    public void onClose() {
        // Ignore
    }

    private void updateSlopeSelection() {
        slopeSelection.setValue(null);
        terrainEditorService.call(new RemoteCallback<Collection<ObjectNameId>>() {
            @Override
            public void callback(Collection<ObjectNameId> objectNameIds) {
                slopeSelection.setAcceptableValues(objectNameIds);
                SlopeConfig slopeConfig = getSlopeConfig();
                if (slopeConfig != null && slopeConfig.hasId()) {
                    slopeSelection.setValue(slopeConfig.createSlopeNameId());
                }

            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "getSlopeNameIds failed: " + message, throwable);
                return false;
            }
        }).getSlopeNameIds();
    }

    @EventHandler("newSlope")
    private void newSlopeButtonClick(ClickEvent event) {
        SlopeConfig slopeConfig = new SlopeConfig();
        slopeConfig.setSlopeSkeleton(new SlopeSkeleton());
        slopeConfig.getSlopeSkeleton().setSegments(1);
        slopeConfig.getSlopeSkeleton().setVerticalSpace(30);
        List<SlopeShape> slopeShapes = new ArrayList<>();
        slopeShapes.add(new SlopeShape(new Index(0, 0), 0));
        slopeShapes.add(new SlopeShape(new Index(20, 20), 0));
        slopeConfig.setShape(slopeShapes);
        initEditor(slopeConfig);
        slopeSelection.setValue(null);
    }

    @EventHandler("delete")
    private void deleteButtonClick(ClickEvent event) {
        // TODO are you sure dialog
        SlopeConfig slopeConfig = getSlopeConfig();
        hideEditor();
        if (slopeConfig.hasId()) {
            terrainEditorService.call(new RemoteCallback<Void>() {
                @Override
                public void callback(Void response) {
                    updateSlopeSelection();
                }
            }, new ErrorCallback<Object>() {
                @Override
                public boolean error(Object message, Throwable throwable) {
                    logger.log(Level.SEVERE, "deleteSlopeConfig failed: " + message, throwable);
                    return false;
                }
            }).deleteSlopeConfig(slopeConfig);
        }
    }

    @EventHandler("save")
    private void saveButtonClick(ClickEvent event) {
        terrainEditorService.call(new RemoteCallback<SlopeConfig>() {
            @Override
            public void callback(SlopeConfig slopeConfig) {
                initEditor(slopeConfig);
                updateSlopeSelection();
            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "saveSlopeConfig failed: " + message, throwable);
                return false;
            }
        }).saveSlopeConfig(getSlopeConfig());
    }

    private void loadSlopeConfig(ObjectNameId value) {
        loadingLabel.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        terrainEditorService.call(new RemoteCallback<SlopeConfig>() {
            @Override
            public void callback(SlopeConfig slopeConfig) {
                loadingLabel.getElement().getStyle().setDisplay(Style.Display.NONE);
                initEditor(slopeConfig);
            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "loadSlopeConfig failed: " + message, throwable);
                return false;
            }
        }).loadSlopeConfig(value.getId());

    }

    private void initEditor(SlopeConfig slopeConfig) {
        try {
            Double zoom = null;
            if (content.getWidget() != null) {
                zoom = ((SlopeConfigPanel) content.getWidget()).getZoom();
            }

            SlopeConfigPanel slopeConfigPanel = plateauPanelInstance.get();
            slopeConfigPanel.init(slopeConfig, zoom);
            content.setWidget(slopeConfigPanel);
            delete.getElement().getStyle().setDisplay(Style.Display.BLOCK);
            save.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        } catch(Exception e) {
            logger.log(Level.SEVERE, "loadSlopeConfig failed: " + e.getMessage(), e);
        }
    }

    private void hideEditor() {
        content.clear();
        delete.getElement().getStyle().setDisplay(Style.Display.NONE);
        save.getElement().getStyle().setDisplay(Style.Display.NONE);
    }

    private SlopeConfig getSlopeConfig() {
        return ((SlopeConfigPanel) content.getWidget()).getSlopeConfig();
    }
}
