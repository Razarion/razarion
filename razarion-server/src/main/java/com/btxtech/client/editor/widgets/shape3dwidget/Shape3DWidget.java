package com.btxtech.client.editor.widgets.shape3dwidget;

import com.btxtech.client.dialog.ModalDialogManager;
import com.btxtech.client.editor.shape3dgallery.Shape3DGalleryDialog;
import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.shared.Shape3DProvider;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.uiservice.Shape3DUiService;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import elemental.html.File;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.Date;
import java.util.function.Consumer;

/**
 * Created by Beat
 * 16.08.2016.
 */
@Templated("Shape3DWidget.html#shape3D-widget")
public class Shape3DWidget extends Composite {
    // private Logger logger = Logger.getLogger(Shape3DWidget.class.getName());
    @Inject
    private ModalDialogManager modalDialogManager;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private Caller<Shape3DProvider> editorHelperCaller;
    @Inject
    private Shape3DUiService shape3DUiService;
    //    @DataField
//    private Element fileElement = (Element) Browser.getDocument().createInputElement();
    //    @SuppressWarnings("CdiInjectionPointsInspection")
//    @Inject
//    @DataField
//    TODO private Label loaded;
//    @SuppressWarnings("CdiInjectionPointsInspection")
//    @Inject
//    @DataField
//    TODO private Label lastModified;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button galleryButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button uploadButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button reloadButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label dbId;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label internalName;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private VertexContainerWidget vertexContainerWidget;
    private File file;
    private String lastLoadedColladaString;
    private Integer shape3DId;
    private Consumer<Integer> shape3DIdConsumer;

    public void init(Integer shape3DId, Consumer<Integer> shape3DIdConsumer) {
        this.shape3DId = shape3DId;
        this.shape3DIdConsumer = shape3DIdConsumer;
        uploadButton.setEnabled(shape3DId != null);
        if (shape3DId != null) {
            shape3DUiService.request(shape3DId, this::displayShape3D, true);
        }
        reloadButton.setEnabled(false);
    }

    @EventHandler("galleryButton")
    private void galleryButtonClicked(ClickEvent event) {
        modalDialogManager.show("Shape 3D Gallery", ModalDialogManager.Type.QUEUE_ABLE, Shape3DGalleryDialog.class, shape3DId, selectedId -> {
            shape3DId = selectedId;
            shape3DIdConsumer.accept(shape3DId);
            shape3DUiService.request(shape3DId, this::displayShape3D, true);
        });
    }


    // TODO save to server
    // TODO update renderer
    // TODO animations config
    // TODO textureids

    //    @EventHandler("fileElement")
//    public void fileElementChanged(ChangeEvent e) {
//        InputElement inputElement = (InputElement) fileElement;
//        FileList fileList = inputElement.getFiles();
//        file = fileList.item(0);
//        loadFile(file);
//    }
//
//    @EventHandler("reloadButton")
//    private void reloadButtonClick(ClickEvent event) {
//        if (file != null) {
//            loadFile(file);
//        }
//    }
//
//    private void loadFile(final File file) {
//        use below
//        ControlUtils.openSingleFileTextUpload((dataUrl, file) -> shape3DUiService.create(dataUrl, this::fill));
//        final FileReader fileReader = Browser.getWindow().newFileReader();
//        fileReader.setOnload(evt -> {
//            lastLoadedColladaString = (String) fileReader.getResult();
//            editorHelperCaller.call(shape3D -> {
//                try {
//                    loaded.setText(DisplayUtils.formatDate(new Date()));
//                    lastModified.setText(DisplayUtils.formatDate(getLastModifiedDate(file)));
//                    displayShape3D((Shape3D) shape3D);
//                    // TODO shape3DIdConsumer.accept();
//                } catch (Exception e) {
//                    logger.log(Level.SEVERE, e.getMessage(), e);
//                }
//            }, (message, throwable) -> {
//                logger.log(Level.SEVERE, "Shape3DProvider.colladaConvert failed: " + message, throwable);
//                return false;
//            }).colladaConvert(lastLoadedColladaString);
//        });
//        fileReader.readAsText(file, "UFT-8");
//    }
//
    private void displayShape3D(Shape3D shape3D) {
        uploadButton.setEnabled(true);
        dbId.setText(DisplayUtils.handleInteger(shape3D.getDbId()));
        internalName.setText(DisplayUtils.handleString(shape3D.getInternalName()));
        vertexContainerWidget.init(shape3D);
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
