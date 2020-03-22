package com.btxtech.client.editor.widgets.bot.selector;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.rest.CommonEditorProvider;
import com.btxtech.uiservice.control.GameUiControl;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 22.08.2016.
 */
@Templated("BotSelectionDialog.html#bot-selection-dialog")
public class BotSelectionDialog extends Composite implements ModalDialogContent<Integer> {
    // private Logger logger = Logger.getLogger(BotSelectionDialog.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private Caller<CommonEditorProvider> provider;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    @AutoBound
    private Span loadingSpan;
    @Inject
    @DataField
    @ListContainer("tbody")
    private ListComponent<ObjectNameId, BotSelectionEntryWidget> botList;
    private ModalDialogPanel<Integer> modalDialogPanel;

    @Override
    public void init(Integer selectedId) {
        DOMUtil.removeAllElementChildren(botList.getElement()); // Remove placeholder table row from template.
        provider.call(response -> {
            fillTable(selectedId, (List<ObjectNameId>) response);
        }, exceptionHandler.restErrorHandler("CommonEditorProvider.getAllBotsFromPlanet() failed: ")).getAllBotsFromPlanet(gameUiControl.getPlanetConfig().getId());

        botList.addComponentCreationHandler(botSelectionEntryWidget -> botSelectionEntryWidget.setBotSelectionDialog(BotSelectionDialog.this));
        botList.setSelector(baseItemTypeSelectionEntry -> baseItemTypeSelectionEntry.setSelected(true));
        botList.setDeselector(baseItemTypeSelectionEntry -> baseItemTypeSelectionEntry.setSelected(false));
    }

    private void fillTable(Integer selectedId, List<ObjectNameId> objectNameIds) {
        loadingSpan.getStyle().setProperty("display", "none");
        botList.setValue(objectNameIds);
        if (selectedId != null) {
            botList.selectModel(objectNameIds.stream().filter(objectNameId -> objectNameId.getId() == selectedId).findFirst().orElseThrow(() -> new IllegalArgumentException("BotSelectionDialog.display() selectedId not found: " + selectedId)));
        }
    }

    @Override
    public void customize(ModalDialogPanel<Integer> modalDialogPanel) {
        this.modalDialogPanel = modalDialogPanel;
    }

    public void selectComponent(BotSelectionEntryWidget widget) {
        botList.deselectAll();
        botList.selectComponent(widget);
        modalDialogPanel.setApplyValue(widget.getValue().getId());
    }

    @Override
    public void onClose() {

    }
}
