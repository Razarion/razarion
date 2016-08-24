package com.btxtech.client.editor.framework;

import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.client.editor.sidebar.LeftSideBarManager;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.ValueListBox;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 23.08.2016.
 */
@Templated("AbstractCrudeParent.html#abstract-crud-parent")
public abstract class AbstractCrudeParent<T extends ObjectNameIdProvider, U extends AbstractPropertyPanel<T>> extends Composite implements LeftSideBarContent {
    private Logger logger = Logger.getLogger(AbstractCrudeParent.class.getName());
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
    private Button deleteButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button saveButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button closeButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private SimplePanel content;

    protected abstract CrudEditor<T> getCrudEditor();

    protected abstract U createPropertyPanel();

    @PostConstruct
    public void init() {
        logger.severe("AbstractCrudeParent.init");
        getCrudEditor().monitor(this::updateSelector);
        selector.addValueChangeHandler(event -> displayPropertyBook(selector.getValue()));
    }

    @EventHandler("createButton")
    private void newButtonClick(ClickEvent event) {
        getCrudEditor().create();
    }

    @EventHandler("deleteButton")
    private void deleteButtonClick(ClickEvent event) {
        T t = getConfigObject();
        if (t != null) {
            getCrudEditor().delete(t);
        }
    }

    @EventHandler("saveButton")
    private void saveButtonClick(ClickEvent event) {
        T t = getConfigObject();
        if (t != null) {
            getCrudEditor().save(t);
        }
    }

    @EventHandler("reloadButton")
    private void reloadButtonClick(ClickEvent event) {
        getCrudEditor().reload();
    }

    @EventHandler("closeButton")
    private void closeButtonClick(ClickEvent event) {
        leftSideBarManager.close(this);
    }

    private void updateSelector(List<ObjectNameId> objectNameIds) {
        T t = getConfigObject();
        if (t != null) {
            if (objectNameIds.contains(t.getObjectNameId())) {
                selector.setValue(t.getObjectNameId());
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
        deleteButton.setEnabled(true);
        saveButton.setEnabled(true);
    }

    @Override
    public void onClose() {
        getCrudEditor().removeMonitor(this::updateSelector);
    }

}
