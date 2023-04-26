package org.geoserver.rest.catalog;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogBuilder;
import org.geoserver.catalog.CoverageStoreInfo;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ResourcePool;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.platform.resource.Paths;
import org.geoserver.platform.resource.Resource;
import org.geoserver.platform.resource.Resources;
import org.geoserver.rest.RestBaseController;
import org.geoserver.rest.RestException;
import org.geoserver.rest.util.IOUtils;
import org.geoserver.rest.util.RESTUploadPathMapper;
import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFactory;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.util.URLs;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.vfny.geoserver.util.DataStoreUtils;

@RestController
@ControllerAdvice
@RequestMapping(path = RestBaseController.ROOT_PATH + "/workspaces/{workspaceName}/datastores/{storeName}/{method}.{format}")
public class DataStoreFileController extends AbstractStoreUploadController {

    @Autowired
    public DataStoreFileController(@Qualifier("catalog") Catalog catalog) {
        super(catalog);
    }

    private static final Pattern H2_FILE_PATTERN = Pattern.compile("(.*?)\\.(?:data.db)");

    protected static final HashMap<String, String> formatToDataStoreFactory = new HashMap();

    static {
        formatToDataStoreFactory.put("shp", "org.geotools.data.shapefile.ShapefileDataStoreFactory");
        formatToDataStoreFactory.put("properties", "org.geotools.data.property.PropertyDataStoreFactory");
        formatToDataStoreFactory.put("h2", "org.geotools.data.h2.H2DataStoreFactory");
        formatToDataStoreFactory.put("spatialite", "org.geotools.data.spatialite.SpatiaLiteDataStoreFactory");
        formatToDataStoreFactory.put("appschema", "org.geotools.data.complex.AppSchemaDataAccessFactory");
        formatToDataStoreFactory.put("gpkg", "org.geotools.geopkg.GeoPkgDataStoreFactory");
    }

    protected static final HashMap<String, Map> dataStoreFactoryToDefaultParams = new HashMap();

    static {
        HashMap map = new HashMap();
        map.put("database", "@DATA_DIR@/@NAME@");
        map.put("dbtype", "h2");
        dataStoreFactoryToDefaultParams.put("org.geotools.data.h2.H2DataStoreFactory", map);
        map = new HashMap();
        map.put("database", "@DATA_DIR@/@NAME@");
        map.put("dbtype", "spatialite");
        dataStoreFactoryToDefaultParams.put("org.geotools.data.spatialite.SpatiaLiteDataStoreFactory", map);
    }

