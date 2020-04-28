package com.btxtech.client.editor.generic.model;

import org.jboss.errai.databinding.client.BindableListWrapper;
import org.jboss.errai.databinding.client.BindableProxy;
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
    private Branch parent;
    private HasProperties hasProperties;
    private String propertyName;
    private Integer propertyIndex;

    public void init(String propertyName, Integer propertyIndex, HasProperties hasProperties, PropertyType propertyType, Branch parent) {
        this.propertyName = propertyName;
        initInternal(propertyType);
        this.hasProperties = hasProperties;
        this.propertyIndex = propertyIndex;
        this.parent = parent;
    }

    @Override
    public String getDisplayName() {
        if (propertyName != null) {
            return propertyName;
        } else if (propertyIndex != null) {
            return "[" + propertyIndex + "]";
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public Object getPropertyValue() {
        return hasProperties;
    }

    public HasProperties getHasProperties() {
        return hasProperties;
    }

    @Override
    public void setPropertyValue(Object value) {
        if(value!= null) {
            this.hasProperties = (HasProperties) BindableProxyFactory.getBindableProxy(value);
        } else {
            this.hasProperties = null;
        }
        if (propertyName != null) {
            parent.hasProperties.set(propertyName, value);
        } else if (propertyIndex != null) {
            if(value != null) {
                ((BindableListWrapper) (parent.getHasProperties())).set(propertyIndex, value);
            } else {
                ((BindableListWrapper) (parent.getHasProperties())).remove(propertyIndex.intValue());
            }
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public boolean isPropertyNullable() {
        if (hasProperties == null) {
            return true;
        }
        return hasProperties instanceof BindableProxy && !(hasProperties instanceof BindableListWrapper);
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
        if (hasProperties == null) {
            return;
        }
        hasProperties.getBeanProperties().forEach((propertyName, propertyType) -> {
            HasProperties childHasProperties = null;
            if (propertyType.isBindable() || propertyType.isList()) {
                Object childPropertyValue = hasProperties.get(propertyName);
                if (childPropertyValue != null) {
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
        if (hasProperties == null) {
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
            branch.init(propertyName, propertyIndex, childHasProperties, propertyType, this);
            return branch;
        } else {
            Leaf node = nodeInstance.select(Leaf.class).get();
            node.init(propertyName, propertyIndex, propertyType, this);
            return node;
        }
    }
}
