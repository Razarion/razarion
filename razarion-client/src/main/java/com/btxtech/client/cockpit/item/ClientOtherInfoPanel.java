package com.btxtech.client.cockpit.item;

import com.btxtech.client.clientI18n.ClientI18nHelper;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.uiservice.cockpit.item.OtherInfoPanel;
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
    private Label type;
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
//    @SuppressWarnings("CdiInjectionPointsInspection")
//    @Inject
//    @DataField
//    private Label guildName;
//    @SuppressWarnings("CdiInjectionPointsInspection")
//    @Inject
//    @DataField
//    private Button inviteGuildButton;
//    @SuppressWarnings("CdiInjectionPointsInspection")
//    @Inject
//    @DataField
//    private Button requestMembership;

    @Override
    public void init(SyncItem target) {
        // TODO image = ImageHandler.getItemTypeImage(syncItem.getItemType(), 50, 50);
        itemTypeName.setText(ClientI18nHelper.getLocalizedString(target.getItemType().getI18Name()));
        itemTypeDescr.setHTML(ClientI18nHelper.getLocalizedString(target.getItemType().getDescription()));
        friendImage.setVisible(false);
        enemyImage.setVisible(false);
        // TODO inviteGuildButton.setVisible(false);
        // TODO requestMembership.setVisible(false);
        // TODO guildName.setVisible(false);
        if (target instanceof SyncBaseItem) {
            SyncBaseItem syncBaseItem = (SyncBaseItem) target;
            baseName.setText(syncBaseItem.getBase().getName());
            switch (syncBaseItem.getBase().getCharacter()) {
                case HUMAN:
                    type.setText(ClientI18nHelper.CONSTANTS.playerFriend());
                    enemyImage.setVisible(false);
                    break;
                case BOT:
                    type.setText(ClientI18nHelper.CONSTANTS.botEnemy());
                    enemyImage.setVisible(true);
                    break;
                case BOT_NCP:
                    type.setText(ClientI18nHelper.CONSTANTS.botNpc());
                    enemyImage.setVisible(false);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown character: " + syncBaseItem.getBase().getCharacter());
            }
        } else if (target instanceof SyncResourceItem) {
            baseName.setVisible(false);
            type.setVisible(false);
        } else if (target instanceof SyncBoxItem) {
            baseName.setVisible(false);
            type.setVisible(false);
        }
    }
}
