package org.dspace.content.packager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.DSpaceObject;
import org.dspace.content.crosswalk.AbstractPackagerWrappingCrosswalk;
import org.dspace.content.crosswalk.CrosswalkException;
import org.dspace.content.crosswalk.CrosswalkObjectNotSupported;
import org.dspace.content.crosswalk.IngestionCrosswalk;
import org.dspace.content.crosswalk.MetadataValidationException;
import org.dspace.content.crosswalk.StreamIngestionCrosswalk;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.factory.CoreServiceFactory;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

public class METSManifest {

    public interface Mdref {

        public InputStream getInputStream(Element mdRef) throws MetadataValidationException, PackageValidationException, IOException, SQLException, AuthorizeException;
    }

    private static final Logger log = LogManager.getLogger(METSManifest.class);

    private static final ConfigurationService configurationService = DSpaceServicesFactory.getInstance().getConfigurationService();

    public static final String MANIFEST_FILE = "mets.xml";

    public static final String CONFIG_METS_PREFIX = "mets.";

    protected static final String CONFIG_XSD_PREFIX = CONFIG_METS_PREFIX + "xsd.";

    protected static final Namespace dcNS = Namespace.getNamespace("http://purl.org/dc/elements/1.1/");

    protected static final Namespace dcTermNS = Namespace.getNamespace("http://purl.org/dc/terms/");

    public static final Namespace metsNS = Namespace.getNamespace("mets", "http://www.loc.gov/METS/");

    public static final Namespace xlinkNS = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");

    protected Element mets = null;

    protected List mdFiles = null;

    protected List<Element> contentFiles = null;

    protected List<Element> bundleFiles = null;

    protected SAXBuilder parser = null;

    protected String configName;

    protected static String localSchemas;

