package com.btxtech.client.sidebar;

import com.btxtech.client.editor.object.TerrainObjectEditor;
import com.btxtech.game.jsre.client.common.CollectionUtils;
import com.btxtech.shared.TerrainEditorService;
import com.btxtech.shared.dto.ObjectNameId;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.ValueListBox;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 13.05.2016.
 */
@Templated("TerrainObjectEditorSidebar.html#terrainObjectEditor")
public class TerrainObjectEditorSidebar extends Composite implements LeftSideBarContent {
    private Logger logger = Logger.getLogger(TerrainObjectEditorSidebar.class.getName());
    @Inject
    private Caller<TerrainEditorService> terrainEditorService;
    @Inject
    private TerrainObjectEditor terrainObjectEditor;
    @Inject
    @DataField
    private ValueListBox<ObjectNameId> terrainObjectSelection;
    @Inject
    @DataField
    private DoubleBox randomZRotation;
    @Inject
    @DataField
    private DoubleBox randomScale;
    @Inject
    @DataField
    private Button saveButton;

    @PostConstruct
    public void init() {
        randomZRotation.setValue(terrainObjectEditor.getRandomZRotation());
        randomScale.setValue(terrainObjectEditor.getRandomScale());
        terrainObjectSelection.addValueChangeHandler(new ValueChangeHandler<ObjectNameId>() {
            @Override
            public void onValueChange(ValueChangeEvent<ObjectNameId> event) {
                terrainObjectEditor.setNewObjectId(terrainObjectSelection.getValue());
            }
        });
        terrainEditorService.call(new RemoteCallback<Collection<ObjectNameId>>() {
            @Override
            public void callback(Collection<ObjectNameId> objectNameIds) {
                ObjectNameId objectNameId = CollectionUtils.getFirst(objectNameIds);
                terrainObjectSelection.setAcceptableValues(objectNameIds);
                terrainObjectSelection.setValue(objectNameId);
                terrainObjectEditor.setNewObjectId(objectNameId);
            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "getSlopeNameIds failed: " + message, throwable);
                return false;
            }
        }).getTerrainObjectNameIds();
        terrainObjectEditor.activate();
    }

    @Override
    public void onClose() {
        terrainObjectEditor.deactivate();
    }

    @EventHandler("randomZRotation")
    public void randomZRotationChanged(ChangeEvent e) {
        terrainObjectEditor.setRandomZRotation(randomZRotation.getValue());
    }

    @EventHandler("randomScale")
    public void randomScaleChanged(ChangeEvent e) {
        terrainObjectEditor.setRandomScale(randomScale.getValue());
    }

    @EventHandler("saveButton")
    private void saveButtonClick(ClickEvent event) {
        terrainObjectEditor.save();
    }
}
