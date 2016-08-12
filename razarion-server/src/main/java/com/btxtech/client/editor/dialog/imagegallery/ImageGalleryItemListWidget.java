package com.btxtech.client.editor.dialog.imagegallery;

import com.btxtech.client.utils.GwtUtils;
import com.btxtech.shared.dto.ImageGalleryItem;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import elemental.events.Event;
import elemental.events.EventListener;
import org.jboss.errai.ui.client.widget.ListWidget;

import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 20.06.2016.
 */
public class ImageGalleryItemListWidget extends ListWidget<ImageGalleryItem, ImageGalleryItemWidget> {
    // private Logger logger = Logger.getLogger(ImageGalleryItemListWidget.class.getName());
    private ImageGalleryDialog imageGalleryDialog;

    @Override
    protected Class<ImageGalleryItemWidget> getItemComponentType() {
        return ImageGalleryItemWidget.class;
    }

    public void setSelectedImage(ImageGalleryItem selectedImage) {
        getComponent(selectedImage).setSelected(true);
    }

    @Override
    protected void onItemsRendered(List<ImageGalleryItem> items) {
        for (final ImageGalleryItem item : items) {
            GwtUtils.castElementToElement(getComponent(item).getElement()).addEventListener(Event.CLICK, new EventListener() {
                @Override
                public void handleEvent(Event evt) {
                    Element e = ((JavaScriptObject) evt.getTarget()).cast();
                    if (e.getTagName().equalsIgnoreCase("BUTTON")) {
                        return;
                    }
                    imageGalleryDialog.selectionChanged(item);
                }
            }, false);
        }
    }

    public void setImageGalleryDialog(ImageGalleryDialog imageGalleryDialog) {
        this.imageGalleryDialog = imageGalleryDialog;
    }

    public void setChanged(Collection<ImageGalleryItem> changed) {
        for (ImageGalleryItem imageGalleryItem : getValue()) {
            getComponent(imageGalleryItem).setChanged(changed.contains(imageGalleryItem));
        }
    }
}
