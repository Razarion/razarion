package com.btxtech.client.editor;

import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.client.editor.widgets.itemtype.base.BaseItemTypeWidget;
import com.btxtech.client.editor.widgets.itemtype.basecount.BaseItemTypeCountWidget;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.rest.PlanetEditorProvider;
import com.btxtech.uiservice.control.GameUiControl;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.dom.NumberInput;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 22.08.2017.
 */
@Templated("PlanetConfigPanel.html#planetConfigPanel")
public class PlanetConfigPanel extends LeftSideBarContent {
    private Logger logger = Logger.getLogger(PlanetConfigPanel.class.getName());
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private Caller<PlanetEditorProvider> planetEditorProviderCaller;
    @Inject
    @AutoBound
    private DataBinder<PlanetConfig> dataBinder;
    @Inject
    @Bound
    @DataField
    private Label planetId;
    @Inject
    @Bound
    @DataField
    private NumberInput houseSpace;
    @Inject
    @Bound
    @DataField
    private NumberInput startRazarion;
    @Inject
    @DataField
    private BaseItemTypeWidget startBaseItemTypeId;
    @Inject
    @DataField
    private BaseItemTypeCountWidget itemTypeLimitation;

    @PostConstruct
    public void init() {
        dataBinder.setModel(gameUiControl.getPlanetConfig());
        startBaseItemTypeId.init(gameUiControl.getPlanetConfig().getStartBaseItemTypeId(), baseItemTypeId -> gameUiControl.getPlanetConfig().setStartBaseItemTypeId(baseItemTypeId));
        itemTypeLimitation.init(gameUiControl.getPlanetConfig().getItemTypeLimitation(), itemTypeIds -> gameUiControl.getPlanetConfig().setItemTypeLimitation(itemTypeIds));
    }

    @Override
    protected void onConfigureDialog() {
        registerSaveButton(() -> {
            planetEditorProviderCaller.call((response) -> {
                    },
                    (message, throwable) -> {
                        logger.log(Level.SEVERE, "PlanetEditorProvider.failed: " + message, throwable);
                        return false;
                    }).updatePlanetConfig(dataBinder.getModel());
        });
        enableSaveButton(true);
    }

}
