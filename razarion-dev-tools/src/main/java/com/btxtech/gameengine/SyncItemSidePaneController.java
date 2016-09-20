package com.btxtech.gameengine;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * Created by Beat
 * 19.05.2016.
 */
public class SyncItemSidePaneController {
    public TextField idLabel;
    public TextField positionLabel;
    public TextField velocityLabel;
    public Button hideButton;
    private SyncItem syncItem;
    private Runnable deselectListener;

    public void init(SyncItem syncItem) {
        this.syncItem = syncItem;
        idLabel.setText(Long.toString(syncItem.getId()));
        positionLabel.setText(vectorAsString(syncItem.getSyncPhysicalArea().getXYPosition()));
        if (syncItem.getSyncPhysicalArea().canMove()) {
            velocityLabel.setText(vectorAsString(((SyncPhysicalMovable) syncItem.getSyncPhysicalArea()).getVelocity()));
        } else {
            velocityLabel.setText(vectorAsString(null));
        }
    }

    public boolean isSame(SyncItem syncItem) {
        return this.syncItem.equals(syncItem);
    }

    private String vectorAsString(DecimalPosition vector) {
        if (vector != null) {
            return String.format("%.2f:%.2f", vector.getX(), vector.getY());
        } else {
            return "-:-";
        }
    }

    public void setSelected(Runnable deselectListener) {
        this.deselectListener = deselectListener;
        hideButton.disableProperty().setValue(false);
    }

    public void onHideButton() {
        deselectListener.run();
    }

    public void onDebugAll() {
        // TODO DebugHelper.setDebugAllFilter(syncItem.getId());
    }

    public void onDebugSelective() {
        // TODO DebugHelper.setDebugSelectiveFilter(syncItem.getId());
    }
}
