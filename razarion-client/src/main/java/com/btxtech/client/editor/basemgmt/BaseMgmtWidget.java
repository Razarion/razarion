package com.btxtech.client.editor.basemgmt;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.utils.DateStringConverter;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Button;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

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
    private TableRow baseTableRow;
    @Inject
    @Bound
    @DataField
    private Span id;
    @Inject
    @Bound
    @DataField
    private Span name;
    @Inject
    @Bound(converter = DateStringConverter.class)
    @DataField
    private Span lastLoggedIn;
    @Inject
    @DataField
    private Button killButton;

    @Override
    public void setValue(BaseMgmtModel baseMgmtModel) {
        dataBinder.setModel(baseMgmtModel);
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
    public HTMLElement getElement() {
        return baseTableRow;
    }
}
