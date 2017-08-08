package com.btxtech.client.editor.widgets.itemtype.basecount;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 07.08.2017.
 */
@Templated("BaseItemTypeCountWidget.html#widget")
public class BaseItemTypeCountWidget {
    @Inject
    @DataField
    @ListContainer("tbody")
    private ListComponent<ItemTypeCountModel, BaseItemTypeCountEntry> baseItemTypeCounts;
    @Inject
    @DataField
    private Button baseItemTypeCountCreateButton;
    private Consumer<Map<Integer, Integer>> baseItemTypeCountCallback;

    @PostConstruct
    public void postConstruct() {
        DOMUtil.removeAllElementChildren(baseItemTypeCounts.getElement()); // Remove placeholder table row from template.
    }

    public void init(Map<Integer, Integer> baseItemTypeCount, Consumer<Map<Integer, Integer>> baseItemTypeCountCallback) {
        this.baseItemTypeCountCallback = baseItemTypeCountCallback;
        if (baseItemTypeCount != null) {
            baseItemTypeCounts.setValue(baseItemTypeCount.entrySet().stream().map(integerIntegerEntry -> new ItemTypeCountModel(integerIntegerEntry, this::update, this::removed)).collect(Collectors.toList()));
        } else {
            baseItemTypeCounts.setValue(new ArrayList<>());
        }
    }

    @EventHandler("baseItemTypeCountCreateButton")
    private void baseItemTypeCountCreateButtonClicked(ClickEvent event) {
        List<ItemTypeCountModel> values = baseItemTypeCounts.getValue();
        values.add(new ItemTypeCountModel(this::update, this::removed));
        baseItemTypeCounts.setValue(new ArrayList<>(values));
        update();
    }

    private void removed(ItemTypeCountModel itemTypeCountModel) {
        List<ItemTypeCountModel> values = baseItemTypeCounts.getValue();
        values.remove(itemTypeCountModel);
        baseItemTypeCounts.setValue(new ArrayList<>(values));
        update();
    }

    private void update() {
        baseItemTypeCountCallback.accept(baseItemTypeCounts.getValue().stream().filter(itemTypeCountModel -> itemTypeCountModel.getItemType() != null).collect(Collectors.toMap(ItemTypeCountModel::getItemType, ItemTypeCountModel::getCount, (a, b) -> b)));
    }

}
