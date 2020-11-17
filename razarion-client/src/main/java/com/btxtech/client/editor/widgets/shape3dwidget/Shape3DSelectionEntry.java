package com.btxtech.client.editor.widgets.shape3dwidget;

import com.btxtech.common.DisplayUtils;
import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;
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
public class Shape3DSelectionEntry implements TakesValue<Shape3DConfig>, IsElement {
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
    private Shape3DConfig shape3DConfig;

    @Override
    public HTMLElement getElement() {
        return tableRow;
    }

    @Override
    public void setValue(Shape3DConfig shape3DConfig) {
        this.shape3DConfig = shape3DConfig;
        dbId.setText(DisplayUtils.handleInteger(shape3DConfig.getId()));
        internalName.setText(shape3DConfig.getInternalName());
    }

    @Override
    public Shape3DConfig getValue() {
        return shape3DConfig;
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
