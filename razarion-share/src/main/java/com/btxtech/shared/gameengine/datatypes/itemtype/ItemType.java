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
import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;

/**
 * User: beat
 * Date: 17.11.2009
 * Time: 22:50:01
 */
public abstract class ItemType implements Config {
    private int id;
    private String internalName;
    private I18nString i18nName;
    private I18nString i18nDescription;
    @CollectionReference(CollectionReferenceType.SHAPE_3D)
    private Integer shape3DId;
    private Integer meshContainerId;
    private Integer thumbnail;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    public I18nString getI18nName() {
        return i18nName;
    }

    public ItemType setId(int id) {
        this.id = id;
        return this;
    }

    @Override
    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public ItemType setI18nDescription(I18nString i18nDescription) {
        this.i18nDescription = i18nDescription;
        return this;
    }

    public I18nString getI18nDescription() {
        return i18nDescription;
    }

    public ItemType setI18nName(I18nString i18nName) {
        this.i18nName = i18nName;
        return this;
    }

    public Integer getShape3DId() {
        return shape3DId;
    }

    public ItemType setShape3DId(Integer shape3DId) {
        this.shape3DId = shape3DId;
        return this;
    }

    public Integer getMeshContainerId() {
        return meshContainerId;
    }

    public void setMeshContainerId(Integer meshContainerId) {
        this.meshContainerId = meshContainerId;
    }

    public Integer getThumbnail() {
        return thumbnail;
    }

    public ItemType setThumbnail(Integer thumbnail) {
        this.thumbnail = thumbnail;
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
