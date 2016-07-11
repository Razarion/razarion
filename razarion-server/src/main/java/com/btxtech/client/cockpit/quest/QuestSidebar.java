package com.btxtech.client.cockpit.quest;

import com.btxtech.uiservice.ZIndexConstants;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;

/**
 * Created by Beat
 * 11.07.2016.
 */
@Templated("QuestSidebar.html#questSidebar")
public class QuestSidebar extends Composite {

    @PostConstruct
    public void init() {
        getElement().getStyle().setZIndex(ZIndexConstants.QUEST_SIDE_BAR);
        setStyleName("quest-sidebar");
    }
}
