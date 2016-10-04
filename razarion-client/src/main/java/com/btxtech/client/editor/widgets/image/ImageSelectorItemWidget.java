package com.btxtech.client.editor.widgets.image;

import com.btxtech.client.imageservice.ImageUiService;
import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.shared.dto.ImageGalleryItem;
import com.btxtech.uiservice.dialog.ModalDialogManager;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Table;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 15.06.2016.
 */
@Templated("ImageSelectorDialog.html#imageSelectorItemWidget")
public class ImageSelectorItemWidget implements TakesValue<ImageGalleryItem>, IsElement {
    // private Logger logger = Logger.getLogger(ImageGalleryItemWidget.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ImageUiService imageUiService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ModalDialogManager modalDialogManager;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Table imageSelectorItemWidget;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Image image;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label dimension;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label size;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label type;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label id;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private HTML internalName;
    private ImageGalleryItem imageGalleryItem;
    private ImageSelectorDialog imageGalleryDialog;

    @Override
    public HTMLElement getElement() {
        return imageSelectorItemWidget;
    }

    @Override
    public void setValue(ImageGalleryItem imageGalleryItem) {
        this.imageGalleryItem = imageGalleryItem;
        imageUiService.requestImage(imageGalleryItem.getId(), this::onLoaded);
        setSelected(false);
    }

    @Override
    public ImageGalleryItem getValue() {
        return imageGalleryItem;
    }

    @EventHandler("imageSelectorItemWidget")
    public void onClick(final ClickEvent event) {
        imageGalleryDialog.selectionChanged(imageGalleryItem);
    }

    public void cleanup() {
        imageUiService.removeListener(imageGalleryItem.getId(), this::onLoaded);

        // TODO 2: consumer addListener & remove listener -> ungleicher callback
    }

    public void setSelected(boolean selected) {
        if (selected) {
            DOMUtil.addCSSClass(imageSelectorItemWidget, "gallery-item-table-selected");
            DOMUtil.removeCSSClass(imageSelectorItemWidget, "gallery-item-table-not-selected");
        } else {
            DOMUtil.addCSSClass(imageSelectorItemWidget, "gallery-item-table-not-selected");
            DOMUtil.removeCSSClass(imageSelectorItemWidget, "gallery-item-table-selected");
        }
    }

    private void onLoaded(ImageElement imageElement, ImageGalleryItem imageGalleryItem) {
        id.setText(Integer.toString(imageGalleryItem.getId()));
        dimension.setText(imageElement.getWidth() + "*" + imageElement.getHeight());
        size.setText(DisplayUtils.humanReadableSize(imageGalleryItem.getSize(), true));
        type.setText(imageGalleryItem.getType());
        internalName.setHTML(DisplayUtils.handleEmptyHtmlString(imageGalleryItem.getInternalName()));
        image.setUrl(imageElement.getSrc());
    }

    public void setImageGalleryDialog(ImageSelectorDialog imageGalleryDialog) {
        this.imageGalleryDialog = imageGalleryDialog;
    }
}
