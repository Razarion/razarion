package com.btxtech.client.editor.widgets.childpanel;

import com.btxtech.client.utils.GwtUtils;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 22.08.2017.
 */
@Templated("ChildContainer.html#childContainer")
public class ChildContainer<T> extends Composite {
    private Logger logger = Logger.getLogger(ChildContainer.class.getName());
    @Inject
    @DataField
    private Button childContainerCreateButton;
    @Inject
    @DataField
    private Div childWrapperPanel;
    @Inject
    @DataField
    private Button childPanelDeleteButton;
    @Inject
    @DataField
    private Div childPanel;
    private T child;
    private Consumer<T> creationDeletionCallback;
    private Provider<T> creationProvider;
    @Inject
    private Instance<Object> panelInstance;
    private Class childPanelClass;

    public void init(T child, Consumer<T> creationDeletionCallback, Provider<T> creationProvider, Class childPanelClass) {
        this.child = child;
        this.creationDeletionCallback = creationDeletionCallback;
        this.creationProvider = creationProvider;
        this.childPanelClass = childPanelClass;
        setupGui();
    }

    @EventHandler("childContainerCreateButton")
    public void childContainerCreateButtonClicked(ClickEvent event) {
        child = creationProvider.get();
        creationDeletionCallback.accept(child);
        setupGui();
    }

    @EventHandler("childPanelDeleteButton")
    public void childPanelDeleteButtonClicked(ClickEvent event) {
        child = null;
        creationDeletionCallback.accept(null);
        setupGui();
    }

    private void setupGui() {
        if (child != null) {
            childWrapperPanel.getStyle().setProperty("display", "block");
            childContainerCreateButton.getElement().getStyle().setDisplay(Style.Display.NONE);
            Object o = panelInstance.select(childPanelClass).get();
            if (o instanceof TakesValue) {
                ((TakesValue) o).setValue(child);
            }
            if (o instanceof IsElement) {
                childPanel.appendChild(((IsElement) o).getElement());
            } else if (o instanceof Widget) {
                childPanel.appendChild(GwtUtils.castElementToJBossNode(((Widget) o).getElement()));
            } else {
                logger.severe("ChildPanel.setValue() can not handle: " + o.getClass());
            }
        } else {
            childWrapperPanel.getStyle().setProperty("display", "none");
            childContainerCreateButton.getElement().getStyle().setDisplay(Style.Display.BLOCK);
            childPanel.setInnerHTML("");
        }
    }
}
