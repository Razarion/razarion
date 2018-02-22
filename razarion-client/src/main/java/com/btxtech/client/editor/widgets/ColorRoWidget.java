package com.btxtech.client.editor.widgets;

import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.shared.datatypes.Color;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 22.02.2018.
 */
@Templated("ColorRoWidget.html#colorRoWidget")
public class ColorRoWidget implements IsElement {
    @Inject
    @DataField
    private Div colorRoWidget;
    @Inject
    @DataField
    private Span rLabel;
    @Inject
    @DataField
    private Span gLabel;
    @Inject
    @DataField
    private Span bLabel;
    @Inject
    @DataField
    private Div colorDiv;

    public void init(Color color) {
        rLabel.setTextContent(DisplayUtils.handleDouble2(color.getR()));
        gLabel.setTextContent(DisplayUtils.handleDouble2(color.getG()));
        bLabel.setTextContent(DisplayUtils.handleDouble2(color.getB()));
        colorDiv.getStyle().setProperty("background-color", color.toHtmlColor());
    }

    @Override
    public HTMLElement getElement() {
        return colorRoWidget;
    }
}
