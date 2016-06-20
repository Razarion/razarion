package com.btxtech.client.dialog.imagegallery;

import com.btxtech.client.dialog.ModalDialogContent;
import com.btxtech.client.dialog.ModalDialogManager;
import com.btxtech.client.imageservice.ImageUiService;
import com.btxtech.client.utils.ControlUtils;
import com.btxtech.client.utils.GwtUtils;
import com.btxtech.shared.dto.ImageGalleryItem;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.ButtonElement;
import elemental.html.File;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 19.06.2016.
 */
@Templated("ImageGalleryDialog.html#image-gallery-dialog")
public class ImageGalleryDialog extends Composite implements ModalDialogContent<Integer>, ImageUiService.ImageGalleryItemListener, ImageUiService.ChangeListener {
    // private Logger logger = Logger.getLogger(ImageGalleryDialog.class.getName());
    @Inject
    private ImageUiService imageUiService;
    @Inject
    @DataField
    private ImageGalleryItemListWidget imageGalleryItemListWidget;
    @Inject
    @DataField
    private Button reloadButton;
    @Inject
    @DataField
    private Button saveButton;
    @Inject
    @DataField
    private Button newButton;
    private int selectedImageId;
    private ModalDialogManager modalDialogManager;

    @Override
    public void init(Integer imageId) {
        selectedImageId = imageId;
        imageUiService.getImageGalleryItems(this);
        imageGalleryItemListWidget.setImageGalleryDialog(this);
        imageGalleryItemListWidget.setChanged(imageUiService.getChanged());
        imageUiService.addChangeListener(this);
    }

    @Override
    public void customize(ModalDialogManager modalDialogManager) {
        this.modalDialogManager = modalDialogManager;
        modalDialogManager.setApplyValue(selectedImageId);
    }

    @Override
    public void onClose() {
        imageUiService.removeChangeListener(this);
    }

    @Override
    public void onLoaded(List<ImageGalleryItem> imageGalleryItems) {
        imageGalleryItemListWidget.setItems(imageGalleryItems);
        for (ImageGalleryItem imageGalleryItem : imageGalleryItems) {
            if (imageGalleryItem.getId() == selectedImageId) {
                imageGalleryItemListWidget.setSelectedImage(imageGalleryItem);
            }
        }
    }

    @EventHandler("reloadButton")
    private void reloadButtonClicked(ClickEvent event) {
        imageUiService.reload(this);
    }

    @EventHandler("saveButton")
    private void saveButtonClicked(ClickEvent event) {
        imageUiService.save(this);
    }

    @EventHandler("newButton")
    private void newButtonClicked(ClickEvent event) {
        ControlUtils.openSingleFileDataUrlUpload(new ControlUtils.SingleFileDataUrlListener() {
            @Override
            public void onLoaded(String dataUrl, File file) {
                imageUiService.create(dataUrl, ImageGalleryDialog.this);
            }
        });
    }

    public void selectionChanged(ImageGalleryItem newSelection) {
        for (ImageGalleryItem imageGalleryItem : imageGalleryItemListWidget.getValue()) {
            if (imageGalleryItem.getId() == selectedImageId) {
                imageGalleryItemListWidget.getWidget(imageGalleryItem).setSelected(false);
                break;
            }
        }
        selectedImageId = newSelection.getId();
        imageGalleryItemListWidget.getWidget(newSelection).setSelected(true);
        modalDialogManager.setApplyValue(selectedImageId);
    }

    @Override
    public void onChanged(Collection<ImageGalleryItem> changed) {
        imageGalleryItemListWidget.setChanged(changed);
    }
}
