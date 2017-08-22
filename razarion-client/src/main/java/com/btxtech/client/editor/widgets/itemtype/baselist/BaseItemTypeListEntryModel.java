package com.btxtech.client.editor.widgets.itemtype.baselist;

import java.util.function.Consumer;

/**
 * Created by Beat
 * on 22.08.2017.
 */
public class BaseItemTypeListEntryModel {
    private Integer baseItemTypeId;
    private final Consumer<BaseItemTypeListEntryModel> removeCallback;

    public BaseItemTypeListEntryModel(Integer baseItemTypeId, Consumer<BaseItemTypeListEntryModel> removeCallback) {
        this.baseItemTypeId = baseItemTypeId;
        this.removeCallback = removeCallback;
    }

    public Integer getBaseItemTypeId() {
        return baseItemTypeId;
    }

    public void setBaseItemTypeId(Integer baseItemTypeId) {
        this.baseItemTypeId = baseItemTypeId;
    }

    public void remove() {
        removeCallback.accept(this);
    }
}
