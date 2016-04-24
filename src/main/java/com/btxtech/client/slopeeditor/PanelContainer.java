package com.btxtech.client.slopeeditor;

import com.btxtech.client.terrain.slope.skeleton.SlopeSkeletonFactory;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.SlopeConfigEntity;
import com.btxtech.shared.SlopeNameId;
import com.btxtech.shared.SlopeShapeEntity;
import com.btxtech.shared.TerrainEditorService;
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
@Templated("PanelContainer.html#editorPanelContainer")
public class PanelContainer extends Composite {
    private Logger logger = Logger.getLogger(PanelContainer.class.getName());
    @Inject
    private Caller<TerrainEditorService> terrainEditorService;
    @Inject
    private Instance<SlopePanel> plateauPanelInstance;
    @Inject
    @DataField
    private SimplePanel content;
    @Inject
    @DataField
    private ValueListBox<SlopeNameId> slopeSelection;
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
    @Inject
    @DataField
    private Button closeButton;

    @PostConstruct
    public void init() {
        slopeSelection.addValueChangeHandler(new ValueChangeHandler<SlopeNameId>() {
            @Override
            public void onValueChange(ValueChangeEvent<SlopeNameId> event) {
                loadSlopeConfigEntity(slopeSelection.getValue());
            }
        });
    }

    public void showPanelContainer() {
        content.clear();
        closeButton.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        newSlope.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        slopeSelection.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        updateSlopeSelection();
    }

    private void updateSlopeSelection() {
        slopeSelection.setValue(null);
        terrainEditorService.call(new RemoteCallback<Collection<SlopeNameId>>() {
            @Override
            public void callback(Collection<SlopeNameId> slopeNameIds) {
                slopeSelection.setAcceptableValues(slopeNameIds);
                SlopeConfigEntity slopeConfigEntity = getSlopeConfigEntity();
                if (slopeConfigEntity != null && slopeConfigEntity.hasId()) {
                    slopeSelection.setValue(slopeConfigEntity.createSlopeNameId());
                }

            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "save failed: " + message, throwable);
                return false;
            }
        }).getSlopeNameIds();
    }

    @EventHandler("newSlope")
    private void newSlopeButtonClick(ClickEvent event) {
        SlopeConfigEntity slopeConfigEntity = new SlopeConfigEntity();
        slopeConfigEntity.setSegments(1);
        slopeConfigEntity.setVerticalSpace(30);
        List<SlopeShapeEntity> slopeShapeEntityList = new ArrayList<>();
        slopeShapeEntityList.add(new SlopeShapeEntity(new Index(0, 0), 1));
        slopeShapeEntityList.add(new SlopeShapeEntity(new Index(20, 20), 1));
        slopeConfigEntity.setShape(slopeShapeEntityList);
        initEditor(slopeConfigEntity);
        slopeSelection.setValue(null);
    }

    @EventHandler("closeButton")
    private void closeButtonClick(ClickEvent event) {
        hidePanelContainer();
    }

    @EventHandler("delete")
    private void deleteButtonClick(ClickEvent event) {
        // TODO are you sure dialog
        SlopeConfigEntity slopeConfigEntity = getSlopeConfigEntity();
        hideEditor();
        if (slopeConfigEntity.hasId()) {
            terrainEditorService.call(new RemoteCallback<Void>() {
                @Override
                public void callback(Void response) {
                    updateSlopeSelection();
                }
            }, new ErrorCallback<Object>() {
                @Override
                public boolean error(Object message, Throwable throwable) {
                    logger.log(Level.SEVERE, "save failed: " + message, throwable);
                    return false;
                }
            }).delete(slopeConfigEntity);
        }
    }

    @EventHandler("save")
    private void saveButtonClick(ClickEvent event) {
        SlopeSkeletonFactory.sculpt(getSlopeConfigEntity());
        terrainEditorService.call(new RemoteCallback<SlopeConfigEntity>() {
            @Override
            public void callback(SlopeConfigEntity slopeConfigEntity) {
                initEditor(slopeConfigEntity);
                updateSlopeSelection();
            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "save failed: " + message, throwable);
                return false;
            }
        }).save(getSlopeConfigEntity());
    }

    private void loadSlopeConfigEntity(SlopeNameId value) {
        loadingLabel.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        terrainEditorService.call(new RemoteCallback<SlopeConfigEntity>() {
            @Override
            public void callback(SlopeConfigEntity slopeConfigEntity) {
                loadingLabel.getElement().getStyle().setDisplay(Style.Display.NONE);
                initEditor(slopeConfigEntity);
            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "save failed: " + message, throwable);
                return false;
            }
        }).load(value.getId());

    }

    private void initEditor(SlopeConfigEntity slopeConfigEntity) {
        SlopePanel slopePanel = plateauPanelInstance.get();
        slopePanel.init(slopeConfigEntity);
        content.setWidget(slopePanel);
        delete.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        save.getElement().getStyle().setDisplay(Style.Display.BLOCK);
    }

    private void hideEditor() {
        content.clear();
        delete.getElement().getStyle().setDisplay(Style.Display.NONE);
        save.getElement().getStyle().setDisplay(Style.Display.NONE);
    }

    private void hidePanelContainer() {
        hideEditor();
        closeButton.getElement().getStyle().setDisplay(Style.Display.NONE);
        slopeSelection.getElement().getStyle().setDisplay(Style.Display.NONE);
        newSlope.getElement().getStyle().setDisplay(Style.Display.NONE);
        loadingLabel.getElement().getStyle().setDisplay(Style.Display.NONE);
    }

    private SlopeConfigEntity getSlopeConfigEntity() {
        return ((SlopePanel) content.getWidget()).getSlopeConfigEntity();
    }
}
