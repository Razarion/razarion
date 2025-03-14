package com.btxtech.shared.dto;

import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;

/**
 * Created by Beat
 * 01.11.2016.
 */
public class BotRemoveOwnItemCommandConfig extends AbstractBotCommandConfig<BotRemoveOwnItemCommandConfig> {
    @CollectionReference(CollectionReferenceType.BASE_ITEM)
    private Integer baseItemType2RemoveId;

    public Integer getBaseItemType2RemoveId() {
        return baseItemType2RemoveId;
    }

    public BotRemoveOwnItemCommandConfig setBaseItemType2RemoveId(Integer baseItemType2RemoveId) {
        this.baseItemType2RemoveId = baseItemType2RemoveId;
        return this;
    }
}
