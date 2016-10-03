package com.btxtech.client.editor.framework;

import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.client.editor.sidebar.LeftSideBarManager;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.ValueListBox;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 23.08.2016.
 */
@Templated("AbstractCrudeParentSidebar.html#abstract-crud-parent")
public abstract class AbstractCrudeParentSidebar<T extends ObjectNameIdProvider, U extends AbstractPropertyPanel<T>> extends LeftSideBarContent {
    // private Logger logger = Logger.getLogger(AbstractCrudeParentSidebar.class.getName());
    @Inject
    private LeftSideBarManager leftSideBarManager;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private ValueListBox<ObjectNameId> selector;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button createButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button reloadButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private SimplePanel content;

    protected abstract CrudEditor<T> getCrudEditor();

    protected abstract U createPropertyPanel();

    @PostConstruct
    public void init() {
        getCrudEditor().monitor(this::updateSelector);
        getCrudEditor().monitorSelection(this::select);
        selector.addValueChangeHandler(event -> displayPropertyBook(selector.getValue()));
    }

    @Override
    protected void onConfigureDialog() {
        registerSaveButton(() -> {
            T t = getConfigObject();
            if (t != null) {
                getCrudEditor().save(t);
            }
        });
        enableSaveButton(false);
        registerDeleteButton(() -> {
            T t = getConfigObject();
            if (t != null) {
                getCrudEditor().delete(t);
            }
        });
        enableDeleteButton(false);
    }

    public void select(ObjectNameId selection) {
        selector.setValue(selection);
        displayPropertyBook(selection); // TODO may changing the selector fire an event
    }

    @EventHandler("createButton")
    private void newButtonClick(ClickEvent event) {
        getCrudEditor().create();
    }

    @EventHandler("reloadButton")
    private void reloadButtonClick(ClickEvent event) {
        getCrudEditor().reload();
    }

    private void updateSelector(List<ObjectNameId> objectNameIds) {
        T t = getConfigObject();
        if (t != null) {
            if (objectNameIds.contains(t.createObjectNameId())) {
                selector.setValue(t.createObjectNameId());
            } else {
                // Config object has been deleted
                selector.setValue(null);
                content.setWidget(null);
            }
        } else {
            selector.setValue(null);
            content.setWidget(null);
        }
        selector.setAcceptableValues(objectNameIds);
    }

    private T getConfigObject() {
        if (content.getWidget() == null) {
            return null;
        }
        return ((U) content.getWidget()).getConfigObject();
    }

    private void displayPropertyBook(ObjectNameId objectNameId) {
        U u = createPropertyPanel();
        u.init(getCrudEditor().getInstance(objectNameId));
        content.setWidget(u);
        enableSaveButton(true);
        enableDeleteButton(true);
    }

    @Override
    public void onClose() {
        getCrudEditor().removeMonitor(this::updateSelector);
        getCrudEditor().removeSelectionMonitor(this::select);
    }

}
