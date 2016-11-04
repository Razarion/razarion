package com.btxtech.client.cockpit.item;

import com.btxtech.client.clientI18n.ClientI18nHelper;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.uiservice.cockpit.item.BuildupItem;
import com.btxtech.uiservice.storyboard.StoryboardService;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.TableCell;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Beat
 * 30.09.2016.
 */
@Templated("ClientBuildupItemPanel.html#buildItemTd")
public class ClientBuildupItem implements TakesValue<BuildupItem>, IsElement {
    @Inject
    private StoryboardService storyboardService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    @Named("td")
    private TableCell buildItemTd;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Image image;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label priceLabel;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label itemLimitLabel;
    private BuildupItem buildupItem;
    private int itemCount;
    private int itemLimit;
    private EnableState enableState;

    private enum EnableState {
        ENABLE(true) {
            @Override
            String lookup(String itemName) {
                return ClientI18nHelper.CONSTANTS.tooltipBuild(itemName);
            }
        },
        DISABLED_LEVEL(false) {
            @Override
            String lookup(String itemName) {
                return ClientI18nHelper.CONSTANTS.tooltipNoBuildLevel(itemName);
            }
        },
        DISABLED_LEVEL_EXCEEDED(false) {
            @Override
            String lookup(String itemName) {
                return ClientI18nHelper.CONSTANTS.tooltipNoBuildLimit(itemName);
            }
        },
        DISABLED_HOUSE_SPACE_EXCEEDED(false) {
            @Override
            String lookup(String itemName) {
                return ClientI18nHelper.CONSTANTS.tooltipNoBuildHouseSpace(itemName);
            }
        },
        DISABLED_MONEY(false) {
            @Override
            String lookup(String itemName) {
                return ClientI18nHelper.CONSTANTS.tooltipNoBuildMoney(itemName);
            }
        };

        private boolean enabled;

        EnableState(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getToolTip(BaseItemType itemType) {
            return lookup(ClientI18nHelper.getLocalizedString(itemType.getI18Name()));
        }

        abstract String lookup(String itemName);
    }


    @Override
    public HTMLElement getElement() {
        return buildItemTd;
    }

    @Override
    public void setValue(BuildupItem buildupItem) {
        this.buildupItem = buildupItem;
        discoverEnableState();
        priceLabel.setText(Integer.toString(buildupItem.getItemType().getPrice()));
        accomplishEnableState();
    }

    @Override
    public BuildupItem getValue() {
        return buildupItem;
    }

    @EventHandler("buildItemTd")
    public void onClick(final ClickEvent event) {
        buildupItem.onBuild();
    }

    private void discoverEnableState() {
        itemCount = storyboardService.getItemCount(buildupItem.getItemType().getId());
        itemLimit = storyboardService.getLimitation4ItemType(buildupItem.getItemType());
        if (storyboardService.isLevelLimitation4ItemTypeExceeded(buildupItem.getItemType(), 1)) {
            enableState = EnableState.DISABLED_LEVEL_EXCEEDED;
            return;
        }
        if (storyboardService.isHouseSpaceExceeded(buildupItem.getItemType(), 1)) {
            enableState = EnableState.DISABLED_HOUSE_SPACE_EXCEEDED;
            return;
        }
        if (buildupItem.getItemType().getPrice() > storyboardService.getAccountBalance()) {
            enableState = EnableState.DISABLED_MONEY;
            return;
        }
        enableState = EnableState.ENABLE;
    }

    private void accomplishEnableState() {
        buildItemTd.setTitle(enableState.getToolTip((buildupItem.getItemType())));
        // TODO buildupItemButtonContent.setEnabled(enableState.isEnabled());
        itemLimitLabel.setText(itemCount + "/" + itemLimit);
        // TODO button.setEnabled(enableState.isEnabled());
    }

}
