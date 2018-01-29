package com.btxtech.client.cockpit.item;

import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.CommonUrl;
import com.btxtech.uiservice.cockpit.item.OtherInfoPanel;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.item.BaseItemUiService;
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
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    @DataField
    private Image image;
    @Inject
    @DataField
    private Label itemTypeName;
    @Inject
    @DataField
    private Span type;
    @Inject
    @DataField
    private HTML itemTypeDescr;
    @Inject
    @DataField
    private Label baseName;
    @Inject
    @DataField
    private Image friendImage;
    @Inject
    @DataField
    private Image enemyImage;

    @Override
    public void init(SyncItemSimpleDto otherSelection) {
        friendImage.setVisible(false);
        enemyImage.setVisible(false);
        ItemType itemType = null;
        if (otherSelection instanceof SyncBaseItemSimpleDto) {
            itemType = itemTypeService.getBaseItemType(otherSelection.getItemTypeId());
            SyncBaseItemSimpleDto syncBaseItem = (SyncBaseItemSimpleDto) otherSelection;
            PlayerBaseDto base = baseItemUiService.getBase(syncBaseItem.getBaseId());
            baseName.setText(setupName(base));
            switch (base.getCharacter()) {
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
                    throw new UnsupportedOperationException("Unknown character: " + base.getCharacter());
            }
        } else if (otherSelection instanceof SyncResourceItemSimpleDto) {
            itemType = itemTypeService.getResourceItemType(otherSelection.getItemTypeId());
            baseName.setVisible(false);
            type.getStyle().setProperty("display", "none");
        } else if (otherSelection instanceof SyncBoxItemSimpleDto) {
            itemType = itemTypeService.getBoxItemType(otherSelection.getItemTypeId());
            baseName.setVisible(false);
            type.getStyle().setProperty("display", "none");
        }
        if (itemType != null) {
            image.setUrl(CommonUrl.getImageServiceUrlSafe(itemType.getThumbnail()));
            itemTypeName.setText(I18nHelper.getLocalizedString(itemType.getI18nName()));
            itemTypeDescr.setHTML(I18nHelper.getLocalizedString(itemType.getI18nDescription()));
        }
    }

    private String setupName(PlayerBaseDto base) {
        if (base.getCharacter() == Character.HUMAN) {
            if (base.getHumanPlayerId().getUserId() == null) {
                return I18nHelper.getConstants().unregisteredUser();
            } else if (base.getName() == null || base.getName().trim().isEmpty()) {
                return I18nHelper.getConstants().unnamedUser();
            } else {
                return base.getName();
            }
        } else {
            return base.getName();
        }
    }
}
