package com.btxtech.client.editor.widgets.bot.selector;

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
@Templated("BotListWidget.html#widget")
public class BotListWidget {
    @Inject
    @DataField
    @ListContainer("tbody")
    private ListComponent<BotEntryModel, BotEntryWidget> botList;
    @Inject
    @DataField
    private Button botCreateButton;
    private Consumer<List<Integer>> botIdsCallback;
    private List<Integer> botIds;

    @PostConstruct
    public void postConstruct() {
        DOMUtil.removeAllElementChildren(botList.getElement()); // Remove placeholder table row from template.
    }

    public void init(List<Integer> botIds, Consumer<List<Integer>> botIdsCallback) {
        this.botIds = botIds;
        this.botIdsCallback = botIdsCallback;
        if (botIds != null) {
            createAndSetModels();
        } else {
            botList.setValue(new ArrayList<>());
        }
    }

    private void createAndSetModels() {
        botList.setValue(botIds.stream().map(botId -> new BotEntryModel(botId, this::update, this::removed)).collect(Collectors.toList()));
    }

    private void update() {
        if (botList == null) {
            return;
        }
        botIds.clear();
        for (BotEntryModel botEntryModel : botList.getValue()) {
            botIds.add(botEntryModel.getBotId());
        }
    }

    @EventHandler("botCreateButton")
    private void botCreateButtonClicked(ClickEvent event) {
        if (botIds == null) {
            botIds = new ArrayList<>();
            botIdsCallback.accept(botIds);
        }
        botIds.add(null);
        createAndSetModels();
    }

    private void removed(BotEntryModel botEntryModel) {
        botIds.remove(botEntryModel.getBotId());
        createAndSetModels();
    }

}
