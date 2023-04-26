package org.geoserver.wfs3.response;

import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.IOException;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.ows.LocalWorkspace;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.template.DirectTemplateFeatureCollectionFactory;
import org.geoserver.template.FeatureWrapper;
import org.geoserver.template.GeoServerTemplateLoader;

public class FreemarkerTemplateSupport {

    private static Configuration templateConfig = new Configuration();

    private final GeoServerResourceLoader resoureLoader;

    private final GeoServer geoServer;

    static DirectTemplateFeatureCollectionFactory FC_FACTORY = new DirectTemplateFeatureCollectionFactory();

    static {
        templateConfig = new Configuration();
        templateConfig.setObjectWrapper(new FeatureWrapper(FC_FACTORY));
    }

    public FreemarkerTemplateSupport(GeoServerResourceLoader loader, GeoServer geoServer) {
        this.resoureLoader = loader;
        this.geoServer = geoServer;
    }

    Template getTemplate(ResourceInfo resource, String templateName) throws IOException {
        GeoServerTemplateLoader templateLoader = new GeoServerTemplateLoader(getClass(), resoureLoader);
        if (resource != null) {
            templateLoader.setResource(resource);
        } else {
            WorkspaceInfo ws = LocalWorkspace.get();
            if (ws != null) {
                templateLoader.setWorkspace(ws);
            }
        }
        synchronized (templateConfig) {
            templateConfig.setTemplateLoader(templateLoader);
            Template t = templateConfig.getTemplate(templateName);
            t.setEncoding("UTF-8");
            return t;
        }
    }
}
