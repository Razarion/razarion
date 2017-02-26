package com.btxtech.client.dialog.levelup;

import com.btxtech.shared.rest.RestUrl;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Beat
 * 26.02.2017.
 */
@Templated("LevelUpDialog.html#itemLimitationRow")
public class ItemTypeLimitationComponent implements TakesValue<ItemTypeLimitation>, IsElement {
    @Inject
    @DataField
    @Named("tr")
    private TableRow itemLimitationRow;
    @Inject
    @DataField
    private Image image;
    @Inject
    @DataField
    private Label count;
    @Inject
    @DataField
    private Label name;
    private ItemTypeLimitation itemTypeLimitation;

    @Override
    public void setValue(ItemTypeLimitation itemTypeLimitation) {
        this.itemTypeLimitation = itemTypeLimitation;
        image.setUrl(RestUrl.getImageServiceUrlSafe(itemTypeLimitation.getImageId()));
        count.setText(Integer.toString(itemTypeLimitation.getCount()));
        name.setText(itemTypeLimitation.getName());
    }

    @Override
    public ItemTypeLimitation getValue() {
        return itemTypeLimitation;
    }

    @Override
    public HTMLElement getElement() {
        return itemLimitationRow;
    }
}
