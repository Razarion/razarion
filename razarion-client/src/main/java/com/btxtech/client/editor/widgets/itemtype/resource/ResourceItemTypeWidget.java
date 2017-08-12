package com.btxtech.client.editor.widgets.itemtype.resource;

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
@Templated("ResourceItemTypeWidget.html#resourceitemtype")
public class ResourceItemTypeWidget {
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
    private Integer resourceItemTypeId;
    private Consumer<Integer> changeCallback;

    public void init(Integer resourceItemTypeId, Consumer<Integer> changeCallback) {
        this.resourceItemTypeId = resourceItemTypeId;
        this.changeCallback = changeCallback;
        setupNameLabel();
    }

    @EventHandler("galleryButton")
    private void onFullScreenButtonClick(ClickEvent event) {
        modalDialogManager.show("Resource items", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, ResourceItemTypeSelectionDialog.class, resourceItemTypeId, (button, selectedId) -> {
            if (button == DialogButton.Button.APPLY) {
                resourceItemTypeId = selectedId;
                changeCallback.accept(resourceItemTypeId);
                setupNameLabel();
            }
        }, null, DialogButton.Button.CANCEL, DialogButton.Button.APPLY);
    }

    private void setupNameLabel() {
        if (resourceItemTypeId != null) {
            nameLabel.setInnerHTML(itemTypeService.getResourceItemType(resourceItemTypeId).createObjectNameId().toString());
        } else {
            nameLabel.setInnerHTML("-");
        }

    }

}
