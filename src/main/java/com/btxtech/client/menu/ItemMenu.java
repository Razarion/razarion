package com.btxtech.client.menu;

import com.btxtech.client.units.ItemService;
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
    private ItemService itemService;
    @Inject
    @DataField
    private DoubleBox specularIntensity;
    @Inject
    @DataField
    private DoubleBox specularHardness;
    @Inject
    @DataField
    private CheckBox moving;
    @Inject
    @DataField
    private Button reload;

    @PostConstruct
    public void init() {
        specularIntensity.setValue(itemService.getSpecularIntensity());
        specularHardness.setValue(itemService.getSpecularHardness());
        moving.setValue(itemService.isMoving());
    }

    @EventHandler("specularIntensity")
    public void specularIntensityChanged(ChangeEvent e) {
        itemService.setSpecularIntensity(specularIntensity.getValue());
    }

    @EventHandler("specularHardness")
    public void specularHardnessChanged(ChangeEvent e) {
        itemService.setSpecularHardness(specularHardness.getValue());
    }

    @EventHandler("moving")
    public void movingChanged(ChangeEvent e) {
        itemService.setMoving(moving.getValue());
    }

    @EventHandler("reload")
    private void reloadButtonClick(ClickEvent event) {
        // itemService.reloadModel();
    }
}
