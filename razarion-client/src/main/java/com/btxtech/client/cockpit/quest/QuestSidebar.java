package com.btxtech.client.cockpit.quest;

import com.btxtech.client.StaticResourcePath;
import com.btxtech.client.cockpit.ZIndexConstants;
import com.btxtech.client.utils.GwtUtils;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
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
    @DataField
    private Label titleLabel;
    @Inject
    @DataField
    private Label descriptionLabel;
    @Inject
    @DataField
    @ListContainer("tbody")
    private ListComponent<ProgressTableRowModel, ProgressTableRowWidget> progressTable;
    private QuestConfig activeQuest;

    @PostConstruct
    public void init() {
        getElement().getStyle().setZIndex(ZIndexConstants.QUEST_SIDE_BAR);
        GwtUtils.preventContextMenu(this);
        //noinspection GWTStyleCheck
        setStyleName("quest-sidebar");
        DOMUtil.removeAllElementChildren(progressTable.getElement()); // Remove placeholder table row from template.
    }

    public void setQuest(QuestDescriptionConfig descriptionConfig, QuestProgressInfo questProgressInfo) {
        activeQuest = null;
        titleLabel.setText(descriptionConfig.getTitle());
        descriptionLabel.setText(descriptionConfig.getDescription());
        if (descriptionConfig instanceof QuestConfig) {
            activeQuest = (QuestConfig) descriptionConfig;
            setupProgressTableModels(questProgressInfo);
        } else {
            progressTable.setValue(new ArrayList<>());
        }
    }

    public void onQuestProgress(QuestProgressInfo questProgressInfo) {
        setupProgressTableModels(questProgressInfo);
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
                fillBaseItemCount(progressTableModels, activeQuest.getConditionConfig().getComparisonConfig(), questProgressInfo, I18nHelper.getConstants().questBoxesPicked());
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
            progressTableRowModel.setBaseItemImage(RestUrl.getImageServiceUrlSafe(baseItemType.getThumbnail()));
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
}
