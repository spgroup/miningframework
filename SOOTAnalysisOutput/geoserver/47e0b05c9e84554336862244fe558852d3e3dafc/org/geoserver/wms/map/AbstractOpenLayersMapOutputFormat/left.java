package org.geoserver.wms.map;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geoserver.ows.LocalPublished;
import org.geoserver.ows.LocalWorkspace;
import org.geoserver.ows.URLMangler.URLType;
import org.geoserver.ows.util.ResponseUtils;
import org.geoserver.platform.ServiceException;
import org.geoserver.template.TemplateUtils;
import org.geoserver.wms.*;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.*;
import org.geotools.referencing.CRS;
import org.geotools.referencing.CRS.AxisOrder;
import org.geotools.renderer.crs.ProjectionHandler;
import org.geotools.renderer.crs.ProjectionHandlerFinder;
import org.geotools.renderer.crs.WrappingProjectionHandler;
import org.geotools.util.logging.Logging;
import org.opengis.feature.type.FeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public abstract class AbstractOpenLayersMapOutputFormat implements GetMapOutputFormat {

    protected static final Logger LOGGER = Logging.getLogger(AbstractOpenLayersMapOutputFormat.class);

    static MapProducerCapabilities CAPABILITIES = new MapProducerCapabilities(true, false, true, true, null);

    private static final Set<String> ignoredParameters;

    static {
        ignoredParameters = new HashSet<String>();
        ignoredParameters.add("REQUEST");
        ignoredParameters.add("TILED");
        ignoredParameters.add("BBOX");
        ignoredParameters.add("SERVICE");
        ignoredParameters.add("VERSION");
        ignoredParameters.add("FORMAT");
        ignoredParameters.add("WIDTH");
        ignoredParameters.add("HEIGHT");
        ignoredParameters.add("SRS");
    }

    private static Configuration cfg;

    static {
        cfg = TemplateUtils.getSafeConfiguration();
        cfg.setClassForTemplateLoading(AbstractOpenLayersMapOutputFormat.class, "");
        BeansWrapper bw = new BeansWrapper();
        bw.setExposureLevel(BeansWrapper.EXPOSE_PROPERTIES_ONLY);
        cfg.setObjectWrapper(bw);
    }

    private WMS wms;

    public AbstractOpenLayersMapOutputFormat(WMS wms) {
        this.wms = wms;
    }

    public RawMap produceMap(WMSMapContent mapContent) throws ServiceException, IOException {
        try {
            String templateName = getTemplateName(mapContent);
            Template template = cfg.getTemplate(templateName);
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("context", mapContent);
            boolean hasOnlyCoverages = hasOnlyCoverages(mapContent);
            map.put("pureCoverage", hasOnlyCoverages);
            map.put("supportsFiltering", supportsFiltering(mapContent));
            map.put("styles", styleNames(mapContent));
            GetMapRequest request = mapContent.getRequest();
            map.put("request", request);
            map.put("yx", String.valueOf(isWms13FlippedCRS(request.getCrs())));
            map.put("maxResolution", new Double(getMaxResolution(mapContent.getRenderingArea())));
            ProjectionHandler handler = null;
            try {
                handler = ProjectionHandlerFinder.getHandler(new ReferencedEnvelope(request.getCrs()), request.getCrs(), wms.isContinuousMapWrappingEnabled());
            } catch (MismatchedDimensionException e) {
                LOGGER.log(Level.FINER, e.getMessage(), e);
            } catch (FactoryException e) {
                LOGGER.log(Level.FINER, e.getMessage(), e);
            }
            map.put("global", String.valueOf(handler != null && handler instanceof WrappingProjectionHandler));
            String baseUrl = ResponseUtils.buildURL(request.getBaseUrl(), "/", null, URLType.RESOURCE);
            String queryString = null;
            if (baseUrl.indexOf("?") > 0) {
                int idx = baseUrl.indexOf("?");
                queryString = baseUrl.substring(idx);
                baseUrl = baseUrl.substring(0, idx);
            }
            map.put("baseUrl", canonicUrl(baseUrl));
            String servicePath = "wms";
            if (LocalPublished.get() != null) {
                servicePath = LocalPublished.get().getName() + "/" + servicePath;
            }
            if (LocalWorkspace.get() != null) {
                servicePath = LocalWorkspace.get().getName() + "/" + servicePath;
            }
            if (queryString != null) {
                servicePath += queryString;
            }
            map.put("servicePath", servicePath);
            map.put("parameters", getLayerParameter(request.getRawKvp()));
            map.put("units", getUnits(mapContent));
            if (mapContent.layers().size() == 1) {
                map.put("layerName", mapContent.layers().get(0).getTitle());
            } else {
                map.put("layerName", "Geoserver layers");
            }
            template.setOutputEncoding("UTF-8");
            ByteArrayOutputStream buff = new ByteArrayOutputStream();
            template.process(map, new OutputStreamWriter(buff, Charset.forName("UTF-8")));
            RawMap result = new RawMap(mapContent, buff, getMimeType());
            return result;
        } catch (TemplateException e) {
            throw new ServiceException(e);
        }
    }

    protected abstract String getUnits(WMSMapContent mapContent);

    protected abstract String getTemplateName(WMSMapContent mapContent);

    private boolean isWms13FlippedCRS(CoordinateReferenceSystem crs) {
        try {
            String code = CRS.lookupIdentifier(crs, false);
            if (!code.contains("EPSG:")) {
                code = "EPGS:" + code;
            }
            code = WMS.toInternalSRS(code, WMS.version("1.3.0"));
            CoordinateReferenceSystem crs13 = CRS.decode(code);
            return CRS.getAxisOrder(crs13) == AxisOrder.NORTH_EAST;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to determine CRS axis order, assuming is EN", e);
            return false;
        }
    }

    private boolean hasOnlyCoverages(WMSMapContent mapContent) {
        for (Layer layer : mapContent.layers()) {
            FeatureType schema = layer.getFeatureSource().getSchema();
            boolean grid = schema.getName().getLocalPart().equals("GridCoverage") && schema.getDescriptor("geom") != null && schema.getDescriptor("grid") != null && !(layer instanceof WMSLayer) && !(layer instanceof WMTSMapLayer);
            if (!grid)
                return false;
        }
        return true;
    }

    private boolean supportsFiltering(WMSMapContent mapContent) {
        return mapContent.layers().stream().anyMatch(layer -> {
            if (layer instanceof FeatureLayer) {
                return true;
            }
            if (!(layer instanceof GridReaderLayer)) {
                return false;
            }
            GeneralParameterValue[] readParams = ((GridReaderLayer) layer).getParams();
            if (readParams == null || readParams.length == 0) {
                return false;
            }
            for (GeneralParameterValue readParam : readParams) {
                if (readParam.getDescriptor().getName().getCode().equalsIgnoreCase("FILTER")) {
                    return true;
                }
            }
            return false;
        });
    }

    private List<String> styleNames(WMSMapContent mapContent) {
        if (mapContent.layers().size() != 1 || mapContent.getRequest() == null)
            return Collections.emptyList();
        MapLayerInfo info = mapContent.getRequest().getLayers().get(0);
        return info.getOtherStyleNames();
    }

    private List<Map<String, String>> getLayerParameter(Map<String, String> rawKvp) {
        List<Map<String, String>> result = new ArrayList<>(rawKvp.size());
        boolean exceptionsFound = false;
        for (Map.Entry<String, String> en : rawKvp.entrySet()) {
            String paramName = en.getKey();
            exceptionsFound |= paramName.equalsIgnoreCase("exceptions");
            if (ignoredParameters.contains(paramName.toUpperCase())) {
                continue;
            }
            Map<String, String> map = new HashMap<>();
            map.put("name", paramName);
            map.put("value", en.getValue());
            result.add(map);
        }
        if (!exceptionsFound) {
            Map<String, String> map = new HashMap<>();
            map.put("name", "exceptions");
            map.put("value", "application/vnd.ogc.se_inimage");
            result.add(map);
        }
        return result;
    }

    private String canonicUrl(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        } else {
            return baseUrl;
        }
    }

    private double getMaxResolution(ReferencedEnvelope areaOfInterest) {
        double w = areaOfInterest.getWidth();
        double h = areaOfInterest.getHeight();
        return ((w > h) ? w : h) / 256;
    }

    public MapProducerCapabilities getCapabilities(String format) {
        return CAPABILITIES;
    }
}
