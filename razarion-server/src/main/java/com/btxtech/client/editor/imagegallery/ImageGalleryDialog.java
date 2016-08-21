package com.btxtech.client.editor.imagegallery;

import com.btxtech.client.dialog.ModalDialogContent;
import com.btxtech.client.dialog.ModalDialogManager;
import com.btxtech.client.dialog.ModalDialogPanel;
import com.btxtech.client.imageservice.ImageUiService;
import com.btxtech.client.utils.ControlUtils;
import com.btxtech.shared.dto.ImageGalleryItem;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

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
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button reloadButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button saveButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button newButton;
    private int selectedImageId;
    private ModalDialogPanel<Integer> modalDialogPanel;

    @Override
    public void init(Integer imageId) {
        selectedImageId = imageId;
        imageUiService.getImageGalleryItems(this);
        imageGalleryItemListWidget.setImageGalleryDialog(this);
        imageGalleryItemListWidget.setChanged(imageUiService.getChanged());
        imageUiService.addChangeListener(this);
    }

    @Override
    public void customize(ModalDialogPanel<Integer> modalDialogPanel) {
        this.modalDialogPanel = modalDialogPanel;
        modalDialogPanel.setApplyValue(selectedImageId);
    }

    @Override
    public void onClose() {
        imageUiService.removeChangeListener(this);
    }

    @Override
    public void onLoaded(List<ImageGalleryItem> imageGalleryItems) {
        imageGalleryItemListWidget.setItems(imageGalleryItems);
        imageGalleryItems.stream().filter(imageGalleryItem -> imageGalleryItem.getId() == selectedImageId).forEach(imageGalleryItem -> imageGalleryItemListWidget.setSelectedImage(imageGalleryItem));
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
        ControlUtils.openSingleFileDataUrlUpload((dataUrl, file) -> imageUiService.create(dataUrl, ImageGalleryDialog.this));
    }

    public void selectionChanged(ImageGalleryItem newSelection) {
        for (ImageGalleryItem imageGalleryItem : imageGalleryItemListWidget.getValue()) {
            if (imageGalleryItem.getId() == selectedImageId) {
                imageGalleryItemListWidget.getComponent(imageGalleryItem).setSelected(false);
                break;
            }
        }
        selectedImageId = newSelection.getId();
        imageGalleryItemListWidget.getComponent(newSelection).setSelected(true);
        modalDialogPanel.setApplyValue(selectedImageId);
    }

    @Override
    public void onChanged(Collection<ImageGalleryItem> changed) {
        imageGalleryItemListWidget.setChanged(changed);
    }
}
