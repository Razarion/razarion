package com.btxtech.shared.dto.editor;


import java.util.List;
import java.util.Map;

public class GenericPropertyInfo {
    private Map<String, Map<String, String>> listElementTypes;
    private List<CollectionReferenceInfo> collectionReferenceInfos;

    public Map<String, Map<String, String>> getListElementTypes() {
        return listElementTypes;
    }

    public void setListElementTypes(Map<String, Map<String, String>> listElementTypes) {
        this.listElementTypes = listElementTypes;
    }

    public List<CollectionReferenceInfo> getCollectionReferenceInfos() {
        return collectionReferenceInfos;
    }

    public void setCollectionReferenceInfos(List<CollectionReferenceInfo> collectionReferenceInfos) {
        this.collectionReferenceInfos = collectionReferenceInfos;
    }

    public GenericPropertyInfo listElementTypes(Map<String, Map<String, String>> listElementTypes) {
        setListElementTypes(listElementTypes);
        return this;
    }

    public GenericPropertyInfo collectionReferenceInfos(List<CollectionReferenceInfo> collectionReferenceInfos) {
        setCollectionReferenceInfos(collectionReferenceInfos);
        return this;
    }
}
