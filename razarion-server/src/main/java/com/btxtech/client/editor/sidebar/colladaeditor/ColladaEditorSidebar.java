package com.btxtech.client.editor.sidebar.colladaeditor;

import com.btxtech.uiservice.ColladaUiService;
import com.btxtech.client.renderer.engine.ClientRenderServiceImpl;
import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.uiservice.terrain.TerrainObjectService;
import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.client.utils.GradToRadConverter;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.TerrainElementService;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.datatypes.Vertex;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.File;
import elemental.html.FileList;
import elemental.html.FileReader;
import elemental.html.InputElement;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.PropertyChangeEvent;
import org.jboss.errai.databinding.client.api.PropertyChangeHandler;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 13.05.2016.
 */
@Templated("ColladaEditorSidebar.html#colladaEditorSidebar")
public class ColladaEditorSidebar extends Composite implements LeftSideBarContent {
    private Logger logger = Logger.getLogger(ColladaEditorSidebar.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private Caller<TerrainElementService> terrainEditorService;
    @Inject
    private TerrainObjectService terrainObjectService;
    @Inject
    private ClientRenderServiceImpl renderService;
    @Inject
    private ColladaUiService colladaUiService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @AutoBound
    private DataBinder<ColladaUiService> colladaUiServiceDataBinder;
    @Inject
    @Bound(converter = GradToRadConverter.class, property = "rotateX")
    @DataField
    private DoubleBox rotateXSlider;
    @Inject
    @Bound(converter = GradToRadConverter.class, property = "rotateX")
    @DataField
    private DoubleBox rotateXBox;
    @Inject
    @Bound(converter = GradToRadConverter.class, property = "rotateZ")
    @DataField
    private DoubleBox rotateZSlider;
    @Inject
    @Bound(converter = GradToRadConverter.class, property = "rotateZ")
    @DataField
    private DoubleBox rotateZBox;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label directionLabel;
    @Inject
    @Bound
    @DataField
    private DoubleBox generalScale;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private ValueListBox<ObjectNameId> terrainObjectSelection;
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
    @Inject
    @DataField
    private VertexContainerListWidget vertexContainerListWidget;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button saveButton;
    private File file;
    private String lastLoadedColladaString;

    @PostConstruct
    public void init() {
        colladaUiServiceDataBinder.setModel(colladaUiService);
        colladaUiServiceDataBinder.addPropertyChangeHandler(new PropertyChangeHandler<Object>() {
            @Override
            public void onPropertyChange(PropertyChangeEvent<Object> event) {
                displayLightDirectionLabel();
            }
        });
        displayLightDirectionLabel();
        terrainEditorService.call(new RemoteCallback<Collection<ObjectNameId>>() {
            @Override
            public void callback(Collection<ObjectNameId> objectNameIds) {
                try {
                    ObjectNameId objectNameId = CollectionUtils.getFirst(objectNameIds);
                    terrainObjectSelection.setAcceptableValues(objectNameIds);
                    terrainObjectSelection.setValue(objectNameId);
                    TerrainObjectConfig terrainObjectConfig = terrainObjectService.getTerrainObject(objectNameId.getId());
                    // TODO vertexContainerListWidget.setItems(new ArrayList<>(terrainObjectConfig.getVertexContainers()));
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "getTerrainObjectNameIds failed: " + message, throwable);
                return false;
            }
        }).getTerrainObjectNameIds();
        terrainObjectSelection.addValueChangeHandler(new ValueChangeHandler<ObjectNameId>() {
            @Override
            public void onValueChange(ValueChangeEvent<ObjectNameId> event) {
                TerrainObjectConfig terrainObjectConfig = terrainObjectService.getTerrainObject(terrainObjectSelection.getValue().getId());
                // TODO vertexContainerListWidget.setItems(new ArrayList<>(terrainObjectConfig.getVertexContainers()));
                loaded.setText("");
                lastModified.setText("");
                lastLoadedColladaString = null;
            }
        });
    }

    @Override
    public void onClose() {

    }

    private void displayLightDirectionLabel() {
        Vertex lightDirection = colladaUiServiceDataBinder.getModel().getDirection();
        directionLabel.setText(DisplayUtils.formatVertex(lightDirection));
    }

    @EventHandler("generalScale")
    public void generalScaleChanged(ChangeEvent e) {
        terrainObjectService.setupModelMatrices();
    }

    @EventHandler("fileElement")
    public void fileElementChanged(ChangeEvent e) {
        InputElement inputElement = (InputElement) fileElement;
        FileList fileList = inputElement.getFiles();
        file = fileList.item(0);
        loadFile(file);
    }

    @EventHandler("saveButton")
    private void saveButtonClick(ClickEvent event) {
        int terrainObjectId = terrainObjectSelection.getValue().getId();
        terrainEditorService.call(new RemoteCallback<Collection<ObjectNameId>>() {
            @Override
            public void callback(Collection<ObjectNameId> objectNameIds) {
            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "saveTerrainObject failed: " + message, throwable);
                return false;
            }
        }).saveTerrainObject(terrainObjectId, lastLoadedColladaString, extractTextureIds(terrainObjectId));
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
                terrainEditorService.call(new RemoteCallback<TerrainObjectConfig>() {
                    @Override
                    public void callback(TerrainObjectConfig terrainObjectConfig) {
                        try {
                            terrainObjectService.overrideTerrainObject(terrainObjectConfig);
                            renderService.setupTerrainObjectRenderer();
                            loaded.setText(DisplayUtils.formatDate(new Date()));
                            lastModified.setText(DisplayUtils.formatDate(getLastModifiedDate(file)));
                            // TODO vertexContainerListWidget.setItems(new ArrayList<>(terrainObjectConfig.getVertexContainers()));
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, e.getMessage(), e);
                        }
                    }
                }, new ErrorCallback<Object>() {
                    @Override
                    public boolean error(Object message, Throwable throwable) {
                        logger.log(Level.SEVERE, "colladaConvert failed: " + message, throwable);
                        return false;
                    }
                }).colladaConvert(terrainObjectSelection.getValue().getId(), lastLoadedColladaString);
            }
        });
        fileReader.readAsText(file, "UFT-8");
    }

    private Map<String, Integer> extractTextureIds(int terrainObjectId) {
        Map<String, Integer> textures = new HashMap<>();
        TerrainObjectConfig terrainObjectConfig = terrainObjectService.getTerrainObject(terrainObjectId);
        // TODO
//        for (VertexContainer vertexContainer : terrainObjectConfig.getVertexContainers()) {
//            if (vertexContainer.hasTextureId()) {
//                textures.put(vertexContainer.getMaterialId(), vertexContainer.getTextureId());
//            }
//        }
        return textures;
    }

    private Date getLastModifiedDate(File file) {
        // Unfortunately, the GWT elemental has trouble with the date
        return new Date(Long.parseLong(getLastModifiedDateAsLongString(file)));
    }

    private native String getLastModifiedDateAsLongString(File file) /*-{
        // Unfortunately long can not be returned
        return file.lastModifiedDate.getTime() + "";
    }-*/;

}
