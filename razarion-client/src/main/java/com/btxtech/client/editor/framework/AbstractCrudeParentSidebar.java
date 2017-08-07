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
public abstract class AbstractCrudeParentSidebar<T extends ObjectNameIdProvider, U extends AbstractPropertyPanel<T>> extends LeftSideBarContent implements CrudEditor.LoadedListener, CrudEditor.SelectionListener {
    // private Logger logger = Logger.getLogger(AbstractCrudeParentSidebar.class.getName());
    @Inject
    @DataField
    private ValueListBox<ObjectNameId> selector;
    @Inject
    @DataField
    private Button createButton;
    @Inject
    @DataField
    private Button reloadButton;
    @Inject
    @DataField
    private SimplePanel content;

    protected abstract CrudEditor<T> getCrudEditor();

    protected abstract U createPropertyPanel();

    @PostConstruct
    public void init() {
        getCrudEditor().monitor(this);
        getCrudEditor().monitorSelection(this);
        selector.addValueChangeHandler(event -> displayPropertyBook(selector.getValue()));
        getCrudEditor().init();
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

    @Override
    public void onSelect(ObjectNameId objectNameId) {
        selector.setValue(objectNameId);
        displayPropertyBook(objectNameId); // TODO may changing the selector fire an event
    }

    @EventHandler("createButton")
    private void newButtonClick(ClickEvent event) {
        getCrudEditor().create();
    }

    @EventHandler("reloadButton")
    private void reloadButtonClick(ClickEvent event) {
        getCrudEditor().reload();
    }

    @Override
    public void onLoaded(List<ObjectNameId> objectNameIds) {
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

    public T getConfigObject() {
        if (content.getWidget() == null) {
            return null;
        }
        return ((U) content.getWidget()).getConfigObject();
    }

    private void displayPropertyBook(ObjectNameId objectNameId) {
        if (content.getWidget() == null) {
            getCrudEditor().removeChangeListener((CrudEditor.ChangeListener) content.getWidget());
        }
        U u = createPropertyPanel();
        getCrudEditor().addChangeListener(u);
        getCrudEditor().getInstance(objectNameId, t -> {
            u.init(t);
            content.setWidget(u);
            enableSaveButton(true);
            enableDeleteButton(true);
        });
    }

    @Override
    public void onClose() {
        getCrudEditor().removeMonitor(this);
        getCrudEditor().removeSelectionMonitor(this);
    }

}
