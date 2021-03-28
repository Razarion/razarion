package com.btxtech.client.editor.generic;

import com.btxtech.client.editor.generic.model.AbstractPropertyModel;
import jsinterop.annotations.JsType;
import jsinterop.base.Any;

@JsType
public class AngularTreeNode {
    // --- Used by Angular PrimeNG in Javascript
    public String label;
    public AngularTreeNodeData data;
    public String icon;
    public Any expandedIcon;
    public Any collapsedIcon;
    public AngularTreeNode[] children;
    public boolean leaf;
    public boolean expanded;
    public String type;
    public AngularTreeNode parent;
    public boolean partialSelected;
    public String styleClass;
    public boolean draggable;
    public boolean droppable;
    public boolean selectable;
    // --- Used by Razarion code
    public AbstractPropertyModel abstractPropertyModel;

    public AngularTreeNode(AbstractPropertyModel abstractPropertyModel, AngularTreeNode parent) {
        this.parent = parent;
        this.abstractPropertyModel = abstractPropertyModel;
    }
}
