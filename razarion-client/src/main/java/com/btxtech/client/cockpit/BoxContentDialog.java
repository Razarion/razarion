package com.btxtech.client.cockpit;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.ui.shared.api.annotations.Templated;

/**
 * Created by Beat
 * 26.10.2016.
 */
@Templated("BoxContentDialog.html#box-picked-dialog")
public class BoxContentDialog extends Composite implements ModalDialogContent<BoxContent> {
    @Override
    public void init(BoxContent boxContent) {

    }

    @Override
    public void customize(ModalDialogPanel<BoxContent> modalDialogPanel) {

    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void onClose() {
        // Ignore
    }

}
