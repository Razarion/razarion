package com.btxtech.client.editor.client.scene;

import com.btxtech.client.editor.framework.ObjectNamePropertyPanel;
import com.btxtech.client.editor.widgets.bot.BotConfigPropertyPanel;
import com.btxtech.client.editor.widgets.childtable.ChildTable;
import com.btxtech.client.editor.widgets.marker.Rectangle2DWidget;
import com.btxtech.client.editor.widgets.quest.QuestPropertyPanel;
import com.btxtech.client.guielements.CommaDoubleBox;
import com.btxtech.client.guielements.DecimalPositionBox;
import com.btxtech.client.utils.BooleanNullConverter;
import com.btxtech.shared.dto.BotAttackCommandConfig;
import com.btxtech.shared.dto.BotHarvestCommandConfig;
import com.btxtech.shared.dto.BotKillHumanCommandConfig;
import com.btxtech.shared.dto.BotKillOtherBotCommandConfig;
import com.btxtech.shared.dto.BotMoveCommandConfig;
import com.btxtech.shared.dto.BotRemoveOwnItemCommandConfig;
import com.btxtech.shared.dto.BoxItemPosition;
import com.btxtech.shared.dto.KillBotCommandConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ResourceItemPosition;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.rest.SceneEditorProvider;
import com.btxtech.uiservice.control.GameUiControl;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.dom.CheckboxInput;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.Input;
import org.jboss.errai.common.client.dom.NumberInput;
import org.jboss.errai.common.client.dom.TextInput;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 12.08.2017.
 */
@Templated("SceneConfigPropertyPanel.html#propertyPanel")
public class SceneConfigPropertyPanel extends ObjectNamePropertyPanel {
    private Logger logger = Logger.getLogger(SceneConfigPropertyPanel.class.getName());
    @Inject
    private Caller<SceneEditorProvider> provider;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    @AutoBound
    private DataBinder<SceneConfig> dataBinder;
    @Inject
    @Bound
    @DataField
    private Label id;
    @Inject
    @Bound
    @DataField
    private Input internalName;
    @Inject
    @Bound
    @DataField
    private Input introText;
    @Inject
    @Bound(converter = BooleanNullConverter.class)
    @DataField
    private CheckboxInput suppressSell;
    @Inject
    @Bound
    @DataField
    private CheckboxInput removeLoadingCover;
    @Inject
    @Bound(converter = BooleanNullConverter.class)
    @DataField
    private CheckboxInput wait4LevelUpDialog;
    @Inject
    @Bound(converter = BooleanNullConverter.class)
    @DataField
    private CheckboxInput wait4QuestPassedDialog;
    @Inject
    @Bound(converter = BooleanNullConverter.class)
    @DataField
    private CheckboxInput waitForBaseLostDialog;
    @Inject
    @Bound
    @DataField
    private NumberInput duration;
    @Inject
    @Bound(property = "viewFieldConfig.fromPosition")
    @DataField
    private DecimalPositionBox vfcFromPosition;
    @Inject
    @Bound(property = "viewFieldConfig.toPosition")
    @DataField
    private DecimalPositionBox vfcToPosition;
    @Inject
    @Bound(property = "viewFieldConfig.speed")
    @DataField
    private CommaDoubleBox vfcSpeed;
    @Inject
    @Bound(property = "viewFieldConfig.cameraLocked", converter = BooleanNullConverter.class)
    @DataField
    private CheckboxInput vfcCameraLocked;
    @Inject
    @Bound(property = "viewFieldConfig.bottomWidth")
    @DataField
    private CommaDoubleBox vfcBottomWidth;
    @Inject
    @Bound
    @DataField
    @ListContainer("tbody")
    private ListComponent<ResourceItemPosition, ResourceItemPositionRow> resourceItemTypePositions;
    @Inject
    @DataField
    private Button resourcePositionCreateButton;
    @Inject
    @DataField
    private StartPointPlacerWidget startPointPlacerConfig;
    @Inject
    @Bound(property = "scrollUiQuest.scrollTargetRectangle")
    @DataField
    private Rectangle2DWidget scrollUiQuestTargetRectangle;
    @Inject
    @Bound(property = "scrollUiQuest.title")
    @DataField
    private TextInput scrollUiQuestI18nTitle;
    @Inject
    @Bound(property = "scrollUiQuest.description")
    @DataField
    private TextInput scrollUiQuestI18nDescription;
    @Inject
    @Bound(property = "scrollUiQuest.xp")
    @DataField
    private NumberInput scrollUiQuestXp;
    @Inject
    @Bound(property = "scrollUiQuest.razarion")
    @DataField
    private NumberInput scrollUiQuestRazarion;
    @Inject
    @Bound(property = "scrollUiQuest.crystal")
    @DataField
    private NumberInput scrollUiQuestCrystal;
    @Inject
    @Bound(property = "scrollUiQuest.passedMessage")
    @DataField
    private TextInput scrollUiQuestI18nPassedMessage;
    @Inject
    @Bound(property = "scrollUiQuest.hidePassedDialog", converter = BooleanNullConverter.class)
    @DataField
    private CheckboxInput scrollUiQuestI18nHidePassedDialog;
    @Inject
    @Bound
    @DataField
    @ListContainer("tbody")
    private ListComponent<BoxItemPosition, BoxItemPositionRow> boxItemPositions;
    @Inject
    @DataField
    private Button boxPositionCreateButton;
    @Inject
    @DataField
    private GameTipConfigPanel gameTipConfigPanel;
    @Inject
    @DataField
    private QuestPropertyPanel questConfigPanel;
    @Inject
    @DataField
    private Button questConfigButton;
    @Inject
    @DataField
    private ChildTable<BotConfig> botConfigPanel;
    @Inject
    @DataField
    private ChildTable<BotMoveCommandConfig> botMoveCommandConfigs;
    @Inject
    @DataField
    private ChildTable<BotHarvestCommandConfig> botHarvestCommandConfigs;
    @Inject
    @DataField
    private ChildTable<BotAttackCommandConfig> botAttackCommandConfigs;
    @Inject
    @DataField
    private ChildTable<BotKillOtherBotCommandConfig> botKillOtherBotCommandConfigs;
    @Inject
    @DataField
    private ChildTable<BotKillHumanCommandConfig> botKillHumanCommandConfigs;
    @Inject
    @DataField
    private ChildTable<BotRemoveOwnItemCommandConfig> botRemoveOwnItemCommandConfigs;
    @Inject
    @DataField
    private ChildTable<KillBotCommandConfig> killBotCommandConfigs;

