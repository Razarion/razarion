package com.btxtech.client.editor.client.scene;

import com.btxtech.client.editor.widgets.itemtype.base.BaseItemTypeWidget;
import com.btxtech.client.editor.widgets.itemtype.box.BoxItemTypeWidget;
import com.btxtech.client.editor.widgets.itemtype.inventoryitem.InventoryItemWidget;
import com.btxtech.client.editor.widgets.itemtype.resource.ResourceItemTypeWidget;
import com.btxtech.client.editor.widgets.marker.DecimalPositionWidget;
import com.btxtech.client.editor.widgets.placeconfig.PlaceConfigWidget;
import com.btxtech.shared.dto.GameTipConfig;
import com.google.gwt.user.client.ui.ValueListBox;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 15.08.2017.
 */
@Templated("GameTipConfigPanel.html#tippanel")
public class GameTipConfigPanel {
    @Inject
    @DataField
    private ValueListBox<GameTipConfig.Tip> tipSelector;
    @Inject
    @DataField
    private BaseItemTypeWidget actorWidget;
    @Inject
    @DataField
    private TableRow actorWidgetTr;
    @Inject
    @DataField
    private BaseItemTypeWidget toCreatedItemTypeWidget;
    @Inject
    @DataField
    private TableRow toCreatedItemTypeWidgetTr;
    @Inject
    @DataField
    private DecimalPositionWidget terrainPositionHintWidget;
    @Inject
    @DataField
    private TableRow terrainPositionHintTr;
    @Inject
    @DataField
    private ResourceItemTypeWidget resourceItemTypeWidget;
    @Inject
    @DataField
    private TableRow resourceItemTypeWidgetTr;
    @Inject
    @DataField
    private PlaceConfigWidget placeConfigWidget;
    @Inject
    @DataField
    private TableRow placeConfigWidgetTr;
    @Inject
    @DataField
    private BoxItemTypeWidget boxItemTypeWidget;
    @Inject
    @DataField
    private TableRow boxItemTypeWidgetTr;
    @Inject
    @DataField
    private InventoryItemWidget inventoryItemWidget;
    @Inject
    @DataField
    private TableRow inventoryItemWidgetTr;
    private GameTipConfig gameTipConfig;

    public void init(GameTipConfig gameTipConfig, Consumer<GameTipConfig> gameTipConfigListener) {
        this.gameTipConfig = gameTipConfig;
        tipSelector.setAcceptableValues(Arrays.asList(GameTipConfig.Tip.values()));
        if (gameTipConfig != null && gameTipConfig.getTip() != null) {
            tipSelector.setValue(gameTipConfig.getTip());
        }
        tipSelector.addValueChangeHandler(event -> {
            if (event.getValue() != null) {
                this.gameTipConfig = new GameTipConfig();
                this.gameTipConfig.setTip(event.getValue());
            } else {
                this.gameTipConfig = null;
            }
            gameTipConfigListener.accept(this.gameTipConfig);
            setupGui();
        });
        setupGui();
    }

