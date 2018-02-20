package com.btxtech.client.editor.widgets.image;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.imageservice.ImageUiService;
import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.shared.dto.ImageGalleryItem;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.dialog.DialogButton;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * Created by Beat
 * 15.06.2016.
 */
@Templated("ImageItemWidget.html#imageItemWidget")
public class ImageItemWidget extends Composite implements ImageUiService.ImageGalleryListener {
    // private Logger logger = Logger.getLogger(ImageItemWidget.class.getName());
    @Inject
    private ImageUiService imageUiService;
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
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
    private Button deleteButton;
    private Integer imageId;
    private Consumer<Integer> imageItemWidgetListener;

    public void setImageId(Integer imageId, Consumer<Integer> imageItemWidgetListener) {
        this.imageItemWidgetListener = imageItemWidgetListener;
        if (imageId != null) {
            this.imageId = imageId;
            id.setText(Integer.toString(imageId));
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
        if (imageId != null) {
            imageUiService.removeListener(imageId, this);
        }
    }

    @EventHandler("galleryButton")
    private void galleryButtonClicked(ClickEvent event) {
        modalDialogManager.show("Image Gallery", ClientModalDialogManagerImpl.Type.STACK_ABLE, ImageSelectorDialog.class, imageId, (button, id1) -> {
                if (button == DialogButton.Button.APPLY) {
                    if (imageId != null) {
                        imageUiService.removeListener(imageId, this);
                    }
                    imageId = id1;
                    imageUiService.requestImage(imageId, this);
                    imageItemWidgetListener.accept(id1);
                }
        }, null, DialogButton.Button.CANCEL, DialogButton.Button.APPLY);
    }

    @EventHandler("deleteButton")
    private void deleteButtonClicked(ClickEvent event) {
        if (imageId != null) {
            imageUiService.removeListener(imageId, this);
        }
        imageItemWidgetListener.accept(null);
        imageId = null;
        id.setText("");
        dimension.setText("");
        size.setText("");
        type.setText("");
        internalName.setText("");
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
}
