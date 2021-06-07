package com.btxtech.client.editor.terrain;

import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.rest.DrivewayEditorController;
import com.btxtech.shared.rest.SlopeEditorController;
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
@Templated("SlopeControlPanel.html#slopeControlPanel")
public class SlopeControlPanel implements IsElement {
    @Inject
    private TerrainEditorService terrainEditorService;
    @Inject
    private Caller<SlopeEditorController> slopeEditorController;
    @Inject
    private Caller<DrivewayEditorController> drivewayEditorController;
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    @DataField
    private HTMLInputElement cursorRadius;
    @Inject
    @DataField
    private HTMLInputElement cursorCorners;
    @Inject
    @DataField
    private ValueListBox<ObjectNameId> slopeSelection;
    @Inject
    @DataField
    private HTMLInputElement slopeInverted;
    @Inject
    @DataField
    private HTMLInputElement drivewayMode;
    @Inject
    @DataField
    private ValueListBox<ObjectNameId> drivewaySelection;

    @PostConstruct
    public void init() {
        cursorRadius.value = Js.uncheckedCast(terrainEditorService.getCursorRadius());
        slopeInverted.value = Js.uncheckedCast(terrainEditorService.isInvertedSlope());
        cursorCorners.value = Js.uncheckedCast(terrainEditorService.getCursorCorners());

        slopeSelection.addValueChangeHandler(event -> terrainEditorService.setSlope4New(slopeSelection.getValue()));
        slopeEditorController.call((RemoteCallback<Collection<ObjectNameId>>) objectNameIds -> {
            ObjectNameId objectNameId = CollectionUtils.getFirst(objectNameIds);
            slopeSelection.setAcceptableValues(objectNameIds);
            slopeSelection.setValue(objectNameId);
            terrainEditorService.setSlope4New(objectNameId);
        }, exceptionHandler.restErrorHandler("SlopeEditorController.getObjectNameIds() failed: ")).getObjectNameIds();

        drivewayMode.value = Js.uncheckedCast(terrainEditorService.isDrivewayMode());
        drivewaySelection.addValueChangeHandler(event -> terrainEditorService.setDriveway4New(drivewaySelection.getValue()));
        drivewayEditorController.call((RemoteCallback<Collection<ObjectNameId>>) objectNameIds -> {
            if (objectNameIds == null || objectNameIds.isEmpty()) {
                return;
            }
            ObjectNameId objectNameId = CollectionUtils.getFirst(objectNameIds);
            drivewaySelection.setAcceptableValues(objectNameIds);
            drivewaySelection.setValue(objectNameId);
            terrainEditorService.setDriveway4New(objectNameId);
        }, exceptionHandler.restErrorHandler("DrivewayEditorController.getObjectNameIds failed: ")).getObjectNameIds();
    }

    @EventHandler("drivewayMode")
    public void drivewayModeChanged(ChangeEvent e) {
        terrainEditorService.setDrivewayMode(Js.castToBoolean(drivewayMode.value));
    }

    @EventHandler("cursorRadius")
    public void cursorRadiusChanged(ChangeEvent e) {
        terrainEditorService.setCursorRadius(Js.castToDouble(cursorRadius.value));
    }

    @EventHandler("cursorCorners")
    public void cursorCornersChanged(ChangeEvent e) {
        terrainEditorService.setCursorCorners(Js.castToInt(cursorCorners.value));
    }

    @EventHandler("slopeInverted")
    public void slopeInvertedCheckboxChanged(ChangeEvent e) {
        terrainEditorService.setInvertedSlope(Js.castToBoolean(slopeInverted.value));
    }

}
