package com.btxtech.client.editor.generic;

import com.btxtech.client.editor.generic.model.AbstractPropertyModel;
import com.btxtech.client.editor.generic.model.Branch;
import com.btxtech.client.editor.generic.model.GenericPropertyInfoProvider;
import com.btxtech.client.editor.generic.model.Leaf;
import com.btxtech.client.editor.generic.propertyeditors.AbstractPropertyEditor;
import com.btxtech.client.editor.generic.propertyeditors.ListEditor;
import com.btxtech.shared.datatypes.SingleHolder;
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
    private Logger logger = Logger.getLogger(GenericEditorFrontendProvider.class.getName());

    @SuppressWarnings("unused") // Called by Angular
    public Promise<ObjectNameId[]> requestConfigs(int crudControllerIndex) {
        return new Promise<>((resolve, reject) -> {
            CrudControllerEntry crudControllerEntry = CRUD_CONTROLLERS[crudControllerIndex];
            MessageBuilder.createCall(
                    (RemoteCallback<List<ObjectNameId>>) response -> resolve.onInvoke(response.toArray(new ObjectNameId[0])),
                    (message, throwable) -> {
                        logger.log(Level.SEVERE, "CrudController.loadObjectNameId() " + crudControllerEntry.crudControllerClass + "\n" + message, throwable);
                        reject.onInvoke("CrudController.loadObjectNameId() " + crudControllerEntry.crudControllerClass + "\n" + message + "\n" + throwable);
                        return false;
                    },
                    crudControllerEntry.crudControllerClass).getObjectNameIds();
        });
    }

    @SuppressWarnings("unused") // Called by Angular
    public Array<String> crudControllers() {
        genericPropertyInfoProvider.load();
        Array<String> crudControllers = new Array<>();
        Arrays.stream(CRUD_CONTROLLERS).forEach(crudControllerEntry -> crudControllers.push(crudControllerEntry.name));
        return crudControllers;
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<GwtAngularPropertyTable> readConfig(int crudControllerIndex, int configId) {
        CrudControllerEntry crudControllerEntry = CRUD_CONTROLLERS[crudControllerIndex];
        return new Promise<>((resolve, reject) -> MessageBuilder.createCall(
                (RemoteCallback<Config>) config -> config2GwtAngularPropertyTable(config, crudControllerEntry.crudControllerClass, configId, resolve, reject),
                (message, throwable) -> {
                    logger.log(Level.SEVERE, "CrudController.readConfig() " + crudControllerEntry.crudControllerClass + "\n" + "configId:" + configId + "\n" + message, throwable);
                    reject.onInvoke("CrudController.readConfig() " + crudControllerEntry.crudControllerClass + "\n" + "configId:" + configId + "\n" + message + "\n" + throwable);
                    return false;
                },
                crudControllerEntry.crudControllerClass).read(configId));
    }

    private void config2GwtAngularPropertyTable(Object config, Class<? extends CrudController<? extends Config>> crudControllerClass, int configId,
                                                Promise.PromiseExecutorCallbackFn.ResolveCallbackFn<GwtAngularPropertyTable> resolve,
                                                Promise.PromiseExecutorCallbackFn.RejectCallbackFn reject) {
        try {
            Branch branch = branchInstance.get();
            branch.init(null, null,
                    (HasProperties) BindableProxyFactory.getBindableProxy(config),
                    new PropertyType(config.getClass(), true, false),
                    null);
            resolve.onInvoke(branch2GwtAngularPropertyTable(branch));
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "configToAngularTreeNodes() failed. Config: " + config + "\n" + crudControllerClass + "\n" + "configId:" + configId, throwable);
            reject.onInvoke("configToAngularTreeNodes() failed. Config: " + config + "\n" + crudControllerClass + "\n" + "configId:" + configId + "\n" + throwable);
        }
    }

    private GwtAngularPropertyTable branch2GwtAngularPropertyTable(Branch branch) {
        GwtAngularPropertyTable gwtAngularPropertyTable = new GwtAngularPropertyTable();
        gwtAngularPropertyTable.rootTreeNodes = branch2AngularTreeNodes(branch);
        gwtAngularPropertyTable.rootBranch = branch;
        return gwtAngularPropertyTable;
    }

    private AngularTreeNode[] branch2AngularTreeNodes(Branch branch) {
        List<AngularTreeNode> angularTreeNodes = new ArrayList<>();
        branch.createBindableChildren(childPropertyModel -> angularTreeNodes.add(propertyModel2AngularTreeNode(childPropertyModel)));
        return angularTreeNodes.toArray(new AngularTreeNode[0]);
    }

    private AngularTreeNode propertyModel2AngularTreeNode(AbstractPropertyModel propertyModel) {
        AngularTreeNode angularTreeNode = new AngularTreeNode();
        final SingleHolder<Branch> listBranchHolder = new SingleHolder<>();
        angularTreeNode.data = new AngularTreeNodeData() {
            @Override
            public void onCreate(GwtAngularPropertyTable gwtAngularPropertyTable) {
                try {
                    if (listBranchHolder.isEmpty()) {
                        throw new IllegalStateException("Is not a list element");
                    }
                    AbstractPropertyModel child = listBranchHolder.getO().createListElement();
                    if (angularTreeNode.children == null) {
                        angularTreeNode.children = Js.cast(new Array<AngularTreeNode>());
                    }
                    Array<AngularTreeNode> treeNodeArray = Js.cast(angularTreeNode.children);
                    treeNodeArray.push(propertyModel2AngularTreeNode(child));
                    rootTreeNodes(gwtAngularPropertyTable);
                } catch (Throwable throwable) {
                    logger.log(Level.SEVERE, "onCreate failed", throwable);
                    throw throwable;
                }
            }
        };
        angularTreeNode.data.name = propertyModel.getDisplayName();
        if (propertyModel instanceof Branch) {
            Branch branch = (Branch) propertyModel;
            if (branch.isPropertyValueNotNull() || !branch.isPropertyNullable()) {
                Class<? extends AbstractPropertyEditor> editorClass = branch.getEditorClass();
                if (editorClass == ListEditor.class) {
                    angularTreeNode.data.createAllowed = true;
                    listBranchHolder.setO(branch);
                    List<AngularTreeNode> listAngularTreeNodes = new ArrayList<>();
                    branch.createListChildren(childListPropertyModel -> listAngularTreeNodes.add(propertyModel2AngularTreeNode(childListPropertyModel)));
                    if (listAngularTreeNodes.isEmpty()) {
                        angularTreeNode.leaf = true;
                    } else {
                        angularTreeNode.children = listAngularTreeNodes.toArray(new AngularTreeNode[0]);
                    }
                } else {
                    angularTreeNode.children = branch2AngularTreeNodes(branch);
                    angularTreeNode.data.deleteAllowed = true;
                }
            } else {
                angularTreeNode.leaf = true;
                angularTreeNode.data.createAllowed = true;
            }
        } else if (propertyModel instanceof Leaf) {
            angularTreeNode.data.value = Any.of(propertyModel.getPropertyValue());
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
