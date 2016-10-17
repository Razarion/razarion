package com.btxtech.client.editor.shape3dgallery;

import com.btxtech.shared.datatypes.shape.AnimationTrigger;
import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
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
import javax.inject.Inject;
import java.util.Arrays;

/**
 * Created by Beat
 * 26.08.2016.
 */
@Templated("Shape3DPropertyPanel.html#tableRowAnimation")
public class AnimationPanel implements TakesValue<ModelMatrixAnimation>, IsElement {
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
    private ValueListBox<AnimationTrigger> animationTrigger;
    private AnimationTrigger newAnimationTrigger;
    private Shape3DPropertyPanel shape3DPropertyPanel;

    @PostConstruct
    public void postConstruct() {
        animationTrigger.setAcceptableValues(Arrays.asList(AnimationTrigger.values()));
        animationTrigger.addValueChangeHandler(event -> {
            newAnimationTrigger = animationTrigger.getValue();
            shape3DPropertyPanel.animationPanelChanged(this);
        });
    }

    @Override
    public void setValue(ModelMatrixAnimation modelMatrixAnimation) {
        binder.setModel(modelMatrixAnimation);
        if (modelMatrixAnimation.getAnimationTrigger() != null) {
            animationTrigger.setValue(modelMatrixAnimation.getAnimationTrigger());
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

    public AnimationTrigger getNewAnimationTrigger() {
        return newAnimationTrigger;
    }

    public void setShape3DPropertyPanel(Shape3DPropertyPanel shape3DPropertyPanel) {
        this.shape3DPropertyPanel = shape3DPropertyPanel;
    }
}
