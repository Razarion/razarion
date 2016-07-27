package com.btxtech.client.editor.menu;

import com.btxtech.uiservice.item.BaseItemUiService;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 06.11.2015.
 */
@Templated("ItemMenu.html#menu-item")
public class ItemMenu extends Composite {
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    @DataField
    private DoubleBox specularIntensity;
    @Inject
    @DataField
    private DoubleBox specularHardness;
    @Inject
    @DataField
    private CheckBox running;
    @Inject
    @DataField
    private Button restart;

    @PostConstruct
    public void init() {
        specularIntensity.setValue(baseItemUiService.getSpecularIntensity());
        specularHardness.setValue(baseItemUiService.getSpecularHardness());
        // TODO running.setValue(itemServiceRunner.isRunning());
    }

    @EventHandler("specularIntensity")
    public void specularIntensityChanged(ChangeEvent e) {
        baseItemUiService.setSpecularIntensity(specularIntensity.getValue());
    }

    @EventHandler("specularHardness")
    public void specularHardnessChanged(ChangeEvent e) {
        baseItemUiService.setSpecularHardness(specularHardness.getValue());
    }

    @EventHandler("running")
    public void movingChanged(ChangeEvent e) {
        // TODO  itemServiceRunner.setRunning(running.getValue());
    }

    @EventHandler("restart")
    private void restartButtonClick(ClickEvent event) {
        // TODO baseItemUiService.setupItems();
    }
}
