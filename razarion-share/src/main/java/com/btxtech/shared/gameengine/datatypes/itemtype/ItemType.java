/*
 * Copyright (c) 2010.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */

package com.btxtech.shared.gameengine.datatypes.itemtype;

import com.btxtech.shared.datatypes.I18nString;
import com.btxtech.shared.dto.Config;
import com.btxtech.shared.system.Nullable;
import jsinterop.annotations.JsType;

/**
 * User: beat
 * Date: 17.11.2009
 * Time: 22:50:01
 */
@JsType
public abstract class ItemType implements Config {
    private int id;
    private String internalName;
    private String name;
    private String description;
    private Integer model3DId;
    private Integer thumbnail;

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Override
    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public @Nullable Integer getModel3DId() {
        return model3DId;
    }

    public void setModel3DId(@Nullable Integer model3DId) {
        this.model3DId = model3DId;
    }

    public @Nullable Integer getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(@Nullable Integer thumbnail) {
        this.thumbnail = thumbnail;
    }

    public ItemType id(int id) {
        setId(id);
        return this;
    }

    public ItemType internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public ItemType i18nName(String i18nName) {
        setName(i18nName);
        return this;
    }

    public ItemType i18nDescription(String i18nDescription) {
        setDescription(i18nDescription);
        return this;
    }

    public ItemType model3DId(Integer model3DId) {
        setModel3DId(model3DId);
        return this;
    }

    public ItemType thumbnail(Integer thumbnail) {
        setThumbnail(thumbnail);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) { // equals needed because Errai binder proxy. May be wrong -> BindableProxyFactory.getBindableProxy()
            return false;
        }

        ItemType itemType = (ItemType) o;
        return getId() == itemType.getId(); // itemType.getId() needed because Errai binder proxy. May be wrong -> BindableProxyFactory.getBindableProxy()
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "ItemType id: " + id + " internalName: " + internalName;
    }
}
