package com.btxtech.client.editor.basemgmt;

import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.datatypes.AdditionUserInfo;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.shared.rest.ServerGameEngineControlProvider;
import com.btxtech.shared.rest.UserServiceProvider;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 12.03.2018.
 */
@Templated("BaseMgmtEditorPanel.html#baseMgmt")
public class BaseMgmtEditorPanel extends LeftSideBarContent {
    private Logger logger = Logger.getLogger(BaseMgmtEditorPanel.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private Caller<UserServiceProvider> userCaller;
    @Inject
    private Caller<ServerGameEngineControlProvider> engineCaller;
    @Inject
    @DataField
    private Button loadUserInfoButton;
    @Inject
    @Named("th")
    @DataField
    private HTMLElement idTh;
    @Inject
    @Named("th")
    @DataField
    private HTMLElement nameTh;
    @Inject
    @Named("th")
    @DataField
    private HTMLElement lastLoggedInTh;
    @Inject
    @DataField
    @ListContainer("tbody")
    private ListComponent<BaseMgmtModel, BaseMgmtWidget> baseTable;
    private HTMLElement sortColumn;
    private boolean sortDesc;

    @PostConstruct
    public void postConstruct() {
        DOMUtil.removeAllElementChildren(baseTable.getElement()); // Remove placeholder table row from template.
        baseTable.setValue(baseItemUiService.getBases().stream().map((PlayerBaseDto playerBaseDto) -> create(playerBaseDto, null)).collect(Collectors.toList()));
    }

    @EventHandler("loadUserInfoButton")
    private void onLoadUserInfoButtonClick(ClickEvent event) {
        loadUserInfoButton.setEnabled(false);
        userCaller.call((RemoteCallback<List<AdditionUserInfo>>) additionUserInfos -> {
                    baseTable.setValue(baseItemUiService.getBases().stream().map((PlayerBaseDto playerBaseDto) -> BaseMgmtEditorPanel.this.create(playerBaseDto, additionUserInfos)).collect(Collectors.toList()));
                    loadUserInfoButton.setEnabled(true);
                },
                (message, throwable) -> {
                    loadUserInfoButton.setEnabled(true);
                    logger.log(Level.SEVERE, "UserServiceProvider.additionUserInfo() failed: " + message, throwable);
                    return false;
                }).additionUserInfo();
    }

    @EventHandler("idTh")
    private void onIdThClick(ClickEvent event) {
        onThClicked(idTh);
    }

    @EventHandler("nameTh")
    private void onNameThClick(ClickEvent event) {
        onThClicked(nameTh);
    }

    @EventHandler("lastLoggedInTh")
    private void onLastLoggedInThClick(ClickEvent event) {
        onThClicked(lastLoggedInTh);
    }

    private void onThClicked(HTMLElement th) {
        removeSortSymbol(idTh);
        removeSortSymbol(nameTh);
        removeSortSymbol(lastLoggedInTh);
        if (sortColumn != th) {
            sortColumn = th;
            sortDesc = false;
            setSortSymbol(th);
        } else {
            sortDesc = !sortDesc;
            setSortSymbol(th);
        }
        sort();
    }

    private void removeSortSymbol(HTMLElement th) {
        if (th.getInnerHTML().lastIndexOf(" (") >= 0) {
            th.setInnerHTML(th.getInnerHTML().substring(0, th.getInnerHTML().lastIndexOf(" (")));
        }
    }

    private void setSortSymbol(HTMLElement th) {
        if (sortDesc) {
            th.setInnerHTML(th.getInnerHTML() + " (desc)");
        } else {
            th.setInnerHTML(th.getInnerHTML() + " (asc)");
        }
    }

    private void sort() {
        List<BaseMgmtModel> models = baseTable.getValue();
        baseTable.setValue(models.stream().sorted((o1, o2) -> {
            if (sortColumn == idTh) {
                return o1.compareId(o2, sortDesc);
            } else if (sortColumn == nameTh) {
                return o1.compareName(o2, sortDesc);
            } else if (sortColumn == lastLoggedInTh) {
                return o1.compareLastLoggedIn(o2, sortDesc);
            } else {
                return 0;
            }
        }).collect(Collectors.toList()));
    }

    private BaseMgmtModel create(PlayerBaseDto playerBaseDto, List<AdditionUserInfo> additionUserInfos) {
        BaseMgmtModel baseMgmtModel = new BaseMgmtModel();
        baseMgmtModel.setPlayerBase(playerBaseDto);
        baseMgmtModel.setKillCallback(engineCaller.call(ignore -> {
        }, exceptionHandler.restErrorHandler("ServerGameEngineControlProvider.deleteBase() failed: "))::deleteBase);
        if (additionUserInfos != null) {
            AdditionUserInfo additionUserInfo = find(playerBaseDto.getHumanPlayerId(), additionUserInfos);
            if (additionUserInfo != null) {
                baseMgmtModel.setLastLoggedIn(additionUserInfo.getLastLoggedIn());
            }
        }
        return baseMgmtModel;
    }

    private AdditionUserInfo find(HumanPlayerId humanPlayerId, List<AdditionUserInfo> additionUserInfos) {
        if (humanPlayerId == null) {
            return null;
        }
        return additionUserInfos.stream().filter(additionUserInfo -> additionUserInfo.getHumanPlayerId().equals(humanPlayerId)).findFirst().orElse(null);
    }


}
