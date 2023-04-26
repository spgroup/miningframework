package org.activiti.rest.api.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.DeploymentQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.SecuredResource;
import org.activiti.rest.application.ActivitiRestServicesApplication;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

public class DeploymentCollectionResource extends SecuredResource {

    protected static final String DEPRECATED_API_DEPLOYMENT_SEGMENT = "deployment";

    Map<String, QueryProperty> allowedSortProperties = new HashMap<String, QueryProperty>();

    public DeploymentCollectionResource() {
        allowedSortProperties.put("id", DeploymentQueryProperty.DEPLOYMENT_ID);
        allowedSortProperties.put("name", DeploymentQueryProperty.DEPLOYMENT_NAME);
        allowedSortProperties.put("deployTime", DeploymentQueryProperty.DEPLOY_TIME);
    }

    @Get
    public DataResponse getDeployments() {
        if (authenticate() == false)
            return null;
        DeploymentQuery deploymentQuery = ActivitiUtil.getRepositoryService().createDeploymentQuery();
        Form query = getQuery();
        if (getQuery().getNames().contains("name")) {
            deploymentQuery.deploymentName(getQueryParameter("name", query));
        }
        if (getQuery().getNames().contains("nameLike")) {
            deploymentQuery.deploymentNameLike(getQueryParameter("nameLike", query));
        }
        if (getQuery().getNames().contains("category")) {
            deploymentQuery.deploymentCategory(getQueryParameter("category", query));
        }
        if (getQuery().getNames().contains("categoryNotEquals")) {
            deploymentQuery.deploymentCategoryNotEquals(getQueryParameter("categoryNotEquals", query));
        }
        DataResponse response = new DeploymentsPaginateList(this).paginateList(getQuery(), deploymentQuery, "id", allowedSortProperties);
        return response;
    }

    @Post
    public DeploymentResponse uploadDeployment(Representation entity) {
        try {
            if (authenticate() == false)
                return null;
            if (entity == null || entity.getMediaType() == null || !MediaType.MULTIPART_FORM_DATA.isCompatible(entity.getMediaType())) {
                throw new ActivitiIllegalArgumentException("The request should be of type" + MediaType.MULTIPART_FORM_DATA + ".");
            }
            RestletFileUpload upload = new RestletFileUpload(new DiskFileItemFactory());
            List<FileItem> items = upload.parseRepresentation(entity);
            FileItem uploadItem = null;
            for (FileItem fileItem : items) {
                if (fileItem.getName() != null) {
                    uploadItem = fileItem;
                }
            }
            if (uploadItem == null) {
                throw new ActivitiIllegalArgumentException("No file content was found in request body.");
            }
            DeploymentBuilder deploymentBuilder = ActivitiUtil.getRepositoryService().createDeployment();
            String fileName = uploadItem.getName();
            if (fileName.endsWith(".bpmn20.xml") || fileName.endsWith(".bpmn")) {
                deploymentBuilder.addInputStream(fileName, uploadItem.getInputStream());
            } else if (fileName.toLowerCase().endsWith(".bar") || fileName.toLowerCase().endsWith(".zip")) {
                deploymentBuilder.addZipInputStream(new ZipInputStream(uploadItem.getInputStream()));
            } else {
                throw new ActivitiIllegalArgumentException("File must be of type .bpmn20.xml, .bpmn, .bar or .zip");
            }
            deploymentBuilder.name(fileName);
            Deployment deployment = deploymentBuilder.deploy();
            setStatus(Status.SUCCESS_CREATED);
            return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory().createDeploymentResponse(this, deployment);
        } catch (Exception e) {
            if (e instanceof ActivitiException) {
                throw (ActivitiException) e;
            }
            throw new ActivitiException(e.getMessage(), e);
        }
    }
}
