package org.geoserver.catalog.rest;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogBuilder;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.rest.RestletException;
import org.geoserver.rest.format.DataFormat;
import org.geoserver.rest.format.MediaTypes;
import org.geotools.styling.Style;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

public class StyleResource extends AbstractCatalogResource {

    public static final MediaType MEDIATYPE_SLD = new MediaType("application/vnd.ogc.sld+xml");

    static {
        MediaTypes.registerExtension("sld", MEDIATYPE_SLD);
    }

    public StyleResource(Context context, Request request, Response response, Catalog catalog) {
        super(context, request, response, StyleInfo.class, catalog);
    }

    @Override
    protected List<DataFormat> createSupportedFormats(Request request, Response response) {
        List<DataFormat> formats = super.createSupportedFormats(request, response);
        formats.add(new SLDFormat());
        return formats;
    }

    @Override
    protected Object handleObjectGet() {
        String workspace = getAttribute("workspace");
        String style = getAttribute("style");
        LOGGER.fine("GET style " + style);
        StyleInfo sinfo = workspace == null ? catalog.getStyleByName(style) : catalog.getStyleByName(workspace, style);
        DataFormat format = getFormatGet();
        if (format instanceof SLDFormat) {
            try {
                return sinfo.getStyle();
            } catch (IOException e) {
                throw new RestletException("", Status.SERVER_ERROR_INTERNAL, e);
            }
        }
        return sinfo;
    }

    @Override
    public boolean allowPost() {
        if (getAttribute("workspace") == null && !isAuthenticatedAsAdmin()) {
            return false;
        }
        return getAttribute("style") == null;
    }

    @Override
    protected String handleObjectPost(Object object) throws Exception {
        String workspace = getAttribute("workspace");
        String layer = getAttribute("layer");
        if (object instanceof StyleInfo) {
            StyleInfo style = (StyleInfo) object;
            if (layer != null) {
                StyleInfo existing = catalog.getStyleByName(style.getName());
                if (existing == null) {
                    throw new RestletException("No such style: " + style.getName(), Status.CLIENT_ERROR_NOT_FOUND);
                }
                LayerInfo l = catalog.getLayerByName(layer);
                l.getStyles().add(existing);
                String def = getRequest().getResourceRef().getQueryAsForm().getFirstValue("default");
                if ("true".equals(def)) {
                    l.setDefaultStyle(existing);
                }
                catalog.save(l);
                LOGGER.info("POST style " + style.getName() + " to layer " + layer);
            } else {
                if (workspace != null) {
                    style.setWorkspace(catalog.getWorkspaceByName(workspace));
                }
                catalog.add(style);
                LOGGER.info("POST style " + style.getName());
            }
            return style.getName();
        } else if (object instanceof Style) {
            Style style = (Style) object;
            String name = getRequest().getResourceRef().getQueryAsForm().getFirstValue("name");
            if (name == null) {
                name = style.getName();
            }
            if (name == null) {
                throw new RestletException("Style must have a name.", Status.CLIENT_ERROR_BAD_REQUEST);
            }
            if (catalog.getStyleByName(workspace, name) != null) {
                throw new RestletException("Style " + name + " already exists.", Status.CLIENT_ERROR_FORBIDDEN);
            }
            GeoServerResourceLoader loader = catalog.getResourceLoader();
            String path = "styles/" + name + ".sld";
            if (workspace != null) {
                path = "workspaces/" + workspace + "/" + path;
            }
            File f;
            try {
                f = loader.find(path);
            } catch (IOException e) {
                throw new RestletException("Error looking up file", Status.SERVER_ERROR_INTERNAL, e);
            }
            if (f != null) {
                String msg = "SLD file " + path + ".sld already exists.";
                throw new RestletException(msg, Status.CLIENT_ERROR_FORBIDDEN);
            }
            try {
                f = loader.createFile(path);
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
                SLDFormat format = new SLDFormat(true);
                format.toRepresentation(style).write(out);
                out.flush();
                out.close();
            } catch (IOException e) {
                throw new RestletException("Error creating file", Status.SERVER_ERROR_INTERNAL, e);
            }
            StyleInfo sinfo = catalog.getFactory().createStyle();
            sinfo.setName(name);
            sinfo.setFilename(f.getName());
            if (workspace != null) {
                sinfo.setWorkspace(catalog.getWorkspaceByName(workspace));
            }
            catalog.add(sinfo);
            LOGGER.info("POST SLD " + name);
            return name;
        }
        return null;
    }

    @Override
    public boolean allowPut() {
        if (getAttribute("workspace") == null && !isAuthenticatedAsAdmin()) {
            return false;
        }
        return getAttribute("style") != null;
    }

    @Override
    protected void handleObjectPut(Object object) throws Exception {
        String style = getAttribute("style");
        String workspace = getAttribute("workspace");
        if (object instanceof StyleInfo) {
            StyleInfo s = (StyleInfo) object;
            StyleInfo original = catalog.getStyleByName(workspace, style);
            if (s.getWorkspace() != null) {
                if (!s.getWorkspace().equals(original.getWorkspace())) {
                    throw new RestletException("Can't change the workspace of a style, instead " + "DELETE from existing workspace and POST to new workspace", Status.CLIENT_ERROR_FORBIDDEN);
                }
            }
            new CatalogBuilder(catalog).updateStyle(original, s);
            catalog.save(original);
        } else if (object instanceof Style) {
            StyleInfo s = catalog.getStyleByName(workspace, style);
            catalog.getResourcePool().writeStyle(s, (Style) object, true);
            catalog.save(s);
        }
        LOGGER.info("PUT style " + style);
    }

    @Override
    public boolean allowDelete() {
        return getAttribute("style") != null;
    }

    @Override
    protected void handleObjectDelete() throws Exception {
        String workspace = getAttribute("workspace");
        String style = getAttribute("style");
        StyleInfo s = workspace != null ? catalog.getStyleByName(workspace, style) : catalog.getStyleByName(style);
        List<LayerInfo> layers = catalog.getLayers(s);
        if (!layers.isEmpty()) {
            throw new RestletException("Can't delete style referenced by existing layers.", Status.CLIENT_ERROR_FORBIDDEN);
        }
        catalog.remove(s);
        String p = getRequest().getResourceRef().getQueryAsForm().getFirstValue("purge");
        boolean purge = (p != null) ? Boolean.parseBoolean(p) : false;
        catalog.getResourcePool().deleteStyle(s, purge);
        LOGGER.info("DELETE style " + style);
    }
}
