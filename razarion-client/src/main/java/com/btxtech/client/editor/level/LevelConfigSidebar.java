package com.btxtech.client.editor.level;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * on 28.07.2017.
 */
@Templated("../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class LevelConfigSidebar extends AbstractCrudeParentSidebar<LevelConfig, LevelConfigPropertyPanel> {
    @Inject
    private Instance<LevelConfigPropertyPanel> instance;
    @Inject
    private LevelConfigCrudEditor levelConfigCrudEditor;

    @Override
    protected CrudEditor<LevelConfig> getCrudEditor() {
        return levelConfigCrudEditor;
    }

    @Override
    protected LevelConfigPropertyPanel createPropertyPanel() {
        return instance.get();
    }
}
