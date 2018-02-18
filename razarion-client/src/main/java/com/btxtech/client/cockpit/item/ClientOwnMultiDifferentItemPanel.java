package com.btxtech.client.cockpit.item;

import com.btxtech.client.cockpit.ClientCockpitHelper;
import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.uiservice.cockpit.item.OwnMultiDifferentItemPanel;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.google.gwt.dom.client.Style;
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
    @Inject
    private ClientCockpitHelper clientCockpitHelper;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    @DataField
    private Button leftArrowButton;
    @Inject
    @DataField
    private Button rightArrowButton;
    @Inject
    @DataField
    @ListContainer("tbody")
    private ListComponent<BaseItemTypeCount, BaseItemTypeCountPanel> selectedItemTypePanel;
    @Inject
    @DataField
    private Button sellButton;

    @Override
    public void init(Map<BaseItemType, Collection<SyncBaseItemSimpleDto>> itemTypes) {
        DOMUtil.removeAllElementChildren(selectedItemTypePanel.getElement()); // Remove placeholder table row from template.
        selectedItemTypePanel.setValue(setupBaseItemTypeCounts(itemTypes));
        sellButton.setTitle(I18nHelper.getConstants().tooltipSell());
        if(gameUiControl.isSellSuppressed()) {
            sellButton.getElement().getStyle().setDisplay(Style.Display.NONE);
        } else {
            sellButton.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        }
    }

    @EventHandler("leftArrowButton")
    private void leftArrowButtonClick(ClickEvent event) {
        // throw new UnsupportedOperationException();
    }

    @EventHandler("rightArrowButton")
    private void rightArrowButtonClick(ClickEvent event) {
        // throw new UnsupportedOperationException();
    }

    @EventHandler("sellButton")
    private void sellButtonClick(ClickEvent event) {
        clientCockpitHelper.sell();
    }

    private List<BaseItemTypeCount> setupBaseItemTypeCounts(Map<BaseItemType, Collection<SyncBaseItemSimpleDto>> itemTypes) {
        List<BaseItemTypeCount> baseItemTypeCounts = new ArrayList<>();
        for (Map.Entry<BaseItemType, Collection<SyncBaseItemSimpleDto>> entry : itemTypes.entrySet()) {
            baseItemTypeCounts.add(new BaseItemTypeCount(entry.getKey(), entry.getValue().size()));
        }
        return baseItemTypeCounts;
    }
}