    @Override
    public void setObjectNameId(ObjectNameId objectNameId) {
        DOMUtil.removeAllElementChildren(resourceItemTypePositions.getElement()); // Remove placeholder table row from template.
        DOMUtil.removeAllElementChildren(boxItemPositions.getElement()); // Remove placeholder table row from template.
        resourceItemTypePositions.addComponentCreationHandler(resourceItemPositionRow -> resourceItemPositionRow.setSceneConfigPropertyPanel(SceneConfigPropertyPanel.this));
        boxItemPositions.addComponentCreationHandler(boxItemPositionRow -> boxItemPositionRow.setSceneConfigPropertyPanel(SceneConfigPropertyPanel.this));
        int gameUiControlConfigId = gameUiControl.getColdGameUiControlConfig().getWarmGameUiControlConfig().getGameUiControlConfigId();
        provider.call(new RemoteCallback<SceneConfig>() {
            @Override
            public void callback(SceneConfig sceneConfig) {
                dataBinder.setModel(sceneConfig);
                startPointPlacerConfig.init(sceneConfig.getStartPointPlacerConfig(), sceneConfig::setStartPointPlacerConfig);
                gameTipConfigPanel.init(sceneConfig.getGameTipConfig(), sceneConfig::setGameTipConfig);
                handleQuestConfigVisibility();
                botConfigPanel.init(sceneConfig.getBotConfigs(), sceneConfig::setBotConfigs, BotConfig::new, BotConfigPropertyPanel.class);
                botMoveCommandConfigs.init(sceneConfig.getBotMoveCommandConfigs(), sceneConfig::setBotMoveCommandConfigs, BotMoveCommandConfig::new, BotMoveCommandConfigPropertyPanel.class);
                botHarvestCommandConfigs.init(sceneConfig.getBotHarvestCommandConfigs(), sceneConfig::setBotHarvestCommandConfigs, BotHarvestCommandConfig::new, BotHarvestCommandConfigPropertyPanel.class);
                botAttackCommandConfigs.init(sceneConfig.getBotAttackCommandConfigs(), sceneConfig::setBotAttackCommandConfigs, BotAttackCommandConfig::new, BotAttackCommandConfigPropertyPanel.class);
                botKillOtherBotCommandConfigs.init(sceneConfig.getBotKillOtherBotCommandConfigs(), sceneConfig::setBotKillOtherBotCommandConfigs, BotKillOtherBotCommandConfig::new, BotKillOtherBotCommandConfigPropertyPanel.class);
                botKillHumanCommandConfigs.init(sceneConfig.getBotKillHumanCommandConfigs(), sceneConfig::setBotKillHumanCommandConfigs, BotKillHumanCommandConfig::new, BotKillHumanCommandConfigPropertyPanel.class);
                botRemoveOwnItemCommandConfigs.init(sceneConfig.getBotRemoveOwnItemCommandConfigs(), sceneConfig::setBotRemoveOwnItemCommandConfigs, BotRemoveOwnItemCommandConfig::new, BotRemoveOwnItemCommandConfigPropertyPanel.class);
                killBotCommandConfigs.init(sceneConfig.getKillBotCommandConfigs(), sceneConfig::setKillBotCommandConfigs, KillBotCommandConfig::new, KillBotCommandConfigPropertyPanel.class);

            }
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "SceneEditorProvider.readSceneConfig failed: " + message, throwable);
            return false;
        }).readSceneConfig(gameUiControlConfigId, objectNameId.getId());
        registerSaveButton(this::save);
        enableSaveButton(true);
    }

    private void save() {
        int gameUiControlConfigId = gameUiControl.getColdGameUiControlConfig().getWarmGameUiControlConfig().getGameUiControlConfigId();
        provider.call(response -> {
        }, (message, throwable) -> {
            logger.log(Level.SEVERE, "SceneEditorProvider.updateSceneConfig failed: " + message, throwable);
            return false;
        }).updateSceneConfig(gameUiControlConfigId, dataBinder.getModel());
    }

    @Override
    public Object getConfigObject() {
        return dataBinder.getModel();
    }

    @EventHandler("resourcePositionCreateButton")
    private void resourcePositionCreateButtonClicked(ClickEvent event) {
        List<ResourceItemPosition> resourceItemPositions = resourceItemTypePositions.getValue();
        if (resourceItemPositions == null) {
            resourceItemPositions = new ArrayList<>();
        }
        resourceItemPositions.add(new ResourceItemPosition());
        resourceItemPositions = new ArrayList<>(resourceItemPositions);
        resourceItemTypePositions.setValue(resourceItemPositions);
        dataBinder.getModel().setResourceItemTypePositions(resourceItemPositions);
    }

    public void removeResourceItemPosition(ResourceItemPosition resourceItemPosition) {
        List<ResourceItemPosition> resourceItemPositions = resourceItemTypePositions.getValue();
        resourceItemPositions.remove(resourceItemPosition);
        resourceItemPositions = new ArrayList<>(resourceItemPositions);
        resourceItemTypePositions.setValue(resourceItemPositions);
        dataBinder.getModel().setResourceItemTypePositions(resourceItemPositions);
    }

    @EventHandler("boxPositionCreateButton")
    private void boxPositionCreateButtonClicked(ClickEvent event) {
        List<BoxItemPosition> boxItemPositions = this.boxItemPositions.getValue();
        if (boxItemPositions == null) {
            boxItemPositions = new ArrayList<>();
        }
        boxItemPositions.add(new BoxItemPosition());
        boxItemPositions = new ArrayList<>(boxItemPositions);
        this.boxItemPositions.setValue(boxItemPositions);
        dataBinder.getModel().setBoxItemPositions(boxItemPositions);
    }

    public void removeBoxItemPosition(BoxItemPosition boxItemPosition) {
        List<BoxItemPosition> boxItemPositions = this.boxItemPositions.getValue();
        boxItemPositions.remove(boxItemPosition);
        boxItemPositions = new ArrayList<>(boxItemPositions);
        this.boxItemPositions.setValue(boxItemPositions);
        dataBinder.getModel().setBoxItemPositions(boxItemPositions);
    }

    @EventHandler("questConfigButton")
    private void questConfigButtonClicked(ClickEvent event) {
        if (dataBinder.getModel().getQuestConfig() != null) {
            dataBinder.getModel().setQuestConfig(null);
        } else {
            dataBinder.getModel().setQuestConfig(new QuestConfig());
        }
        handleQuestConfigVisibility();
    }

    private void handleQuestConfigVisibility() {
        if (dataBinder.getModel().getQuestConfig() != null) {
            questConfigPanel.getElement().getStyle().setDisplay(Style.Display.BLOCK);
            questConfigButton.setText("-");
            questConfigPanel.init(dataBinder.getModel().getQuestConfig());
        } else {
            questConfigButton.setText("+");
            questConfigPanel.getElement().getStyle().setDisplay(Style.Display.NONE);
        }
    }
}
