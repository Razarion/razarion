package com.btxtech.client.editor.imagegallery;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.client.imageservice.ImageUiService;
import com.btxtech.client.utils.ControlUtils;
import com.btxtech.shared.dto.ImageGalleryItem;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 19.06.2016.
 */
@Templated("ImageGalleryDialog.html#image-gallery-dialog")
public class ImageGalleryDialog extends Composite implements ModalDialogContent<Void> {
    // private Logger logger = Logger.getLogger(ImageGalleryDialog.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ImageUiService imageUiService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    @ListContainer("div")
    private ListComponent<ImageGalleryItem, ImageGalleryItemWidget> imageGallery;

    @Override
    public void init(Void ignore) {
        DOMUtil.removeAllElementChildren(imageGallery.getElement()); // Remove placeholder table row from template.
        imageUiService.addChangeListener(this::onChanged);
        imageUiService.getImageGalleryItems(this::onLoaded);
    }

    private void onChanged(Collection<ImageGalleryItem> imageGalleryItems) {
        for (ImageGalleryItem imageGalleryItem : imageGallery.getValue()) {
            imageGallery.getComponent(imageGalleryItem).ifPresent(arg -> arg.setChanged(imageGalleryItems.contains(imageGalleryItem)));
        }
    }

    @Override
    public void customize(ModalDialogPanel<Void> modalDialogPanel) {
        modalDialogPanel.addNonClosableFooterButton("Reload", () -> imageUiService.reload(this::onLoaded));
        modalDialogPanel.addNonClosableFooterButton("New", () -> ControlUtils.openSingleFileDataUrlUpload((dataUrl, file) -> imageUiService.create(dataUrl, this::onLoaded)));
        modalDialogPanel.addNonClosableFooterButton("Save", () -> imageUiService.save(this::onLoaded));
    }

    @Override
    public void onClose() {
        for (ImageGalleryItem imageGalleryItem : imageGallery.getValue()) {
            imageGallery.getComponent(imageGalleryItem).ifPresent(ImageGalleryItemWidget::cleanup);
        }
        imageUiService.removeChangeListener(this::onChanged);
    }

    private void onLoaded(List<ImageGalleryItem> imageGalleryItems) {
        imageGallery.setValue(imageGalleryItems);
        onChanged(imageUiService.getChanged());
    }
}
