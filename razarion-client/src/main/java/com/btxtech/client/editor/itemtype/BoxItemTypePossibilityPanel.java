package com.btxtech.client.editor.itemtype;

import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemTypePossibility;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.shared.api.annotations.Templated;

/**
 * Created by Beat
 * on 18.09.2017.
 */
@Templated("BoxItemTypePossibilityPanel.html#boxItemTypePossibilityPanel")
public class BoxItemTypePossibilityPanel extends Composite implements TakesValue<BoxItemTypePossibility> {
    private BoxItemTypePossibility boxItemTypePossibility;

    @Override
    public void setValue(BoxItemTypePossibility boxItemTypePossibility) {
        this.boxItemTypePossibility = boxItemTypePossibility;
    }

    @Override
    public BoxItemTypePossibility getValue() {
        return boxItemTypePossibility;
    }
}
