package com.btxtech.client.editor.generic;

import com.btxtech.client.editor.generic.model.AbstractPropertyModel;
import com.btxtech.client.editor.generic.model.Branch;
import com.btxtech.client.editor.generic.model.GenericPropertyInfoProvider;
import com.btxtech.client.editor.generic.model.Leaf;
import com.btxtech.client.editor.generic.model.PropertyEditorSelector;
import com.btxtech.client.editor.generic.updater.EngineUpdater;
import com.btxtech.shared.datatypes.shape.Shape3DComposite;
import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;
import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import com.btxtech.shared.rest.CrudController;
import com.btxtech.shared.rest.Shape3DEditorController;
import elemental2.core.Array;
import elemental2.promise.Promise;
import jsinterop.annotations.JsType;
import jsinterop.base.Any;
import jsinterop.base.Js;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.databinding.client.BindableProxyFactory;
import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.databinding.client.PropertyType;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@JsType
@ApplicationScoped
public class GenericEditorFrontendProvider {
    @Inject
    private Instance<Branch> branchInstance;
    @Inject
    private GenericPropertyInfoProvider genericPropertyInfoProvider;
    @Inject
    private EngineUpdater engineUpdater;
    @Inject
    private Caller<Shape3DEditorController> shape3DEditorController;
    private Logger logger = Logger.getLogger(GenericEditorFrontendProvider.class.getName());

