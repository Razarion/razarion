package com.btxtech.client.cockpit.item;

import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.TableCell;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Beat
 * 30.09.2016.
 */
@Templated("ClientOwnMultiDifferentItemPanel.html#selectedItemTd")
public class BaseItemTypeCountPanel implements TakesValue<BaseItemTypeCount>, IsElement {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    @Named("td")
    private TableCell selectedItemTd;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Image image;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label countLabel;
    private BaseItemTypeCount baseItemTypeCount;

    @Override
    public HTMLElement getElement() {
        return selectedItemTd;
    }

    @Override
    public void setValue(BaseItemTypeCount baseItemTypeCount) {
        this.baseItemTypeCount = baseItemTypeCount;
        // TODO image
        countLabel.setText(Integer.toString(baseItemTypeCount.getCount()));
    }

    @Override
    public BaseItemTypeCount getValue() {
        return baseItemTypeCount;
    }
}
