package com.btxtech.client.editor.basemgmt;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.utils.DateStringConverter;
import com.btxtech.common.DisplayUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableRowElement;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Beat
 * on 12.03.2018.
 */
@Templated("BaseMgmtEditorPanel.html#baseTableRow")
public class BaseMgmtWidget implements TakesValue<BaseMgmtModel>, IsElement {
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    @AutoBound
    private DataBinder<BaseMgmtModel> dataBinder;
    @Inject
    @DataField
    private HTMLTableRowElement baseTableRow;
    @Inject
    @DataField
    @Named("Span")
    private HTMLElement id;
    @Inject
    @DataField
    @Named("Span")
    private HTMLElement name;
    @Inject
    @Bound(converter = DateStringConverter.class)
    @DataField
    @Named("Span")
    private HTMLElement lastLoggedIn;
    @Inject
    @DataField
    private HTMLButtonElement killButton;

    @Override
    public void setValue(BaseMgmtModel baseMgmtModel) {
        dataBinder.setModel(baseMgmtModel);
        id.textContent = DisplayUtils.handleInteger(baseMgmtModel.getId());
        name.textContent = baseMgmtModel.getName();
    }

    @Override
    public BaseMgmtModel getValue() {
        return dataBinder.getModel();
    }

    @EventHandler("killButton")
    private void onKillButtonClick(ClickEvent event) {
        modalDialogManager.showQuestionDialog("Delete Base", "Delete Base: " + dataBinder.getModel().getName() + " (" + dataBinder.getModel().getId() + ")?",
                () -> dataBinder.getModel().getKillCallback().accept(getValue().getPlayerBase().getBaseId()), null);
    }

    @Override
    public org.jboss.errai.common.client.dom.HTMLElement getElement() {
        return (org.jboss.errai.common.client.dom.HTMLElement) baseTableRow;
    }
}