    @SuppressWarnings("unused") // Called by Angular
    public Array<String> collectionNames() {
        genericPropertyInfoProvider.load();
        Array<String> crudControllers = new Array<>();
        Arrays.stream(CollectionReferenceType.values())
                .filter(collectionReferenceType -> collectionReferenceType.getCrudControllerClass() != null)
                .forEach(crudControllerEntry -> crudControllers.push(crudControllerEntry.getCollectionName()));
        return crudControllers;
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<ObjectNameId[]> requestObjectNameIds(String collectionName) {
        return new Promise<>((resolve, reject) -> {
            CollectionReferenceType collectionReferenceType = CollectionReferenceType.getType4CollectionName(collectionName);
            MessageBuilder.createCall(
                    (RemoteCallback<List<ObjectNameId>>) response -> resolve.onInvoke(response.toArray(new ObjectNameId[0])),
                    (message, throwable) -> {
                        logger.log(Level.SEVERE, "CrudController.getObjectNameIds() " + collectionReferenceType.getCrudControllerClass() + "\n" + message, throwable);
                        reject.onInvoke(throwable.getMessage());
                        return false;
                    },
                    collectionReferenceType.getCrudControllerClass()).getObjectNameIds();
        });
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<ObjectNameId> requestObjectNameId(String collectionName, int configId) {
        return new Promise<>((resolve, reject) -> {
            CollectionReferenceType collectionReferenceType = CollectionReferenceType.getType4CollectionName(collectionName);
            MessageBuilder.createCall(
                    (RemoteCallback<ObjectNameId>) resolve::onInvoke,
                    (message, throwable) -> {
                        logger.log(Level.SEVERE, "CrudController.getObjectNameId() " + collectionReferenceType.getCrudControllerClass() + " id:" + configId + "\n" + message, throwable);
                        reject.onInvoke(throwable.getMessage());
                        return false;
                    },
                    collectionReferenceType.getCrudControllerClass()).getObjectNameId(configId);
        });
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<GwtAngularPropertyTable> createConfig(String collectionName) {
        CollectionReferenceType collectionReferenceType = CollectionReferenceType.getType4CollectionName(collectionName);

        return new Promise<>((resolve, reject) -> MessageBuilder.createCall(
                (RemoteCallback<Config>) config -> config2GwtAngularPropertyTableAndConnect(config, collectionReferenceType.getCrudControllerClass(), config.getId(), resolve, reject),
                (message, throwable) -> {
                    logger.log(Level.SEVERE, "CrudController.create() " + collectionReferenceType.getCrudControllerClass() + "\n" + message, throwable);
                    reject.onInvoke(throwable.getMessage());
                    return false;
                },
                collectionReferenceType.getCrudControllerClass()).create());
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<GwtAngularPropertyTable> readConfig(String collectionName, int configId) {
        CollectionReferenceType collectionReferenceType = CollectionReferenceType.getType4CollectionName(collectionName);
        return new Promise<>((resolve, reject) -> MessageBuilder.createCall(
                (RemoteCallback<Config>) config -> config2GwtAngularPropertyTableAndConnect(config, collectionReferenceType.getCrudControllerClass(), configId, resolve, reject),
                (message, throwable) -> {
                    logger.log(Level.SEVERE, "CrudController.readConfig() " + collectionReferenceType.getCrudControllerClass() + "\n" + "configId:" + configId + "\n" + message, throwable);
                    reject.onInvoke(throwable.getMessage());
                    return false;
                },
                collectionReferenceType.getCrudControllerClass()).read(configId));
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<Void> updateConfig(String collectionName, GwtAngularPropertyTable gwtAngularPropertyTable) {
        CollectionReferenceType collectionReferenceType = CollectionReferenceType.getType4CollectionName(collectionName);
        Config config = Js.cast(gwtAngularPropertyTable.rootBranch.getPropertyValue());

        return new Promise<>((resolve, reject) -> MessageBuilder.createCall(
                (RemoteCallback<Void>) ignore -> resolve.onInvoke((Void) null),
                (message, throwable) -> {
                    logger.log(Level.SEVERE, "CrudController.update() " + collectionReferenceType.getCrudControllerClass() + "\n" + "config:" + config + "\n" + message, throwable);
                    reject.onInvoke(throwable.getMessage());
                    return false;
                },
                collectionReferenceType.getCrudControllerClass()).update(Js.cast(config)));
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<GwtAngularPropertyTable> deleteConfig(String collectionName, GwtAngularPropertyTable gwtAngularPropertyTable) {
        CollectionReferenceType collectionReferenceType = CollectionReferenceType.getType4CollectionName(collectionName);

        return new Promise<>((resolve, reject) -> MessageBuilder.createCall(
                (RemoteCallback<GwtAngularPropertyTable>) resolve::onInvoke,
                (message, throwable) -> {
                    logger.log(Level.SEVERE, "CrudController.update() " + collectionReferenceType.getCrudControllerClass() + "\n" + "configId:" + gwtAngularPropertyTable.configId + "\n" + message, throwable);
                    reject.onInvoke(throwable.getMessage());
                    return false;
                },
                collectionReferenceType.getCrudControllerClass()).delete(gwtAngularPropertyTable.configId));
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<Void> colladaConvert(GwtAngularPropertyTable gwtAngularPropertyTable, String colladaString) {
        return new Promise<>((resolve, reject) -> {
            Shape3DConfig shape3DConfig = Js.cast(gwtAngularPropertyTable.rootBranch.getPropertyValue());
            shape3DConfig.colladaString(colladaString);
            shape3DEditorController.call((RemoteCallback<Shape3DComposite>) shape3DComposite -> {
                shape3DComposite.getShape3DConfig().setColladaString(colladaString);
                engineUpdater.onShape3D(shape3DComposite);
                config2GwtAngularPropertyTableAndConnect(shape3DComposite.getShape3DConfig(),
                        Shape3DEditorController.class,
                        gwtAngularPropertyTable.configId,
                        resolveUnionType -> {
                            resolve.onInvoke((Void) null);
                            gwtAngularPropertyTable.rootTreeNodes = resolveUnionType.asT().rootTreeNodes; // Needed in AngularPrimeNg
                            gwtAngularPropertyTable.rootBranch = resolveUnionType.asT().rootBranch; // Needed in AngularPrimeNg
                        }, reject);
            }, (message, throwable) -> {
                shape3DConfig.colladaString(null);
                logger.log(Level.SEVERE, "Shape3DEditorController.colladaConvert() failed: " + message, throwable);
                reject.onInvoke(throwable.getMessage());
                return false;
            }).colladaConvert(shape3DConfig);
        });
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
        gwtAngularPropertyTable.rootBranch = branch;
        gwtAngularPropertyTable.configId = configId;
        gwtAngularPropertyTable.rootTreeNodes = branch2AngularTreeNodes(null, branch, gwtAngularPropertyTable);
        return gwtAngularPropertyTable;
    }

    private AngularTreeNode[] branch2AngularTreeNodes(AngularTreeNode parent, Branch branch, GwtAngularPropertyTable gwtAngularPropertyTable) {
        List<AngularTreeNode> angularTreeNodes = new ArrayList<>();
        branch.createBindableChildren(childPropertyModel -> angularTreeNodes.add(propertyModel2AngularTreeNode(parent, childPropertyModel, gwtAngularPropertyTable)));
        return angularTreeNodes.toArray(new AngularTreeNode[0]);
    }

    private AngularTreeNode[] listBranch2AngularTreeNodes(AngularTreeNode parent, Branch branch, GwtAngularPropertyTable gwtAngularPropertyTable) {
        List<AngularTreeNode> listAngularTreeNodes = new ArrayList<>();
        branch.createListChildren(childListPropertyModel -> listAngularTreeNodes.add(propertyModel2AngularTreeNode(parent, childListPropertyModel, gwtAngularPropertyTable)));
        return listAngularTreeNodes.toArray(new AngularTreeNode[0]);
    }

    private AngularTreeNode propertyModel2AngularTreeNode(AngularTreeNode parent, AbstractPropertyModel propertyModel, GwtAngularPropertyTable gwtAngularPropertyTable) {
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
                            treeNodeArray.push(propertyModel2AngularTreeNode(angularTreeNode, child, gwtAngularPropertyTable));
                            angularTreeNode.expanded = true;
                        } else if(branch.getPropertyType().isBindable()) {
                            angularTreeNode.abstractPropertyModel.createAndSetPropertyValue();
                            angularTreeNode.children = branch2AngularTreeNodes(angularTreeNode, branch, gwtAngularPropertyTable);
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
                    angularTreeNode.abstractPropertyModel.setPropertyValue(null);
                    if (angularTreeNode.parent != null && angularTreeNode.parent.abstractPropertyModel.getPropertyType().isList()) {
                        angularTreeNode.parent.children = listBranch2AngularTreeNodes(angularTreeNode.parent, (Branch) angularTreeNode.parent.abstractPropertyModel, gwtAngularPropertyTable);
                        angularTreeNode.parent.leaf = angularTreeNode.parent.children.length == 0;
                    } else {
                        angularTreeNode.children = new AngularTreeNode[0];
                        angularTreeNode.data.createAllowed = true;
                        angularTreeNode.data.deleteAllowed = false;
                        angularTreeNode.leaf = true;
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
                        javaValue = PropertyEditorSelector.fromSelector(angularTreeNode.data.propertyEditorSelector).convertFromAngular(value,  angularTreeNode.abstractPropertyModel.getPropertyClass());
                    }
                    angularTreeNode.abstractPropertyModel.setPropertyValue(javaValue);
                    engineUpdater.handleSetValue(gwtAngularPropertyTable.rootBranch.getPropertyValue());
                } catch (Throwable throwable) {
                    logger.log(Level.SEVERE, "setValue failed", throwable);
                    throw throwable;
                }
            }
        };
        angularTreeNode.data.name = propertyModel.getDisplayName();
        if (propertyModel instanceof Branch) {
            angularTreeNode.data.canHaveChildren = true;
            Branch branch = (Branch) propertyModel;
            if (branch.isPropertyValueNotNull() || !branch.isPropertyNullable()) {
                if (branch.getPropertyType().isList()) {
                    angularTreeNode.data.createAllowed = true;
                    angularTreeNode.children = listBranch2AngularTreeNodes(angularTreeNode, branch, gwtAngularPropertyTable);
                    angularTreeNode.leaf = angularTreeNode.children.length == 0;
                } else {
                    angularTreeNode.children = branch2AngularTreeNodes(angularTreeNode, branch, gwtAngularPropertyTable);
                    angularTreeNode.data.deleteAllowed = true;
                    angularTreeNode.abstractPropertyModel = propertyModel;
                }
            } else {
                angularTreeNode.leaf = true;
                angularTreeNode.data.createAllowed = true;
            }
        } else if (propertyModel instanceof Leaf) {
            PropertyEditorSelector propertyEditorSelector = ((Leaf) propertyModel).getPropertyEditorSelector();
            if (propertyModel.isPropertyValueNotNull()) {
                angularTreeNode.data.value = propertyEditorSelector.convertToAngular(propertyModel.getPropertyValue());
            } else {
                angularTreeNode.data.value = propertyEditorSelector.convertNullToAngular();
            }
            angularTreeNode.data.options = propertyEditorSelector.angularOptions(propertyModel.getPropertyClass());
            angularTreeNode.data.propertyEditorSelector = propertyEditorSelector.getSelector();
            angularTreeNode.data.nullable = propertyModel.isPropertyNullable();
            angularTreeNode.leaf = true;
        } else {
            throw new IllegalStateException("Unknown propertyModel type: " + propertyModel);
        }
        return angularTreeNode;
    }

    /**
     * This method is ued to force the PrimeNG TreeTable update
     *
     * @param gwtAngularPropertyTable containes the Angular PrimeNG root TreeNodes
     */
    private void rootTreeNodes(GwtAngularPropertyTable gwtAngularPropertyTable) {
        gwtAngularPropertyTable.rootTreeNodes = Arrays.stream(gwtAngularPropertyTable.rootTreeNodes).toArray(AngularTreeNode[]::new);
    }
}
