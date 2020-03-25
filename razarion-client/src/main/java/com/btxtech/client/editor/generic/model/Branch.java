package com.btxtech.client.editor.generic.model;

import org.jboss.errai.databinding.client.BindableListWrapper;
import org.jboss.errai.databinding.client.BindableProxyFactory;
import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.databinding.client.PropertyType;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.function.Consumer;

@Dependent
public class Branch extends AbstractPropertyModel {
    @Inject
    private Instance<Leaf> nodeInstance;
    @Inject
    private Instance<Branch> branchInstance;
    private HasProperties hasProperties;
    private String propertyName;

    public void init(String propertyName, HasProperties hasProperties, PropertyType propertyType) {
        this.propertyName = propertyName;
        initInternal(propertyType);
        this.hasProperties = hasProperties;
    }

    @Override
    public String getDisplayName() {
        if (propertyName != null) {
            return propertyName;
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public Object getPropertyValue() {
        return hasProperties;
    }

    public Object getNamedChildPropertyValue(String propertyName) {
        return hasProperties.get(propertyName);
    }

    public Object getIndexedChildPropertyValue(Integer propertyIndex) {
        return ((BindableListWrapper) hasProperties).get(propertyIndex);
    }

    public void createBindableChildren(Consumer<AbstractPropertyModel> childConsumer) {
        if (!getPropertyType().isBindable()) {
            throw new IllegalStateException("Property is not bindable: " + this);
        }
        if(hasProperties == null) {
            return;
        }
        hasProperties.getBeanProperties().forEach((propertyName, propertyType) -> {
            HasProperties childHasProperties = null;
            if (propertyType.isBindable() || propertyType.isList()) {
                Object childPropertyValue = hasProperties.get(propertyName);
                if(childPropertyValue != null) {
                    childHasProperties = (HasProperties) BindableProxyFactory.getBindableProxy(childPropertyValue);
                }
            }
            childConsumer.accept(createChild(propertyName, null, propertyType, childHasProperties));
        });
    }


    public void createListChildren(Consumer<AbstractPropertyModel> childConsumer) {
        if (!getPropertyType().isList()) {
            throw new IllegalStateException("Property is not a list: " + this);
        }
        if(hasProperties == null) {
            return;
        }
        BindableListWrapper bindableListWrapper = (BindableListWrapper) hasProperties;
        for (int propertyIndex = 0; propertyIndex < bindableListWrapper.size(); propertyIndex++) {
            Object childObject = bindableListWrapper.get(propertyIndex);
            PropertyType propertyType;
            HasProperties childHasProperties = null;
            if (BindableProxyFactory.isBindableType(childObject)) {
                childHasProperties = (HasProperties) childObject;
                propertyType = new PropertyType(childObject.getClass(), true, false);
            } else if (childObject instanceof BindableListWrapper) {
                childHasProperties = (HasProperties) childObject;
                propertyType = new PropertyType(childObject.getClass(), false, true);
            } else {
                propertyType = new PropertyType(childObject.getClass());
            }
            childConsumer.accept(createChild(null, propertyIndex, propertyType, childHasProperties));
        }
    }

    private AbstractPropertyModel createChild(String propertyName, Integer propertyIndex, PropertyType propertyType, HasProperties childHasProperties) {
        if (propertyType.isList() || propertyType.isBindable()) {
            Branch branch = branchInstance.get();
            branch.init(propertyName, childHasProperties, propertyType);
            return branch;
        } else {
            Leaf node = nodeInstance.select(Leaf.class).get();
            node.init(propertyName, propertyIndex, propertyType, this);
            return node;
        }
    }

    protected HasProperties getHasProperties() {
        return hasProperties;
    }
}
