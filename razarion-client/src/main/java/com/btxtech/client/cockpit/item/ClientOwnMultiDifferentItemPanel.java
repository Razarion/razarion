package com.btxtech.client.cockpit.item;

import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.uiservice.cockpit.item.OwnMultiDifferentItemPanel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 30.09.2016.
 */
@Templated("ClientOwnMultiDifferentItemPanel.html#own-multi-different-info-panel")
public class ClientOwnMultiDifferentItemPanel extends Composite implements OwnMultiDifferentItemPanel {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button leftArrowButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button rightArrowButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    @ListContainer("tbody")
    private ListComponent<BaseItemTypeCount, BaseItemTypeCountPanel> selectedItemTypePanel;

    @Override
    public void init(Map<BaseItemType, Collection<SyncBaseItem>> itemTypes) {
        DOMUtil.removeAllElementChildren(selectedItemTypePanel.getElement()); // Remove placeholder table row from template.
        selectedItemTypePanel.setValue(setupBaseItemTypeCounts(itemTypes));
    }

    @EventHandler("leftArrowButton")
    private void leftArrowButtonClick(ClickEvent event) {
        throw new UnsupportedOperationException();
    }

    @EventHandler("rightArrowButton")
    private void rightArrowButtonClick(ClickEvent event) {
        throw new UnsupportedOperationException();
    }

    private List<BaseItemTypeCount> setupBaseItemTypeCounts(Map<BaseItemType, Collection<SyncBaseItem>> itemTypes) {
        List<BaseItemTypeCount> baseItemTypeCounts = new ArrayList<>();
        for (Map.Entry<BaseItemType, Collection<SyncBaseItem>> entry : itemTypes.entrySet()) {
            baseItemTypeCounts.add(new BaseItemTypeCount(entry.getKey(), entry.getValue().size()));
        }
        return baseItemTypeCounts;
    }
}
