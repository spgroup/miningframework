package org.geoserver.wms.featureinfo;

import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;
import net.opengis.wfs.FeatureCollectionType;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.ows.Dispatcher;
import org.geoserver.platform.ServiceException;
import org.geoserver.template.DirectTemplateFeatureCollectionFactory;
import org.geoserver.template.FeatureWrapper;
import org.geoserver.template.GeoServerTemplateLoader;
import org.geoserver.template.TemplateUtils;
import org.geoserver.wms.GetFeatureInfoRequest;
import org.geoserver.wms.WMS;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;

public class HTMLFeatureInfoOutputFormat extends GetFeatureInfoOutputFormat {

    private static final String FORMAT = "text/html";

    private static Configuration templateConfig;

    private static DirectTemplateFeatureCollectionFactory tfcFactory = new DirectTemplateFeatureCollectionFactory();

    static {
        templateConfig = TemplateUtils.getSafeConfiguration();
        templateConfig.setObjectWrapper(new FeatureWrapper(tfcFactory) {

            @Override
            public TemplateModel wrap(Object object) throws TemplateModelException {
                if (object instanceof FeatureCollection) {
                    SimpleHash map = (SimpleHash) super.wrap(object);
                    map.put("request", Dispatcher.REQUEST.get().getKvp());
                    map.put("environment", new EnvironmentVariablesTemplateModel());
                    return map;
                }
                return super.wrap(object);
            }
        });
    }

    GeoServerTemplateLoader templateLoader;

    private WMS wms;

    public HTMLFeatureInfoOutputFormat(final WMS wms) {
        super(FORMAT);
        this.wms = wms;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void write(FeatureCollectionType results, GetFeatureInfoRequest request, OutputStream out) throws ServiceException, IOException {
        final Charset charSet = wms.getCharSet();
        final OutputStreamWriter osw = new OutputStreamWriter(out, charSet);
        try {
            Template header = null;
            Template footer = null;
            List<FeatureCollection> collections = results.getFeature();
            if (collections.size() == 1) {
                header = getTemplate(FeatureCollectionDecorator.getName(collections.get(0)), "header.ftl", charSet);
                footer = getTemplate(FeatureCollectionDecorator.getName(collections.get(0)), "footer.ftl", charSet);
            } else {
                header = getTemplate(null, "header.ftl", charSet);
                footer = getTemplate(null, "footer.ftl", charSet);
            }
            try {
                header.process(null, osw);
            } catch (TemplateException e) {
                String msg = "Error occured processing header template.";
                throw (IOException) new IOException(msg).initCause(e);
            }
            for (int i = 0; i < collections.size(); i++) {
                FeatureCollection fc = collections.get(i);
                if (fc != null && fc.size() > 0) {
                    Template content = null;
                    if (!(fc.getSchema() instanceof SimpleFeatureType)) {
                        content = getTemplate(FeatureCollectionDecorator.getName(fc), "complex_content.ftl", charSet);
                    }
                    if (content == null) {
                        content = getTemplate(FeatureCollectionDecorator.getName(fc), "content.ftl", charSet);
                    }
                    try {
                        content.process(fc, osw);
                    } catch (TemplateException e) {
                        String msg = "Error occured processing content template " + content.getName() + " for " + request.getQueryLayers().get(i).getName();
                        throw (IOException) new IOException(msg).initCause(e);
                    }
                }
            }
            if (footer != null) {
                try {
                    footer.process(null, osw);
                } catch (TemplateException e) {
                    String msg = "Error occured processing footer template.";
                    throw (IOException) new IOException(msg).initCause(e);
                }
            }
            osw.flush();
        } finally {
            tfcFactory.purge();
        }
    }

    Template getTemplate(Name name, String templateFileName, Charset charset) throws IOException {
        ResourceInfo ri = null;
        if (name != null) {
            ri = wms.getResourceInfo(name);
        }
        synchronized (templateConfig) {
            if (templateLoader == null) {
                templateLoader = new GeoServerTemplateLoader(getClass());
            }
            templateLoader.setResource(ri);
            templateConfig.setTemplateLoader(templateLoader);
            Template t = templateConfig.getTemplate(templateFileName);
            t.setEncoding(charset.name());
            return t;
        }
    }

    @Override
    public String getCharset() {
        return wms.getGeoServer().getSettings().getCharset();
    }
}