    public void setupGui() {
        actorWidgetTr.getStyle().setProperty("display", "none");
        toCreatedItemTypeWidgetTr.getStyle().setProperty("display", "none");
        terrainPositionHintTr.getStyle().setProperty("display", "none");
        resourceItemTypeWidgetTr.getStyle().setProperty("display", "none");
        placeConfigWidgetTr.getStyle().setProperty("display", "none");
        boxItemTypeWidgetTr.getStyle().setProperty("display", "none");
        inventoryItemWidgetTr.getStyle().setProperty("display", "none");
        if (gameTipConfig == null) {
            return;
        }
        switch (gameTipConfig.getTip()) {
            case BUILD:
                actorWidgetTr.getStyle().setProperty("display", "table-row");
                actorWidget.init(gameTipConfig.getActor(), baseItemTypeId -> gameTipConfig.setActor(baseItemTypeId));
                toCreatedItemTypeWidgetTr.getStyle().setProperty("display", "table-row");
                toCreatedItemTypeWidget.init(gameTipConfig.getToCreatedItemTypeId(), toBeCreatedId -> gameTipConfig.setToCreatedItemTypeId(toBeCreatedId));
                terrainPositionHintTr.getStyle().setProperty("display", "table-row");
                terrainPositionHintWidget.init(gameTipConfig.getTerrainPositionHint(), position -> gameTipConfig.setTerrainPositionHint(position));
                break;
            case FABRICATE:
                actorWidgetTr.getStyle().setProperty("display", "table-row");
                actorWidget.init(gameTipConfig.getActor(), baseItemTypeId -> gameTipConfig.setActor(baseItemTypeId));
                toCreatedItemTypeWidgetTr.getStyle().setProperty("display", "table-row");
                toCreatedItemTypeWidget.init(gameTipConfig.getToCreatedItemTypeId(), toBeCreatedId -> gameTipConfig.setToCreatedItemTypeId(toBeCreatedId));
                break;
            case HARVEST:
                actorWidgetTr.getStyle().setProperty("display", "table-row");
                actorWidget.init(gameTipConfig.getActor(), baseItemTypeId -> gameTipConfig.setActor(baseItemTypeId));
                resourceItemTypeWidgetTr.getStyle().setProperty("display", "table-row");
                resourceItemTypeWidget.init(gameTipConfig.getResourceItemTypeId(), resourceId -> gameTipConfig.setResourceItemTypeId(resourceId));
                placeConfigWidgetTr.getStyle().setProperty("display", "table-row");
                placeConfigWidget.init(gameTipConfig.getPlaceConfig(), placeConfig -> gameTipConfig.setPlaceConfig(placeConfig));
                break;
            case MOVE:
                actorWidgetTr.getStyle().setProperty("display", "table-row");
                actorWidget.init(gameTipConfig.getActor(), baseItemTypeId -> gameTipConfig.setActor(baseItemTypeId));
                terrainPositionHintTr.getStyle().setProperty("display", "table-row");
                terrainPositionHintWidget.init(gameTipConfig.getTerrainPositionHint(), position -> gameTipConfig.setTerrainPositionHint(position));
                break;
            case ATTACK:
                actorWidgetTr.getStyle().setProperty("display", "table-row");
                actorWidget.init(gameTipConfig.getActor(), baseItemTypeId -> gameTipConfig.setActor(baseItemTypeId));
                placeConfigWidgetTr.getStyle().setProperty("display", "table-row");
                placeConfigWidget.init(gameTipConfig.getPlaceConfig(), placeConfig -> gameTipConfig.setPlaceConfig(placeConfig));
                break;
            case START_PLACER:
                toCreatedItemTypeWidgetTr.getStyle().setProperty("display", "table-row");
                toCreatedItemTypeWidget.init(gameTipConfig.getToCreatedItemTypeId(), toBeCreatedId -> gameTipConfig.setToCreatedItemTypeId(toBeCreatedId));
                terrainPositionHintTr.getStyle().setProperty("display", "table-row");
                terrainPositionHintWidget.init(gameTipConfig.getTerrainPositionHint(), position -> gameTipConfig.setTerrainPositionHint(position));
                break;
            case PICK_BOX:
                actorWidgetTr.getStyle().setProperty("display", "table-row");
                actorWidget.init(gameTipConfig.getActor(), baseItemTypeId -> gameTipConfig.setActor(baseItemTypeId));
                boxItemTypeWidgetTr.getStyle().setProperty("display", "table-row");
                boxItemTypeWidget.init(gameTipConfig.getBoxItemTypeId(), boxId -> gameTipConfig.setBoxItemTypeId(boxId));
                break;
            case SPAN_INVENTORY_ITEM:
                inventoryItemWidgetTr.getStyle().setProperty("display", "table-row");
                inventoryItemWidget.init(gameTipConfig.getInventoryItemId(), inventoryItemId -> gameTipConfig.setInventoryItemId(inventoryItemId));
                terrainPositionHintTr.getStyle().setProperty("display", "table-row");
                terrainPositionHintWidget.init(gameTipConfig.getTerrainPositionHint(), position -> gameTipConfig.setTerrainPositionHint(position));
                break;
            case SCROLL:
                terrainPositionHintTr.getStyle().setProperty("display", "table-row");
                terrainPositionHintWidget.init(gameTipConfig.getTerrainPositionHint(), position -> gameTipConfig.setTerrainPositionHint(position));
                break;
            default:
                throw new IllegalArgumentException("GameTipConfigPanel.setupGui() unknown tip: " + gameTipConfig.getTip());
        }
    }

}
