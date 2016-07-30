package com.btxtech.shared.datatypes;

import com.btxtech.shared.gameengine.datatypes.syncobject.SyncItem;

/**
 * Created by Beat
 * 15.05.2016.
 */
public class ModelMatrices {
    private Matrix4 model;
    private Matrix4 norm;
    private SyncItem syncItem;

    public ModelMatrices setModel(Matrix4 model) {
        this.model = model;
        return this;
    }

    public ModelMatrices setNorm(Matrix4 norm) {
        this.norm = norm;
        return this;
    }

    public ModelMatrices setSyncItem(SyncItem syncItem) {
        this.syncItem = syncItem;
        return this;
    }

    public Matrix4 getModel() {
        return model;
    }

    public Matrix4 getNorm() {
        return norm;
    }

    public SyncItem getSyncItem() {
        return syncItem;
    }

    public ModelMatrices multiply(Matrix4 model, Matrix4 norm) {
        ModelMatrices modelMatrices = new ModelMatrices();
        modelMatrices.setSyncItem(syncItem);
        if (model != null) {
            modelMatrices.setModel(this.model.multiply(model));
        } else {
            modelMatrices.setModel(this.model);
        }
        if (norm != null) {
            modelMatrices.setNorm(this.norm.multiply(norm));
        } else {
            modelMatrices.setNorm(this.norm);
        }
        return modelMatrices;
    }
}
