package com.btxtech.shared.gameengine.datatypes;

import jsinterop.annotations.JsType;

/**
 * Rareness of an {@link InventoryArtifact}. Ported from the legacy controltheland project.
 */
@JsType
public enum Rareness {
    COMMON("#d6f6ff"),
    UN_COMMON("#70d460"),
    RARE("#1273d2"),
    EPIC("#a042cc"),
    LEGENDARY("#f07d4e");

    private final String htmlColor;

    Rareness(String htmlColor) {
        this.htmlColor = htmlColor;
    }

    public String getHtmlColor() {
        return htmlColor;
    }
}
