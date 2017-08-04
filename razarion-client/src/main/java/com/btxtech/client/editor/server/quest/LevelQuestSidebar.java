package com.btxtech.client.editor.server.quest;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.shared.dto.ServerLevelQuestConfig;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * on 28.07.2017.
 */
@Templated("../../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class LevelQuestSidebar extends AbstractCrudeParentSidebar<ServerLevelQuestConfig, LevelQuestPropertyPanel> {
    @Inject
    private Instance<LevelQuestPropertyPanel> levelQuestPropertyPanelInstance;
    @Inject
    private LevelQuestSidebarCrudEditor levelQuestSidebarCrudEditor;


    @Override
    protected CrudEditor<ServerLevelQuestConfig> getCrudEditor() {
        return levelQuestSidebarCrudEditor;
    }

    @Override
    protected LevelQuestPropertyPanel createPropertyPanel() {
        return levelQuestPropertyPanelInstance.get();
    }
}
