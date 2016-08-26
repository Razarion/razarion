package com.btxtech.client.editor.shape3dgallery;

import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemState;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.Arrays;

/**
 * Created by Beat
 * 26.08.2016.
 */
@Templated("Shape3DPropertyPanel.html#tableRowAnimation")
public class AnimationPanel implements TakesValue<ModelMatrixAnimation>, IsElement {
    @Inject
    private Event<AnimationPanel> eventTrigger;
    @Inject
    @AutoBound
    private DataBinder<ModelMatrixAnimation> binder;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private TableRow tableRowAnimation;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @Bound
    @DataField
    private Label id;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private ValueListBox<ItemState> animationTrigger;
    private ItemState newItemState;

    @PostConstruct
    public void postConstruct() {
        animationTrigger.setAcceptableValues(Arrays.asList(ItemState.values()));
        animationTrigger.addValueChangeHandler(event -> {
            newItemState = animationTrigger.getValue();
            eventTrigger.fire(this);
        });
    }

    @Override
    public void setValue(ModelMatrixAnimation modelMatrixAnimation) {
        binder.setModel(modelMatrixAnimation);
        if(modelMatrixAnimation.getItemState() != null) {
            animationTrigger.setValue(modelMatrixAnimation.getItemState());
        }
    }

    @Override
    public HTMLElement getElement() {
        return tableRowAnimation;
    }

    @Override
    public ModelMatrixAnimation getValue() {
        return binder.getModel();
    }

    public ItemState getNewItemState() {
        return newItemState;
    }
}
