package com.btxtech.client.editor.server.quest;

import com.btxtech.client.editor.widgets.itemtype.basecount.BaseItemTypeCountWidget;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ValueListBox;
import org.jboss.errai.common.client.dom.CheckboxInput;
import org.jboss.errai.common.client.dom.NumberInput;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 07.08.2017.
 */
@Templated("ConditionConfigPropertyPanel.html#condition")
public class ConditionConfigPropertyPanel extends Composite {
    // private Logger logger = Logger.getLogger(ConditionConfigPropertyPanel.class.getName());
    @Inject
    @AutoBound
    private DataBinder<ComparisonConfig> dataBinder;
    @Inject
    @DataField
    private ValueListBox<ConditionTrigger> conditionTrigger;
    @Inject
    @Bound
    @DataField
    private NumberInput count;
    @Inject
    @DataField
    private TableRow countTr;
    @Inject
    @Bound
    @DataField
    private NumberInput time;
    @Inject
    @DataField
    private TableRow timeTr;
    @Inject
    // @Bound
    @DataField
    private CheckboxInput addExisting;
    @Inject
    @DataField
    private TableRow addExistingTr;
    @Inject
    @DataField
    private TableRow baseItemTypeCountTr;
    @Inject
    @DataField
    private BaseItemTypeCountWidget baseItemTypeCount;
    private ConditionConfig conditionConfig;
    private Consumer<ConditionConfig> creationDeletionCallback;

    public void init(ConditionConfig conditionConfig, Consumer<ConditionConfig> creationDeletionCallback) {
        this.creationDeletionCallback = creationDeletionCallback;
        if (conditionConfig != null) {
            this.conditionConfig = conditionConfig;
            if (conditionConfig.getComparisonConfig() != null) {
                dataBinder.setModel(conditionConfig.getComparisonConfig());
            }
            if (conditionConfig.getConditionTrigger() != null) {
                conditionTrigger.setValue(conditionConfig.getConditionTrigger());
            }
            setupFieldsForConditionTrigger(conditionConfig.getConditionTrigger());
        } else {
            hideAllConditionConfigFields();
        }
        conditionTrigger.setAcceptableValues(Arrays.asList(ConditionTrigger.values()));
        conditionTrigger.addValueChangeHandler(this::onConditionTriggerChanged);
    }

    private void onConditionTriggerChanged(ValueChangeEvent<ConditionTrigger> conditionTriggerValueChangeEvent) {
        if (conditionConfig == null) {
            conditionConfig = new ConditionConfig();
            creationDeletionCallback.accept(conditionConfig);
        }
        conditionConfig.setConditionTrigger(conditionTriggerValueChangeEvent.getValue());
        if (conditionTriggerValueChangeEvent.getValue() != null) {
            conditionConfig.setComparisonConfig(new ComparisonConfig());
        } else {
            conditionConfig.setComparisonConfig(null);
        }
        setupFieldsForConditionTrigger(conditionTriggerValueChangeEvent.getValue());
    }

    private void setupFieldsForConditionTrigger(ConditionTrigger conditionTrigger) {
        hideAllConditionConfigFields();
        if (conditionTrigger != null) {
            dataBinder.setModel(conditionConfig.getComparisonConfig());
            switch (conditionTrigger) {
                case SYNC_ITEM_KILLED:
                    baseItemTypeCountTr.getStyle().setProperty("display", "table-row");
                    countTr.getStyle().setProperty("display", "table-row");
                    baseItemTypeCount.init(conditionConfig.getComparisonConfig().getTypeCount(), itemTypeCount -> conditionConfig.getComparisonConfig().setTypeCount(itemTypeCount));
                    break;
                case HARVEST:
                    countTr.getStyle().setProperty("display", "table-row");
                    break;
                case SYNC_ITEM_CREATED:
                    baseItemTypeCountTr.getStyle().setProperty("display", "table-row");
                    countTr.getStyle().setProperty("display", "table-row");
                    break;
                case BASE_KILLED:
                    countTr.getStyle().setProperty("display", "table-row");
                    break;
                case SYNC_ITEM_POSITION:
                    break;
                case BOX_PICKED:
                    countTr.getStyle().setProperty("display", "table-row");
                    break;
                case INVENTORY_ITEM_PLACED:
                    countTr.getStyle().setProperty("display", "table-row");
                    break;
                default:
                    throw new IllegalArgumentException("Unknown ConditionTrigger: " + conditionTrigger);
            }
        }
    }

    private void hideAllConditionConfigFields() {
        countTr.getStyle().setProperty("display", "none");
        timeTr.getStyle().setProperty("display", "none");
        addExistingTr.getStyle().setProperty("display", "none");
        baseItemTypeCountTr.getStyle().setProperty("display", "none");
    }
}