    public static DataAccessFactory lookupDataStoreFactory(String format) {
        String factoryClassName = formatToDataStoreFactory.get(format);
        if (factoryClassName != null) {
            try {
                Class factoryClass = Class.forName(factoryClassName);
                return (DataAccessFactory) factoryClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RestException("Datastore format unavailable: " + factoryClassName, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        String extension = "." + format;
        for (DataAccessFactory dataAccessFactory : DataStoreUtils.getAvailableDataStoreFactories()) {
            if (dataAccessFactory instanceof FileDataStoreFactorySpi) {
                FileDataStoreFactorySpi factory = (FileDataStoreFactorySpi) dataAccessFactory;
                for (String handledExtension : factory.getFileExtensions()) {
                    if (extension.equalsIgnoreCase(handledExtension)) {
                        return factory;
                    }
                }
            }
        }
        throw new RestException("Unsupported format: " + format, HttpStatus.BAD_REQUEST);
    }

    public static String lookupDataStoreFactoryFormat(String type) {
        for (DataAccessFactory factory : DataStoreUtils.getAvailableDataStoreFactories()) {
            if (factory == null) {
                continue;
            }
            if (factory.getDisplayName() != null && factory.getDisplayName().equals(type)) {
                for (Map.Entry e : formatToDataStoreFactory.entrySet()) {
                    if (e.getValue().equals(factory.getClass().getCanonicalName())) {
                        return (String) e.getKey();
                    }
                }
                return factory.getDisplayName();
            }
        }
        return null;
    }

    @GetMapping
    public ResponseEntity dataStoresGet(@PathVariable String workspaceName, @PathVariable String storeName) throws IOException {
        DataStoreInfo info = catalog.getDataStoreByName(workspaceName, storeName);
        if (info == null) {
            throw new RestException("No such datastore " + storeName, HttpStatus.NOT_FOUND);
        }
        ResourcePool rp = info.getCatalog().getResourcePool();
        GeoServerResourceLoader resourceLoader = info.getCatalog().getResourceLoader();
        Map<String, Serializable> rawParamValues = info.getConnectionParameters();
        Map<String, Serializable> paramValues = rp.getParams(rawParamValues, resourceLoader);
        File directory = null;
        try {
            DataAccessFactory factory = rp.getDataStoreFactory(info);
            for (DataAccessFactory.Param param : factory.getParametersInfo()) {
                if (File.class.isAssignableFrom(param.getType())) {
                    Object result = param.lookUp(paramValues);
                    if (result instanceof File) {
                        directory = (File) result;
                    }
                } else if (URL.class.isAssignableFrom(param.getType())) {
                    Object result = param.lookUp(paramValues);
                    if (result instanceof URL) {
                        directory = URLs.urlToFile((URL) result);
                    }
                }
                if (directory != null && !"directory".equals(param.key)) {
                    directory = directory.getParentFile();
                }
                if (directory != null) {
                    break;
                }
            }
        } catch (Exception e) {
            throw new RestException("Failed to lookup source directory for store " + storeName, HttpStatus.NOT_FOUND, e);
        }
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            throw new RestException("No files for datastore " + storeName, HttpStatus.NOT_FOUND);
        }
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
            ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream)) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
                    FileInputStream fileInputStream = new FileInputStream(file);
                    IOUtils.copy(fileInputStream, zipOutputStream);
                    fileInputStream.close();
                    zipOutputStream.closeEntry();
                }
            }
            zipOutputStream.finish();
            zipOutputStream.flush();
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add("content-disposition", "attachment; filename=" + info.getName() + ".zip");
            responseHeaders.add("Content-Type", "application/zip");
            return new ResponseEntity(byteArrayOutputStream.toByteArray(), responseHeaders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping
    public void dataStorePut(@PathVariable String workspaceName, @PathVariable String storeName, @PathVariable UploadMethod method, @PathVariable String format, @RequestParam(name = "configure", required = false) String configure, @RequestParam(name = "target", required = false) String target, @RequestParam(name = "update", required = false) String update, @RequestParam(name = "charset", required = false) String characterset, @RequestParam(name = "filename", required = false) String filename, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.ACCEPTED.value());
        List<Resource> files = doFileUpload(method, workspaceName, storeName, filename, format, request);
        final Resource uploadedFile = files.get(0);
        DataAccessFactory factory = lookupDataStoreFactory(format);
        String sourceDataStoreFormat = format;
        String targetDataStoreFormat = target;
        if (targetDataStoreFormat == null) {
            targetDataStoreFormat = sourceDataStoreFormat;
        }
        sourceDataStoreFormat = sourceDataStoreFormat.toLowerCase();
        targetDataStoreFormat = targetDataStoreFormat.toLowerCase();
        CatalogBuilder builder = new CatalogBuilder(catalog);
        builder.setWorkspace(catalog.getWorkspaceByName(workspaceName));
        DataStoreInfo info = catalog.getDataStoreByName(workspaceName, storeName);
        NamespaceInfo namespace = catalog.getNamespaceByPrefix(workspaceName);
        boolean add = false;
        boolean save = false;
        boolean canRemoveFiles = false;
        if (info == null) {
            LOGGER.info("Auto-configuring datastore: " + storeName);
            info = builder.buildDataStore(storeName);
            add = true;
            if (characterset != null && characterset.length() > 0) {
                info.getConnectionParameters().put("charset", characterset);
            }
            DataAccessFactory targetFactory = factory;
            if (!targetDataStoreFormat.equals(sourceDataStoreFormat)) {
                targetFactory = lookupDataStoreFactory(targetDataStoreFormat);
                if (targetFactory == null) {
                    throw new RestException("Unable to create data store of type " + targetDataStoreFormat, HttpStatus.BAD_REQUEST);
                }
                autoCreateParameters(info, namespace, targetFactory);
                canRemoveFiles = true;
            } else {
                updateParameters(info, namespace, targetFactory, uploadedFile);
            }
            info.setType(targetFactory.getDisplayName());
        } else {
            LOGGER.info("Using existing datastore: " + storeName);
            targetDataStoreFormat = lookupDataStoreFactoryFormat(info.getType());
            if (targetDataStoreFormat == null) {
                throw new RuntimeException("Unable to locate data store factory of type " + info.getType());
            }
            if (targetDataStoreFormat.equals(sourceDataStoreFormat)) {
                save = true;
                updateParameters(info, namespace, factory, uploadedFile);
            } else {
                canRemoveFiles = true;
            }
        }
        builder.setStore(info);
        if (add) {
            catalog.validate(info, true).throwIfInvalid();
            catalog.add(info);
        } else if (save) {
            catalog.validate(info, false).throwIfInvalid();
            catalog.save(info);
        }
        boolean createNewSource;
        DataAccess<?, ?> source;
        try {
            HashMap params = new HashMap();
            if (characterset != null && characterset.length() > 0) {
                params.put("charset", characterset);
            }
            params.put("namespace", namespace.getURI());
            updateParameters(params, factory, uploadedFile);
            createNewSource = !sameTypeAndUrl(params, info.getConnectionParameters());
            source = (createNewSource) ? factory.createDataStore(params) : info.getDataStore(null);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create source data store", e);
        }
        try {
            DataAccess ds = info.getDataStore(null);
            if (!targetDataStoreFormat.equals(sourceDataStoreFormat) && (source instanceof DataStore && ds instanceof DataStore)) {
                DataStore sourceDataStore = (DataStore) source;
                DataStore targetDataStore = (DataStore) ds;
                for (String featureTypeName : sourceDataStore.getTypeNames()) {
                    try {
                        targetDataStore.getSchema(featureTypeName);
                    } catch (Exception e) {
                        LOGGER.info(featureTypeName + " does not exist in data store " + storeName + ". Attempting to create it");
                        targetDataStore.createSchema(sourceDataStore.getSchema(featureTypeName));
                        sourceDataStore.getSchema(featureTypeName);
                    }
                    FeatureSource featureSource = targetDataStore.getFeatureSource(featureTypeName);
                    if (!(featureSource instanceof FeatureStore)) {
                        LOGGER.warning(featureTypeName + " is not writable, skipping");
                        continue;
                    }
                    Transaction tx = new DefaultTransaction();
                    FeatureStore featureStore = (FeatureStore) featureSource;
                    featureStore.setTransaction(tx);
                    try {
                        if ("overwrite".equalsIgnoreCase(update)) {
                            LOGGER.fine("Removing existing features from " + featureTypeName);
                            featureStore.removeFeatures(Filter.INCLUDE);
                        }
                        LOGGER.fine("Adding features to " + featureTypeName);
                        FeatureCollection features = sourceDataStore.getFeatureSource(featureTypeName).getFeatures();
                        featureStore.addFeatures(features);
                        tx.commit();
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Failed to import data, rolling back the transaction", e);
                        tx.rollback();
                    } finally {
                        tx.close();
                    }
                }
            }
            if ("none".equalsIgnoreCase(configure)) {
                response.setStatus(HttpStatus.CREATED.value());
                return;
            }
            Map<String, FeatureTypeInfo> featureTypesByNativeName = new HashMap<>();
            for (FeatureTypeInfo ftInfo : catalog.getFeatureTypesByDataStore(info)) {
                featureTypesByNativeName.put(ftInfo.getNativeName(), ftInfo);
            }
            List<Name> featureTypeNames = source.getNames();
            for (int i = 0; i < featureTypeNames.size(); i++) {
                if (!"all".equalsIgnoreCase(configure) && i > 0) {
                    break;
                }
                FeatureSource fs = ds.getFeatureSource(featureTypeNames.get(i));
                FeatureTypeInfo ftinfo = featureTypesByNativeName.get(featureTypeNames.get(i).getLocalPart());
                if (ftinfo == null) {
                    ftinfo = builder.buildFeatureType(fs);
                    builder.lookupSRS(ftinfo, true);
                    builder.setupBounds(ftinfo);
                }
                ReferencedEnvelope bounds = fs.getBounds();
                ftinfo.setNativeBoundingBox(bounds);
                if (ftinfo.getId() == null) {
                    if (catalog.getFeatureTypeByName(namespace, ftinfo.getName()) != null) {
                        LOGGER.warning(String.format("Feature type %s already exists in namespace %s, " + "attempting to rename", ftinfo.getName(), namespace.getPrefix()));
                        int x = 1;
                        String originalName = ftinfo.getName();
                        do {
                            ftinfo.setName(originalName + x);
                            x++;
                        } while (catalog.getFeatureTypeByName(namespace, ftinfo.getName()) != null);
                    }
                    catalog.validate(ftinfo, true).throwIfInvalid();
                    catalog.add(ftinfo);
                    LayerInfo layer = builder.buildLayer(ftinfo);
                    boolean valid = true;
                    try {
                        if (!catalog.validate(layer, true).isValid()) {
                            valid = false;
                        }
                    } catch (Exception e) {
                        valid = false;
                    }
                    layer.setEnabled(valid);
                    catalog.add(layer);
                    LOGGER.info("Added feature type " + ftinfo.getName());
                } else {
                    LOGGER.info("Updated feature type " + ftinfo.getName());
                    catalog.validate(ftinfo, false).throwIfInvalid();
                    catalog.save(ftinfo);
                }
                response.setStatus(HttpStatus.CREATED.value());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (createNewSource) {
                source.dispose();
            }
            if (method.isInline() && canRemoveFiles) {
                if (uploadedFile.getType() == Resource.Type.RESOURCE) {
                    if (!uploadedFile.parent().delete()) {
                        LOGGER.info("Unable to delete " + uploadedFile.path());
                    }
                } else if (uploadedFile.getType() == Resource.Type.DIRECTORY) {
                    for (Resource file : files) {
                        if (file.getType() == Resource.Type.RESOURCE) {
                            if (!file.delete()) {
                                LOGGER.info("Unable to delete " + file.path());
                            }
                        }
                    }
                }
            }
        }
    }

    protected List<Resource> doFileUpload(UploadMethod method, String workspaceName, String storeName, String filename, String format, HttpServletRequest request) throws IOException {
        Resource directory = null;
        boolean postRequest = request != null && HttpMethod.POST.name().equalsIgnoreCase(request.getMethod());
        if (method.isInline()) {
            if (method == UploadMethod.url) {
                directory = createFinalRoot(null, null, postRequest);
            } else {
                directory = createFinalRoot(workspaceName, storeName, postRequest);
            }
        }
        return handleFileUpload(storeName, workspaceName, filename, method, format, directory, request);
    }

    private Resource createFinalRoot(String workspaceName, String storeName, boolean isPost) throws IOException {
        Resource directory = null;
        if (isPost && storeName != null) {
            CoverageStoreInfo coverage = catalog.getCoverageStoreByName(storeName);
            if (coverage != null) {
                if (workspaceName == null || coverage.getWorkspace().getName().equalsIgnoreCase(workspaceName)) {
                    directory = Resources.fromPath(URLs.urlToFile(new URL(coverage.getURL())).getPath(), catalog.getResourceLoader().get(""));
                }
            }
        }
        if (directory == null) {
            directory = catalog.getResourceLoader().get(Paths.path("data", workspaceName, storeName));
        }
        StringBuilder root = new StringBuilder(directory.path());
        Map<String, String> storeParams = new HashMap<>();
        List<RESTUploadPathMapper> mappers = GeoServerExtensions.extensions(RESTUploadPathMapper.class);
        for (RESTUploadPathMapper mapper : mappers) {
            mapper.mapStorePath(root, workspaceName, storeName, storeParams);
        }
        directory = Resources.fromPath(root.toString());
        return directory;
    }

    @Override
    protected Resource findPrimaryFile(Resource directory, String format) {
        if ("shp".equalsIgnoreCase(format) || "h2".equalsIgnoreCase(format)) {
            return directory;
        } else {
            return super.findPrimaryFile(directory, format);
        }
    }

    void updateParameters(DataStoreInfo info, NamespaceInfo namespace, DataAccessFactory factory, Resource uploadedFile) {
        Map connectionParameters = info.getConnectionParameters();
        updateParameters(connectionParameters, factory, uploadedFile);
        connectionParameters.put("namespace", namespace.getURI());
        if (!factory.canProcess(connectionParameters)) {
            throw new RestException("Unable to configure datastore, bad parameters.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    void updateParameters(Map connectionParameters, DataAccessFactory factory, Resource uploadedFile) {
        File f = Resources.find(uploadedFile);
        for (DataAccessFactory.Param p : factory.getParametersInfo()) {
            if (File.class == p.type || URL.class == p.type) {
                if ("directory".equals(p.key)) {
                    f = f.getParentFile();
                }
                Object converted = null;
                if (URI.class.equals(p.type)) {
                    converted = f.toURI();
                } else if (URL.class.equals(p.type)) {
                    converted = URLs.fileToUrl(f);
                }
                if (converted != null) {
                    connectionParameters.put(p.key, converted);
                } else {
                    connectionParameters.put(p.key, f);
                }
                continue;
            }
            if (p.required) {
                try {
                    p.lookUp(connectionParameters);
                } catch (Exception e) {
                    connectionParameters.put(p.key, p.sample);
                }
            }
        }
        if (factory.getDisplayName().equalsIgnoreCase("SpatiaLite")) {
            connectionParameters.put(JDBCDataStoreFactory.DATABASE.getName(), f.getAbsolutePath());
        } else if (factory.getDisplayName().equalsIgnoreCase("H2")) {
            String databaseFile = f.getAbsolutePath();
            if (f.isDirectory()) {
                Optional<Resource> found = Resources.list(uploadedFile, resource -> resource.name().endsWith("data.db")).stream().findFirst();
                if (!found.isPresent()) {
                    throw new RestException(String.format("H2 database file could not be found in directory '%s'.", f.getAbsolutePath()), HttpStatus.INTERNAL_SERVER_ERROR);
                }
                databaseFile = found.get().file().getAbsolutePath();
            }
            Matcher matcher = H2_FILE_PATTERN.matcher(databaseFile);
            if (!matcher.matches()) {
                throw new RestException(String.format("Invalid H2 database file '%s'.", databaseFile), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            connectionParameters.put(JDBCDataStoreFactory.DATABASE.getName(), matcher.group(1));
        }
    }

    void autoCreateParameters(DataStoreInfo info, NamespaceInfo namespace, DataAccessFactory factory) {
        Map defaultParams = dataStoreFactoryToDefaultParams.get(factory.getClass().getCanonicalName());
        if (defaultParams == null) {
            throw new RuntimeException("Unable to auto create parameters for " + factory.getDisplayName());
        }
        HashMap params = new HashMap(defaultParams);
        String dataDirRoot = catalog.getResourceLoader().getBaseDirectory().getAbsolutePath();
        for (Object o : params.entrySet()) {
            Map.Entry e = (Map.Entry) o;
            if (e.getValue() instanceof String) {
                String string = (String) e.getValue();
                string = string.replace("@NAME@", info.getName()).replace("@DATA_DIR@", dataDirRoot);
                e.setValue(string);
            }
        }
        params.put("namespace", namespace.getURI());
        info.getConnectionParameters().putAll(params);
    }

    private boolean sameTypeAndUrl(Map sourceParams, Map targetParams) {
        boolean sameType = sourceParams.get("dbtype") != null && targetParams.get("dbtype") != null && sourceParams.get("dbtype").equals(targetParams.get("dbtype"));
        boolean sameUrl = sourceParams.get("url") != null && targetParams.get("url") != null && sourceParams.get("url").equals(targetParams.get("url"));
        return sameType && sameUrl;
    }
}