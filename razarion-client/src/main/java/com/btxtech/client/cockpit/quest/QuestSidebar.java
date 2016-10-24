package com.btxtech.client.cockpit.quest;

import com.btxtech.client.cockpit.ZIndexConstants;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 11.07.2016.
 */
@Templated("QuestSidebar.html#questSidebar")
public class QuestSidebar extends Composite {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label questSidebarTitle;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label questSidebarCenter;

    @PostConstruct
    public void init() {
        getElement().getStyle().setZIndex(ZIndexConstants.QUEST_SIDE_BAR);
        setStyleName("quest-sidebar");
    }

    public void setQuest(QuestDescriptionConfig descriptionConfig) {
        questSidebarTitle.setText(descriptionConfig.getTitle());
        questSidebarCenter.setText(descriptionConfig.getDescription());
    }
}
