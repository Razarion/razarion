package com.btxtech.client.cockpit.item;

import com.btxtech.client.cockpit.ClientCockpitHelper;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.uiservice.cockpit.item.OwnInfoPanel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 30.09.2016.
 */
@Templated("ClientOwnInfoPanel.html#own-info-panel")
public class ClientOwnInfoPanel extends Composite implements OwnInfoPanel {
    @Inject
    private ClientCockpitHelper clientCockpitHelper;
    @Inject
    @DataField
    private Image image;
    @Inject
    @DataField
    private Label itemTypeName;
    @Inject
    @DataField
    private HTML itemTypeDescr;
    @Inject
    @DataField
    private Label countLabel;
    @Inject
    @DataField
    private Button sellButton;

    @Override
    public void init(BaseItemType baseItemType, int count) {
        image.setUrl(RestUrl.getImageServiceUrlSafe(baseItemType.getThumbnail()));
        itemTypeName.setText(I18nHelper.getLocalizedString(baseItemType.getI18nName()));
        itemTypeDescr.setHTML(I18nHelper.getLocalizedString(baseItemType.getI18nDescription()));
        if (count > 1) {
            countLabel.setText(Integer.toString(count));
        } else {
            countLabel.setVisible(false);
        }
        sellButton.setTitle(I18nHelper.getConstants().tooltipSell());
    }

    @EventHandler("sellButton")
    private void sellButtonClick(ClickEvent event) {
        clientCockpitHelper.sell();
    }

}
