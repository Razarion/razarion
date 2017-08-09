package com.btxtech.client.cockpit.quest;

import com.btxtech.client.StaticResourcePath;
import com.btxtech.client.cockpit.ZIndexConstants;
import com.btxtech.client.utils.GwtUtils;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.components.ListComponent;
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
    private ListComponent<ProgressTableRowModel, ProgressTableRowWidget> progressTable;

    @PostConstruct
    public void init() {
        getElement().getStyle().setZIndex(ZIndexConstants.QUEST_SIDE_BAR);
        GwtUtils.preventContextMenu(this);
        setStyleName("quest-sidebar");
        DOMUtil.removeAllElementChildren(progressTable.getElement()); // Remove placeholder table row from template.
    }

    public void setQuest(QuestDescriptionConfig descriptionConfig) {
        titleLabel.setText(descriptionConfig.getTitle());
        descriptionLabel.setText(descriptionConfig.getDescription());
        progressTable.setValue(setupProgressTableModels(descriptionConfig));
    }

    private List<ProgressTableRowModel> setupProgressTableModels(QuestDescriptionConfig descriptionConfig) {
        List<ProgressTableRowModel> progressTableModels = new ArrayList<>();
        if (!(descriptionConfig instanceof QuestConfig)) {
            return progressTableModels;
        }
        QuestConfig questConfig = (QuestConfig) descriptionConfig;
        switch (questConfig.getConditionConfig().getConditionTrigger()) {
            case SYNC_ITEM_KILLED:
                fillBaseItemCount(progressTableModels, questConfig.getConditionConfig().getComparisonConfig(), I18nHelper.getConstants().questDestroyed());
                fillCount(progressTableModels, questConfig.getConditionConfig().getComparisonConfig(), I18nHelper.getConstants().questUnitStructuresDestroyed());
                break;
            case HARVEST:
                fillCount(progressTableModels, questConfig.getConditionConfig().getComparisonConfig(), I18nHelper.getConstants().questResourcesCollected());
                break;
            case SYNC_ITEM_CREATED:
                fillBaseItemCount(progressTableModels, questConfig.getConditionConfig().getComparisonConfig(), I18nHelper.getConstants().questBuilt());
                fillCount(progressTableModels, questConfig.getConditionConfig().getComparisonConfig(), I18nHelper.getConstants().questUnitStructuresBuilt());
                break;
            case BASE_KILLED:
                fillCount(progressTableModels, questConfig.getConditionConfig().getComparisonConfig(), I18nHelper.getConstants().questBasesKilled());
                break;
            case SYNC_ITEM_POSITION:
                fillBaseItemCount(progressTableModels, questConfig.getConditionConfig().getComparisonConfig(), null);
                fillCount(progressTableModels, questConfig.getConditionConfig().getComparisonConfig(), I18nHelper.getConstants().questMinutesPast());
                break;
            case BOX_PICKED:
                fillBaseItemCount(progressTableModels, questConfig.getConditionConfig().getComparisonConfig(), I18nHelper.getConstants().questBoxesPicked());
                break;
            case INVENTORY_ITEM_PLACED:
                logger.severe("QuestSidebar.setupProgressTableModels() TODO INVENTORY_ITEM_PLACED");
                break;
            default:
                throw new IllegalArgumentException("QuestSidebar.setupProgressTableModels() Unknown ConditionTrigger: " + questConfig.getConditionConfig().getConditionTrigger());
        }

        return progressTableModels;
    }

    private void fillBaseItemCount(List<ProgressTableRowModel> progressTableModels, ComparisonConfig comparisonConfig, String actionWord) {
        if (comparisonConfig.getTypeCount() == null) {
            return;
        }
        List<Integer> itemIds = new ArrayList<>(comparisonConfig.getTypeCount().keySet());
        itemIds.sort(Comparator.naturalOrder());
        for (Integer itemId : itemIds) {
            int amount = comparisonConfig.getTypeCount().get(itemId);
            ProgressTableRowModel progressTableRowModel = new ProgressTableRowModel();
            progressTableRowModel.setStatusImage(StaticResourcePath.IMG_NAME_EXCLAMATION);// TODO Actual
            // progressTableRowModel.setImage(StaticResourcePath.IMG_NAME_TICK);
            progressTableRowModel.setText(amount + "/" + "0"); // TODO Actual
            BaseItemType baseItemType = itemTypeService.getBaseItemType(itemId);
            progressTableRowModel.setBaseItemImage(RestUrl.getImageServiceUrlSafe(baseItemType.getThumbnail()));
            progressTableRowModel.setActionWord(I18nHelper.getLocalizedString(baseItemType.getI18nName()) + (actionWord != null ? (" " + actionWord) : ""));
            progressTableModels.add(progressTableRowModel);
        }
    }


    private void fillCount(List<ProgressTableRowModel> progressTableModels, ComparisonConfig comparisonConfig, String actionWord) {
        if (comparisonConfig.getCount() == null) {
            return;
        }
        ProgressTableRowModel progressTableRowModel = new ProgressTableRowModel();
        progressTableRowModel.setStatusImage(StaticResourcePath.IMG_NAME_EXCLAMATION);// TODO Actual
        // progressTableRowModel.setImage(StaticResourcePath.IMG_NAME_TICK);
        progressTableRowModel.setText(comparisonConfig.getCount() + "/" + "0"); // TODO Actual
        if (actionWord != null) {
            progressTableRowModel.setActionWord(actionWord);
        }
        progressTableModels.add(progressTableRowModel);
    }

}
