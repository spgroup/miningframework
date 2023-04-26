package org.dspace.content.crosswalk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataSchema;
import org.dspace.content.MetadataSchemaEnum;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.SelfNamedPlugin;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

public class QDCCrosswalk extends SelfNamedPlugin implements DisseminationCrosswalk, IngestionCrosswalk {

    private static final Logger log = LogManager.getLogger(QDCCrosswalk.class);

    private final Map<String, Element> qdc2element = new HashMap<>();

    private final Map<String, String> element2qdc = new HashMap<>();

    private Namespace[] namespaces = null;

    private static final Namespace DCTERMS_NS = Namespace.getNamespace("dcterms", "http://purl.org/dc/terms/");

    private boolean inited = false;

    private String myName = null;

    private static final String CONFIG_PREFIX = "crosswalk.qdc";

    private String schemaLocation = null;

    private static final SAXBuilder builder = new SAXBuilder();

    protected ItemService itemService = ContentServiceFactory.getInstance().getItemService();

    protected final ConfigurationService configurationService = DSpaceServicesFactory.getInstance().getConfigurationService();

    private final CrosswalkMetadataValidator metadataValidator = new CrosswalkMetadataValidator();

    private static String[] aliases = null;

    static {
        initStatic();
    }

    public static void initStatic() {
        List<String> aliasList = new ArrayList<>();
        String propname = CONFIG_PREFIX + ".properties.";
        List<String> configKeys = DSpaceServicesFactory.getInstance().getConfigurationService().getPropertyKeys(propname);
        for (String key : configKeys) {
            aliasList.add(key.substring(propname.length()));
        }
        aliases = (String[]) aliasList.toArray(new String[aliasList.size()]);
    }

    public static String[] getPluginNames() {
        return (String[]) ArrayUtils.clone(aliases);
    }

    private String makeQualifiedTagName(Element element) {
        String prefix = "";
        Namespace ns = element.getNamespace();
        if (ns != null) {
            prefix = ns.getPrefix() + ":";
        }
        String tagName;
        String nsQualifier = element.getAttributeValue("type", DisseminationCrosswalk.XSI_NS);
        if (nsQualifier == null || nsQualifier.length() < 1) {
            String qualifier = element.getAttributeValue("type");
            if (qualifier == null || qualifier.length() < 1) {
                tagName = prefix + element.getName();
            } else {
                tagName = prefix + element.getName() + qualifier;
            }
        } else {
            tagName = prefix + element.getName() + nsQualifier;
        }
        return tagName;
    }

    private void init() throws CrosswalkException, IOException {
        if (inited) {
            return;
        }
        inited = true;
        myName = getPluginInstanceName();
        if (myName == null) {
            throw new CrosswalkInternalException("Cannot determine plugin name. " + "You must use PluginService to instantiate QDCCrosswalk so the " + "instance knows its name.");
        }
        List<Namespace> nsList = new ArrayList<>();
        String propname = CONFIG_PREFIX + ".namespace." + myName + ".";
        List<String> configKeys = configurationService.getPropertyKeys(propname);
        for (String key : configKeys) {
            nsList.add(Namespace.getNamespace(key.substring(propname.length()), configurationService.getProperty(key)));
        }
        nsList.add(Namespace.XML_NAMESPACE);
        namespaces = (Namespace[]) nsList.toArray(new Namespace[nsList.size()]);
        schemaLocation = configurationService.getProperty(CONFIG_PREFIX + ".schemaLocation." + myName);
        String cmPropName = CONFIG_PREFIX + ".properties." + myName;
        String propsFilename = configurationService.getProperty(cmPropName);
        if (propsFilename == null) {
            throw new CrosswalkInternalException("Configuration error: " + "No properties file configured for QDC crosswalk named \"" + myName + "\"");
        }
        String parent = configurationService.getProperty("dspace.dir") + File.separator + "config" + File.separator;
        File propsFile = new File(parent, propsFilename);
        Properties qdcProps = new Properties();
        FileInputStream pfs = null;
        try {
            pfs = new FileInputStream(propsFile);
            qdcProps.load(pfs);
        } finally {
            if (pfs != null) {
                try {
                    pfs.close();
                } catch (IOException ioe) {
                }
            }
        }
        String postlog = "</wrapper>";
        StringBuilder prologb = new StringBuilder("<wrapper");
        for (int i = 0; i < namespaces.length; ++i) {
            prologb.append(" xmlns:");
            prologb.append(namespaces[i].getPrefix());
            prologb.append("=\"");
            prologb.append(namespaces[i].getURI());
            prologb.append("\"");
        }
        prologb.append(">");
        String prolog = prologb.toString();
        Enumeration<String> qdcKeys = (Enumeration<String>) qdcProps.propertyNames();
        while (qdcKeys.hasMoreElements()) {
            String qdc = qdcKeys.nextElement();
            String val = qdcProps.getProperty(qdc);
            try {
                Document d = builder.build(new StringReader(prolog + val + postlog));
                Element element = (Element) d.getRootElement().getContent(0);
                qdc2element.put(qdc, element);
                element2qdc.put(makeQualifiedTagName(element), qdc);
                log.debug("Building Maps: qdc=\"" + qdc + "\", element=\"" + element.toString() + "\"");
            } catch (org.jdom.JDOMException je) {
                throw new CrosswalkInternalException("Failed parsing XML fragment in properties file: \"" + prolog + val + postlog + "\": " + je.toString(), je);
            }
        }
    }

