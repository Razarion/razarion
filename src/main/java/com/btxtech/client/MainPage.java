package com.btxtech.client;

import com.btxtech.client.menu.Menu;
import com.btxtech.client.menu.MenuModel;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.utils.GradToRadConverter;
import com.btxtech.client.utils.RadToStringGradConverter;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
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
    @DataField
    private Canvas canvas = Canvas.createIfSupported();
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    @DataField
    private Menu menu;
    @Inject
    @AutoBound
    private DataBinder<MenuModel> dataBinder;
//    @Inject
//    @Bound
//    @DataField("bumpMapDepth")
//    private DoubleBox bumpMapDepth;
//    @Inject
//    @DataField("dumpShadowPositionButton")
//    private Button dumpShadowPositionButton;
    @Inject
    @Bound
    @DataField("showMonitor")
    private CheckBox showMonitor;
    @Inject
    @Bound
    @DataField("showDeepMap")
    private CheckBox showDeepMap;
    @Inject
    @Bound
    @DataField("wireMode")
    private CheckBox wireMode;

    @PostConstruct
    public void init() {
        try {
            if (canvas == null) {
                throw new IllegalStateException("Canvas is not supported");
            }
            gameCanvas.init(canvas);
            // dataBinder.setModel(menuModel);
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "MainPage init failed", throwable);
        }
    }

//    @EventHandler("dumpShadowPositionButton")
//    private void handleDumpShadowPositionButtonnClick(ClickEvent event) {
//        shadowing.testPrint();
//    }

}
