package com.btxtech.client.editor;

import com.btxtech.client.editor.generic.AngularTreeNode;
import com.btxtech.client.editor.generic.AngularTreeNodeData;
import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.rest.BaseItemTypeEditorController;
import com.btxtech.shared.rest.CrudController;
import com.btxtech.shared.rest.DrivewayEditorController;
import com.btxtech.shared.rest.GameUiContextEditorController;
import com.btxtech.shared.rest.GroundEditorController;
import com.btxtech.shared.rest.LevelEditorController;
import com.btxtech.shared.rest.ParticleEmitterSequenceEditorController;
import com.btxtech.shared.rest.ParticleShapeEditorController;
import com.btxtech.shared.rest.PlanetEditorController;
import com.btxtech.shared.rest.ResourceItemTypeEditorController;
import com.btxtech.shared.rest.Shape3DEditorController;
import com.btxtech.shared.rest.SlopeEditorController;
import com.btxtech.shared.rest.TerrainObjectEditorController;
import com.btxtech.shared.rest.WaterEditorController;
import elemental2.core.Array;
import elemental2.promise.Promise;
import jsinterop.annotations.JsType;
import jsinterop.base.Any;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.databinding.client.BindableProxyFactory;
import org.jboss.errai.databinding.client.HasProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@JsType
public class EditorFrontendProvider {
    private static final CrudControllerEntry[] CRUD_CONTROLLERS = new CrudControllerEntry[]{
            new CrudControllerEntry(LevelEditorController.class, "Levels"),
            new CrudControllerEntry(PlanetEditorController.class, "Planets"),
            new CrudControllerEntry(GroundEditorController.class, "Grounds"),
            new CrudControllerEntry(SlopeEditorController.class, "Slope"),
            new CrudControllerEntry(DrivewayEditorController.class, "Driveway"),
            new CrudControllerEntry(WaterEditorController.class, "Water"),
            new CrudControllerEntry(GameUiContextEditorController.class, "Game Ui Context"),
            new CrudControllerEntry(Shape3DEditorController.class, "Shape 3D"),
            new CrudControllerEntry(TerrainObjectEditorController.class, "Terrain Object"),
            new CrudControllerEntry(BaseItemTypeEditorController.class, "Base Items"),
            new CrudControllerEntry(ResourceItemTypeEditorController.class, "Resource Items"),
            new CrudControllerEntry(ParticleShapeEditorController.class, "Particle Shapes"),
            new CrudControllerEntry(ParticleEmitterSequenceEditorController.class, "Particle Emitter Sequences")
    };
    private Logger logger = Logger.getLogger(EditorFrontendProvider.class.getName());

    @SuppressWarnings("unused") // Called by Angular
    public Promise<ObjectNameId[]> requestConfigs(int crudControllerIndex) {
        return new Promise<>((resolve, reject) -> {
            CrudControllerEntry crudControllerEntry = CRUD_CONTROLLERS[crudControllerIndex];
            MessageBuilder.createCall(
                    (RemoteCallback<List<ObjectNameId>>) response -> resolve.onInvoke(response.toArray(new ObjectNameId[0])),
                    (message, throwable) -> {
                        logger.log(Level.SEVERE, "EditorFrontendProvider loadObjectNameId() " + crudControllerEntry.crudControllerClass + "\n" + message, throwable);
                        reject.onInvoke("GenericCrudControllerEditor.loadObjectNameId() " + crudControllerEntry.crudControllerClass + "\n" + message + "\n" + throwable);
                        return false;
                    },
                    crudControllerEntry.crudControllerClass).getObjectNameIds();
        });
    }

    @SuppressWarnings("unused") // Called by Angular
    public Array<String> crudControllers() {
        Array<String> crudControllers = new Array<>();
        Arrays.stream(CRUD_CONTROLLERS).forEach(crudControllerEntry -> crudControllers.push(crudControllerEntry.name));
        return crudControllers;
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<AngularTreeNode[]> readConfig(int crudControllerIndex, int configId) {
        CrudControllerEntry crudControllerEntry = CRUD_CONTROLLERS[crudControllerIndex];
        return new Promise<>((resolve, reject) -> MessageBuilder.createCall((RemoteCallback<Config>) config -> resolve.onInvoke(buildTreeNodes(config)),
                (message, throwable) -> {
                    logger.log(Level.SEVERE, "EditorFrontendProvider readConfig() " + crudControllerEntry.crudControllerClass + "\n" + "configId:" + configId + "\n" + message, throwable);
                    reject.onInvoke("GenericCrudControllerEditor.readConfig() " + crudControllerEntry.crudControllerClass + "\n" + "configId:" + configId + "\n" + message + "\n" + throwable);
                    return false;
                },
                crudControllerEntry.crudControllerClass).read(configId));
    }

    private AngularTreeNode[] buildTreeNodes(Object config) {
        HasProperties hasProperties = (HasProperties) BindableProxyFactory.getBindableProxy(config);
        List<AngularTreeNode> angularTreeNodes = new ArrayList<>();
        hasProperties.getBeanProperties().forEach((propertyName, propertyType) -> {
            AngularTreeNode angularTreeNode = new AngularTreeNode();
            angularTreeNode.data = new AngularTreeNodeData();
            angularTreeNode.data.name = propertyName;
            angularTreeNodes.add(angularTreeNode);
            Object childPropertyValue = hasProperties.get(propertyName);
            if (propertyType.isBindable()) {
                if (childPropertyValue != null) {
                    angularTreeNode.children = buildTreeNodes(childPropertyValue);
                } else {
                    angularTreeNode.leaf = true;
                }
            } else if (propertyType.isList()) {
                if (childPropertyValue != null) {
                    List<?> childList = (List<?>) childPropertyValue;
                    AngularTreeNode[] listElementNodes = new AngularTreeNode[childList.size()];
                    for (int i = 0; i < childList.size(); i++) {
                        Object listElement = childList.get(i);
                        AngularTreeNode angularTreeNodeListElement = new AngularTreeNode();
                        angularTreeNodeListElement.data = new AngularTreeNodeData();
                        angularTreeNodeListElement.data.name = "[" + i + "]";
                        if (BindableProxyFactory.isBindableType(listElement)) {
                            angularTreeNodeListElement.children = buildTreeNodes(listElement);
                        } else {
                            angularTreeNodeListElement.data.value = Any.of(listElement);
                        }
                        listElementNodes[i] = angularTreeNodeListElement;
                    }
                    angularTreeNode.children = listElementNodes;
                } else {
                    angularTreeNode.leaf = true;
                }
            } else {
                angularTreeNode.data.value = Any.of(childPropertyValue);
                angularTreeNode.leaf = true;
            }
        });
        return angularTreeNodes.toArray(new AngularTreeNode[0]);
    }

    private static class CrudControllerEntry {
        private Class<? extends CrudController<? extends Config>> crudControllerClass;
        private String name;

        public CrudControllerEntry(Class<? extends CrudController<? extends Config>> crudControllerClass, String name) {
            this.crudControllerClass = crudControllerClass;
            this.name = name;
        }
    }
}
