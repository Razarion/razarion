package com.btxtech.shared.datatypes.tracking;

/**
 * Created by Beat
 * on 08.01.2018.
 */
public class DialogTracking extends DetailedTracking {
    private Integer left;
    private Integer top;
    private Integer width;
    private Integer height;
    private Integer indexZ; // zIndex does not work here. Errai marshalling
    private String title;
    private boolean appearing;
    private int identityHashCode;

    public Integer getLeft() {
        return left;
    }

    public DialogTracking setLeft(Integer left) {
        this.left = left;
        return this;
    }

    public Integer getTop() {
        return top;
    }

    public DialogTracking setTop(Integer top) {
        this.top = top;
        return this;
    }

    public Integer getWidth() {
        return width;
    }

    public DialogTracking setWidth(Integer width) {
        this.width = width;
        return this;
    }

    public Integer getHeight() {
        return height;
    }

    public DialogTracking setHeight(Integer height) {
        this.height = height;
        return this;
    }

    public Integer getIndexZ() {
        return indexZ;
    }

    public DialogTracking setIndexZ(Integer indexZ) {
        this.indexZ = indexZ;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public DialogTracking setTitle(String title) {
        this.title = title;
        return this;
    }

    public boolean isAppearing() {
        return appearing;
    }

    public DialogTracking setAppearing(boolean appearing) {
        this.appearing = appearing;
        return this;
    }

    public int getIdentityHashCode() {
        return identityHashCode;
    }

    public DialogTracking setIdentityHashCode(int identityHashCode) {
        this.identityHashCode = identityHashCode;
        return this;
    }
}
