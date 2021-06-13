package com.btxtech.shared.datatypes;

public enum CollectionReferenceType {
    IMAGE("image"),
    BASE_ITEM("base-item");

    private String collectionName;

    CollectionReferenceType(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getCollectionName() {
        return collectionName;
    }
}
