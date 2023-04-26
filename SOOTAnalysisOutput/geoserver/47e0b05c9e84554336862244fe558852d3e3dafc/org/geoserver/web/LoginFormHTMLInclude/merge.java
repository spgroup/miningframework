package org.geoserver.web;

import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.include.Include;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.template.TemplateUtils;
import org.geotools.util.logging.Logging;

public class LoginFormHTMLInclude extends Include {

    protected static final Logger LOGGER = Logging.getLogger(LoginFormHTMLInclude.class);

    private static final long serialVersionUID = 2413413223248385722L;

    private static final String DEFAULT_AUTOCOMPLETE_VALUE = "on";

    public static final String GEOSERVER_LOGIN_AUTOCOMPLETE = "geoserver.login.autocomplete";

    private static Configuration templateConfig;

    static {
        templateConfig = TemplateUtils.getSafeConfiguration();
    }

    private PackageResourceReference resourceReference;

    public LoginFormHTMLInclude(String id, PackageResourceReference packageResourceReference) {
        super(id);
        this.resourceReference = packageResourceReference;
    }

    @Override
    public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
        String content = importAsString();
        replaceComponentTagBody(markupStream, openTag, content);
    }

    @Override
    protected String importAsString() {
        if (resourceReference != null) {
            try {
                templateConfig.setClassForTemplateLoading(this.resourceReference.getScope(), "");
                Template template = templateConfig.getTemplate(this.resourceReference.getName());
                Map<String, Object> params = new HashMap<>();
                String autocompleteValue = GeoServerExtensions.getProperty(GEOSERVER_LOGIN_AUTOCOMPLETE);
                if (autocompleteValue == null) {
                    autocompleteValue = DEFAULT_AUTOCOMPLETE_VALUE;
                }
                params.put("autocomplete", autocompleteValue);
                StringWriter writer = new StringWriter();
                template.process(params, writer);
                return writer.toString();
            } catch (Exception ex) {
                LOGGER.log(Level.FINEST, "Problem reading resource contents.", ex);
            }
        }
        return "";
    }
}
