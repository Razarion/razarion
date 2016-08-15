package com.btxtech.client.editor;

import com.btxtech.client.TerrainKeyDownEvent;
import com.btxtech.client.editor.menu.Menu;
import com.google.gwt.user.client.ui.RootPanel;
import elemental.events.KeyboardEvent;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 09.07.2016.
 */
@Singleton
@Deprecated
public class Editor {
    @Inject
    private Instance<Menu> menuInstance;
    private boolean active;
    private Menu menu;

    public void show() {
        if (active) {
            return;
        }
        activate();
        active = true;
    }

    public void hide() {
        if (!active) {
            return;
        }
        deactivate();
        active = false;
    }

    public void onKeyEvent(@Observes TerrainKeyDownEvent terrainKeyDownEvent) {
        if (terrainKeyDownEvent.getKeyCode() == KeyboardEvent.KeyCode.INSERT) {
            if (active) {
                hide();
            } else {
                show();
            }
        }
    }

    private void activate() {
        menu = menuInstance.get();
        RootPanel.get().add(menu);
    }

    private void deactivate() {
        RootPanel.get().remove(menu);
        menu = null;

    }

}
