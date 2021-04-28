package com.btxtech.client.editor.generic;

import com.btxtech.client.editor.generic.model.AbstractPropertyModel;
import com.btxtech.client.editor.generic.model.Branch;
import com.btxtech.client.editor.generic.model.GenericPropertyInfoProvider;
import com.btxtech.client.editor.generic.model.Leaf;
import com.btxtech.client.editor.generic.model.PropertyEditorSelector;
import com.btxtech.client.editor.generic.updater.EngineUpdater;
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
import jsinterop.base.Js;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.databinding.client.BindableProxyFactory;
import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.databinding.client.PropertyType;

import javax.enterprise.inject.Instance;
import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@JsType
@ApplicationScoped
public class GenericEditorFrontendProvider {
    private static final GenericEditorFrontendProvider.CrudControllerEntry[] CRUD_CONTROLLERS = new GenericEditorFrontendProvider.CrudControllerEntry[]{
            new GenericEditorFrontendProvider.CrudControllerEntry(LevelEditorController.class, "Levels"),
            new GenericEditorFrontendProvider.CrudControllerEntry(PlanetEditorController.class, "Planets"),
            new GenericEditorFrontendProvider.CrudControllerEntry(GroundEditorController.class, "Grounds"),
            new GenericEditorFrontendProvider.CrudControllerEntry(SlopeEditorController.class, "Slope"),
            new GenericEditorFrontendProvider.CrudControllerEntry(DrivewayEditorController.class, "Driveway"),
            new GenericEditorFrontendProvider.CrudControllerEntry(WaterEditorController.class, "Water"),
            new GenericEditorFrontendProvider.CrudControllerEntry(GameUiContextEditorController.class, "Game Ui Context"),
            new GenericEditorFrontendProvider.CrudControllerEntry(Shape3DEditorController.class, "Shape 3D"),
            new GenericEditorFrontendProvider.CrudControllerEntry(TerrainObjectEditorController.class, "Terrain Object"),
            new GenericEditorFrontendProvider.CrudControllerEntry(BaseItemTypeEditorController.class, "Base Items"),
            new GenericEditorFrontendProvider.CrudControllerEntry(ResourceItemTypeEditorController.class, "Resource Items"),
            new GenericEditorFrontendProvider.CrudControllerEntry(ParticleShapeEditorController.class, "Particle Shapes"),
            new GenericEditorFrontendProvider.CrudControllerEntry(ParticleEmitterSequenceEditorController.class, "Particle Emitter Sequences")
    };
    @Inject
    private Instance<Branch> branchInstance;
    @Inject
    private GenericPropertyInfoProvider genericPropertyInfoProvider;
    @Inject
    private EngineUpdater engineUpdater;
    private Logger logger = Logger.getLogger(GenericEditorFrontendProvider.class.getName());

