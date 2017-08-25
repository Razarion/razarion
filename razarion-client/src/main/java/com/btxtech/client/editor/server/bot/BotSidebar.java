package com.btxtech.client.editor.server.bot;

import com.btxtech.client.editor.framework.AbstractCrudeParentSidebar;
import com.btxtech.client.editor.framework.CrudEditor;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * on 28.07.2017.
 */
@Templated("../../framework/AbstractCrudeParentSidebar.html#abstract-crud-parent")
public class BotSidebar extends AbstractCrudeParentSidebar<BotConfig, BotConfigWrapperPropertyPanel> {
    @Inject
    private Instance<BotConfigWrapperPropertyPanel> propertyPanelInstance;
    @Inject
    private BotConfigCrudEditor resourceRegionCrudEditor;

    @Override
    protected CrudEditor<BotConfig> getCrudEditor() {
        return resourceRegionCrudEditor;
    }

    @Override
    protected BotConfigWrapperPropertyPanel createPropertyPanel() {
        return propertyPanelInstance.get();
    }
}
