package com.btxtech.client.cockpit;

import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 11.02.2017.
 */
@Templated("StoryCoverPanel.html#storyCover")
public class StoryCoverPanel extends Composite {
    @Inject
    @DataField
    private Span text;

    public void setText(String text) {
        this.text.setTextContent(text);
    }
}
