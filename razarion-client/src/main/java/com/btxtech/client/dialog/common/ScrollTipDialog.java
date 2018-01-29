package com.btxtech.client.dialog.common;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.shared.CommonUrl;
import com.btxtech.uiservice.tip.tiptask.ScrollTipDialogModel;
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
public class ScrollTipDialog extends Composite implements ModalDialogContent<ScrollTipDialogModel> {
    @Inject
    @DataField
    private Div message;
    @Inject
    @DataField
    private Image mapImage;
    @Inject
    @DataField
    private Image keyboardImage;
    private ScrollTipDialogModel scrollTipDialogModel;
    private ModalDialogPanel<ScrollTipDialogModel> modalDialogPanel;

    @Override
    public void init(ScrollTipDialogModel scrollTipDialogModel) {
        this.scrollTipDialogModel = scrollTipDialogModel;
        this.message.setTextContent(scrollTipDialogModel.getDialogMessage());
        mapImage.setUrl(CommonUrl.getImageServiceUrlSafe(scrollTipDialogModel.getScrollDialogMapImageId()));
        keyboardImage.setUrl(CommonUrl.getImageServiceUrlSafe(scrollTipDialogModel.getScrollDialogKeyboardImageId()));
        if (scrollTipDialogModel.getDialogOpenCallback() != null) {
            scrollTipDialogModel.getDialogOpenCallback().accept(() -> modalDialogPanel.close());
        }
    }

    @Override
    public void onClose() {
        if (scrollTipDialogModel.getDialogCloseCallback() != null) {
            scrollTipDialogModel.getDialogCloseCallback().run();
        }
    }

    @Override
    public void customize(ModalDialogPanel<ScrollTipDialogModel> modalDialogPanel) {
        this.modalDialogPanel = modalDialogPanel;
    }
}
