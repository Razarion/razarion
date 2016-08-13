package com.btxtech.client.cockpit;

import com.btxtech.uiservice.cockpit.StoryCover;
import elemental.client.Browser;
import elemental.css.CSSStyleDeclaration;
import elemental.html.DivElement;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 10.07.2016.
 */
@Singleton
public class ClientStoryCoverImpl implements StoryCover {
    private DivElement topDiv;
    private DivElement bottomDiv;

    @Override
    public void show(String html) {
        if (topDiv == null || bottomDiv == null) {
            topDiv = createCoverDiv(true);
            topDiv.setInnerHTML(html);
            Browser.getDocument().getBody().appendChild(topDiv);
            bottomDiv = createCoverDiv(false);
            Browser.getDocument().getBody().appendChild(bottomDiv);
        }
    }

    @Override
    public void hide() {
        if (topDiv != null || bottomDiv != null) {
            Browser.getDocument().getBody().removeChild(topDiv);
            topDiv = null;
            Browser.getDocument().getBody().removeChild(bottomDiv);
            bottomDiv = null;
        }
    }

    private DivElement createCoverDiv(boolean top) {
        DivElement div = Browser.getDocument().createDivElement();
        div.getStyle().setPosition(CSSStyleDeclaration.Position.ABSOLUTE);
        if (top) {
            div.getStyle().setTop("0");
        } else {
            div.getStyle().setBottom("0");
        }
        div.getStyle().setLeft("0");
        div.getStyle().setRight("0");
        div.getStyle().setWidth(100, CSSStyleDeclaration.Unit.PCT);
        div.getStyle().setHeight(10, CSSStyleDeclaration.Unit.PCT);
        div.getStyle().setZIndex(ZIndexConstants.STORY_COVER);
        div.getStyle().setBackgroundColor("black");
        div.getStyle().setBackgroundColor("black");
        div.getStyle().setColor("#FFF");
        div.getStyle().setFontSize(200, CSSStyleDeclaration.Unit.PCT);
        return div;
    }
}
