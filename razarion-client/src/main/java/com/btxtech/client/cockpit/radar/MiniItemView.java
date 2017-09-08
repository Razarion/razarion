package com.btxtech.client.cockpit.radar;

import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.ItemUiService;
import elemental.html.CanvasRenderingContext2D;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * on 08.09.2017.
 */
@Dependent
public class MiniItemView extends AbstractGameCoordinates {
    private static final int REDRAW_TIME = 2000;
    // private Logger logger = Logger.getLogger(MiniItemView.class.getName());
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private ItemUiService itemUiService;
    @Inject
    private SimpleExecutorService simpleExecutorService;
    private SimpleScheduledFuture simpleScheduledFuture;


    @Override
    protected void draw(CanvasRenderingContext2D ctx) {
        Rectangle2D rectangle2D = getVisibleField();
        double width = toCanvasPixel(1.0 * getZoom());
        for (SyncBaseItemSimpleDto syncBaseItemSimpleDto : baseItemUiService.getSyncBaseItems()) {
            if (!rectangle2D.contains(syncBaseItemSimpleDto.getPosition2d())) {
                continue;
            }
            getCtx().setFillStyle(itemUiService.color4SyncItem(syncBaseItemSimpleDto).toHtmlColor());
            getCtx().fillRect((float) syncBaseItemSimpleDto.getPosition2d().getX(), (float) syncBaseItemSimpleDto.getPosition2d().getY(), (float) width, (float) width);
        }
    }

    public void startUpdater() {
        stopUpdater();
        simpleExecutorService.scheduleAtFixedRate(REDRAW_TIME, true, this::update, SimpleExecutorService.Type.UNSPECIFIED);
    }

    public void stopUpdater() {
        if (simpleScheduledFuture != null) {
            simpleScheduledFuture.cancel();
            simpleScheduledFuture = null;
        }
    }
}
