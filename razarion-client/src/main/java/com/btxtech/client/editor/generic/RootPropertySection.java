package com.btxtech.client.editor.generic;

import com.btxtech.client.editor.framework.AbstractPropertyPanel;
import com.btxtech.client.editor.generic.custom.CustomWidget;
import com.btxtech.client.editor.generic.model.Branch;
import com.btxtech.client.editor.generic.propertyeditors.PropertySection;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import com.btxtech.shared.system.ExceptionHandler;
import elemental2.core.JsObject;
import elemental2.dom.Element;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.NodeList;
import jsinterop.base.Any;
import jsinterop.base.Js;
import org.jboss.errai.databinding.client.BindableProxyFactory;
import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.databinding.client.PropertyType;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Templated("RootPropertySection.html#panel")
public class RootPropertySection extends AbstractPropertyPanel<ObjectNameIdProvider> {
    // private Logger logger = Logger.getLogger(RootPropertySection.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private Branch branch;
    @Inject
    private Instance<CustomWidget<?>> customWidgetInstance;
    @Inject
    @DataField
    private PropertySection rootPropertySection;
    @Inject
    @DataField
    private HTMLDivElement panel;
    @Inject
    @DataField
    private HTMLDivElement customWidgetDiv;
    //    @Inject
//    @DataField
//    private Element propertyTable;
    private Class<? extends CustomWidget> customWidgetClass;

    @Override
    public void init(ObjectNameIdProvider rootPropertyValue) {
        try {
            NodeList<Element> nodeList = panel.getElementsByTagName("angular-property-table");
            AngularTreeTable angularTreeTable = Js.uncheckedCast(nodeList.getAt(0));
            angularTreeTable.treeNodes = buildTreeNodes(rootPropertyValue);
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
        branch.init(null,
                null,
                (HasProperties) BindableProxyFactory.getBindableProxy(rootPropertyValue),
                new PropertyType(rootPropertyValue.getClass(), true, false),
                null);

        rootPropertySection.init(branch);
        if (customWidgetClass != null) {
            CustomWidget customWidget = customWidgetInstance.get();
            customWidget.setRootPropertyValue(rootPropertyValue);
            customWidgetDiv.appendChild(customWidget.getElement());
        } else {
            customWidgetDiv.style.display = "none";
        }
    }

    private AngularTreeNode[] buildTreeNodes(Object propertyValue) {
        HasProperties hasProperties = (HasProperties) BindableProxyFactory.getBindableProxy(propertyValue);
        List<AngularTreeNode> angularTreeNodes = new ArrayList<>();
        hasProperties.getBeanProperties().forEach((propertyName, propertyType) -> {
            AngularTreeNode angularTreeNode = Js.uncheckedCast(new JsObject());
            angularTreeNode.data = Js.uncheckedCast(new JsObject());
            angularTreeNode.data.name = propertyName;
            angularTreeNodes.add(angularTreeNode);
            Object childPropertyValue = hasProperties.get(propertyName);
            if (propertyType.isBindable() || propertyType.isList()) {
                if (childPropertyValue != null) {
                    angularTreeNode.children = buildTreeNodes(childPropertyValue);
                }
            } else {
                angularTreeNode.data.value = Any.of(childPropertyValue);
            }
        });
        return angularTreeNodes.toArray(new AngularTreeNode[0]);
    }

    @Override
    public ObjectNameIdProvider getConfigObject() {
        return (ObjectNameIdProvider) branch.getPropertyValue();
    }

    public void setCustomWidgetClass(Class<? extends CustomWidget> customWidgetClass) {
        this.customWidgetClass = customWidgetClass;
    }
}
