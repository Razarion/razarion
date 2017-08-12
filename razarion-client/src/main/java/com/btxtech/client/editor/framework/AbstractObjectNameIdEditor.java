package com.btxtech.client.editor.framework;

import com.btxtech.shared.dto.ObjectNameId;
import org.jboss.errai.common.client.api.RemoteCallback;

import java.util.List;

/**
 * Created by Beat
 * on 04.08.2017.
 */
public abstract class AbstractObjectNameIdEditor {
    private List<ObjectNameId> objectNameIds;
    private Runnable updateListener;

    protected abstract void read(RemoteCallback<List<ObjectNameId>> callback);

    protected abstract void create(RemoteCallback<?> callback);

    protected abstract void swap(int index1, int index2, RemoteCallback<?> callback);

    protected abstract void delete(ObjectNameId objectNameId, RemoteCallback<?> callback);

    protected abstract <T extends ObjectNamePropertyPanel> Class<T> getObjectNamePropertyPanelClass();

    public void setUpdateListener(Runnable updateListener) {
        this.updateListener = updateListener;
    }

    public void setObjectNameIds(List<ObjectNameId> objectNameIds) {
        this.objectNameIds = objectNameIds;
        updateListener.run();
    }

    public List<ObjectNameId> getObjectNameIds() {
        return objectNameIds;
    }

    void up(ObjectNameId objectNameId, RemoteCallback<?> callback) {
        int index = objectNameIds.indexOf(objectNameId);
        if (index - 1 < 0) {
            return;
        }
        swap(index, index - 1, callback);
    }

    void down(ObjectNameId objectNameId, RemoteCallback<?> callback) {
        int index = objectNameIds.indexOf(objectNameId);
        if (index + 1 >= objectNameIds.size()) {
            return;
        }
        swap(index, index + 1, callback);
    }

    boolean hasSuccessor(ObjectNameId objectNameId) {
        return objectNameIds.indexOf(objectNameId) < objectNameIds.size() - 1;
    }

    boolean hasPredecessor(ObjectNameId objectNameId) {
        return objectNameIds.indexOf(objectNameId) > 0;
    }

}