    @Override
    public Namespace[] getNamespaces() {
        try {
            init();
        } catch (IOException | CrosswalkException e) {
        }
        return (Namespace[]) ArrayUtils.clone(namespaces);
    }

    @Override
    public String getSchemaLocation() {
        try {
            init();
        } catch (IOException | CrosswalkException e) {
        }
        return schemaLocation;
    }

    @Override
    public List<Element> disseminateList(Context context, DSpaceObject dso) throws CrosswalkException, IOException, SQLException, AuthorizeException {
        return disseminateListInternal(dso, true);
    }

    private List<Element> disseminateListInternal(DSpaceObject dso, boolean addSchema) throws CrosswalkException, IOException, SQLException, AuthorizeException {
        if (dso.getType() != Constants.ITEM) {
            throw new CrosswalkObjectNotSupported("QDCCrosswalk can only crosswalk an Item.");
        }
        Item item = (Item) dso;
        init();
        List<MetadataValue> dc = itemService.getMetadata(item, Item.ANY, Item.ANY, Item.ANY, Item.ANY);
        List<Element> result = new ArrayList<>(dc.size());
        for (int i = 0; i < dc.size(); i++) {
            MetadataValue metadataValue = dc.get(i);
            MetadataField metadataField = metadataValue.getMetadataField();
            MetadataSchema metadataSchema = metadataField.getMetadataSchema();
            String qdc = metadataSchema.getName() + "." + ((metadataField.getQualifier() == null) ? metadataField.getElement() : (metadataField.getElement() + "." + metadataField.getQualifier()));
            Element elt = qdc2element.get(qdc);
            if (elt == null) {
                if (metadataField.getMetadataSchema().getName().equals(MetadataSchemaEnum.DC.getName())) {
                    log.warn("WARNING: " + myName + ": No QDC mapping for \"" + qdc + "\"");
                }
            } else {
                Element qe = (Element) elt.clone();
                qe.setText(metadataValue.getValue());
                if (addSchema && schemaLocation != null) {
                    qe.setAttribute("schemaLocation", schemaLocation, XSI_NS);
                }
                if (metadataValue.getLanguage() != null) {
                    qe.setAttribute("lang", metadataValue.getLanguage(), Namespace.XML_NAMESPACE);
                }
                result.add(qe);
            }
        }
        return result;
    }

    @Override
    public Element disseminateElement(Context context, DSpaceObject dso) throws CrosswalkException, IOException, SQLException, AuthorizeException {
        init();
        Element root = new Element("qualifieddc", DCTERMS_NS);
        if (schemaLocation != null) {
            root.setAttribute("schemaLocation", schemaLocation, XSI_NS);
        }
        root.addContent(disseminateListInternal(dso, false));
        return root;
    }

    @Override
    public boolean canDisseminate(DSpaceObject dso) {
        return true;
    }

    @Override
    public void ingest(Context context, DSpaceObject dso, Element root, boolean createMissingMetadataFields) throws CrosswalkException, IOException, SQLException, AuthorizeException {
        init();
        if (!(root.getName().equals("qualifieddc"))) {
            throw new MetadataValidationException("Wrong root element for Qualified DC: " + root.toString());
        }
        ingest(context, dso, root.getChildren(), createMissingMetadataFields);
    }

    @Override
    public void ingest(Context context, DSpaceObject dso, List<Element> ml, boolean createMissingMetadataFields) throws CrosswalkException, IOException, SQLException, AuthorizeException {
        init();
        if (dso.getType() != Constants.ITEM) {
            throw new CrosswalkInternalException("Wrong target object type, QDCCrosswalk can only crosswalk to an Item.");
        }
        Item item = (Item) dso;
        for (Element me : ml) {
            String key = makeQualifiedTagName(me);
            if ("qualifieddc".equals(me.getName())) {
                ingest(context, dso, me.getChildren(), createMissingMetadataFields);
            } else if (element2qdc.containsKey(key)) {
                String[] qdc = (element2qdc.get(key)).split("\\.");
                MetadataField metadataField;
                if (qdc.length == 3) {
                    metadataField = metadataValidator.checkMetadata(context, qdc[0], qdc[1], qdc[2], createMissingMetadataFields);
                } else if (qdc.length == 2) {
                    metadataField = metadataValidator.checkMetadata(context, qdc[0], qdc[1], null, createMissingMetadataFields);
                } else {
                    throw new CrosswalkInternalException("Unrecognized format in QDC element identifier for key=\"" + key + "\", qdc=\"" + element2qdc.get(key) + "\"");
                }
                String lang = me.getAttributeValue("lang", Namespace.XML_NAMESPACE);
                if (lang == null) {
                    lang = me.getAttributeValue("lang");
                }
                itemService.addMetadata(context, item, metadataField, lang, me.getText());
            } else {
                log.warn("WARNING: " + myName + ": No mapping for Element=\"" + key + "\" to qdc.");
            }
        }
    }

    @Override
    public boolean preferList() {
        return true;
    }
}
