package com.btxtech.uiservice.cockpit.item;

/**
 * Created by Beat
 * 29.09.2016.
 */
public class OwnItemCockpit {
    public String imageUrl;
    public String itemTypeName;
    public String itemTypeDescr;
    public BuildupItemCockpit[] buildupItemInfos;
    public ItemContainerCockpit itemContainerInfo;
    public SellHandler sellHandler;

    public interface SellHandler {
        @SuppressWarnings("unused") // Called by Angular
        void onSell();
    }
}
