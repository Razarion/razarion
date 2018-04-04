package com.btxtech.client.cockpit.quest;

import com.btxtech.common.DisplayUtils;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneIndicationInfo;
import com.google.gwt.user.client.TakesValue;
import elemental2.dom.DomGlobal;
import elemental2.svg.SVGStopElement;
import elemental2.svg.SVGTextElement;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 30.03.2018.
 */
@Templated("TopRightCockpitWidget.html#botSceneIndication")
public class BotSceneIndicationInfoWidget implements TakesValue<BotSceneIndicationInfo>, IsElement {
    private static final Color RED_BG = Color.fromHtmlColor("#FF0000");
    private static final Color RED_FG = Color.fromHtmlColor("#FF6600");
    private static final Color GREEN = Color.fromHtmlColor("#00FF00");
    // private Logger logger = Logger.getLogger(BotSceneIndicationInfoWidget.class.getName());
    @Inject
    @DataField
    private Div botSceneIndication;
    @DataField
    private SVGTextElement botSceneIndicationMoodText = (SVGTextElement) DomGlobal.document.createElementNS("http://www.w3.org/2000/svg", "text");
    @DataField
    private SVGStopElement stopLinearGradientGreen1 = (SVGStopElement) DomGlobal.document.createElementNS("http://www.w3.org/2000/svg", "stop");
    @DataField
    private SVGStopElement stopLinearGradientRed = (SVGStopElement) DomGlobal.document.createElementNS("http://www.w3.org/2000/svg", "stop");
    @DataField
    private SVGStopElement stopLinearGradientGreen2 = (SVGStopElement) DomGlobal.document.createElementNS("http://www.w3.org/2000/svg", "stop");
    private BotSceneIndicationInfo botSceneIndicationInfo;

    @Override
    public void setValue(BotSceneIndicationInfo botSceneIndicationInfo) {
        this.botSceneIndicationInfo = botSceneIndicationInfo;
        botSceneIndicationMoodText.textContent = DisplayUtils.handleInteger(botSceneIndicationInfo.getConflictStep()) + "/" + DisplayUtils.handleInteger(botSceneIndicationInfo.getConflictStepCount());
        double value = (double)botSceneIndicationInfo.getConflictStep() / (double)botSceneIndicationInfo.getConflictStepCount();
        String htmlColor = GREEN.mix(RED_BG, value).toHtmlColor();
        stopLinearGradientRed.setAttribute("stop-color", RED_FG.toHtmlColor());
        stopLinearGradientGreen1.setAttribute("stop-color", htmlColor);
        stopLinearGradientGreen2.setAttribute("stop-color", htmlColor);
    }

    @Override
    public BotSceneIndicationInfo getValue() {
        return botSceneIndicationInfo;
    }

    @Override
    public org.jboss.errai.common.client.dom.HTMLElement getElement() {
        return botSceneIndication;
    }
}
