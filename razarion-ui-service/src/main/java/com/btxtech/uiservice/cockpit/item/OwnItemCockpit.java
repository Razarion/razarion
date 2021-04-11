package com.btxtech.uiservice.cockpit.item;

import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 29.09.2016.
 */
@JsType
public class OwnItemCockpit {
    public String imageUrl;
    public String itemTypeName;
    public String itemTypeDescr;
    public boolean sellButton;
    public BuildupItemCockpit[] buildupItemInfos;
}
