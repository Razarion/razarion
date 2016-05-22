package com.btxtech.client;

import com.btxtech.client.menu.Menu;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.sidebar.LeftSideBar;
import com.btxtech.client.system.boot.ClientRunner;
import com.btxtech.client.system.boot.GameStartupSeq;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 31.08.2015.
 */
@Page(role = DefaultPage.class)
@Templated("MainPage.html#app-template")
public class MainPage extends Composite {
    private Logger logger = Logger.getLogger(MainPage.class.getName());
    @Inject
    private ClientRunner clientRunner;
    @Inject
    private LeftSideBar leftSideBar;
    @DataField
    private Canvas canvas = Canvas.createIfSupported();
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    @DataField
    private Menu menu;
    @Inject
    @DataField
    private SideBarPanel sideBarPanel;

    @PostConstruct
    public void init() {
        try {
            if (canvas == null) {
                throw new IllegalStateException("Canvas is not supported");
            }
            gameCanvas.setCanvas(canvas);
            leftSideBar.setSideBarPanel(sideBarPanel);
            clientRunner.guiErrayReady(GameStartupSeq.COLD_SIMULATED);
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "MainPage init failed", throwable);
        }
        logger.severe("MainPage init() called twice?");
    }

}
