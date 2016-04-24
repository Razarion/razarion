package com.btxtech.client.slopeeditor;

import com.btxtech.shared.SlopeConfigEntity;
import com.btxtech.shared.SlopeShapeEntity;
import com.btxtech.shared.SlopeNameId;
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
    private Label loadingLabel;
    @Inject
    @DataField
    private Button closeButton;

    @PostConstruct
    public void init() {
        slopeSelection.addValueChangeHandler(new ValueChangeHandler<SlopeNameId>() {
            @Override
            public void onValueChange(ValueChangeEvent<SlopeNameId> event) {
                loadSlopeSkeleton(slopeSelection.getValue());
            }
        });
    }

    public void showSlopeEditor() {
        content.clear();
        closeButton.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        newSlope.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        slopeSelection.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        terrainEditorService.call(new RemoteCallback<Collection<SlopeNameId>>() {
            @Override
            public void callback(Collection<SlopeNameId> slopeNameIds) {
                slopeSelection.setAcceptableValues(slopeNameIds);
            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "save failed: " + message, throwable);
                return false;
            }
        }).getSlopeNameIds();
        slopeSelection.setValue(null);
    }

    @EventHandler("newSlope")
    private void newSlopeButtonClick(ClickEvent event) {
        SlopeConfigEntity slopeConfigEntity = new SlopeConfigEntity();
        slopeConfigEntity.setShape(new ArrayList<SlopeShapeEntity>());

        initEditor(slopeConfigEntity);
    }

    @EventHandler("closeButton")
    private void closeButtonClick(ClickEvent event) {
        content.clear();
        closeButton.getElement().getStyle().setDisplay(Style.Display.NONE);
        slopeSelection.getElement().getStyle().setDisplay(Style.Display.NONE);
        newSlope.getElement().getStyle().setDisplay(Style.Display.NONE);
        loadingLabel.getElement().getStyle().setDisplay(Style.Display.NONE);
    }

    private void loadSlopeSkeleton(SlopeNameId value) {
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
    }
}
