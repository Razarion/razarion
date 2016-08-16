package com.btxtech.client.editor.shape3d;

import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.shared.EditorHelper;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.File;
import elemental.html.FileList;
import elemental.html.FileReader;
import elemental.html.InputElement;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 16.08.2016.
 */
@Templated("Shape3DPanel.html#shape3D-panel")
public class Shape3DPanel extends Composite {
    private Logger logger = Logger.getLogger(Shape3DPanel.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private Caller<EditorHelper> editorHelperCaller;
    @DataField
    private Element fileElement = (Element) Browser.getDocument().createInputElement();
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label loaded;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label lastModified;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button reloadButton;
//    @Inject
//    @DataField
//    private VertexContainerListWidget vertexContainerListWidget;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label dbId;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label internalName;
    private File file;
    private String lastLoadedColladaString;


    public void setShape3D() {
        // ...
    }

    // TODO save to server
    // TODO update renderer
    // TODO animations config
    // TODO textureids

    @EventHandler("fileElement")
    public void fileElementChanged(ChangeEvent e) {
        InputElement inputElement = (InputElement) fileElement;
        FileList fileList = inputElement.getFiles();
        file = fileList.item(0);
        loadFile(file);
    }

    @EventHandler("reloadButton")
    private void reloadButtonClick(ClickEvent event) {
        if (file != null) {
            loadFile(file);
        }
    }

    private void loadFile(final File file) {
        final FileReader fileReader = Browser.getWindow().newFileReader();
        fileReader.setOnload(new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                lastLoadedColladaString = (String) fileReader.getResult();
                editorHelperCaller.call(shape3D -> {
                    try {
                        loaded.setText(DisplayUtils.formatDate(new Date()));
                        lastModified.setText(DisplayUtils.formatDate(getLastModifiedDate(file)));
                        displayShape3D((Shape3D) shape3D);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, e.getMessage(), e);
                    }
                }, (message, throwable) -> {
                    logger.log(Level.SEVERE, "EditorHelper.colladaConvert failed: " + message, throwable);
                    return false;
                }).colladaConvert(lastLoadedColladaString);
            }
        });
        fileReader.readAsText(file, "UFT-8");
    }

    private void displayShape3D(Shape3D shape3D) {
        dbId.setText(Integer.toString(shape3D.getDbId()));
        internalName.setText(shape3D.getInternalName());
        // TODO vertexContainerListWidget.setItems(new ArrayList<>(terrainObjectConfig.getVertexContainers()));
    }

//    private Map<String, Integer> extractTextureIds(int terrainObjectId) {
//        Map<String, Integer> textures = new HashMap<>();
//        TerrainObjectConfig terrainObjectConfig = terrainObjectService.getTerrainObject(terrainObjectId);
//        // TODO
////        for (VertexContainer vertexContainer : terrainObjectConfig.getVertexContainers()) {
////            if (vertexContainer.hasTextureId()) {
////                textures.put(vertexContainer.getMaterialId(), vertexContainer.getTextureId());
////            }
////        }
//        return textures;
//    }

    private Date getLastModifiedDate(File file) {
        // Unfortunately, the GWT elemental has trouble with the date
        return new Date(Long.parseLong(getLastModifiedDateAsLongString(file)));
    }

    private native String getLastModifiedDateAsLongString(File file) /*-{
        // Unfortunately long can not be returned
        return file.lastModifiedDate.getTime() + "";
    }-*/;

}
