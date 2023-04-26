package org.geoserver.wms.featureinfo;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.geoserver.template.FeatureWrapper;
import org.geoserver.template.GeoServerTemplateLoader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class FeatureTemplate {

    static Configuration templateConfig;

    static {
        templateConfig = new Configuration();
        templateConfig.setObjectWrapper(new FeatureWrapper());
        templateConfig.setDateFormat("MM/dd/yyyy");
        templateConfig.setDateTimeFormat("MM/dd/yyyy HH:mm:ss");
        templateConfig.setTimeFormat("HH:mm:ss");
        templateConfig.setLocale(Locale.US);
        templateConfig.setNumberFormat("0.###########");
    }

    public static String DATE_FORMAT_PATTERN = "MM/dd/yy";

    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_PATTERN);

    public static String DATETIME_FORMAT_PATTERN = "MM/dd/yy HH:mm:ss";

    public static SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat(DATETIME_FORMAT_PATTERN);

    public static String TIME_FORMAT_PATTERN = "HH:mm:ss";

    public static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat();

    Map templateCache = new HashMap();

    CharArrayWriter caw = new CharArrayWriter();

    public void title(SimpleFeature feature, OutputStream output) throws IOException {
        title(feature, new OutputStreamWriter(output, Charset.forName("UTF-8")));
    }

    public void link(SimpleFeature feature, OutputStream output) throws IOException {
        link(feature, new OutputStreamWriter(output, Charset.forName("UTF-8")));
    }

    public void description(SimpleFeature feature, OutputStream output) throws IOException {
        description(feature, new OutputStreamWriter(output, Charset.forName("UTF-8")));
    }

    public void title(SimpleFeature feature, Writer writer) throws IOException {
        execute(feature, feature.getFeatureType(), writer, "title.ftl", null);
    }

    public void link(SimpleFeature feature, Writer writer) throws IOException {
        execute(feature, feature.getFeatureType(), writer, "link.ftl", null);
    }

    public void description(SimpleFeature feature, Writer writer) throws IOException {
        execute(feature, feature.getFeatureType(), writer, "description.ftl", null);
    }

    public String title(SimpleFeature feature) throws IOException {
        caw.reset();
        title(feature, caw);
        return caw.toString();
    }

    public String link(SimpleFeature feature) throws IOException {
        caw.reset();
        link(feature, caw);
        return caw.toString();
    }

    public String description(SimpleFeature feature) throws IOException {
        caw.reset();
        description(feature, caw);
        return caw.toString();
    }

    public void template(SimpleFeature feature, Writer writer, String template, Class lookup) throws IOException {
        execute(feature, feature.getFeatureType(), writer, template, lookup);
    }

    public void template(SimpleFeature feature, OutputStream output, String template, Class lookup) throws IOException {
        template(feature, new OutputStreamWriter(output), template, lookup);
    }

    public String template(SimpleFeature feature, String template, Class lookup) throws IOException {
        caw.reset();
        template(feature, caw, template, lookup);
        return caw.toString();
    }

    private void execute(Object feature, SimpleFeatureType featureType, Writer writer, String template, Class lookup) throws IOException {
        Template t = null;
        t = lookupTemplate(featureType, template, lookup);
        try {
            t.process(feature, writer);
        } catch (TemplateException e) {
            String msg = "Error occured processing template.";
            throw (IOException) new IOException(msg).initCause(e);
        }
    }

    private Template lookupTemplate(SimpleFeatureType featureType, String template, Class lookup) throws IOException {
        Template t;
        TemplateKey key = new TemplateKey(featureType, template);
        t = (Template) templateCache.get(key);
        if (t != null)
            return t;
        GeoServerTemplateLoader templateLoader = new GeoServerTemplateLoader(lookup != null ? lookup : getClass());
        templateLoader.setFeatureType(featureType);
        synchronized (templateConfig) {
            templateConfig.setTemplateLoader(templateLoader);
            t = templateConfig.getTemplate(template);
            t.setEncoding("UTF-8");
        }
        templateCache.put(key, t);
        return t;
    }

    public boolean isTemplateEmpty(SimpleFeatureType featureType, String template, Class<FeatureTemplate> lookup, String defaultContent) throws IOException {
        Template t = lookupTemplate(featureType, template, lookup);
        if (t == null) {
            return true;
        }
        StringWriter sw = new StringWriter();
        t.dump(sw);
        String templateText = sw.toString();
        return "".equals(templateText) || (defaultContent != null && defaultContent.equals(templateText));
    }

    private static class TemplateKey {

        SimpleFeatureType type;

        String template;

        public TemplateKey(SimpleFeatureType type, String template) {
            super();
            this.type = type;
            this.template = template;
        }

        public int hashCode() {
            final int PRIME = 31;
            int result = 1;
            result = PRIME * result + ((template == null) ? 0 : template.hashCode());
            result = PRIME * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }

        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final TemplateKey other = (TemplateKey) obj;
            if (template == null) {
                if (other.template != null)
                    return false;
            } else if (!template.equals(other.template))
                return false;
            if (type == null) {
                if (other.type != null)
                    return false;
            } else if (!type.equals(other.type))
                return false;
            return true;
        }
    }
}
