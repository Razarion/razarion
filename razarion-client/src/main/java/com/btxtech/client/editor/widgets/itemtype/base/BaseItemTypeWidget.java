package com.btxtech.client.editor.widgets.itemtype.base;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.uiservice.dialog.DialogButton;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 07.08.2017.
 */
@Templated("BaseItemTypeWidget.html#baseitemtype")
public class BaseItemTypeWidget {
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    @DataField
    private Span nameLabel;
    @Inject
    @DataField
    private Button galleryButton;
    private Integer baseItemTypeId;
    private Consumer<Integer> changeCallback;

    public void init(Integer baseItemTypeId, Consumer<Integer> changeCallback) {
        this.baseItemTypeId = baseItemTypeId;
        this.changeCallback = changeCallback;
        setupNameLabel();
    }

    @EventHandler("galleryButton")
    private void onGalleryButtonButtonClick(ClickEvent event) {
        modalDialogManager.show("Base items", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, BaseItemTypeSelectionDialog.class, baseItemTypeId, (button, selectedId) -> {
            if (button == DialogButton.Button.APPLY) {
                baseItemTypeId = selectedId;
                changeCallback.accept(baseItemTypeId);
                setupNameLabel();
            }
        }, null, DialogButton.Button.CANCEL, DialogButton.Button.APPLY);
    }

    private void setupNameLabel() {
        if (baseItemTypeId != null) {
            nameLabel.setInnerHTML(itemTypeService.getBaseItemType(baseItemTypeId).createObjectNameId().toString());
        } else {
            nameLabel.setInnerHTML("-");
        }

    }

}
