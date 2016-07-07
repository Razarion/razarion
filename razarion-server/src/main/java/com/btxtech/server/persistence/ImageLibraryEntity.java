package com.btxtech.server.persistence;

import com.btxtech.shared.dto.ImageGalleryItem;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Created by Beat
 * 18.06.2016.
 */
@Entity
@Table(name = "IMAGE_LIBRARY")
public class ImageLibraryEntity {
    @Id
    @GeneratedValue
    private Long id;
    @Lob
    private byte[] data;
    private String internalName;
    private String type;
    private long size;

    public ImageGalleryItem toImageGalleryItem() {
        ImageGalleryItem imageGalleryItem = new ImageGalleryItem();
        imageGalleryItem.setId(id.intValue());
        imageGalleryItem.setSize((int)size);
        imageGalleryItem.setType(type);
        imageGalleryItem.setInternalName(internalName);
        return imageGalleryItem;
    }

    public Long getId() {
        return id;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ImageLibraryEntity that = (ImageLibraryEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
