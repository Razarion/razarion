package com.btxtech.client.editor.widgets.childtable;

import java.util.function.Consumer;

/**
 * Created by Beat
 * on 16.08.2017.
 */
public class ChildModel<T> {
    private T child;
    private Consumer<T> childRemoveCallback;
    private Class childPanelClass;

    public T getChild() {
        return child;
    }

    public void setChild(T child) {
        this.child = child;
    }

    public void setRemoveCallback(Consumer<T> childRemoveCallback) {
        this.childRemoveCallback = childRemoveCallback;
    }

    public void remove() {
        childRemoveCallback.accept(child);
    }

    public void setChildPanelClass(Class childPanelClass) {
        this.childPanelClass = childPanelClass;
    }

    public Class getChildPanelClass() {
        return childPanelClass;
    }
}
