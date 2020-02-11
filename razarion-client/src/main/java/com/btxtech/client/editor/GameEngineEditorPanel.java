package com.btxtech.client.editor;

import com.btxtech.client.editor.sidebar.AbstractEditor;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.google.gwt.event.dom.client.ChangeEvent;
import org.jboss.errai.common.client.dom.CheckboxInput;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 26.08.2016.
 */
@Templated("GameEngineEditorPanel.html#game-engine")
public class GameEngineEditorPanel extends AbstractEditor {
    @Inject
    private PlanetService planetService;
    @Inject
    @DataField
    private CheckboxInput pauseCheckbox;

    @PostConstruct
    public void postConstruct() {
        pauseCheckbox.setChecked(planetService.isPause());
    }

    @EventHandler("pauseCheckbox")
    public void pauseCheckboxChanged(ChangeEvent e) {
        planetService.setPause(pauseCheckbox.getChecked());
    }

}
