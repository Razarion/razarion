package com.btxtech.client.cockpit.quest;

import com.btxtech.client.StaticResourcePath;
import com.btxtech.client.cockpit.ZIndexConstants;
import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.dialog.quest.QuestSelectionDialog;
import com.btxtech.client.utils.GwtUtils;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.uiservice.dialog.DialogButton;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.questvisualization.InGameQuestVisualizationService;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.dom.CheckboxInput;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 11.07.2016.
 */
@Templated("QuestSidebar.html#questSidebar")
public class QuestSidebar extends Composite {
    private Logger logger = Logger.getLogger(QuestSidebar.class.getName());
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private InGameQuestVisualizationService inGameQuestVisualizationService;
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    @DataField
    private Label titleLabel;
    @Inject
    @DataField
    private Label descriptionLabel;
    @Inject
    @DataField
    private Div questBotBasesDiv;
    @Inject
    @DataField
    private Div questBotBasesDescription;
    @Inject
    @DataField
    @ListContainer("tbody")
    private ListComponent<ProgressTableRowModel, ProgressTableRowWidget> progressTable;
    @Inject
    @DataField
    private Div questProgressDiv;
    @Inject
    @DataField
    private Button questDialogButton;
    @Inject
    @DataField
    private CheckboxInput questVisualizationCheckbox;
    @Inject
    @DataField
    private Div questVisualizationDiv;
    private QuestConfig activeQuest;

    @PostConstruct
    public void init() {
        inGameQuestVisualizationService.setVisibleCallback(questVisualizationCheckbox::setChecked);
        inGameQuestVisualizationService.setSuppressCallback(suppressed -> questVisualizationDiv.getStyle().setProperty("display", suppressed ? "none" : "block"));
        questVisualizationCheckbox.setChecked(inGameQuestVisualizationService.isVisible());
        getElement().getStyle().setZIndex(ZIndexConstants.QUEST_SIDE_BAR);
        GwtUtils.preventContextMenu(this);
        //noinspection GWTStyleCheck
        setStyleName("quest-sidebar");
        DOMUtil.removeAllElementChildren(progressTable.getElement()); // Remove placeholder table row from template.
    }

    public void setQuest(QuestDescriptionConfig descriptionConfig, QuestProgressInfo questProgressInfo, boolean showQuestSelectionButton) {
        activeQuest = null;
        if (descriptionConfig == null) {
            titleLabel.setText(I18nHelper.getConstants().noActiveQuest());
            descriptionLabel.setText("");
            questProgressDiv.getStyle().setProperty("display", "none");
            setupQuestBotBasesDescription(null);
            questDialogButton.getElement().getStyle().setDisplay(Style.Display.INLINE_BLOCK);
        } else {
            questProgressDiv.getStyle().setProperty("display", "block");
            titleLabel.setText(descriptionConfig.getTitle());
            descriptionLabel.setText(descriptionConfig.getDescription());
            if (descriptionConfig instanceof QuestConfig) {
                activeQuest = (QuestConfig) descriptionConfig;
                setupProgressTableModels(questProgressInfo);
            } else {
                progressTable.setValue(new ArrayList<>());
            }
            setupQuestBotBasesDescription(questProgressInfo);
            if (showQuestSelectionButton) {
                questDialogButton.getElement().getStyle().setDisplay(Style.Display.INLINE_BLOCK);
            } else {
                questDialogButton.getElement().getStyle().setDisplay(Style.Display.NONE);
            }
        }
    }

    public void onQuestProgress(QuestProgressInfo questProgressInfo) {
        setupQuestBotBasesDescription(questProgressInfo);
        setupProgressTableModels(questProgressInfo);
    }

    private void setupQuestBotBasesDescription(QuestProgressInfo questProgressInfo) {
        if (questProgressInfo != null && questProgressInfo.getBotBasesInformation() != null) {
            questBotBasesDiv.getStyle().setProperty("display", "block");
            questBotBasesDescription.setTextContent(I18nHelper.getConstants().questBotBasesText(questProgressInfo.getBotBasesInformation()));
        } else {
            questBotBasesDiv.getStyle().setProperty("display", "none");
        }
    }

