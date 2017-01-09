package com.btxtech.client.cockpit.item;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.rest.RestUrl;
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
    private GameUiControl gameUiControl;
    @Inject
    private BaseItemUiService baseItemUiService;
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
    private Span itemLimitLabel;
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
//        DISABLED_LEVEL(false) {
//            @Override
//            String lookup(String itemName) {
//                return I18nHelper.getConstants().tooltipNoBuildLevel(itemName);
//            }
//        },
//        DISABLED_LEVEL_EXCEEDED(false) {
//            @Override
//            String lookup(String itemName) {
//                return I18nHelper.getConstants().tooltipNoBuildLimit(itemName);
//            }
//        },
//        DISABLED_HOUSE_SPACE_EXCEEDED(false) {
//            @Override
//            String lookup(String itemName) {
//                return I18nHelper.getConstants().tooltipNoBuildHouseSpace(itemName);
//            }
//        },
        DISABLED_MONEY(false) {
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
            return lookup(I18nHelper.getLocalizedString(itemType.getI18Name()));
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
        image.setUrl(RestUrl.getImageServiceUrlSafe(buildupItem.getItemType().getThumbnail()));
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

    Rectangle getBuildButtonLocation() {
        return new Rectangle(image.getAbsoluteLeft(), image.getAbsoluteTop(), image.getOffsetWidth(), image.getOffsetHeight());
    }

    private void discoverEnableState() {
        // TODO
//        itemCount = gameUiControl.getItemCount(buildupItem.getItemType().getId());
//        itemLimit = gameUiControl.getLimitation4ItemType(buildupItem.getItemType());
//        if (gameUiControl.isLevelLimitation4ItemTypeExceeded(buildupItem.getItemType())) {
//            enableState = EnableState.DISABLED_LEVEL_EXCEEDED;
//            return;
//        }
//        if (gameUiControl.isHouseSpaceExceeded(buildupItem.getItemType())) {
//            enableState = EnableState.DISABLED_HOUSE_SPACE_EXCEEDED;
//            return;
//        }
//        if (buildupItem.getItemType().getPrice() > baseItemUiService.getResources()) {
//            enableState = EnableState.DISABLED_MONEY;
//            return;
//        }
        enableState = EnableState.ENABLE;
    }

    private void accomplishEnableState() {
        buildItemTd.setTitle(enableState.getToolTip((buildupItem.getItemType())));
        // TODO buildupItemButtonContent.setEnabled(enableState.isEnabled());
        itemLimitLabel.setTextContent(itemCount + "/" + itemLimit);
        // TODO button.setEnabled(enableState.isEnabled());
    }
}
