package com.btxtech.client.editor.widgets.image;

import com.btxtech.client.dialog.ModalDialogContent;
import com.btxtech.client.dialog.ModalDialogPanel;
import com.btxtech.client.imageservice.ImageUiService;
import com.btxtech.shared.dto.ImageGalleryItem;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 19.06.2016.
 */
@Templated("ImageSelectorDialog.html#image-selector-dialog")
public class ImageSelectorDialog extends Composite implements ModalDialogContent<Integer> {
    // private Logger logger = Logger.getLogger(ImageGalleryDialog.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ImageUiService imageUiService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    @ListContainer("div")
    private ListComponent<ImageGalleryItem, ImageSelectorItemWidget> imageGallery;
    private Integer selectedImageId;
    private ModalDialogPanel<Integer> modalDialogPanel;

    @Override
    public void init(Integer selectedImageId) {
        this.selectedImageId = selectedImageId;
        DOMUtil.removeAllElementChildren(imageGallery.getElement()); // Remove placeholder table row from template.
        imageUiService.getImageGalleryItems(this::onLoaded);
        imageGallery.addComponentCreationHandler(imageGalleryItemWidget -> imageGalleryItemWidget.setImageGalleryDialog(this));
        imageGallery.setSelector(imageGalleryItemWidget -> imageGalleryItemWidget.setSelected(true));
        imageGallery.setDeselector(imageGalleryItemWidget -> imageGalleryItemWidget.setSelected(false));
    }

    @Override
    public void customize(ModalDialogPanel<Integer> modalDialogPanel) {
        this.modalDialogPanel = modalDialogPanel;
    }

    @Override
    public void onClose() {
        for (ImageGalleryItem imageGalleryItem : imageGallery.getValue()) {
            imageGallery.getComponent(imageGalleryItem).ifPresent(ImageSelectorItemWidget::cleanup);
        }
    }

    private void onLoaded(List<ImageGalleryItem> imageGalleryItems) {
        imageGallery.setValue(imageGalleryItems);
        if (selectedImageId != null) {
            imageGalleryItems.stream().filter(imageGalleryItem -> imageGalleryItem.getId() == selectedImageId).forEach(imageGalleryItem -> imageGallery.selectModel(imageGalleryItem));
        }
    }

    public void selectionChanged(ImageGalleryItem newSelection) {
        imageGallery.deselectAll();
        imageGallery.selectModel(newSelection);
        selectedImageId = newSelection.getId();
        imageGallery.getComponent(newSelection).ifPresent(arg -> arg.setSelected(true));
        modalDialogPanel.setApplyValue(selectedImageId);
    }
}
