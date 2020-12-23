package com.btxtech.server.rest;

import com.btxtech.server.util.GenericPropertyEditorGenerator;
import com.btxtech.shared.dto.editor.GenericPropertyInfo;
import com.btxtech.shared.rest.GenericPropertyEditorController;

public class GenericPropertyEditorControllerImpl implements GenericPropertyEditorController {

    @Override
    public GenericPropertyInfo getGenericPropertyInfo() {
        return GenericPropertyEditorGenerator.generate();
    }
}
