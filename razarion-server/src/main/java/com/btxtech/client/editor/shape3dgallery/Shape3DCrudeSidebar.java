package com.btxtech.client.editor.shape3dgallery;

import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.client.utils.ControlUtils;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.uiservice.Shape3DUiService;
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
 * 17.08.2016.
 */
@Templated("Shape3DCrudeSidebar.html#shape3d-crud-sidebar")
public class Shape3DCrudeSidebar extends Composite implements LeftSideBarContent {
    // private Logger logger = Logger.getLogger(Shape3DCrudeSidebar.class.getName());
    @Inject
    private Shape3DCrud shape3DCrud;
    @Inject
    private Shape3DUiService shape3DUiService;
    @Inject
    private Instance<Shape3DPropertyPanel> propertyPanelInstance;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private ValueListBox<ObjectNameId> shape3DSelector;
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
        shape3DCrud.monitor(this::updateShape3DSelector);
        shape3DSelector.addValueChangeHandler(event -> displayPropertyBook(shape3DSelector.getValue()));
    }

    @EventHandler("createButton")
    private void createButtonClick(ClickEvent event) {
        ControlUtils.openSingleFileTextUpload((dataUrl, file) -> shape3DCrud.create(dataUrl));
    }

    @EventHandler("deleteButton")
    private void deleteButtonClick(ClickEvent event) {
        Shape3D shape3D = getShape3D();
        if (shape3D != null) {
            shape3DCrud.delete(shape3D);
        }
    }

    @EventHandler("saveButton")
    private void saveButtonClick(ClickEvent event) {
        Shape3D shape3D = getShape3D();
        if (shape3D != null) {
            shape3DCrud.save(shape3D);
        }
    }

    @EventHandler("reloadButton")
    private void reloadButtonClick(ClickEvent event) {
        shape3DCrud.reload();
    }

    private void updateShape3DSelector(List<ObjectNameId> objectNameIds) {
        shape3DSelector.setAcceptableValues(objectNameIds);
        Shape3D shape3D = getShape3D();
        if (shape3D != null) {
            shape3DSelector.setValue(shape3D.createSlopeNameId());
        } else {
            shape3DSelector.setValue(null);
        }
    }

    private Shape3D getShape3D() {
        if (content.getWidget() == null) {
            return null;
        }
        return ((Shape3DPropertyPanel) content.getWidget()).getShape3D();
    }

    private void displayPropertyBook(ObjectNameId objectNameId) {
        Shape3DPropertyPanel shape3DPropertyPanel = propertyPanelInstance.get();
        shape3DUiService.request(objectNameId.getId(), shape3DPropertyPanel::init, false);
        content.setWidget(shape3DPropertyPanel);
        deleteButton.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        saveButton.getElement().getStyle().setDisplay(Style.Display.BLOCK);
    }

    @Override
    public void onClose() {
        shape3DCrud.removeMonitor(this::updateShape3DSelector);
    }
}
