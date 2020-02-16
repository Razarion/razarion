package com.btxtech.client.utils;

import elemental2.dom.Node;

public class Elemental2Utils {
    public static void removeAllChildren(Node node) {
        for (int i = 0; i < node.childNodes.length; i++) {
            Node oldChild = node.childNodes.item(i);
            node.removeChild(oldChild);
        }
    }
}
