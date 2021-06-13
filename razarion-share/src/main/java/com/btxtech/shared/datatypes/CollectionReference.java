package com.btxtech.shared.datatypes;

public class CollectionReference {
    public enum Type {
        IMAGE("image"),
        BASE_ITEM("base-item");

        private String collectionName;

        Type(String collectionName) {
            this.collectionName = collectionName;
        }

        public String getCollectionName() {
            return collectionName;
        }
    }
}
