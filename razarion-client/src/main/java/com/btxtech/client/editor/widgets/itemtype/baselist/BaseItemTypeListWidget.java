package com.btxtech.client.editor.widgets.itemtype.baselist;

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
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 07.08.2017.
 */
@Templated("BaseItemTypeListWidget.html#widget")
public class BaseItemTypeListWidget {
    @Inject
    @DataField
    @ListContainer("tbody")
    private ListComponent<BaseItemTypeListEntryModel, BaseItemTypeListEntry> baseItemTypeList;
    @Inject
    @DataField
    private Button baseItemTypeListCreateButton;
    private Consumer<List<Integer>> baseItemTypesCallback;
    private List<Integer> baseItemTypes;

    @PostConstruct
    public void postConstruct() {
        DOMUtil.removeAllElementChildren(baseItemTypeList.getElement()); // Remove placeholder table row from template.
    }

    public void init(List<Integer> baseItemTypes, Consumer<List<Integer>> baseItemTypesCallback) {
        this.baseItemTypes = baseItemTypes;
        this.baseItemTypesCallback = baseItemTypesCallback;
        if (baseItemTypes != null) {
            createAndSetModels();
        } else {
            baseItemTypeList.setValue(new ArrayList<>());
        }
    }

    private void createAndSetModels() {
        baseItemTypeList.setValue(baseItemTypes.stream().map(baseItemTypeId -> new BaseItemTypeListEntryModel(baseItemTypeId, this::removed)).collect(Collectors.toList()));
    }

    @EventHandler("baseItemTypeListCreateButton")
    private void baseItemTypeListCreateButtonButtonClicked(ClickEvent event) {
        if (baseItemTypes == null) {
            baseItemTypes = new ArrayList<>();
            baseItemTypesCallback.accept(baseItemTypes);
        }
        baseItemTypes.add(null);
        createAndSetModels();
    }

    private void removed(BaseItemTypeListEntryModel baseItemTypeListEntryModel) {
        baseItemTypes.remove(baseItemTypeListEntryModel.getBaseItemTypeId());
        createAndSetModels();
    }

}
