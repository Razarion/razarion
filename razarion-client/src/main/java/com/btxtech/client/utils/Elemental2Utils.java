package com.btxtech.client.utils;

import elemental2.dom.Node;

public class Elemental2Utils {
    public static void removeAllChildren(Node node) {
        while (node.firstChild != null) {
            node.removeChild(node.lastChild);
        }
    }
}
