package org.broadleafcommerce.openadmin.client.service;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class AppServices {

    public static final UploadProgressServiceAsync UPLOAD = GWT.create(UploadProgressService.class);

    public static final AdminSecurityServiceAsync SECURITY = GWT.create(AdminSecurityService.class);

    public static final DynamicEntityServiceAsync DYNAMIC_ENTITY = GWT.create(DynamicEntityService.class);

    public static final UtilityServiceAsync UTILITY = GWT.create(UtilityService.class);

    static {
        ServiceDefTarget endpoint = (ServiceDefTarget) DYNAMIC_ENTITY;
        endpoint.setServiceEntryPoint("dynamic.entity.service");
        ServiceDefTarget endpoint2 = (ServiceDefTarget) SECURITY;
        endpoint2.setServiceEntryPoint("security.service");
        ServiceDefTarget endpoint3 = (ServiceDefTarget) UPLOAD;
        endpoint3.setServiceEntryPoint("upload.progress.service");
        ServiceDefTarget endpoint4 = (ServiceDefTarget) UTILITY;
        endpoint4.setServiceEntryPoint("utility.service");
    }
}
