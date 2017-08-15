package com.btxtech.client.editor.widgets.itemtype.box;

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
@Templated("BoxItemTypeWidget.html#boxitemtype")
public class BoxItemTypeWidget {
    // private Logger logger = Logger.getLogger(BoxItemTypeWidget.class.getName());
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
    private Integer boxItemTypeId;
    private Consumer<Integer> changeCallback;

    public void init(Integer boxItemTypeId, Consumer<Integer> changeCallback) {
        this.boxItemTypeId = boxItemTypeId;
        this.changeCallback = changeCallback;
        setupNameLabel();
    }

    @EventHandler("galleryButton")
    private void onFullScreenButtonClick(ClickEvent event) {
        modalDialogManager.show("Box items", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, BoxItemTypeSelectionDialog.class, boxItemTypeId, (button, selectedId) -> {
            if (button == DialogButton.Button.APPLY) {
                boxItemTypeId = selectedId;
                changeCallback.accept(boxItemTypeId);
                setupNameLabel();
            }
        }, null, DialogButton.Button.CANCEL, DialogButton.Button.APPLY);
    }

    private void setupNameLabel() {
        if (boxItemTypeId != null) {
            nameLabel.setInnerHTML(itemTypeService.getBoxItemType(boxItemTypeId).createObjectNameId().toString());
        } else {
            nameLabel.setInnerHTML("-");
        }

    }

}
