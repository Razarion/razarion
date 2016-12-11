package com.btxtech.client.cockpit.item;

import com.btxtech.shared.rest.RestUrl;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.uiservice.cockpit.item.OwnInfoPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 30.09.2016.
 */
@Templated("ClientOwnInfoPanel.html#own-info-panel")
public class ClientOwnInfoPanel extends Composite implements OwnInfoPanel {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Image image;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label itemTypeName;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private HTML itemTypeDescr;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label countLabel;

    @Override
    public void init(BaseItemType baseItemType, int count) {
        image.setUrl(RestUrl.getImageServiceUrlSafe(baseItemType.getThumbnail()));
        itemTypeName.setText(I18nHelper.getLocalizedString(baseItemType.getI18Name()));
        itemTypeDescr.setHTML(I18nHelper.getLocalizedString(baseItemType.getDescription()));
        if (count > 1) {
            countLabel.setText(Integer.toString(count));
        } else {
            countLabel.setVisible(false);
        }
    }
}
