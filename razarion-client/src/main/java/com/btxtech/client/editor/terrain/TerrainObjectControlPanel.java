package com.btxtech.client.editor.terrain;

import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.rest.TerrainObjectEditorController;
import com.btxtech.shared.utils.CollectionUtils;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.user.client.ui.ValueListBox;
import elemental2.dom.HTMLInputElement;
import jsinterop.base.Js;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Collection;

@Deprecated
@Templated("TerrainObjectControlPanel.html#terrainObjectControlPanel")
public class TerrainObjectControlPanel implements IsElement {
    @Inject
    private TerrainEditorService terrainEditorService;
    @Inject
    private Caller<TerrainObjectEditorController> terrainObjectEditorController;
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    @DataField
    private ValueListBox<ObjectNameId> terrainObjectSelection;
    @Inject
    @DataField
    private HTMLInputElement terrainObjectRandomZRotation;
    @Inject
    @DataField
    private HTMLInputElement terrainObjectRandomScale;

    @PostConstruct
    public void init() {
        terrainObjectRandomZRotation.value = Js.uncheckedCast(terrainEditorService.getTerrainObjectRandomZRotation());
        terrainObjectRandomScale.value = Js.uncheckedCast(terrainEditorService.getTerrainObjectRandomScale());
        terrainObjectSelection.addValueChangeHandler(event -> terrainEditorService.setTerrainObject4New(terrainObjectSelection.getValue()));
        terrainObjectEditorController.call((RemoteCallback<Collection<ObjectNameId>>) objectNameIds -> {
            if (objectNameIds == null || objectNameIds.isEmpty()) {
                return;
            }
            ObjectNameId objectNameId = CollectionUtils.getFirst(objectNameIds);
            terrainObjectSelection.setAcceptableValues(objectNameIds);
            terrainObjectSelection.setValue(objectNameId);
            terrainEditorService.setTerrainObject4New(objectNameId);
        }, exceptionHandler.restErrorHandler("getObjectNameIds failed: ")).getObjectNameIds();
    }

    @EventHandler("terrainObjectRandomZRotation")
    public void terrainObjectRandomZRotationChanged(ChangeEvent e) {
        terrainEditorService.setTerrainObjectRandomZRotation(Js.castToDouble(terrainObjectRandomZRotation.value));
    }

    @EventHandler("terrainObjectRandomScale")
    public void terrainObjectRandomScaleChanged(ChangeEvent e) {
        terrainEditorService.setTerrainObjectRandomScale(Js.castToDouble(terrainObjectRandomScale.value));
    }

}
