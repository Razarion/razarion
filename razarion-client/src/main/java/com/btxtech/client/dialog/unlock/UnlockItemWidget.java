package com.btxtech.client.dialog.unlock;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.unlock.UnlockUiService;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Beat
 * on 14.09.2017.
 */
@Templated("UnlockDialog.html#unlockTableRow")
public class UnlockItemWidget implements TakesValue<UnlockItemModel>, IsElement {
    @Inject
    private UnlockUiService unlockUiService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    @DataField
    @Named("tr")
    private TableRow unlockTableRow;
    @Inject
    @DataField
    private Image image;
    @Inject
    @DataField
    private Span span;
    @Inject
    @DataField
    private Label unlockTitle;
    @Inject
    @DataField
    private Label unlockDescription;
    @Inject
    @DataField
    private Label unlockCrystal;
    //    @Inject
//    @DataField
//    private Label unlockArtifacts;
    @Inject
    @DataField
    private Button unlockButton;
    private UnlockItemModel unlockItemModel;

    @Override
    public void setValue(UnlockItemModel unlockItemModel) {
        this.unlockItemModel = unlockItemModel;
        if (unlockItemModel.getLevelUnlockConfig().getBaseItemType() != null) {
            BaseItemType baseItemType = itemTypeService.getBaseItemType(unlockItemModel.getLevelUnlockConfig().getBaseItemType());
            image.setUrl(RestUrl.getImageServiceUrlSafe(baseItemType.getThumbnail()));
            span.setTextContent("+" + unlockItemModel.getLevelUnlockConfig().getBaseItemTypeCount());
        } else {
            throw new UnsupportedOperationException("UnlockItemWidget.setValue() unlockItemModel.getLevelUnlockConfig().getBaseItemType() == null ...TODO...");
        }
        unlockTitle.setText(I18nHelper.getLocalizedString(unlockItemModel.getLevelUnlockConfig().getI18nName()));
        unlockDescription.setText(I18nHelper.getLocalizedString(unlockItemModel.getLevelUnlockConfig().getI18nDescription()));
        unlockCrystal.setText(I18nHelper.getConstants().unlockCrystalCost(unlockItemModel.getLevelUnlockConfig().getCrystalCost()));
        //  unlockArtifacts.setText("-");
    }

    @Override
    public UnlockItemModel getValue() {
        return unlockItemModel;
    }

    @Override
    public HTMLElement getElement() {
        return unlockTableRow;
    }

    @EventHandler("unlockButton")
    private void onUnlockButtonClicked(ClickEvent event) {
        unlockUiService.unlockViaCrystal(unlockItemModel.getLevelUnlockConfig(), success -> {
            if (success) {
                if (!unlockUiService.hasItems2Unlock()) {
                    unlockItemModel.closeDialog();
                }
            } else {
                modalDialogManager.showMessageDialog(I18nHelper.getConstants().unlockFailed(), I18nHelper.getConstants().unlockNotEnoughCrystals());
            }
        });
    }
}