    private void setupProgressTableModels(QuestProgressInfo questProgressInfo) {
        List<ProgressTableRowModel> progressTableModels = new ArrayList<>();
        switch (activeQuest.getConditionConfig().getConditionTrigger()) {
            case SYNC_ITEM_KILLED:
                fillBaseItemCount(progressTableModels, activeQuest.getConditionConfig().getComparisonConfig(), questProgressInfo, I18nHelper.getConstants().questDestroyed());
                fillCount(progressTableModels, activeQuest.getConditionConfig().getComparisonConfig(), questProgressInfo, I18nHelper.getConstants().questUnitStructuresDestroyed());
                break;
            case HARVEST:
                fillCount(progressTableModels, activeQuest.getConditionConfig().getComparisonConfig(), questProgressInfo, I18nHelper.getConstants().questResourcesCollected());
                break;
            case SYNC_ITEM_CREATED:
                fillBaseItemCount(progressTableModels, activeQuest.getConditionConfig().getComparisonConfig(), questProgressInfo, I18nHelper.getConstants().questBuilt());
                fillCount(progressTableModels, activeQuest.getConditionConfig().getComparisonConfig(), questProgressInfo, I18nHelper.getConstants().questUnitStructuresBuilt());
                break;
            case BASE_KILLED:
                fillCount(progressTableModels, activeQuest.getConditionConfig().getComparisonConfig(), questProgressInfo, I18nHelper.getConstants().questBasesKilled());
                break;
            case SYNC_ITEM_POSITION:
                fillBaseItemCount(progressTableModels, activeQuest.getConditionConfig().getComparisonConfig(), questProgressInfo, null);
                fillCount(progressTableModels, activeQuest.getConditionConfig().getComparisonConfig(), questProgressInfo, I18nHelper.getConstants().questMinutesPast());
                break;
            case BOX_PICKED:
                fillCount(progressTableModels, activeQuest.getConditionConfig().getComparisonConfig(), questProgressInfo, I18nHelper.getConstants().questBoxesPicked());
                break;
            case INVENTORY_ITEM_PLACED:
                logger.severe("QuestSidebar.setupProgressTableModels() TODO INVENTORY_ITEM_PLACED");
                break;
            default:
                throw new IllegalArgumentException("QuestSidebar.setupProgressTableModels() Unknown ConditionTrigger: " + activeQuest.getConditionConfig().getConditionTrigger());
        }

        progressTable.setValue(progressTableModels);
    }

    private void fillBaseItemCount(List<ProgressTableRowModel> progressTableModels, ComparisonConfig comparisonConfig, QuestProgressInfo questProgressInfo, String actionWord) {
        if (comparisonConfig.getTypeCount() == null) {
            return;
        }
        List<Integer> itemIds = new ArrayList<>(comparisonConfig.getTypeCount().keySet());
        itemIds.sort(Comparator.naturalOrder());
        for (Integer itemId : itemIds) {
            int expected = comparisonConfig.getTypeCount().get(itemId);
            int actual = 0;
            if (questProgressInfo != null) {
                Integer count = questProgressInfo.getTypeCount().get(itemId);
                actual = count != null ? count : 0;
            }
            ProgressTableRowModel progressTableRowModel = new ProgressTableRowModel();
            if (actual < expected) {
                progressTableRowModel.setStatusImage(StaticResourcePath.IMG_NAME_EXCLAMATION);
            } else {
                progressTableRowModel.setStatusImage(StaticResourcePath.IMG_NAME_TICK);
            }
            progressTableRowModel.setText(actual + "/" + expected);
            BaseItemType baseItemType = itemTypeService.getBaseItemType(itemId);
            progressTableRowModel.setBaseItemImage(CommonUrl.getImageServiceUrlSafe(baseItemType.getThumbnail()));
            progressTableRowModel.setActionWord(I18nHelper.getLocalizedString(baseItemType.getI18nName()) + (actionWord != null ? (" " + actionWord) : ""));
            progressTableModels.add(progressTableRowModel);
        }
    }

    private void fillCount(List<ProgressTableRowModel> progressTableModels, ComparisonConfig comparisonConfig, QuestProgressInfo questProgressInfo, String actionWord) {
        if (comparisonConfig.getCount() == null) {
            return;
        }
        int expected = comparisonConfig.getCount();
        int actual = 0;
        if (questProgressInfo != null) {
            Integer count = questProgressInfo.getCount();
            actual = count != null ? count : 0;
        }
        ProgressTableRowModel progressTableRowModel = new ProgressTableRowModel();
        if (actual < expected) {
            progressTableRowModel.setStatusImage(StaticResourcePath.IMG_NAME_EXCLAMATION);
        } else {
            progressTableRowModel.setStatusImage(StaticResourcePath.IMG_NAME_TICK);
        }
        progressTableRowModel.setText(actual + "/" + expected);
        if (actionWord != null) {
            progressTableRowModel.setActionWord(actionWord);
        }
        progressTableModels.add(progressTableRowModel);
    }

    @EventHandler("questDialogButton")
    public void onQuestDialogButtonClicked(ClickEvent event) {
        modalDialogManager.show(I18nHelper.getConstants().questDialog(), ClientModalDialogManagerImpl.Type.QUEUE_ABLE, QuestSelectionDialog.class, null, null, null, DialogButton.Button.CLOSE);
    }

    @EventHandler("questVisualizationCheckbox")
    public void onQuestVisualizationCheckboxClicked(ChangeEvent event) {
        inGameQuestVisualizationService.setVisible(questVisualizationCheckbox.getChecked());
    }
}
