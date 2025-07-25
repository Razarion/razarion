package com.btxtech.server.model.ui;

import jakarta.persistence.*;


/**
 * Created by Beat
 * 18.06.2016.
 */
@Entity
@Table(name = "IMAGE_LIBRARY")
public class ImageLibraryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Lob
    private byte[] data;
    private String internalName;
    private String type;
    private long size;

    public ImageGalleryItem toImageGalleryItem() {
        ImageGalleryItem imageGalleryItem = new ImageGalleryItem();
        imageGalleryItem.setId(id);
        imageGalleryItem.setSize((int) size);
        imageGalleryItem.setType(type);
        imageGalleryItem.setInternalName(internalName);
        return imageGalleryItem;
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
