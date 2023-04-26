package org.broadleafcommerce.cms.admin.client.presenter.pages;

import com.smartgwt.client.data.Record;
import org.broadleafcommerce.cms.admin.client.presenter.structure.FilterType;
import org.broadleafcommerce.cms.admin.client.presenter.RuleBasedPresenterInitializer;
import org.broadleafcommerce.cms.admin.client.view.pages.PagesDisplay;
import org.broadleafcommerce.openadmin.client.datasource.dynamic.DynamicEntityDataSource;

public class PagesRuleBasedPresenterInitializer extends RuleBasedPresenterInitializer<PagesPresenter, PagesDisplay> {

    public PagesRuleBasedPresenterInitializer(PagesPresenter presenter, DynamicEntityDataSource offerItemCriteriaDataSource, DynamicEntityDataSource orderItemDataSource) {
        this.presenter = presenter;
        this.offerItemCriteriaDataSource = offerItemCriteriaDataSource;
        this.orderItemDataSource = orderItemDataSource;
    }

    public void initSection(Record selectedRecord, boolean disabled) {
        initFilterBuilder(getDisplay().getCustomerFilterBuilder(), selectedRecord.getAttribute(ATTRIBUTEMAP.get(FilterType.CUSTOMER)));
        initFilterBuilder(getDisplay().getProductFilterBuilder(), selectedRecord.getAttribute(ATTRIBUTEMAP.get(FilterType.PRODUCT)));
        initFilterBuilder(getDisplay().getRequestFilterBuilder(), selectedRecord.getAttribute(ATTRIBUTEMAP.get(FilterType.REQUEST)));
        initFilterBuilder(getDisplay().getTimeFilterBuilder(), selectedRecord.getAttribute(ATTRIBUTEMAP.get(FilterType.TIME)));
        initItemQualifiers(selectedRecord, disabled);
    }

    @Override
    protected void bindItemBuilderEvents(org.broadleafcommerce.openadmin.client.view.dynamic.ItemBuilderDisplay display) {
        presenter.bindItemBuilderEvents(display);
    }
}
