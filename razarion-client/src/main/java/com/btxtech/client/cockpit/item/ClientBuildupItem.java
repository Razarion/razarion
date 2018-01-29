package com.btxtech.client.cockpit.item;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.CommonUrl;
import com.btxtech.uiservice.cockpit.item.BuildupItem;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Span;
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
    private BaseItemUiService baseItemUiService;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    @DataField
    @Named("td")
    private TableCell buildItemTd;
    @Inject
    @DataField
    private Image image;
    @Inject
    @DataField
    private Label priceLabel;
    @Inject
    @DataField
    private Span itemLimitLabel;
    @Inject
    @DataField
    private Span disableSpawn;
    private BuildupItem buildupItem;
    private int itemCount;
    private int itemLimit;
    private EnableState enableState;

    private enum EnableState {
        ENABLE(true) {
            @Override
            String lookup(String itemName) {
                return I18nHelper.getConstants().tooltipBuild(itemName);
            }
        },
        DISABLED_LEVEL_EXCEEDED(false) {
            @Override
            String lookup(String itemName) {
                return I18nHelper.getConstants().tooltipNoBuildLimit(itemName);
            }
        },
        DISABLED_HOUSE_SPACE_EXCEEDED(false) {
            @Override
            String lookup(String itemName) {
                return I18nHelper.getConstants().tooltipNoBuildHouseSpace(itemName);
            }
        },
        DISABLED_RESOURCES(false) {
            @Override
            String lookup(String itemName) {
                return I18nHelper.getConstants().tooltipNoBuildMoney(itemName);
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
            return lookup(I18nHelper.getLocalizedString(itemType.getI18nName()));
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
        image.setUrl(CommonUrl.getImageServiceUrlSafe(buildupItem.getItemType().getThumbnail()));
        discoverEnableState();
        priceLabel.setText(Integer.toString(buildupItem.getItemType().getPrice()));
        displayEnableState();
    }

    @Override
    public BuildupItem getValue() {
        return buildupItem;
    }

    @EventHandler("buildItemTd")
    public void onClick(final ClickEvent event) {
        if (enableState.isEnabled()) {
            buildupItem.onBuild();
        }
    }

    Rectangle getBuildButtonLocation() {
        return new Rectangle(image.getAbsoluteLeft(), image.getAbsoluteTop(), image.getOffsetWidth(), image.getOffsetHeight());
    }

    private void discoverEnableState() {
        itemCount = baseItemUiService.getMyItemCount(buildupItem.getItemType().getId());
        itemLimit = gameUiControl.getMyLimitation4ItemType(buildupItem.getItemType().getId());
        if (baseItemUiService.isMyLevelLimitation4ItemTypeExceeded(buildupItem.getItemType(), 1)) {
            enableState = EnableState.DISABLED_LEVEL_EXCEEDED;
            return;
        }
        if (baseItemUiService.isMyHouseSpaceExceeded(buildupItem.getItemType(), 1)) {
            enableState = EnableState.DISABLED_HOUSE_SPACE_EXCEEDED;
            return;
        }
        if (buildupItem.getItemType().getPrice() > baseItemUiService.getResources()) {
            enableState = EnableState.DISABLED_RESOURCES;
            return;
        }
        enableState = EnableState.ENABLE;
    }

    public void onResourcesChanged(int resources) {
        int price = buildupItem.getItemType().getPrice();
        if (price > resources && enableState == EnableState.ENABLE) {
            enableState = EnableState.DISABLED_RESOURCES;
            displayEnableState();
        } else if (price <= resources && enableState == EnableState.DISABLED_RESOURCES) {
            discoverEnableState();
            displayEnableState();
        }
    }

    private void displayEnableState() {
        buildItemTd.setTitle(enableState.getToolTip(buildupItem.getItemType()));
        itemLimitLabel.setTextContent(itemCount + "/" + itemLimit);
        disableSpawn.getStyle().setProperty("visibility", enableState.isEnabled() ? "hidden" : "visible");
    }
}
