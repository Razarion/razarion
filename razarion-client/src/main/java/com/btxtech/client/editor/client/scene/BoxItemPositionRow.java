package com.btxtech.client.editor.client.scene;

import com.btxtech.client.editor.widgets.itemtype.box.BoxItemTypeWidget;
import com.btxtech.client.editor.widgets.marker.DecimalPositionWidget;
import com.btxtech.client.guielements.CommaDoubleBox;
import com.btxtech.client.utils.GradToRadConverter;
import com.btxtech.shared.dto.BoxItemPosition;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Button;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 15.08.2017.
 */
@Templated("SceneConfigPropertyPanel.html#boxItemTypePositionRow")
public class BoxItemPositionRow implements TakesValue<BoxItemPosition>, IsElement {
    @Inject
    @DataField
    private TableRow boxItemTypePositionRow;
    @Inject
    @AutoBound
    private DataBinder<BoxItemPosition> dataBinder;
    @Inject
    @Bound
    @DataField
    private DecimalPositionWidget position;
    @Inject
    @Bound(converter = GradToRadConverter.class)
    @DataField
    private CommaDoubleBox rotationZ;
    @Inject
    @DataField
    private BoxItemTypeWidget boxItemTypeWidget;
    @Inject
    @DataField
    private Button boxPositionDeleteButton;
    private SceneConfigPropertyPanel sceneConfigPropertyPanel;

    @Override
    public void setValue(BoxItemPosition boxItemPosition) {
        dataBinder.setModel(boxItemPosition);
        boxItemTypeWidget.init(boxItemPosition.getBoxItemTypeId(), boxItemPosition::setBoxItemTypeId);
    }

    @Override
    public BoxItemPosition getValue() {
        return dataBinder.getModel();
    }

    @Override
    public HTMLElement getElement() {
        return boxItemTypePositionRow;
    }

    @EventHandler("boxPositionDeleteButton")
    private void boxPositionDeleteButtonClicked(ClickEvent event) {
        sceneConfigPropertyPanel.removeBoxItemPosition(dataBinder.getModel());
    }


    public void setSceneConfigPropertyPanel(SceneConfigPropertyPanel sceneConfigPropertyPanel) {
        this.sceneConfigPropertyPanel = sceneConfigPropertyPanel;
    }
}
