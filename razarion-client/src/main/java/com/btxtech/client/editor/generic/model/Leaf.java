package com.btxtech.client.editor.generic.model;

import org.jboss.errai.databinding.client.BindableListWrapper;
import org.jboss.errai.databinding.client.PropertyType;

import javax.enterprise.context.Dependent;

@Dependent
public class Leaf extends AbstractPropertyModel {
    private Branch branch;
    private String propertyName;
    private Integer propertyIndex;

    protected void init(String propertyName, Integer propertyIndex, PropertyType propertyType, Branch branch) {
        initInternal(propertyType);
        this.propertyName = propertyName;
        this.propertyIndex = propertyIndex;
        this.branch = branch;
    }

    @Override
    public Object getPropertyValue() {
        if (propertyName != null) {
            return branch.getNamedChildPropertyValue(propertyName);
        }
        if (propertyIndex != null) {
            return branch.getIndexedChildPropertyValue(propertyIndex);
        }
        throw new IllegalStateException("No propertyName and no propertyIndex");
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
    protected String getPropertyName() {
        return propertyName;
    }

    @Override
    public void setPropertyValue(Object value) {
        if (propertyName != null) {
            branch.getHasProperties().set(propertyName, value);
        } else if (propertyIndex != null) {
            if(value != null) {
                ((BindableListWrapper) (branch.getHasProperties())).set(propertyIndex, value);
            } else {
                ((BindableListWrapper) (branch.getHasProperties())).remove(propertyIndex.intValue());
            }
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public boolean isPropertyNullable() {
        return false;
    }

    public Branch getBranch() {
        return branch;
    }
}
