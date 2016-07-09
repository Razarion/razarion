package com.btxtech.client.editor.widgets;

import com.btxtech.client.editor.dialog.ApplyListener;
import com.btxtech.client.editor.dialog.ModalDialogManager;
import com.btxtech.client.editor.dialog.imagegallery.ImageGalleryDialog;
import com.btxtech.client.imageservice.ImageUiService;
import com.btxtech.client.utils.ControlUtils;
import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.shared.dto.ImageGalleryItem;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import elemental.html.File;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 15.06.2016.
 */
@Templated("ImageItemWidget.html#imageItemWidget")
public class ImageItemWidget extends Composite implements ImageUiService.ImageGalleryListener {
    public interface ImageItemWidgetListener {
        void onIdChanged(int id);
    }

    // private Logger logger = Logger.getLogger(ImageItemWidget.class.getName());
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
    private Label internalName;
    @Inject
    @DataField
    private Button galleryButton;
    @Inject
    @DataField
    private Button uploadButton;
    private int imageId;
    private ImageItemWidgetListener imageItemWidgetListener;

    public void setImageId(Integer imageId, ImageItemWidgetListener imageItemWidgetListener) {
        this.imageItemWidgetListener = imageItemWidgetListener;
        if (imageId != null) {
            this.imageId = imageId;
            id.setText(Integer.toBinaryString(imageId));
            imageUiService.requestImage(imageId, this);
        } else {
            id.setText("");
            dimension.setText("");
            size.setText("");
            type.setText("");
            internalName.setText("");
        }
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        imageUiService.removeListener(imageId, this);
    }

    @Override
    public void onLoaded(ImageElement imageElement, ImageGalleryItem imageGalleryItem) {
        id.setText(Integer.toString(imageId));
        dimension.setText(imageElement.getWidth() + "*" + imageElement.getHeight());
        size.setText(DisplayUtils.humanReadableSize(imageGalleryItem.getSize(), true));
        type.setText(imageGalleryItem.getType());
        internalName.setText(imageGalleryItem.getInternalName());
        image.setUrl(imageElement.getSrc());
    }

    @EventHandler("galleryButton")
    private void galleryButtonClicked(ClickEvent event) {
        modalDialogManager.show("Image Gallery", ImageGalleryDialog.class, imageId, new ApplyListener<Integer>() {
            @Override
            public void onApply(Integer id) {
                imageUiService.removeListener(imageId, ImageItemWidget.this);
                imageId = id;
                imageUiService.requestImage(imageId, ImageItemWidget.this);
                imageItemWidgetListener.onIdChanged(id);
            }
        });
    }

    @EventHandler("uploadButton")
    public void uploadButtonClicked(ClickEvent e) {
        ControlUtils.openSingleFileDataUrlUpload(new ControlUtils.SingleFileDataUrlListener() {
            @Override
            public void onLoaded(String dataUrl, File file) {
                imageUiService.overrideImage(imageId, dataUrl, (int) file.getSize(), file.getType());
            }
        });
    }

}
