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

import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.Collection;

/**
 * User: beat
 * Date: 17.11.2009
 * Time: 23:18:42
 */
@Portable
public class FactoryType {
    private double progress;
    private Collection<Integer> ableToBuild;

    /**
     * Used by GWT
     */
    public FactoryType() {
    }

    public FactoryType(double progress, Collection<Integer> ableToBuild) {
        this.progress = progress;
        this.ableToBuild = ableToBuild;
    }

    public double getProgress() {
        return progress;
    }

    public boolean isAbleToBuild(int itemTypeId) {
        return ableToBuild.contains(itemTypeId);
    }

    public Collection<Integer> getAbleToBuild() {
        return ableToBuild;
    }

    public void changeTo(FactoryType factoryType) {
        progress = factoryType.progress;
        ableToBuild = factoryType.ableToBuild;
    }
}
