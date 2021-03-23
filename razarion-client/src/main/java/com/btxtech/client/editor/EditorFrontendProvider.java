package com.btxtech.client.editor;

import com.btxtech.client.editor.generic.GenericEditorFrontendProvider;
import jsinterop.annotations.JsType;

import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;

@JsType
@ApplicationScoped
public class EditorFrontendProvider {
    @Inject
    private GenericEditorFrontendProvider genericEditorFrontendProvider;

    @SuppressWarnings("unused") // Called by Angular
    public GenericEditorFrontendProvider getGenericEditorFrontendProvider() {
        return genericEditorFrontendProvider;
    }

}
