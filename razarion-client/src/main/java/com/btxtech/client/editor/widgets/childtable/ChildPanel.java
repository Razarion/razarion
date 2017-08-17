package com.btxtech.client.editor.widgets.childtable;

import com.btxtech.client.utils.GwtUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 16.08.2017.
 */
@Templated("ChildTable.html#childTableRow")
public class ChildPanel implements TakesValue<ChildModel>, IsElement {
    private Logger logger = Logger.getLogger(ChildPanel.class.getName());
    @Inject
    private Instance<Object> panelInstance;
    @Inject
    @DataField
    private TableRow childTableRow;
    @Inject
    @DataField
    private Button childTableRowDeleteButton;
    @Inject
    @DataField
    private Div childComponent;
    private ChildModel childModel;

    @Override
    public void setValue(ChildModel childModel) {
        this.childModel = childModel;
        Object o = panelInstance.select(childModel.getChildPanelClass()).get();
        if (o instanceof TakesValue) {
            ((TakesValue) o).setValue(childModel.getChild());
        }
        if (o instanceof IsElement) {
            childComponent.appendChild(((IsElement) o).getElement());
        } else if (o instanceof Widget) {
            childComponent.appendChild(GwtUtils.castElementToJBossNode(((Widget) o).getElement()));
        } else {
            logger.severe("ChildPanel.setValue() can not handle: " + o.getClass());
        }
    }

    @Override
    public ChildModel getValue() {
        return childModel;
    }

    @EventHandler("childTableRowDeleteButton")
    public void childTableRowDeleteButtonButtonClicked(ClickEvent event) {
        childModel.remove();
    }

    @Override
    public HTMLElement getElement() {
        return childTableRow;
    }
}
