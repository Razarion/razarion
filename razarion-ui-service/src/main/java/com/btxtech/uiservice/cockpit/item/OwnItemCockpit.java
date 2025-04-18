package com.btxtech.uiservice.cockpit.item;

import jsinterop.annotations.JsFunction;
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
    public BuildupItemCockpit[] buildupItemInfos;
    public SellHandler sellHandler;

    @JsFunction
    public interface SellHandler {
        @SuppressWarnings("unused") // Called by Angular
        void onSell();
    }
}
