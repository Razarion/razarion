package com.btxtech.client.cockpit;

import com.btxtech.client.utils.GwtUtils;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 11.02.2017.
 */
@Templated("StoryCoverPanel.html#storyCover")
public class StoryCoverPanel implements IsElement {
    @Inject
    @DataField
    private HTMLDivElement storyCover;
    @Inject
    @DataField
    private Span text;

    @PostConstruct
    public void postConstruct() {
        GwtUtils.preventContextMenu(storyCover);
    }

    @Override
    public HTMLElement getElement() {
        return storyCover;
    }

    public void setText(String text) {
        this.text.setTextContent(text);
    }
}
