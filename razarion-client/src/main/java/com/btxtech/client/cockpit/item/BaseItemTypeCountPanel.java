package com.btxtech.client.cockpit.item;

import com.btxtech.shared.CommonUrl;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.TableCell;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Beat
 * 30.09.2016.
 */
@Templated("ClientOwnMultiDifferentItemPanel.html#selectedItemTd")
public class BaseItemTypeCountPanel implements TakesValue<BaseItemTypeCount>, IsElement {
    @Inject
    private SelectionHandler selectionHandler;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    @Named("td")
    private TableCell selectedItemTd;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Image image;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label countLabel;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button selectButton;
    private BaseItemTypeCount baseItemTypeCount;

    @Override
    public HTMLElement getElement() {
        return selectedItemTd;
    }

    @Override
    public void setValue(BaseItemTypeCount baseItemTypeCount) {
        this.baseItemTypeCount = baseItemTypeCount;
        image.setUrl(CommonUrl.getImageServiceUrlSafe(baseItemTypeCount.getBaseItemType().getThumbnail()));
        countLabel.setText(Integer.toString(baseItemTypeCount.getCount()));
        selectedItemTd.setTitle(I18nHelper.getConstants().tooltipSelect(I18nHelper.getLocalizedString(baseItemTypeCount.getBaseItemType().getI18nName())));
    }

    @Override
    public BaseItemTypeCount getValue() {
        return baseItemTypeCount;
    }

    @EventHandler("selectButton")
    private void selectButtonClick(ClickEvent event) {
        selectionHandler.keepOnlyOwnOfType(baseItemTypeCount.getBaseItemType());
    }
}