    static {
        String dspace_dir = configurationService.getProperty("dspace.dir");
        File xsdPath1 = new File(dspace_dir + "/config/schemas/");
        File xsdPath2 = new File(dspace_dir + "/config/");
<<<<<<< MINE
        List<String> configKeys = configurationService.getPropertyKeys();
        StringBuilder result = new StringBuilder();
        for (String key : configKeys) {
            if (key.startsWith(CONFIG_XSD_PREFIX)) {
                String spec = configurationService.getProperty(key);
                String[] val = spec.trim().split("\\s+");
                if (val.length == 2) {
                    File xsd = new File(xsdPath1, val[1]);
                    if (!xsd.exists()) {
                        xsd = new File(xsdPath2, val[1]);
                    }
                    if (!xsd.exists()) {
                        log.warn("Schema file not found for config entry=\"" + spec + "\"");
                    } else {
                        try {
                            String u = xsd.toURI().toURL().toString();
                            if (result.length() > 0) {
                                result.append(" ");
                            }
                            result.append(val[0]).append(" ").append(u);
                        } catch (java.net.MalformedURLException e) {
                            log.warn("Skipping badly formed XSD URL: " + e.toString());
=======
        List<String> configKeys = configurationService.getPropertyKeys(CONFIG_XSD_PREFIX);
        StringBuilder result = new StringBuilder();
        for (String key : configKeys) {
            String spec = configurationService.getProperty(key);
            String[] val = spec.trim().split("\\s+");
            if (val.length == 2) {
                File xsd = new File(xsdPath1, val[1]);
                if (!xsd.exists()) {
                    xsd = new File(xsdPath2, val[1]);
                }
                if (!xsd.exists()) {
                    log.warn("Schema file not found for config entry=\"{}\"", spec);
                } else {
                    try {
                        String u = xsd.toURI().toURL().toString();
                        if (result.length() > 0) {
                            result.append(" ");
>>>>>>> YOURS
                        }
                        result.append(val[0]).append(" ").append(u);
                    } catch (java.net.MalformedURLException e) {
                        log.warn("Skipping badly formed XSD URL: {}", () -> e.toString());
                    }
                }
            } else {
                log.warn("Schema config entry has wrong format, entry=\"{}\"", spec);
            }
        }
        log.debug("Got local schemas = \"{}\"", () -> result.toString());
    }

    protected METSManifest(SAXBuilder builder, Element mets, String configName) {
        super();
        this.mets = mets;
        this.parser = builder;
        this.configName = configName;
    }

    public static METSManifest create(InputStream is, boolean validate, String configName) throws IOException, MetadataValidationException {
        SAXBuilder builder = new SAXBuilder(validate);
        builder.setIgnoringElementContentWhitespace(true);
        if (validate) {
            builder.setFeature("http://apache.org/xml/features/validation/schema", true);
            if (localSchemas.length() > 0) {
                builder.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", localSchemas);
            }
        } else {
            builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        }
        Document metsDocument;
        try {
            metsDocument = builder.build(is);
        } catch (JDOMException je) {
            throw new MetadataValidationException("Error validating METS in " + is.toString(), je);
        }
        return new METSManifest(builder, metsDocument.getRootElement(), configName);
    }

    public String getProfile() {
        return mets.getAttributeValue("PROFILE");
    }

    public String getObjID() {
        return mets.getAttributeValue("OBJID");
    }

    public List<Element> getBundleFiles() throws MetadataValidationException {
        if (bundleFiles != null) {
            return bundleFiles;
        }
        bundleFiles = new ArrayList<>();
        Element fileSec = mets.getChild("fileSec", metsNS);
        if (fileSec != null) {
            Iterator fgi = fileSec.getChildren("fileGrp", metsNS).iterator();
            while (fgi.hasNext()) {
                Element fg = (Element) fgi.next();
                bundleFiles.add(fg);
            }
        }
        return bundleFiles;
    }

    public List<Element> getContentFiles() throws MetadataValidationException {
        if (contentFiles != null) {
            return contentFiles;
        }
        contentFiles = new ArrayList<>();
        Element fileSec = mets.getChild("fileSec", metsNS);
        if (fileSec != null) {
            Iterator fgi = fileSec.getChildren("fileGrp", metsNS).iterator();
            while (fgi.hasNext()) {
                Element fg = (Element) fgi.next();
                Iterator fi = fg.getChildren("file", metsNS).iterator();
                while (fi.hasNext()) {
                    Element f = (Element) fi.next();
                    contentFiles.add(f);
                }
            }
        }
        return contentFiles;
    }

    public List getMdFiles() throws MetadataValidationException {
        if (mdFiles == null) {
            try {
                XPath xpath = XPath.newInstance("descendant::mets:mdRef");
                xpath.addNamespace(metsNS);
                mdFiles = xpath.selectNodes(mets);
            } catch (JDOMException je) {
                throw new MetadataValidationException("Failed while searching for mdRef elements in manifest: ", je);
            }
        }
        return mdFiles;
    }

    public String getOriginalFilePath(Element file) {
        String groupID = file.getAttributeValue("GROUPID");
        if (groupID == null || groupID.equals("")) {
            return null;
        }
        try {
            XPath xpath = XPath.newInstance("mets:fileSec/mets:fileGrp[@USE=\"CONTENT\"]/mets:file[@GROUPID=\"" + groupID + "\"]");
            xpath.addNamespace(metsNS);
            List oFiles = xpath.selectNodes(mets);
            if (oFiles.size() > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Got ORIGINAL file for derived=" + file.toString());
                }
                Element flocat = ((Element) oFiles.get(0)).getChild("FLocat", metsNS);
                if (flocat != null) {
                    return flocat.getAttributeValue("href", xlinkNS);
                }
            }
            return null;
        } catch (JDOMException je) {
            log.warn("Got exception on XPATH looking for Original file, " + je.toString());
            return null;
        }
    }

    protected static String normalizeBundleName(String in) {
        if (in.equals("CONTENT")) {
            return Constants.CONTENT_BUNDLE_NAME;
        } else if (in.equals("MANIFESTMD")) {
            return Constants.METADATA_BUNDLE_NAME;
        }
        return in;
    }

    public static String getBundleName(Element file) throws MetadataValidationException {
        return getBundleName(file, true);
    }

    public static String getBundleName(Element file, boolean getParent) throws MetadataValidationException {
        Element fg = file;
        if (getParent) {
            fg = file.getParentElement();
        }
        String fgUse = fg.getAttributeValue("USE");
        if (fgUse == null) {
            throw new MetadataValidationException("Invalid METS Manifest: every fileGrp element must have a USE attribute.");
        }
        return normalizeBundleName(fgUse);
    }

    public static String getFileName(Element file) throws MetadataValidationException {
        Element ref;
        if (file.getName().equals("file")) {
            ref = file.getChild("FLocat", metsNS);
            if (ref == null) {
                if (file.getChild("FContent", metsNS) == null) {
                    throw new MetadataValidationException("Invalid METS Manifest: Every file element must have FLocat child.");
                } else {
                    throw new MetadataValidationException("Invalid METS Manifest: file element has forbidden FContent child, only FLocat is allowed.");
                }
            }
        } else if (file.getName().equals("mdRef")) {
            ref = file;
        } else {
            throw new MetadataValidationException("getFileName() called with recognized element type: " + file.toString());
        }
        String loctype = ref.getAttributeValue("LOCTYPE");
        if (loctype != null && loctype.equals("URL")) {
            String result = ref.getAttributeValue("href", xlinkNS);
            if (result == null) {
                throw new MetadataValidationException("Invalid METS Manifest: FLocat/mdRef is missing the required xlink:href attribute.");
            }
            return result;
        }
        throw new MetadataValidationException("Invalid METS Manifest: FLocat/mdRef does not have LOCTYPE=\"URL\" attribute.");
    }

    public Element getPrimaryOrLogoBitstream() throws MetadataValidationException {
        Element objDiv = getObjStructDiv();
        Element fptr = objDiv.getChild("fptr", metsNS);
        if (fptr == null) {
            return null;
        }
        String id = fptr.getAttributeValue("FILEID");
        if (id == null) {
            throw new MetadataValidationException("fptr for Primary Bitstream is missing the required FILEID attribute.");
        }
        Element result = getElementByXPath("descendant::mets:file[@ID=\"" + id + "\"]", false);
        if (result == null) {
            throw new MetadataValidationException("Cannot find file element for Primary Bitstream: looking for ID=" + id);
        }
        return result;
    }

    public String getMdType(Element mdSec) throws MetadataValidationException {
        Element md = mdSec.getChild("mdRef", metsNS);
        if (md == null) {
            md = mdSec.getChild("mdWrap", metsNS);
        }
        if (md == null) {
            throw new MetadataValidationException("Invalid METS Manifest: ?mdSec element has neither mdRef nor mdWrap child.");
        }
        String result = md.getAttributeValue("MDTYPE");
        if (result != null && result.equals("OTHER")) {
            result = md.getAttributeValue("OTHERMDTYPE");
        }
        if (result == null) {
            throw new MetadataValidationException("Invalid METS Manifest: " + md.getName() + " has no MDTYPE or OTHERMDTYPE attribute.");
        }
        return result;
    }

    public String getMdContentMimeType(Element mdSec) throws MetadataValidationException {
        Element mdWrap = mdSec.getChild("mdWrap", metsNS);
        if (mdWrap != null) {
            String mimeType = mdWrap.getAttributeValue("MIMETYPE");
            if (mimeType == null && mdWrap.getChild("xmlData", metsNS) != null) {
                mimeType = "text/xml";
            }
            return mimeType;
        }
        Element mdRef = mdSec.getChild("mdRef", metsNS);
        if (mdRef != null) {
            return mdRef.getAttributeValue("MIMETYPE");
        }
        return null;
    }

    private List<Element> getMdContentAsXml(Element mdSec, Mdref callback) throws MetadataValidationException, PackageValidationException, IOException, SQLException, AuthorizeException {
        try {
            List mdc = mdSec.getChildren();
            if (mdc.size() > 1) {
                String id = mdSec.getAttributeValue("ID");
                StringBuilder sb = new StringBuilder();
                for (Iterator mi = mdc.iterator(); mi.hasNext(); ) {
                    sb.append(", ").append(((Content) mi.next()).toString());
                }
                throw new MetadataValidationException("Cannot parse METS with " + mdSec.getQualifiedName() + " element that contains more than one child, size=" + String.valueOf(mdc.size()) + ", ID=" + id + "Kids=" + sb.toString());
            }
            Element mdRef = null;
            Element mdWrap = mdSec.getChild("mdWrap", metsNS);
            if (mdWrap != null) {
                Element xmlData = mdWrap.getChild("xmlData", metsNS);
                if (xmlData == null) {
                    Element bin = mdWrap.getChild("binData", metsNS);
                    if (bin == null) {
                        throw new MetadataValidationException("Invalid METS Manifest: mdWrap element with neither xmlData nor binData child.");
                    } else {
                        String mimeType = mdWrap.getAttributeValue("MIMETYPE");
                        if (mimeType != null && mimeType.equalsIgnoreCase("text/xml")) {
                            byte[] value = Base64.decodeBase64(bin.getText().getBytes());
                            Document mdd = parser.build(new ByteArrayInputStream(value));
                            List<Element> result = new ArrayList<>(1);
                            result.add(mdd.getRootElement());
                            return result;
                        } else {
                            log.warn("Ignoring binData section because MIMETYPE is not XML, but: " + mimeType);
                            return new ArrayList<>(0);
                        }
                    }
                } else {
                    return xmlData.getChildren();
                }
            } else {
                mdRef = mdSec.getChild("mdRef", metsNS);
                if (mdRef != null) {
                    String mimeType = mdRef.getAttributeValue("MIMETYPE");
                    if (mimeType != null && mimeType.equalsIgnoreCase("text/xml")) {
                        Document mdd = parser.build(callback.getInputStream(mdRef));
                        List<Element> result = new ArrayList<>(1);
                        result.add(mdd.getRootElement());
                        return result;
                    } else {
                        log.warn("Ignoring mdRef section because MIMETYPE is not XML, but: " + mimeType);
                        return new ArrayList<>(0);
                    }
                } else {
                    throw new MetadataValidationException("Invalid METS Manifest: ?mdSec element with neither mdRef nor mdWrap child.");
                }
            }
        } catch (JDOMException je) {
            throw new MetadataValidationException("Error parsing or validating metadata section in mdRef or binData within " + mdSec.toString(), je);
        }
    }

    public InputStream getMdContentAsStream(Element mdSec, Mdref callback) throws MetadataValidationException, PackageValidationException, IOException, SQLException, AuthorizeException {
        Element mdRef = null;
        Element mdWrap = mdSec.getChild("mdWrap", metsNS);
        if (mdWrap != null) {
            Element xmlData = mdWrap.getChild("xmlData", metsNS);
            if (xmlData == null) {
                Element bin = mdWrap.getChild("binData", metsNS);
                if (bin == null) {
                    throw new MetadataValidationException("Invalid METS Manifest: mdWrap element with neither xmlData nor binData child.");
                } else {
                    byte[] value = Base64.decodeBase64(bin.getText().getBytes());
                    return new ByteArrayInputStream(value);
                }
            } else {
                XMLOutputter outputPretty = new XMLOutputter(Format.getPrettyFormat());
                return new ByteArrayInputStream(outputPretty.outputString(xmlData.getChildren()).getBytes());
            }
        } else {
            mdRef = mdSec.getChild("mdRef", metsNS);
            if (mdRef != null) {
                return callback.getInputStream(mdRef);
            } else {
                throw new MetadataValidationException("Invalid METS Manifest: ?mdSec element with neither mdRef nor mdWrap child.");
            }
        }
    }

    public Element getObjStructDiv() throws MetadataValidationException {
        Element sm = mets.getChild("structMap", metsNS);
        if (sm == null) {
            throw new MetadataValidationException("METS document is missing the required structMap element.");
        }
        Element result = sm.getChild("div", metsNS);
        if (result == null) {
            throw new MetadataValidationException("METS document is missing the required first div element in first structMap.");
        }
        if (log.isDebugEnabled()) {
            log.debug("Got getObjStructDiv result=" + result.toString());
        }
        return (Element) result;
    }

    public List getChildObjDivs() throws MetadataValidationException {
        Element objDiv = getObjStructDiv();
        return objDiv.getChildren("div", metsNS);
    }

    public String[] getChildMetsFilePaths() throws MetadataValidationException {
        List childObjDivs = getChildObjDivs();
        List<String> childPathList = new ArrayList<>();
        if (childObjDivs != null && !childObjDivs.isEmpty()) {
            Iterator childIterator = childObjDivs.iterator();
            while (childIterator.hasNext()) {
                Element childDiv = (Element) childIterator.next();
                List childMptrs = childDiv.getChildren("mptr", metsNS);
                if (childMptrs != null && !childMptrs.isEmpty()) {
                    Iterator mptrIterator = childMptrs.iterator();
                    while (mptrIterator.hasNext()) {
                        Element mptr = (Element) mptrIterator.next();
                        String locType = mptr.getAttributeValue("LOCTYPE");
                        if (locType != null && locType.equals("URL")) {
                            String filePath = mptr.getAttributeValue("href", xlinkNS);
                            if (filePath != null && filePath.length() > 0) {
                                childPathList.add(filePath);
                            }
                        }
                    }
                }
            }
        }
        String[] childPaths = new String[childPathList.size()];
        childPaths = (String[]) childPathList.toArray(childPaths);
        return childPaths;
    }

    public String getParentOwnerLink() throws MetadataValidationException {
        List<Element> childStructMaps = mets.getChildren("structMap", metsNS);
        Element parentStructMap = null;
        if (!childStructMaps.isEmpty()) {
            for (Element structMap : childStructMaps) {
                String label = structMap.getAttributeValue("LABEL");
                if (label != null && label.equalsIgnoreCase("Parent")) {
                    parentStructMap = structMap;
                    break;
                }
            }
        }
        if (parentStructMap == null) {
            throw new MetadataValidationException("METS document is missing the required structMap[@LABEL='Parent'] element.");
        }
        Element linkDiv = parentStructMap.getChild("div", metsNS);
        if (linkDiv == null) {
            throw new MetadataValidationException("METS document is missing the required first div element in structMap[@LABEL='Parent'].");
        }
        Element mptr = linkDiv.getChild("mptr", metsNS);
        if (mptr != null) {
            return mptr.getAttributeValue("href", xlinkNS);
        }
        return null;
    }

    protected Element getElementByXPath(String path, boolean nullOk) throws MetadataValidationException {
        try {
            XPath xpath = XPath.newInstance(path);
            xpath.addNamespace(metsNS);
            xpath.addNamespace(xlinkNS);
            Object result = xpath.selectSingleNode(mets);
            if (result == null && nullOk) {
                return null;
            } else if (result instanceof Element) {
                return (Element) result;
            } else {
                throw new MetadataValidationException("METSManifest: Failed to resolve XPath, path=\"" + path + "\"");
            }
        } catch (JDOMException je) {
            throw new MetadataValidationException("METSManifest: Failed to resolve XPath, path=\"" + path + "\"", je);
        }
    }

    protected Object getCrosswalk(String type, Class clazz) {
        String xwalkName = configurationService.getProperty(CONFIG_METS_PREFIX + configName + ".ingest.crosswalk." + type);
        if (xwalkName == null) {
            xwalkName = configurationService.getProperty(CONFIG_METS_PREFIX + "default.ingest.crosswalk." + type);
            if (xwalkName == null) {
                xwalkName = type;
            }
        }
        return CoreServiceFactory.getInstance().getPluginService().getNamedPlugin(clazz, xwalkName);
    }

    public Element[] getItemDmds() throws MetadataValidationException {
        Element objDiv = getObjStructDiv();
        String dmds = objDiv.getAttributeValue("DMDID");
        if (dmds == null) {
            throw new MetadataValidationException("Invalid METS: Missing reference to Item descriptive metadata, first div on first structmap must have" + " a DMDID attribute.");
        }
        return getDmdElements(dmds);
    }

    public Element[] getDmdElements(String dmdList) throws MetadataValidationException {
        if (dmdList != null && !dmdList.isEmpty()) {
            String[] dmdID = dmdList.split("\\s+");
            Element[] result = new Element[dmdID.length];
            for (int i = 0; i < dmdID.length; ++i) {
                result[i] = getElementByXPath("mets:dmdSec[@ID=\"" + dmdID[i] + "\"]", false);
            }
            return result;
        } else {
            return new Element[0];
        }
    }

    public Element[] getItemRightsMD() throws MetadataValidationException {
        Element objDiv = getObjStructDiv();
        String amds = objDiv.getAttributeValue("ADMID");
        if (amds == null) {
            if (log.isDebugEnabled()) {
                log.debug("getItemRightsMD: No ADMID references found.");
            }
            return new Element[0];
        }
        String[] amdID = amds.split("\\s+");
        List<Element> resultList = new ArrayList<>();
        for (int i = 0; i < amdID.length; ++i) {
            List rmds = getElementByXPath("mets:amdSec[@ID=\"" + amdID[i] + "\"]", false).getChildren("rightsMD", metsNS);
            if (rmds.size() > 0) {
                resultList.addAll(rmds);
            }
        }
        return resultList.toArray(new Element[resultList.size()]);
    }

    public void crosswalkItemDmd(Context context, PackageParameters params, DSpaceObject dso, Element dmdSec, Mdref callback) throws MetadataValidationException, PackageValidationException, CrosswalkException, IOException, SQLException, AuthorizeException {
        crosswalkXmd(context, params, dso, dmdSec, callback, false);
    }

    public void crosswalkObjectOtherAdminMD(Context context, PackageParameters params, DSpaceObject dso, Mdref callback) throws MetadataValidationException, PackageValidationException, CrosswalkException, IOException, SQLException, AuthorizeException {
        for (String amdID : getAmdIDs()) {
            Element amdSec = getElementByXPath("mets:amdSec[@ID=\"" + amdID + "\"]", false);
            for (Iterator ti = amdSec.getChildren("techMD", metsNS).iterator(); ti.hasNext(); ) {
                crosswalkXmd(context, params, dso, (Element) ti.next(), callback, false);
            }
            for (Iterator ti = amdSec.getChildren("digiprovMD", metsNS).iterator(); ti.hasNext(); ) {
                crosswalkXmd(context, params, dso, (Element) ti.next(), callback, false);
            }
            for (Iterator ti = amdSec.getChildren("rightsMD", metsNS).iterator(); ti.hasNext(); ) {
                crosswalkXmd(context, params, dso, (Element) ti.next(), callback, false);
            }
        }
    }

    public boolean crosswalkObjectSourceMD(Context context, PackageParameters params, DSpaceObject dso, Mdref callback) throws MetadataValidationException, PackageValidationException, CrosswalkException, IOException, SQLException, AuthorizeException {
        boolean result = false;
        for (String amdID : getAmdIDs()) {
            Element amdSec = getElementByXPath("mets:amdSec[@ID=\"" + amdID + "\"]", false);
            for (Iterator ti = amdSec.getChildren("sourceMD", metsNS).iterator(); ti.hasNext(); ) {
                crosswalkXmd(context, params, dso, (Element) ti.next(), callback, false);
                result = true;
            }
        }
        return result;
    }

    protected String[] getAmdIDs() throws MetadataValidationException {
        Element objDiv = getObjStructDiv();
        String amds = objDiv.getAttributeValue("ADMID");
        if (amds == null) {
            if (log.isDebugEnabled()) {
                log.debug("crosswalkObjectTechMD: No ADMID references found.");
            }
            return new String[0];
        }
        return amds.split("\\s+");
    }

    protected void crosswalkXmd(Context context, PackageParameters params, DSpaceObject dso, Element xmd, Mdref callback, boolean createMissingMetadataFields) throws MetadataValidationException, PackageValidationException, CrosswalkException, IOException, SQLException, AuthorizeException {
        String type = getMdType(xmd);
        IngestionCrosswalk xwalk = (IngestionCrosswalk) getCrosswalk(type, IngestionCrosswalk.class);
        try {
            if (xwalk != null) {
                if (xwalk instanceof AbstractPackagerWrappingCrosswalk) {
                    AbstractPackagerWrappingCrosswalk wrapper = (AbstractPackagerWrappingCrosswalk) xwalk;
                    wrapper.setPackagingParameters(params);
                }
                xwalk.ingest(context, dso, getMdContentAsXml(xmd, callback), false);
            } else {
                StreamIngestionCrosswalk sxwalk = (StreamIngestionCrosswalk) getCrosswalk(type, StreamIngestionCrosswalk.class);
                if (sxwalk != null) {
                    if (sxwalk instanceof AbstractPackagerWrappingCrosswalk) {
                        AbstractPackagerWrappingCrosswalk wrapper = (AbstractPackagerWrappingCrosswalk) sxwalk;
                        wrapper.setPackagingParameters(params);
                    }
                    Element mdRef = xmd.getChild("mdRef", metsNS);
                    if (mdRef != null) {
                        InputStream in = null;
                        try {
                            in = callback.getInputStream(mdRef);
                            sxwalk.ingest(context, dso, in, mdRef.getAttributeValue("MIMETYPE"));
                        } finally {
                            if (in != null) {
                                in.close();
                            }
                        }
                    } else {
                        Element mdWrap = xmd.getChild("mdWrap", metsNS);
                        if (mdWrap != null) {
                            Element bin = mdWrap.getChild("binData", metsNS);
                            if (bin == null) {
                                throw new MetadataValidationException("Invalid METS Manifest: mdWrap element for streaming crosswalk without binData " + "child.");
                            } else {
                                byte[] value = Base64.decodeBase64(bin.getText().getBytes());
                                sxwalk.ingest(context, dso, new ByteArrayInputStream(value), mdWrap.getAttributeValue("MIMETYPE"));
                            }
                        } else {
                            throw new MetadataValidationException("Cannot process METS Manifest: " + "Metadata of type=" + type + " requires a " + "reference to a stream (mdRef), which was not " + "found in " + xmd.getName());
                        }
                    }
                } else {
                    throw new MetadataValidationException("Cannot process METS Manifest: " + "No crosswalk found for contents of " + xmd.getName() + " element, MDTYPE=" + type);
                }
            }
        } catch (CrosswalkObjectNotSupported e) {
            log.warn("Skipping metadata section " + xmd.getName() + ", type=" + type + " inappropriate for this type of object: Object=" + dso.toString() + ", error=" + e.toString());
        }
    }

    public void crosswalkBitstream(Context context, PackageParameters params, Bitstream bitstream, String fileId, Mdref callback) throws MetadataValidationException, PackageValidationException, CrosswalkException, IOException, SQLException, AuthorizeException {
        Element file = getElementByXPath("descendant::mets:file[@ID=\"" + fileId + "\"]", false);
        if (file == null) {
            throw new MetadataValidationException("Failed in Bitstream crosswalk, Could not find file element with ID=" + fileId);
        }
        String amds = file.getAttributeValue("ADMID");
        if (amds == null) {
            log.warn("Got no bitstream ADMID, file@ID=" + fileId);
            return;
        }
        String[] amdID = amds.split("\\s+");
        for (int i = 0; i < amdID.length; ++i) {
            Element amdSec = getElementByXPath("mets:amdSec[@ID=\"" + amdID[i] + "\"]", false);
            for (Iterator ti = amdSec.getChildren("techMD", metsNS).iterator(); ti.hasNext(); ) {
                crosswalkXmd(context, params, bitstream, (Element) ti.next(), callback, false);
            }
            for (Iterator ti = amdSec.getChildren("sourceMD", metsNS).iterator(); ti.hasNext(); ) {
                crosswalkXmd(context, params, bitstream, (Element) ti.next(), callback, false);
            }
            for (Iterator ti = amdSec.getChildren("rightsMD", metsNS).iterator(); ti.hasNext(); ) {
                crosswalkXmd(context, params, bitstream, (Element) ti.next(), callback, false);
            }
        }
    }

    public void crosswalkBundle(Context context, PackageParameters params, Bundle bundle, String fileId, Mdref callback) throws MetadataValidationException, PackageValidationException, CrosswalkException, IOException, SQLException, AuthorizeException {
        Element file = getElementByXPath("descendant::mets:fileGrp[@ADMID=\"" + fileId + "\"]", false);
        if (file == null) {
            throw new MetadataValidationException("Failed in Bitstream crosswalk, Could not find file element with ID=" + fileId);
        }
        String amds = file.getAttributeValue("ADMID");
        if (amds == null) {
            log.warn("Got no bitstream ADMID, file@ID=" + fileId);
            return;
        }
        String[] amdID = amds.split("\\s+");
        for (int i = 0; i < amdID.length; ++i) {
            Element amdSec = getElementByXPath("mets:amdSec[@ID=\"" + amdID[i] + "\"]", false);
            for (Iterator ti = amdSec.getChildren("techMD", metsNS).iterator(); ti.hasNext(); ) {
                crosswalkXmd(context, params, bundle, (Element) ti.next(), callback, false);
            }
            for (Iterator ti = amdSec.getChildren("sourceMD", metsNS).iterator(); ti.hasNext(); ) {
                crosswalkXmd(context, params, bundle, (Element) ti.next(), callback, false);
            }
            for (Iterator ti = amdSec.getChildren("rightsMD", metsNS).iterator(); ti.hasNext(); ) {
                crosswalkXmd(context, params, bundle, (Element) ti.next(), callback, false);
            }
        }
    }

    public Element getMets() {
        return mets;
    }

    public InputStream getMetsAsStream() {
        XMLOutputter outputPretty = new XMLOutputter(Format.getPrettyFormat());
        return new ByteArrayInputStream(outputPretty.outputString(mets).getBytes());
    }
}
