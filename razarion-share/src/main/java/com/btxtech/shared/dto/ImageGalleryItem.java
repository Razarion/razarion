package com.btxtech.shared.dto;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Created by Beat
 * 15.06.2016.
 */
@Portable
public class ImageGalleryItem {
    private int id;
    private int size;
    private String type;
    private String internalName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ImageGalleryItem that = (ImageGalleryItem) o;
        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
