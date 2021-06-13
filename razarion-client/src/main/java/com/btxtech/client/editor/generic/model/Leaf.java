package com.btxtech.client.editor.generic.model;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.I18nString;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.editor.CollectionReferenceInfo;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import org.jboss.errai.databinding.client.BindableListWrapper;
import org.jboss.errai.databinding.client.PropertyType;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Map;

@Dependent
public class Leaf extends AbstractPropertyModel {
    @Inject
    private GenericPropertyInfoProvider genericPropertyInfoProvider;
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

    public PropertyEditorSelector getPropertyEditorSelector() {
        Class<?> clazz = getPropertyClass();
        if (clazz.isEnum()) {
            return PropertyEditorSelector.ENUM;
        } else if (clazz.equals(Map.class)) {
            return PropertyEditorSelector.INTEGER_MAP;
        } else {
            if (getPropertyName() != null) {
                Class<?> parentClass = getBranch().getPropertyClass();
                CollectionReferenceInfo collectionReferenceInfo = genericPropertyInfoProvider.scanForCollectionReference(parentClass, getPropertyName());
                if (collectionReferenceInfo != null) {
                    switch (collectionReferenceInfo.getType()) {
                        case IMAGE:
                            return PropertyEditorSelector.IMAGE_REFERENCE;
                        case BASE_ITEM:
                            return PropertyEditorSelector.BASE_ITEM_REFERENCE;
                        default:
                            throw new IllegalArgumentException("CollectionReferenceType unknown: " + collectionReferenceInfo.getType());
                    }
                }
                // TODO Collada String handling -> SpecialEditor annotation
            }
            if (clazz.equals(String.class)) {
                return PropertyEditorSelector.STRING;
            } else if (clazz.equals(Integer.class)) {
                return PropertyEditorSelector.INTEGER;
            } else if (clazz.equals(Double.class)) {
                return PropertyEditorSelector.DOUBLE;
            } else if (clazz.equals(Boolean.class)) {
                return PropertyEditorSelector.BOOLEAN;
            } else if (clazz.equals(Rectangle.class)) {
                return PropertyEditorSelector.RECTANGLE;
            } else if (clazz.equals(Rectangle2D.class)) {
                return PropertyEditorSelector.RECTANGLE_2D;
            } else if (clazz.equals(DecimalPosition.class)) {
                return PropertyEditorSelector.DECIMAL_POSITION;
            } else if (clazz.equals(Index.class)) {
                return PropertyEditorSelector.INDEX;
            } else if (clazz.equals(Vertex.class)) {
                return PropertyEditorSelector.VERTEX;
            } else if (clazz.equals(PlaceConfig.class)) {
                return PropertyEditorSelector.PLACE_CONFIG;
            } else if (clazz.equals(I18nString.class)) {
                return PropertyEditorSelector.I18N_STRING;
            } else if (clazz.equals(Polygon2D.class)) {
                return PropertyEditorSelector.POLYGON_2D;
            }
            return PropertyEditorSelector.UNKNOWN;
        }
    }
}
