package com.btxtech.client.sidebar;

import com.btxtech.client.ColladaUiService;
import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.terrain.TerrainObjectService;
import com.btxtech.client.utils.GradToRadConverter;
import com.btxtech.game.jsre.client.common.CollectionUtils;
import com.btxtech.shared.TerrainEditorService;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.TerrainObject;
import com.btxtech.shared.primitives.Vertex;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
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
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 13.05.2016.
 */
@Templated("ColladaEditorSidebar.html#colladaEditorSidebar")
public class ColladaEditorSidebar extends Composite implements LeftSideBarContent {
    private Logger logger = Logger.getLogger(ColladaEditorSidebar.class.getName());
    @Inject
    private Caller<TerrainEditorService> terrainEditorService;
    @Inject
    private TerrainObjectService terrainObjectService;
    @Inject
    private RenderService renderService;
    @Inject
    private ColladaUiService colladaUiService;
    @Inject
    @AutoBound
    private DataBinder<ColladaUiService> colladaUiServiceDataBinder;
    @Inject
    @Bound(converter = GradToRadConverter.class, property = "xRotation")
    @DataField
    private DoubleBox xRotationSlider;
    @Inject
    @Bound(converter = GradToRadConverter.class, property = "xRotation")
    @DataField
    private DoubleBox xRotationBox;
    @Inject
    @Bound(converter = GradToRadConverter.class, property = "yRotation")
    @DataField
    private DoubleBox yRotationSlider;
    @Inject
    @Bound(converter = GradToRadConverter.class, property = "yRotation")
    @DataField
    private DoubleBox yRotationBox;
    @Inject
    @DataField
    private Label directionLabel;
    @Inject
    @Bound
    @DataField
    private DoubleBox generalScale;
    @Inject
    @DataField
    private ValueListBox<ObjectNameId> terrainObjectSelection;
    @DataField
    private Element fileElement = (Element) Browser.getDocument().createInputElement();
    @Inject
    @DataField
    private Label loaded;
    @Inject
    @DataField
    private Label lastModified;
    @Inject
    @DataField
    private Button reloadButton;
    private File file;

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
                ObjectNameId objectNameId = CollectionUtils.getFirst(objectNameIds);
                terrainObjectSelection.setAcceptableValues(objectNameIds);
                terrainObjectSelection.setValue(objectNameId);
            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "getTerrainObjectNameIds failed: " + message, throwable);
                return false;
            }
        }).getTerrainObjectNameIds();
    }

    @Override
    public void onClose() {

    }

    private void displayLightDirectionLabel() {
        Vertex lightDirection = colladaUiServiceDataBinder.getModel().getDirection();
        NumberFormat decimalFormat = NumberFormat.getFormat("#.##");
        directionLabel.setText(decimalFormat.format(lightDirection.getX()) + ":" + decimalFormat.format(lightDirection.getY()) + ":" + decimalFormat.format(lightDirection.getZ()));
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
                terrainEditorService.call(new RemoteCallback<TerrainObject>() {
                    @Override
                    public void callback(TerrainObject terrainObject) {
                        try {
                            terrainObjectService.putVertexContainer(terrainObject);
                            renderService.fillBuffers();
                            DateTimeFormat formatter = DateTimeFormat.getFormat("HH:mm:ss dd.MM.yyyy");
                            loaded.setText(formatter.format(new Date()));
                            lastModified.setText(formatter.format(getLastModifiedDate(file)));
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
                }).colladaConvert(terrainObjectSelection.getValue().getId(), (String) fileReader.getResult());
            }
        });
        fileReader.readAsText(file, "UFT-8");
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
