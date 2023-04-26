package org.broadleafcommerce.admin.client.service;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class AppServices {

    public static final AdminCatalogServiceAsync CATALOG = GWT.create(AdminCatalogService.class);

    public static final AdminExporterServiceAsync EXPORT = GWT.create(AdminExporterService.class);

    static {
        ServiceDefTarget endpoint = (ServiceDefTarget) CATALOG;
        endpoint.setServiceEntryPoint("admin.catalog.service");
        ServiceDefTarget endpoint2 = (ServiceDefTarget) EXPORT;
        endpoint2.setServiceEntryPoint("admin.export.service");
    }
}
