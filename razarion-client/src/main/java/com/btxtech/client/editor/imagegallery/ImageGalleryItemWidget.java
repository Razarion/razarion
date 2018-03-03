package com.btxtech.client.editor.imagegallery;

import com.btxtech.client.editor.widgets.FileButton;
import com.btxtech.client.imageservice.ImageUiService;
import com.btxtech.client.utils.ControlUtils;
import com.btxtech.common.DisplayUtils;
import com.btxtech.shared.dto.ImageGalleryItem;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Table;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 15.06.2016.
 */
@Templated("ImageGalleryDialog.html#imageGalleryItemWidget")
public class ImageGalleryItemWidget implements TakesValue<ImageGalleryItem>, IsElement, ImageUiService.ImageGalleryListener {
    // private Logger logger = Logger.getLogger(ImageGalleryItemWidget.class.getName());
    @Inject
    private ImageUiService imageUiService;
    @Inject
    @DataField
    private Table imageGalleryItemWidget;
    @Inject
    @DataField
    private Image image;
    @Inject
    @DataField
    private Label dimension;
    @Inject
    @DataField
    private Label size;
    @Inject
    @DataField
    private Label type;
    @Inject
    @DataField
    private Label id;
    @Inject
    @DataField
    private HTML internalName;
    @Inject
    @DataField
    private FileButton uploadButton;
    private ImageGalleryItem imageGalleryItem;

    @Override
    public HTMLElement getElement() {
        return imageGalleryItemWidget;
    }

    @Override
    public void setValue(ImageGalleryItem imageGalleryItem) {
        this.imageGalleryItem = imageGalleryItem;
        imageUiService.requestImage(imageGalleryItem.getId(), this);
        uploadButton.init("Upload", fileList -> ControlUtils.readFirstAsDataURL(fileList, (dataUrl, file) -> {
            imageUiService.overrideImage(imageGalleryItem.getId(), dataUrl, (int) file.getSize(), file.getType());
        }));
    }

    @Override
    public ImageGalleryItem getValue() {
        return imageGalleryItem;
    }

    public void cleanup() {
        imageUiService.removeListener(imageGalleryItem.getId(), this);
    }

    public void setChanged(boolean changed) {
        if (changed) {
            DOMUtil.addCSSClass(imageGalleryItemWidget, "gallery-item-table-changed");
            DOMUtil.removeCSSClass(imageGalleryItemWidget, "gallery-item-table-not-changed");
        } else {
            DOMUtil.addCSSClass(imageGalleryItemWidget, "gallery-item-table-not-changed");
            DOMUtil.removeCSSClass(imageGalleryItemWidget, "gallery-item-table-changed");
        }
    }

    @Override
    public void onLoaded(ImageElement imageElement, ImageGalleryItem imageGalleryItem) {
        id.setText(Integer.toString(imageGalleryItem.getId()));
        dimension.setText(imageElement.getWidth() + "*" + imageElement.getHeight());
        size.setText(DisplayUtils.humanReadableSize(imageGalleryItem.getSize(), true));
        type.setText(imageGalleryItem.getType());
        internalName.setHTML(DisplayUtils.handleEmptyHtmlString(imageGalleryItem.getInternalName()));
        image.setUrl(imageElement.getSrc());
    }
}
