package org.broadleafcommerce.cms.admin.client.presenter.structure;

import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.rpc.RPCResponse;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.FilterBuilder;
import org.broadleafcommerce.cms.admin.client.datasource.structure.StructuredContentItemCriteriaListDataSourceFactory;
import org.broadleafcommerce.cms.admin.client.datasource.structure.StructuredContentTypeFormListDataSource;
import org.broadleafcommerce.cms.admin.client.view.structure.StructuredContentDisplay;
import org.broadleafcommerce.common.presentation.client.RuleType;
import org.broadleafcommerce.openadmin.client.BLCMain;
import org.broadleafcommerce.openadmin.client.translation.AdvancedCriteriaToMVELTranslator;
import org.broadleafcommerce.openadmin.client.translation.IncompatibleMVELTranslationException;
import org.broadleafcommerce.openadmin.client.view.dynamic.ItemBuilderDisplay;
import org.broadleafcommerce.openadmin.client.view.dynamic.form.FormOnlyView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class StructuredContentPresenterExtractor {

    private static Map<RuleType, String> MVELKEYWORDMAP = new HashMap<RuleType, String>();

    static {
        MVELKEYWORDMAP.put(RuleType.PRODUCT, "product");
        MVELKEYWORDMAP.put(RuleType.ORDER_ITEM, "discreteOrderItem");
        MVELKEYWORDMAP.put(RuleType.REQUEST, "request");
        MVELKEYWORDMAP.put(RuleType.CUSTOMER, "customer");
        MVELKEYWORDMAP.put(RuleType.TIME, "time");
    }

    private static final AdvancedCriteriaToMVELTranslator TRANSLATOR = new AdvancedCriteriaToMVELTranslator();

    protected StructuredContentPresenter presenter;

    protected List<ItemBuilderDisplay> removedItemQualifiers = new ArrayList<ItemBuilderDisplay>();

    public StructuredContentPresenterExtractor(StructuredContentPresenter presenter) {
        this.presenter = presenter;
    }

    protected StructuredContentDisplay getDisplay() {
        return presenter.getDisplay();
    }

    public void removeItemQualifer(final ItemBuilderDisplay builder) {
        if (builder.getRecord() != null) {
            presenter.getPresenterSequenceSetupManager().getDataSource("scItemCriteriaDS").removeData(builder.getRecord(), new DSCallback() {

                @Override
                public void execute(DSResponse response, Object rawData, DSRequest request) {
                    getDisplay().removeItemBuilder(builder);
                }
            });
        } else {
            getDisplay().removeItemBuilder(builder);
        }
    }

    protected void extractData(final Record selectedRecord, Map<String, Object> dirtyValues, String property, FilterBuilder filterBuilder, String keyWord) throws IncompatibleMVELTranslationException {
        setData(selectedRecord, property, TRANSLATOR.createMVEL(keyWord, filterBuilder.getCriteria(), filterBuilder.getDataSource()), dirtyValues);
    }

    protected void setData(Record record, String fieldName, Object value, Map<String, Object> dirtyValues) {
        String attr = record.getAttribute(fieldName);
        String val = value == null ? null : String.valueOf(value);
        if (attr != val && (attr == null || val == null || !attr.equals(val))) {
            record.setAttribute(fieldName, value);
            dirtyValues.put(fieldName, value);
        }
    }

    public void applyData(final Record selectedRecord) {
        try {
            final Map<String, Object> dirtyValues = new HashMap<String, Object>();
            extractData(selectedRecord, dirtyValues, StructuredContentRuleBasedPresenterInitializer.ATTRIBUTEMAP.get(RuleType.CUSTOMER), getDisplay().getCustomerFilterBuilder(), MVELKEYWORDMAP.get(RuleType.CUSTOMER));
            extractData(selectedRecord, dirtyValues, StructuredContentRuleBasedPresenterInitializer.ATTRIBUTEMAP.get(RuleType.PRODUCT), getDisplay().getProductFilterBuilder(), MVELKEYWORDMAP.get(RuleType.PRODUCT));
            extractData(selectedRecord, dirtyValues, StructuredContentRuleBasedPresenterInitializer.ATTRIBUTEMAP.get(RuleType.REQUEST), getDisplay().getRequestFilterBuilder(), MVELKEYWORDMAP.get(RuleType.REQUEST));
            extractData(selectedRecord, dirtyValues, StructuredContentRuleBasedPresenterInitializer.ATTRIBUTEMAP.get(RuleType.TIME), getDisplay().getTimeFilterBuilder(), MVELKEYWORDMAP.get(RuleType.TIME));
            extractQualifierData(null, true, dirtyValues);
            DSRequest requestProperties = new DSRequest();
            for (String key : dirtyValues.keySet()) {
                getDisplay().getDynamicFormDisplay().getFormOnlyDisplay().getForm().setValue(key, (String) dirtyValues.get(key));
            }
            getDisplay().getDynamicFormDisplay().getFormOnlyDisplay().getForm().saveData(new DSCallback() {

                @Override
                public void execute(DSResponse response, Object rawData, DSRequest request) {
                    if (response.getStatus() != RPCResponse.STATUS_FAILURE) {
                        final String newId = response.getAttribute("newId");
                        FormOnlyView legacyForm = (FormOnlyView) ((FormOnlyView) getDisplay().getDynamicFormDisplay().getFormOnlyDisplay()).getMember("contentTypeForm");
                        final DynamicForm form = legacyForm.getForm();
                        StructuredContentTypeFormListDataSource dataSource = (StructuredContentTypeFormListDataSource) form.getDataSource();
                        dataSource.setCustomCriteria(new String[] { "constructForm", newId });
                        form.saveData(new DSCallback() {

                            @Override
                            public void execute(DSResponse response, Object rawData, DSRequest request) {
                                if (response.getStatus() != RPCResponse.STATUS_FAILURE) {
                                    try {
                                        extractQualifierData(newId, false, dirtyValues);
                                        if (!presenter.currentStructuredContentId.equals(newId)) {
                                            Record myRecord = getDisplay().getListDisplay().getGrid().getResultSet().find("id", presenter.currentStructuredContentId);
                                            if (myRecord != null) {
                                                myRecord.setAttribute("id", newId);
                                                presenter.currentStructuredContentRecord = myRecord;
                                                presenter.currentStructuredContentId = newId;
                                            } else {
                                                String primaryKey = getDisplay().getListDisplay().getGrid().getDataSource().getPrimaryKeyFieldName();
                                                getDisplay().getListDisplay().getGrid().getDataSource().fetchData(new Criteria(primaryKey, newId), new DSCallback() {

                                                    @Override
                                                    public void execute(DSResponse response, Object rawData, DSRequest request) {
                                                        getDisplay().getListDisplay().getGrid().clearCriteria();
                                                        getDisplay().getListDisplay().getGrid().setData(response.getData());
                                                        getDisplay().getListDisplay().getGrid().selectRecord(0);
                                                    }
                                                });
                                                SC.say(BLCMain.getMessageManager().getString("criteriaDoesNotMatch"));
                                            }
                                        }
                                        getDisplay().getListDisplay().getGrid().selectRecord(getDisplay().getListDisplay().getGrid().getRecordIndex(presenter.currentStructuredContentRecord));
                                    } catch (IncompatibleMVELTranslationException e) {
                                        SC.warn(e.getMessage());
                                        java.util.logging.Logger.getLogger(getClass().toString()).log(Level.SEVERE, e.getMessage(), e);
                                    }
                                }
                            }
                        });
                    }
                }
            }, requestProperties);
        } catch (IncompatibleMVELTranslationException e) {
            SC.warn(e.getMessage());
            java.util.logging.Logger.getLogger(getClass().toString()).log(Level.SEVERE, e.getMessage(), e);
        }
    }

    protected void resetButtonState() {
        getDisplay().getDynamicFormDisplay().getSaveButton().disable();
        getDisplay().getDynamicFormDisplay().getRefreshButton().disable();
        getDisplay().getRulesSaveButton().disable();
        getDisplay().getRulesRefreshButton().disable();
    }

    protected void extractQualifierData(final String id, boolean isValidation, Map<String, Object> dirtyValues) throws IncompatibleMVELTranslationException {
        for (final ItemBuilderDisplay builder : getDisplay().getItemBuilderViews()) {
            if (builder.getDirty()) {
                String temper = builder.getItemQuantity().getValue().toString();
                Integer quantity = Integer.parseInt(temper);
                String mvel = TRANSLATOR.createMVEL(MVELKEYWORDMAP.get(RuleType.ORDER_ITEM), builder.getItemFilterBuilder().getCriteria(), builder.getItemFilterBuilder().getDataSource());
                if (!isValidation) {
                    if (builder.getRecord() != null) {
                        setData(builder.getRecord(), "quantity", quantity, dirtyValues);
                        setData(builder.getRecord(), "orderItemMatchRule", mvel, dirtyValues);
                        presenter.getPresenterSequenceSetupManager().getDataSource("scItemCriteriaDS").updateData(builder.getRecord(), new DSCallback() {

                            @Override
                            public void execute(DSResponse response, Object rawData, DSRequest request) {
                                builder.setDirty(false);
                                resetButtonState();
                            }
                        });
                    } else {
                        final Record temp = new Record();
                        temp.setAttribute("quantity", quantity);
                        temp.setAttribute("orderItemMatchRule", mvel);
                        temp.setAttribute("_type", new String[] { presenter.getPresenterSequenceSetupManager().getDataSource("scItemCriteriaDS").getDefaultNewEntityFullyQualifiedClassname() });
                        temp.setAttribute(StructuredContentItemCriteriaListDataSourceFactory.foreignKeyName, id);
                        temp.setAttribute("id", "");
                        presenter.getPresenterSequenceSetupManager().getDataSource("scItemCriteriaDS").setLinkedValue(id);
                        presenter.getPresenterSequenceSetupManager().getDataSource("scItemCriteriaDS").addData(temp, new DSCallback() {

                            @Override
                            public void execute(DSResponse response, Object rawData, DSRequest request) {
                                builder.setDirty(false);
                                builder.setRecord(temp);
                                resetButtonState();
                            }
                        });
                    }
                }
            }
        }
        for (ItemBuilderDisplay removedDisplay : removedItemQualifiers) {
            if (removedDisplay.getDirty() && !isValidation) {
                removeItemQualifer(removedDisplay);
            }
        }
        if (getDisplay().getItemBuilderViews().size() == 0) {
            resetButtonState();
        }
    }

    public List<ItemBuilderDisplay> getRemovedItemQualifiers() {
        return removedItemQualifiers;
    }

    public void setRemovedItemQualifiers(List<ItemBuilderDisplay> removedItemQualifiers) {
        this.removedItemQualifiers = removedItemQualifiers;
    }
}
