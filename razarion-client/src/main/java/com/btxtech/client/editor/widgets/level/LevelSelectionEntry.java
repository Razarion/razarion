package com.btxtech.client.editor.widgets.level;

import com.btxtech.common.DisplayUtils;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
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

@Templated("LevelSelectionDialog.html#tableRow")
public class LevelSelectionEntry implements TakesValue<LevelConfig>, IsElement {
    @Inject
    private Event<LevelSelectionEntry> eventTrigger;
    @Inject
    @DataField
    private TableRow tableRow;
    @Inject
    @DataField
    private Label levelId;
    @Inject
    @DataField
    private Label levelNumber;
    private LevelConfig level;

    @Override
    public HTMLElement getElement() {
        return tableRow;
    }

    @Override
    public void setValue(LevelConfig level) {
        this.level = level;
        levelId.setText(DisplayUtils.handleInteger(level.getLevelId()));
        levelNumber.setText(DisplayUtils.handleInteger(level.getNumber()));
    }

    @Override
    public LevelConfig getValue() {
        return level;
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
