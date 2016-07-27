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
import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.shared.gameengine.datatypes.SurfaceType;
import com.btxtech.shared.gameengine.datatypes.TerrainType;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * User: beat
 * Date: 17.11.2009
 * Time: 22:50:01
 */
@Portable
public abstract class ItemType {
    private int id;
    private String name;
    private I18nString i18Name;
    private I18nString description;
    private TerrainType terrainType;
    private SurfaceType adjoinSurfaceType;
    @Deprecated
    private BoundingBox boundingBox;
    private ItemTypeSpriteMap itemTypeSpriteMap;
    private Integer selectionSound;
    private Integer buildupSound;
    private Integer commandSound;
    private Integer explosionClipId;
    private VertexContainer vertexContainer;

    public int getId() {
        return id;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public ItemType setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
        return this;
    }

    public String getName() {
        return name;
    }

    public I18nString getI18Name() {
        return i18Name;
    }

    public ItemType setId(int id) {
        this.id = id;
        return this;
    }

    public ItemType setDescription(I18nString description) {
        this.description = description;
        return this;
    }

    public I18nString getDescription() {
        return description;
    }

    public ItemType setName(String name) {
        this.name = name;
        return this;
    }

    public ItemType setI18Name(I18nString i18Name) {
        this.i18Name = i18Name;
        return this;
    }

    public ItemType setTerrainType(TerrainType terrainType) {
        this.terrainType = terrainType;
        return this;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public SurfaceType getAdjoinSurfaceType() {
        return adjoinSurfaceType;
    }

    public ItemType setAdjoinSurfaceType(SurfaceType adjoinSurfaceType) {
        this.adjoinSurfaceType = adjoinSurfaceType;
        return this;
    }

    public ItemTypeSpriteMap getItemTypeSpriteMap() {
        return itemTypeSpriteMap;
    }

    public ItemType setItemTypeSpriteMap(ItemTypeSpriteMap itemTypeSpriteMap) {
        this.itemTypeSpriteMap = itemTypeSpriteMap;
        return this;
    }

    public Integer getSelectionSound() {
        return selectionSound;
    }

    public ItemType setSelectionSound(Integer selectionSound) {
        this.selectionSound = selectionSound;
        return this;
    }

    public Integer getBuildupSound() {
        return buildupSound;
    }

    public ItemType setBuildupSound(Integer buildupSound) {
        this.buildupSound = buildupSound;
        return this;
    }

    public Integer getCommandSound() {
        return commandSound;
    }

    public ItemType setCommandSound(Integer commandSound) {
        this.commandSound = commandSound;
        return this;
    }

    public Integer getExplosionClipId() {
        return explosionClipId;
    }

    public ItemType setExplosionClipId(Integer explosionClipId) {
        this.explosionClipId = explosionClipId;
        return this;
    }

    public VertexContainer getVertexContainer() {
        return vertexContainer;
    }

    public ItemType setVertexContainer(VertexContainer vertexContainer) {
        this.vertexContainer = vertexContainer;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ItemType itemType = (ItemType) o;
        return id == itemType.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "ItemType: " + name;
    }
}
