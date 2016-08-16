package com.btxtech.client.editor.imagegallery;

import com.btxtech.client.dialog.ModalDialogManager;
import com.btxtech.client.imageservice.ImageUiService;
import com.btxtech.client.utils.ControlUtils;
import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.shared.dto.ImageGalleryItem;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import elemental.html.File;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 15.06.2016.
 */
@Templated("ImageGalleryItemWidget.html#imageGalleryItemWidget")
public class ImageGalleryItemWidget extends Composite implements ImageUiService.ImageGalleryListener, HasModel<ImageGalleryItem> {
    // private Logger logger = Logger.getLogger(ImageGalleryItemWidget.class.getName());
    @Inject
    private ImageUiService imageUiService;
    @Inject
    private ModalDialogManager modalDialogManager;
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
    private Button uploadButton;
    private ImageGalleryItem imageGalleryItem;

    @Override
    protected void onUnload() {
        super.onUnload();
        imageUiService.removeListener(imageGalleryItem.getId(), this);
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


    @EventHandler("uploadButton")
    public void uploadButtonClicked(ClickEvent e) {
        ControlUtils.openSingleFileDataUrlUpload(new ControlUtils.SingleFileDataUrlListener() {
            @Override
            public void onLoaded(String dataUrl, File file) {
                imageUiService.overrideImage(imageGalleryItem.getId(), dataUrl, (int) file.getSize(), file.getType());
            }
        });
    }

    @Override
    public ImageGalleryItem getModel() {
        return imageGalleryItem;
    }

    @Override
    public void setModel(ImageGalleryItem imageGalleryItem) {
        setSelected(false);
        this.imageGalleryItem = imageGalleryItem;
        imageUiService.requestImage(imageGalleryItem.getId(), this);
    }

    public void setSelected(boolean selected) {
        if (selected) {
            addStyleName("gallery-item-table-selected");
            removeStyleName("gallery-item-table-not-selected");
        } else {
            addStyleName("gallery-item-table-not-selected");
            removeStyleName("gallery-item-table-selected");
        }
    }

    public void setChanged(boolean changed) {
        if (changed) {
            addStyleName("gallery-item-table-changed");
            removeStyleName("gallery-item-table-not-changed");
        } else {
            addStyleName("gallery-item-table-not-changed");
            removeStyleName("gallery-item-table-changed");
        }
    }
}
