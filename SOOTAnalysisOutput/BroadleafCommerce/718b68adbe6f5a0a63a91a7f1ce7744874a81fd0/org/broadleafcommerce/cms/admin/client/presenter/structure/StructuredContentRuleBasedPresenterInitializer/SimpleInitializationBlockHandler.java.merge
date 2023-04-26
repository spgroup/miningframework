package org.broadleafcommerce.cms.admin.client.presenter.structure;

import org.broadleafcommerce.cms.admin.client.view.structure.StructuredContentDisplay;
import org.broadleafcommerce.common.presentation.client.RuleType;
import org.broadleafcommerce.openadmin.client.BLCMain;
import org.broadleafcommerce.openadmin.client.datasource.dynamic.DynamicEntityDataSource;
import org.broadleafcommerce.openadmin.client.rules.RuleBasedPresenterInitializer;
import org.broadleafcommerce.openadmin.client.translation.IncompatibleMVELTranslationException;
import org.broadleafcommerce.openadmin.client.view.dynamic.ItemBuilderDisplay;
import com.smartgwt.client.data.AdvancedCriteria;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

public class StructuredContentRuleBasedPresenterInitializer extends RuleBasedPresenterInitializer<StructuredContentPresenter, StructuredContentDisplay> {

    protected DynamicEntityDataSource offerItemCriteriaDataSource;

    protected DynamicEntityDataSource orderItemDataSource;

    static {
        ATTRIBUTEMAP.put(RuleType.PRODUCT, "productRule");
        ATTRIBUTEMAP.put(RuleType.REQUEST, "requestRule");
        ATTRIBUTEMAP.put(RuleType.CUSTOMER, "customerRule");
        ATTRIBUTEMAP.put(RuleType.TIME, "timeRule");
        ATTRIBUTEMAP.put(RuleType.LOCALE, "localeRule");
    }

    public StructuredContentRuleBasedPresenterInitializer(StructuredContentPresenter presenter, DynamicEntityDataSource offerItemCriteriaDataSource, DynamicEntityDataSource orderItemDataSource) {
        this.presenter = presenter;
        this.offerItemCriteriaDataSource = offerItemCriteriaDataSource;
        this.orderItemDataSource = orderItemDataSource;
    }

    public void initSection(Record selectedRecord, boolean disabled) {
        initFilterBuilder(getDisplay().getCustomerFilterBuilder(), selectedRecord.getAttribute(ATTRIBUTEMAP.get(RuleType.CUSTOMER)));
        initFilterBuilder(getDisplay().getProductFilterBuilder(), selectedRecord.getAttribute(ATTRIBUTEMAP.get(RuleType.PRODUCT)));
        initFilterBuilder(getDisplay().getRequestFilterBuilder(), selectedRecord.getAttribute(ATTRIBUTEMAP.get(RuleType.REQUEST)));
        initFilterBuilder(getDisplay().getTimeFilterBuilder(), selectedRecord.getAttribute(ATTRIBUTEMAP.get(RuleType.TIME)));
        initItemQualifiers(selectedRecord, disabled);
    }

    protected void bindItemBuilderEvents(org.broadleafcommerce.openadmin.client.view.dynamic.ItemBuilderDisplay display) {
        presenter.bindItemBuilderEvents(display);
    }

    public void initItemQualifiers(final Record selectedRecord, final boolean disabled) {
        Criteria relationshipCriteria = offerItemCriteriaDataSource.createRelationshipCriteria(offerItemCriteriaDataSource.getPrimaryKeyValue(selectedRecord));
        offerItemCriteriaDataSource.fetchData(relationshipCriteria, new DSCallback() {

            public void execute(DSResponse response, Object rawData, DSRequest request) {
                getDisplay().removeAllItemBuilders();
                for (Record record : response.getData()) {
                    if (Integer.parseInt(record.getAttribute("quantity")) > 0) {
                        final ItemBuilderDisplay display = getDisplay().addItemBuilder(orderItemDataSource);
                        display.setDirty(false);
                        if (disabled) {
                            display.disable();
                        }
                        bindItemBuilderEvents(display);
                        display.getItemFilterBuilder().clearCriteria();
                        display.setRecord(record);
                        display.getItemQuantity().setValue(Integer.parseInt(record.getAttribute("quantity")));
                        try {
                            display.getItemFilterBuilder().setVisible(true);
                            display.getRawItemForm().setVisible(false);
                            AdvancedCriteria myCriteria = TRANSLATOR.createAdvancedCriteria(record.getAttribute("orderItemMatchRule"), orderItemDataSource);
                            if (myCriteria != null) {
                                display.getItemFilterBuilder().setCriteria(myCriteria);
                            }
                        } catch (IncompatibleMVELTranslationException e) {
                            throw new RuntimeException(BLCMain.getMessageManager().getString("mvelTranslationProblem"), e);
                        }
                        display.getRemoveButton().addClickHandler(new ClickHandler() {

                            public void onClick(ClickEvent event) {
                                getDisplay().removeItemBuilder(display);
                            }
                        });
                    }
                }
            }
        });
    }
}