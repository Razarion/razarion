package com.btxtech.client.dialog.common;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.shared.CommonUrl;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 30.10.2016.
 */
@Templated("MessageImageDialog.html#message")
public class MessageImageDialog extends Composite implements ModalDialogContent<MessageImage> {
    @Inject
    @DataField
    private Label messageLabel;
    @Inject
    @DataField
    private Image messageImage;

    @Override
    public void init(MessageImage messageImage) {
        this.messageLabel.setText(messageImage.getMessage());
        if (messageImage.getImageId() != null) {
            this.messageImage.setUrl(CommonUrl.getImageServiceUrlSafe(messageImage.getImageId()));
        }
    }

    @Override
    public void onClose() {

    }

    @Override
    public void customize(ModalDialogPanel<MessageImage> modalDialogPanel) {

    }
}
