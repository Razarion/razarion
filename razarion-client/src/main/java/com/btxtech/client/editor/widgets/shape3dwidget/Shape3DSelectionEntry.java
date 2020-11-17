package com.btxtech.client.editor.widgets.shape3dwidget;

import com.btxtech.common.DisplayUtils;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * Created by Beat
 * 22.08.2016.
 */

@Templated("Shape3DSelectionDialog.html#tableRow")
public class Shape3DSelectionEntry implements TakesValue<Shape3D>, IsElement {
    @Inject
    private Event<Shape3DSelectionEntry> eventTrigger;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private TableRow tableRow;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label dbId;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label internalName;
    private Shape3D shape3D;

    @Override
    public HTMLElement getElement() {
        return tableRow;
    }

    @Override
    public void setValue(Shape3D shape3D) {
        this.shape3D = shape3D;
        dbId.setText(DisplayUtils.handleInteger(shape3D.getId()));
        // TODO internalName.setText(shape3D.getInternalName());
    }

    @Override
    public Shape3D getValue() {
        return shape3D;
    }

    @EventHandler("tableRow")
    public void onClick(final ClickEvent event) {
        eventTrigger.fire(this);
    }

    public void setSelected(boolean selected) {
        if (selected) {
            DOMUtil.addCSSClass(tableRow, "generic-gallery-table-row-selected");
            DOMUtil.removeCSSClass(tableRow, "generic-gallery-table-row-not-selected");
        } else {
            DOMUtil.addCSSClass(tableRow, "generic-gallery-table-row-not-selected");
            DOMUtil.removeCSSClass(tableRow, "generic-gallery-table-row-selected");
        }
    }
}
