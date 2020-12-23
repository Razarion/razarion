package com.btxtech.shared.dto.editor;


import java.util.List;
import java.util.Map;

public class GenericPropertyInfo {
    private Map<String, Map<String, String>> listElementTypes;
    private List<OpenApi3Schema> openApi3Schemas;

    public Map<String, Map<String, String>> getListElementTypes() {
        return listElementTypes;
    }

    public void setListElementTypes(Map<String, Map<String, String>> listElementTypes) {
        this.listElementTypes = listElementTypes;
    }

    public List<OpenApi3Schema> getOpenApi3Schemas() {
        return openApi3Schemas;
    }

    public void setOpenApi3Schemas(List<OpenApi3Schema> openApi3Schemas) {
        this.openApi3Schemas = openApi3Schemas;
    }

    public GenericPropertyInfo listElementTypes(Map<String, Map<String, String>> listElementTypes) {
        setListElementTypes(listElementTypes);
        return this;
    }

    public GenericPropertyInfo openApi3Schemas(List<OpenApi3Schema> openApi3Schemas) {
        setOpenApi3Schemas(openApi3Schemas);
        return this;
    }
}
