package com.btxtech.client.editor.widgets.childtable;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 16.08.2017.
 */
@Templated("ChildTable.html#childTablePanel")
public class ChildTable<T> extends Composite {
    @Inject
    @DataField
    private ListComponent<ChildModel, ChildPanel> childTable;
    @Inject
    @DataField
    private Button childTableCreateButton;
    private Class childPanelClass;
    private Consumer<List<T>> childrenCallback;
    private List<T> children;
    private Provider<T> childCreationCallback;

    public void init(List<T> children, Consumer<List<T>> childrenCallback, Provider<T> childCreationCallback, Class childPanelClass) {
        DOMUtil.removeAllElementChildren(childTable.getElement()); // Remove placeholder table row from template.
        this.children = children;
        this.childPanelClass = childPanelClass;
        this.childrenCallback = childrenCallback;
        this.childCreationCallback = childCreationCallback;
        if (children != null) {
            setupChildren();
        }
    }

    private void onChildModelRemoved(T t) {
        children.remove(t);
        setupChildren();
    }

    @EventHandler("childTableCreateButton")
    public void childTableCreateButtonClicked(ClickEvent event) {
        if (children == null) {
            children = new ArrayList<>();
            childrenCallback.accept(children);
        }
        children.add(childCreationCallback.get());
        setupChildren();
    }

    private void setupChildren() {
        List<ChildModel> childModels = new ArrayList<>();
        for (T t : children) {
            ChildModel<T> childModel = new ChildModel<>();
            childModel.setChild(t);
            childModel.setRemoveCallback(this::onChildModelRemoved);
            childModel.setChildPanelClass(childPanelClass);
            childModels.add(childModel);
        }
        childTable.setValue(childModels);
    }
}
