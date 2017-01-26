package com.btxtech.client.cockpit;

import com.btxtech.uiservice.cockpit.ScreenCover;
import elemental.client.Browser;
import elemental.css.CSSStyleDeclaration;
import elemental.dom.Element;
import elemental.html.DivElement;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 10.07.2016.
 */
@Singleton
public class ClientScreenCoverImpl implements ScreenCover {
    private static final String LOADING_COVER_ID = "RAZARION_LOADING_COVER";
    // private Logger logger = Logger.getLogger(ClientScreenCoverImpl.class.getName());
    private DivElement topDiv;
    private DivElement bottomDiv;

    @Override
    public void showStoryCover(String html) {
        if (topDiv == null || bottomDiv == null) {
            topDiv = createCoverDiv(true);
            topDiv.setInnerHTML(html);
            Browser.getDocument().getBody().appendChild(topDiv);
            bottomDiv = createCoverDiv(false);
            Browser.getDocument().getBody().appendChild(bottomDiv);
        }
    }

    @Override
    public void hideStoryCover() {
        if (topDiv != null || bottomDiv != null) {
            Browser.getDocument().getBody().removeChild(topDiv);
            topDiv = null;
            Browser.getDocument().getBody().removeChild(bottomDiv);
            bottomDiv = null;
        }
    }

    @Override
    public void removeLoadingCover() {
        Browser.getDocument().getBody().removeChild(Browser.getDocument().getElementById(LOADING_COVER_ID));
    }

    @Override
    public void fadeOutLoadingCover() {
        Element element = Browser.getDocument().getElementById(LOADING_COVER_ID);
        element.getStyle().setOpacity(0);
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
