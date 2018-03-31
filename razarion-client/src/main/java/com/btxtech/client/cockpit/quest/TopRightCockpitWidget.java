package com.btxtech.client.cockpit.quest;

import com.btxtech.client.cockpit.ZIndexConstants;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneIndicationInfo;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 30.03.2018.
 */
@Templated("TopRightCockpitWidget.html#topRightCockpitWidget")
public class TopRightCockpitWidget extends Composite {
    @Inject
    @DataField
    private QuestSidebar questSidebar;
    @Inject
    @DataField
    @ListContainer("div")
    private ListComponent<BotSceneIndicationInfo, BotSceneIndicationInfoWidget> botSceneIndications;

    @PostConstruct
    public void postConstruct() {
        getElement().getStyle().setZIndex(ZIndexConstants.TOP_RIGHT_BAR);
        DOMUtil.removeAllElementChildren(botSceneIndications.getElement()); // Remove placeholder table row from template.
    }

    public QuestSidebar getQuestSidebar() {
        return questSidebar;
    }

    public void setBotSceneIndicationInfos(List<BotSceneIndicationInfo> botSceneIndicationInfos) {
        if (botSceneIndicationInfos != null) {
            botSceneIndications.setValue(botSceneIndicationInfos);
        } else {
            botSceneIndications.setValue(new ArrayList<>());
        }
    }
}
