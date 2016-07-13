package com.btxtech.gameengine.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.pathing.DebugHelper;
import com.btxtech.shared.gameengine.pathing.Unit;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * Created by Beat
 * 19.05.2016.
 */
public class UnitSidePaneController {
    public TextField idLabel;
    public TextField positionLabel;
    public TextField velocityLabel;
    public Button hideButton;
    private Unit unit;
    private Runnable deselectListener;

    public void init(Unit unit) {
        this.unit = unit;
        idLabel.setText(Long.toString(unit.getId()));
        positionLabel.setText(vectorAsString(unit.getPosition()));
        velocityLabel.setText(vectorAsString(unit.getVelocity()));
    }

    public boolean isSame(Unit unit) {
        return this.unit.equals(unit);
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
        DebugHelper.setDebugAllFilter(unit.getId());
    }

    public void onDebugSelective() {
        DebugHelper.setDebugSelectiveFilter(unit.getId());
    }
}
