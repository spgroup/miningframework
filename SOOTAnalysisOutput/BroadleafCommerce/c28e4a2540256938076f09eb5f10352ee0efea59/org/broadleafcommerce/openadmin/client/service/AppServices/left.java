package org.broadleafcommerce.openadmin.client.service;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class AppServices {

    public static final UploadProgressServiceAsync UPLOAD = GWT.create(UploadProgressService.class);

    public static final AdminSecurityServiceAsync SECURITY = GWT.create(AdminSecurityService.class);

    public static final DynamicEntityServiceAsync DYNAMIC_ENTITY = GWT.create(DynamicEntityService.class);

    public static final UtilityServiceAsync UTILITY = GWT.create(UtilityService.class);

    public static final AppConfigurationServiceAsync APP_CONFIGURATION = GWT.create(AppConfigurationService.class);

    static {
        ServiceDefTarget endpoint = (ServiceDefTarget) DYNAMIC_ENTITY;
        endpoint.setServiceEntryPoint("admin/dynamic.entity.service");
        ServiceDefTarget endpoint2 = (ServiceDefTarget) SECURITY;
        endpoint2.setServiceEntryPoint("admin/security.service");
        ServiceDefTarget endpoint3 = (ServiceDefTarget) UPLOAD;
        endpoint3.setServiceEntryPoint("admin/upload.progress.service");
        ServiceDefTarget endpoint4 = (ServiceDefTarget) UTILITY;
        endpoint4.setServiceEntryPoint("admin/utility.service");
        ServiceDefTarget endpoint5 = (ServiceDefTarget) APP_CONFIGURATION;
        endpoint5.setServiceEntryPoint("admin/app.configuration.service");
    }
}
