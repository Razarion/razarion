package com.btxtech.server.rest;

import com.btxtech.server.util.ListTypeArgumentGenerator;
import com.btxtech.shared.rest.GenericPropertyEditorController;

import java.util.Map;

public class GenericPropertyEditorControllerImpl implements GenericPropertyEditorController {

    @Override
    public Map<String, Map<String, String>> getListTypeArguments() {
        return new ListTypeArgumentGenerator().generate();
    }
}
