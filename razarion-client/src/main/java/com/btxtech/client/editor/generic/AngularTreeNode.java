package com.btxtech.client.editor.generic;

import jsinterop.annotations.JsType;
import jsinterop.base.Any;

@JsType
public class AngularTreeNode {
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
    public String key;
}