    @SuppressWarnings("unused") // Called by Angular
    public Array<String> crudControllers() {
        genericPropertyInfoProvider.load();
        Array<String> crudControllers = new Array<>();
        Arrays.stream(CRUD_CONTROLLERS).forEach(crudControllerEntry -> crudControllers.push(crudControllerEntry.name));
        return crudControllers;
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<ObjectNameId[]> requestObjectNameIds(int crudControllerIndex) {
        return new Promise<>((resolve, reject) -> {
            CrudControllerEntry crudControllerEntry = CRUD_CONTROLLERS[crudControllerIndex];
            MessageBuilder.createCall(
                    (RemoteCallback<List<ObjectNameId>>) response -> resolve.onInvoke(response.toArray(new ObjectNameId[0])),
                    (message, throwable) -> {
                        logger.log(Level.SEVERE, "CrudController.getObjectNameIds() " + crudControllerEntry.crudControllerClass + "\n" + message, throwable);
                        reject.onInvoke("CrudController.getObjectNameIds() " + crudControllerEntry.crudControllerClass + "\n" + message + "\n" + throwable);
                        return false;
                    },
                    crudControllerEntry.crudControllerClass).getObjectNameIds();
        });
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<GwtAngularPropertyTable> createConfig(int crudControllerIndex) {
        CrudControllerEntry crudControllerEntry = CRUD_CONTROLLERS[crudControllerIndex];

        return new Promise<>((resolve, reject) -> MessageBuilder.createCall(
                (RemoteCallback<Config>) config -> config2GwtAngularPropertyTableAndConnect(config, crudControllerEntry.crudControllerClass, config.getId(), resolve, reject),
                (message, throwable) -> {
                    logger.log(Level.SEVERE, "CrudController.create() " + crudControllerEntry.crudControllerClass + "\n" + message, throwable);
                    reject.onInvoke("CrudController.create() " + crudControllerEntry.crudControllerClass + "\n" + message + "\n" + throwable);
                    return false;
                },
                crudControllerEntry.crudControllerClass).create());
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<GwtAngularPropertyTable> readConfig(int crudControllerIndex, int configId) {
        CrudControllerEntry crudControllerEntry = CRUD_CONTROLLERS[crudControllerIndex];
        return new Promise<>((resolve, reject) -> MessageBuilder.createCall(
                (RemoteCallback<Config>) config -> config2GwtAngularPropertyTableAndConnect(config, crudControllerEntry.crudControllerClass, configId, resolve, reject),
                (message, throwable) -> {
                    logger.log(Level.SEVERE, "CrudController.readConfig() " + crudControllerEntry.crudControllerClass + "\n" + "configId:" + configId + "\n" + message, throwable);
                    reject.onInvoke("CrudController.readConfig() " + crudControllerEntry.crudControllerClass + "\n" + "configId:" + configId + "\n" + message + "\n" + throwable);
                    return false;
                },
                crudControllerEntry.crudControllerClass).read(configId));
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<Void> updateConfig(int crudControllerIndex, GwtAngularPropertyTable gwtAngularPropertyTable) {
        CrudControllerEntry crudControllerEntry = CRUD_CONTROLLERS[crudControllerIndex];
        Config config = Js.cast(gwtAngularPropertyTable.rootBranch.getPropertyValue());

        return new Promise<>((resolve, reject) -> MessageBuilder.createCall(
                (RemoteCallback<Void>) ignore -> resolve.onInvoke((Void) null),
                (message, throwable) -> {
                    logger.log(Level.SEVERE, "CrudController.update() " + crudControllerEntry.crudControllerClass + "\n" + "config:" + config + "\n" + message, throwable);
                    reject.onInvoke("CrudController.update() " + crudControllerEntry.crudControllerClass + "\n" + "config:" + config + "\n" + message + "\n" + throwable);
                    return false;
                },
                crudControllerEntry.crudControllerClass).update(Js.cast(config)));
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<GwtAngularPropertyTable> deleteConfig(int crudControllerIndex, GwtAngularPropertyTable gwtAngularPropertyTable) {
        CrudControllerEntry crudControllerEntry = CRUD_CONTROLLERS[crudControllerIndex];

        return new Promise<>((resolve, reject) -> MessageBuilder.createCall(
                (RemoteCallback<GwtAngularPropertyTable>) resolve::onInvoke,
                (message, throwable) -> {
                    logger.log(Level.SEVERE, "CrudController.update() " + crudControllerEntry.crudControllerClass + "\n" + "configId:" + gwtAngularPropertyTable.configId + "\n" + message, throwable);
                    reject.onInvoke("CrudController.update() " + crudControllerEntry.crudControllerClass + "\n" + "configId:" + gwtAngularPropertyTable.configId + "\n" + message + "\n" + throwable);
                    return false;
                },
                crudControllerEntry.crudControllerClass).delete(gwtAngularPropertyTable.configId));
    }

    private void config2GwtAngularPropertyTableAndConnect(Object config, Class<? extends CrudController<? extends Config>> crudControllerClass, int configId,
                                                          Promise.PromiseExecutorCallbackFn.ResolveCallbackFn<GwtAngularPropertyTable> resolve,
                                                          Promise.PromiseExecutorCallbackFn.RejectCallbackFn reject) {
        try {
            engineUpdater.connect(config);
            Branch branch = branchInstance.get();
            branch.init(null, null,
                    (HasProperties) BindableProxyFactory.getBindableProxy(config),
                    new PropertyType(config.getClass(), true, false),
                    null);
            resolve.onInvoke(branch2GwtAngularPropertyTable(branch, configId));
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "configToAngularTreeNodes() failed. Config: " + config + "\n" + crudControllerClass + "\n" + "configId:" + configId, throwable);
            reject.onInvoke("configToAngularTreeNodes() failed. Config: " + config + "\n" + crudControllerClass + "\n" + "configId:" + configId + "\n" + throwable);
        }
    }

    private GwtAngularPropertyTable branch2GwtAngularPropertyTable(Branch branch, int configId) {
        GwtAngularPropertyTable gwtAngularPropertyTable = new GwtAngularPropertyTable();
        gwtAngularPropertyTable.rootTreeNodes = branch2AngularTreeNodes(null, branch);
        gwtAngularPropertyTable.rootBranch = branch;
        gwtAngularPropertyTable.configId = configId;
        return gwtAngularPropertyTable;
    }

    private AngularTreeNode[] branch2AngularTreeNodes(AngularTreeNode parent, Branch branch) {
        List<AngularTreeNode> angularTreeNodes = new ArrayList<>();
        branch.createBindableChildren(childPropertyModel -> angularTreeNodes.add(propertyModel2AngularTreeNode(parent, childPropertyModel)));
        return angularTreeNodes.toArray(new AngularTreeNode[0]);
    }

    private AngularTreeNode[] listBranch2AngularTreeNodes(AngularTreeNode parent, Branch branch) {
        List<AngularTreeNode> listAngularTreeNodes = new ArrayList<>();
        branch.createListChildren(childListPropertyModel -> listAngularTreeNodes.add(propertyModel2AngularTreeNode(parent, childListPropertyModel)));
        return listAngularTreeNodes.toArray(new AngularTreeNode[0]);
    }

    private AngularTreeNode propertyModel2AngularTreeNode(AngularTreeNode parent, AbstractPropertyModel propertyModel) {
        AngularTreeNode angularTreeNode = new AngularTreeNode(propertyModel, parent);
        angularTreeNode.data = new AngularTreeNodeData() {
            @Override
            public void onCreate(GwtAngularPropertyTable gwtAngularPropertyTable) {
                try {
                    if (angularTreeNode.abstractPropertyModel == null) {
                        throw new IllegalStateException("angularTreeNode.abstractPropertyModel == null");
                    }
                    if(angularTreeNode.abstractPropertyModel instanceof Branch) {
                        Branch branch = (Branch) angularTreeNode.abstractPropertyModel;
                        if(branch.getPropertyType().isList()) {
                            AbstractPropertyModel child = branch.createListElement();
                            if (angularTreeNode.children == null) {
                                angularTreeNode.children = new AngularTreeNode[0];
                            }
                            Array<AngularTreeNode> treeNodeArray = Js.cast(angularTreeNode.children);
                            treeNodeArray.push(propertyModel2AngularTreeNode(angularTreeNode, child));
                            angularTreeNode.expanded = true;
                        } else if(branch.getPropertyType().isBindable()) {
                            angularTreeNode.abstractPropertyModel.createAndSetPropertyValue();
                            angularTreeNode.children = branch2AngularTreeNodes(angularTreeNode, branch);
                            angularTreeNode.data.createAllowed = false;
                            angularTreeNode.data.deleteAllowed = true;
                            angularTreeNode.expanded = true;
                        }
                    }
                    rootTreeNodes(gwtAngularPropertyTable);
                } catch (Throwable throwable) {
                    logger.log(Level.SEVERE, "onCreate failed", throwable);
                    throw throwable;
                }
            }

            @Override
            public void onDelete(GwtAngularPropertyTable gwtAngularPropertyTable) {
                try {
                    if (angularTreeNode.parent.children == null) {
                        throw new IllegalStateException("Parent is not a list");
                    }
                    angularTreeNode.abstractPropertyModel.setPropertyValue(null);
                    if (angularTreeNode.parent.abstractPropertyModel.getPropertyType().isList()) {
                        angularTreeNode.parent.children = listBranch2AngularTreeNodes(angularTreeNode.parent, (Branch) angularTreeNode.parent.abstractPropertyModel);
                        angularTreeNode.parent.leaf = angularTreeNode.parent.children.length == 0;
                    } else {
                        angularTreeNode.children = new AngularTreeNode[0];
                        angularTreeNode.data.createAllowed = true;
                        angularTreeNode.data.deleteAllowed = false;
                    }
                    rootTreeNodes(gwtAngularPropertyTable);
                } catch (Throwable throwable) {
                    logger.log(Level.SEVERE, "onDelete failed", throwable);
                    throw throwable;
                }
            }

            @Override
            public void setValue(Any value) {
                try {
                    Object javaValue = value;
                    if (value != null) {
                        javaValue = PropertyEditorSelector.fromSelector(angularTreeNode.data.propertyEditorSelector).convertFromAngular(value);
                    }
                    angularTreeNode.abstractPropertyModel.setPropertyValue(javaValue);
                } catch (Throwable throwable) {
                    logger.log(Level.SEVERE, "setValue failed", throwable);
                    throw throwable;
                }
            }
        };
        angularTreeNode.data.name = propertyModel.getDisplayName();
        if (propertyModel instanceof Branch) {
            Branch branch = (Branch) propertyModel;
            if (branch.isPropertyValueNotNull() || !branch.isPropertyNullable()) {
                if (branch.getPropertyType().isList()) {
                    angularTreeNode.data.createAllowed = true;
                    angularTreeNode.children = listBranch2AngularTreeNodes(angularTreeNode, branch);
                    angularTreeNode.leaf = angularTreeNode.children.length == 0;
                } else {
                    angularTreeNode.children = branch2AngularTreeNodes(angularTreeNode, branch);
                    angularTreeNode.data.deleteAllowed = true;
                    angularTreeNode.abstractPropertyModel = propertyModel;
                }
            } else {
                angularTreeNode.leaf = true;
                angularTreeNode.data.createAllowed = true;
            }
        } else if (propertyModel instanceof Leaf) {
            PropertyEditorSelector propertyEditorSelector = ((Leaf) propertyModel).getPropertyEditorSelector();
            if(propertyModel.isPropertyValueNotNull()) {
                angularTreeNode.data.value = propertyEditorSelector.convertToAngular(propertyModel.getPropertyValue());
            }
            angularTreeNode.data.propertyEditorSelector = propertyEditorSelector.getSelector();
            angularTreeNode.data.nullable = propertyModel.isPropertyNullable();
            angularTreeNode.leaf = true;
        } else {
            throw new IllegalStateException("Unknown propertyModel type: " + propertyModel);
        }
        return angularTreeNode;
    }

    /**
     * This methodes is ued to force the PrimeNG TreeTable update
     *
     * @param gwtAngularPropertyTable containes the Angular PrimeNG root TreeNodes
     */
    private void rootTreeNodes(GwtAngularPropertyTable gwtAngularPropertyTable) {
        gwtAngularPropertyTable.rootTreeNodes = Arrays.stream(gwtAngularPropertyTable.rootTreeNodes).toArray(AngularTreeNode[]::new);
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
