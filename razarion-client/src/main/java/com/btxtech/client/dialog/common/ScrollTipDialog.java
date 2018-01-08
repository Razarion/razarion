package com.btxtech.client.dialog.common;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.uiservice.tip.tiptask.ScrollTipTask;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 30.10.2016.
 */
@Templated("ScrollTipDialog.html#scrolltipdialog")
public class ScrollTipDialog extends Composite implements ModalDialogContent<ScrollTipTask> {
    @Inject
    @DataField
    private Div message;
    @Inject
    @DataField
    private Image mapImage;
    @Inject
    @DataField
    private Image keyboardImage;
    private ScrollTipTask scrollTipTask;
    private ModalDialogPanel<ScrollTipTask> modalDialogPanel;

    @Override
    public void init(ScrollTipTask scrollTipTask) {
        this.scrollTipTask = scrollTipTask;
        this.message.setTextContent(scrollTipTask.getDialogMessage());
        mapImage.setUrl(RestUrl.getImageServiceUrlSafe(scrollTipTask.getScrollDialogMapImageId()));
        keyboardImage.setUrl(RestUrl.getImageServiceUrlSafe(scrollTipTask.getScrollDialogKeyboardImageId()));
        scrollTipTask.onDialogOpened(() -> modalDialogPanel.close());
    }

    @Override
    public void onClose() {
        scrollTipTask.onDialogClosed();
    }

    @Override
    public void customize(ModalDialogPanel<ScrollTipTask> modalDialogPanel) {
        this.modalDialogPanel = modalDialogPanel;
    }
}
