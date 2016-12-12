package com.btxtech.client.cockpit.item;

import com.btxtech.shared.rest.RestUrl;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.uiservice.cockpit.item.OtherInfoPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 30.09.2016.
 */
@Templated("ClientOtherInfoPanel.html#other-info-panel")
public class ClientOtherInfoPanel extends Composite implements OtherInfoPanel {
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
    private Span type;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private HTML itemTypeDescr;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label baseName;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Image friendImage;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Image enemyImage;

    @Override
    public void init(SyncItem target) {
        image.setUrl(RestUrl.getImageServiceUrlSafe(target.getItemType().getThumbnail()));
        itemTypeName.setText(I18nHelper.getLocalizedString(target.getItemType().getI18Name()));
        itemTypeDescr.setHTML(I18nHelper.getLocalizedString(target.getItemType().getDescription()));
        friendImage.setVisible(false);
        enemyImage.setVisible(false);
        if (target instanceof SyncBaseItem) {
            SyncBaseItem syncBaseItem = (SyncBaseItem) target;
            baseName.setText(syncBaseItem.getBase().getName());
            switch (syncBaseItem.getBase().getCharacter()) {
                case HUMAN:
                    type.setTextContent(I18nHelper.getConstants().playerFriend());
                    friendImage.setVisible(true);
                    enemyImage.setVisible(false);
                    break;
                case BOT:
                    type.setTextContent(I18nHelper.getConstants().botEnemy());
                    friendImage.setVisible(false);
                    enemyImage.setVisible(true);
                    break;
                case BOT_NCP:
                    type.setTextContent(I18nHelper.getConstants().botNpc());
                    friendImage.setVisible(true);
                    enemyImage.setVisible(false);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown character: " + syncBaseItem.getBase().getCharacter());
            }
        } else if (target instanceof SyncResourceItem) {
            baseName.setVisible(false);
            type.getStyle().setProperty("display", "none");
        } else if (target instanceof SyncBoxItem) {
            baseName.setVisible(false);
            type.getStyle().setProperty("display", "none");
        }
    }
}
