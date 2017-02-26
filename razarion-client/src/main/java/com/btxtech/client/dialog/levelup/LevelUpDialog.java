package com.btxtech.client.dialog.levelup;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 24.09.2016.
 */
@Templated("LevelUpDialog.html#level-up-dialog")
public class LevelUpDialog extends Composite implements ModalDialogContent<LevelConfig> {
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private LevelService levelService;
    @Inject
    @DataField
    private Label levelUpText;
    @Inject
    @DataField
    @ListContainer("tbody")
    private ListComponent<ItemTypeLimitation, ItemTypeLimitationComponent> itemLimitationTable;

    @Override
    public void init(LevelConfig levelConfig) {
        levelUpText.setText(I18nHelper.getConstants().youReachedLevel(levelService.getLevel(levelConfig.getLevelId()).getNumber()));
        DOMUtil.removeAllElementChildren(itemLimitationTable.getElement()); // Remove placeholder table row from template.
        List<ItemTypeLimitation> itemTypeLimitations = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : levelConfig.getItemTypeLimitation().entrySet()) {
            itemTypeLimitations.add(new ItemTypeLimitation(itemTypeService.getBaseItemType(entry.getKey()), entry.getValue()));
        }
        itemLimitationTable.setValue(itemTypeLimitations);
    }

    @Override
    public void customize(ModalDialogPanel<LevelConfig> modalDialogPanel) {
    }

    @Override
    public void onClose() {
        // Ignore
    }
}
