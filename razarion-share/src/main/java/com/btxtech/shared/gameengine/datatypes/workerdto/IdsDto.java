package com.btxtech.shared.gameengine.datatypes.workerdto;

import org.dominokit.jackson.annotation.JSONMapper;

import java.util.List;

@JSONMapper
public class IdsDto {
    private List<Integer> ids;

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

    public IdsDto ids(List<Integer> ids) {
        setIds(ids);
        return this;
    }
}
