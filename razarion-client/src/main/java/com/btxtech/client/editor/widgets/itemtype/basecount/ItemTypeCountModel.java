package com.btxtech.client.editor.widgets.itemtype.basecount;

import org.jboss.errai.databinding.client.api.Bindable;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 07.08.2017.
 */
@Bindable
public class ItemTypeCountModel {
    private Consumer<ItemTypeCountModel> removeCallback;
    private Runnable changeCallback;
    private Integer itemType;
    private int count;

    /**
     * Used by Errai
     */
    public ItemTypeCountModel() {
    }

    public ItemTypeCountModel(Map.Entry<Integer, Integer> entry, Runnable changeCallback, Consumer<ItemTypeCountModel> removeCallback) {
        itemType = entry.getKey();
        count = entry.getValue();
        this.changeCallback = changeCallback;
        this.removeCallback = removeCallback;
    }

    public ItemTypeCountModel(Runnable changeCallback, Consumer<ItemTypeCountModel> removeCallback) {
        this.changeCallback = changeCallback;
        this.removeCallback = removeCallback;
    }

    public Integer getItemType() {
        return itemType;
    }

    public void setItemType(Integer itemType) {
        this.itemType = itemType;
        changeCallback.run();
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
        changeCallback.run();
    }

    public void remove() {
        removeCallback.accept(this);
    }
}
