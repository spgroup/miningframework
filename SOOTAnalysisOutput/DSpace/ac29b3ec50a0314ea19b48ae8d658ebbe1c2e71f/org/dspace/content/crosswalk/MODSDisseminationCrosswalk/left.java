package org.dspace.content.crosswalk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.Site;
import org.dspace.content.dto.MetadataValueDTO;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.SelfNamedPlugin;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.handle.service.HandleService;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jdom.Verifier;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

public class MODSDisseminationCrosswalk extends SelfNamedPlugin implements DisseminationCrosswalk {

    private static final Logger log = LogManager.getLogger(MODSDisseminationCrosswalk.class);

    private static final String CONFIG_PREFIX = "crosswalk.mods.properties.";

    protected final CommunityService communityService = ContentServiceFactory.getInstance().getCommunityService();

    protected final CollectionService collectionService = ContentServiceFactory.getInstance().getCollectionService();

    protected final ItemService itemService = ContentServiceFactory.getInstance().getItemService();

    protected final HandleService handleService = HandleServiceFactory.getInstance().getHandleService();

    protected static final ConfigurationService configurationService = DSpaceServicesFactory.getInstance().getConfigurationService();

    private static String[] aliases = null;

    static {
        List<String> aliasList = new ArrayList<>();
        List<String> pe = configurationService.getPropertyKeys();
        for (String key : pe) {
            if (key.startsWith(CONFIG_PREFIX)) {
                aliasList.add(key.substring(CONFIG_PREFIX.length()));
            }
        }
        aliases = (String[]) aliasList.toArray(new String[aliasList.size()]);
    }

    public static String[] getPluginNames() {
        return (String[]) ArrayUtils.clone(aliases);
    }

    public static final Namespace MODS_NS = Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3");

    private static final Namespace XLINK_NS = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");

    private static final Namespace[] namespaces = { MODS_NS, XLINK_NS };

    public static final String MODS_XSD = "http://www.loc.gov/standards/mods/v3/mods-3-1.xsd";

    private static final String schemaLocation = MODS_NS.getURI() + " " + MODS_XSD;

    private static final XMLOutputter outputUgly = new XMLOutputter();

    private static final SAXBuilder builder = new SAXBuilder();

    private Map<String, modsTriple> modsMap = null;

    static class modsTriple {

        public String qdc = null;

        public Element xml = null;

        public XPath xpath = null;

        public static modsTriple create(String qdc, String xml, String xpath) {
            modsTriple result = new modsTriple();
            final String prolog = "<mods xmlns:" + MODS_NS.getPrefix() + "=\"" + MODS_NS.getURI() + "\" " + "xmlns:" + XLINK_NS.getPrefix() + "=\"" + XLINK_NS.getURI() + "\">";
            final String postlog = "</mods>";
            try {
                result.qdc = qdc;
                result.xpath = XPath.newInstance(xpath);
                result.xpath.addNamespace(MODS_NS.getPrefix(), MODS_NS.getURI());
                result.xpath.addNamespace(XLINK_NS);
                Document d = builder.build(new StringReader(prolog + xml + postlog));
                result.xml = (Element) d.getRootElement().getContent(0);
            } catch (JDOMException | IOException je) {
                log.error("Error initializing modsTriple(\"" + qdc + "\",\"" + xml + "\",\"" + xpath + "\"): got " + je.toString());
                return null;
            }
            return result;
        }
    }

    private void initMap() throws CrosswalkInternalException {
        if (modsMap != null) {
            return;
        }
        String myAlias = getPluginInstanceName();
        if (myAlias == null) {
            log.error("Must use PluginService to instantiate MODSDisseminationCrosswalk so the class knows its name.");
            return;
        }
        String cmPropName = CONFIG_PREFIX + myAlias;
        String propsFilename = configurationService.getProperty(cmPropName);
        if (propsFilename == null) {
            String msg = "MODS crosswalk missing " + "configuration file for crosswalk named \"" + myAlias + "\"";
            log.error(msg);
            throw new CrosswalkInternalException(msg);
        } else {
            String parent = configurationService.getProperty("dspace.dir") + File.separator + "config" + File.separator;
            File propsFile = new File(parent, propsFilename);
            Properties modsConfig = new Properties();
            FileInputStream pfs = null;
            try {
                pfs = new FileInputStream(propsFile);
                modsConfig.load(pfs);
            } catch (IOException e) {
                log.error("Error opening or reading MODS properties file: " + propsFile.toString() + ": " + e.toString());
                throw new CrosswalkInternalException("MODS crosswalk cannot " + "open config file: " + e.toString(), e);
            } finally {
                if (pfs != null) {
                    try {
                        pfs.close();
                    } catch (IOException ioe) {
                    }
                }
            }
            modsMap = new HashMap<>();
            Enumeration<String> pe = (Enumeration<String>) modsConfig.propertyNames();
            while (pe.hasMoreElements()) {
                String qdc = pe.nextElement();
                String val = modsConfig.getProperty(qdc);
                String[] pair = val.split("\\s+\\|\\s+", 2);
                if (pair.length < 2) {
                    log.warn("Illegal MODS mapping in " + propsFile.toString() + ", line = " + qdc + " = " + val);
                } else {
                    modsTriple trip = modsTriple.create(qdc, pair[0], pair[1]);
                    if (trip != null) {
                        modsMap.put(qdc, trip);
                    }
                }
            }
        }
    }

    @Override
    public Namespace[] getNamespaces() {
        return (Namespace[]) ArrayUtils.clone(namespaces);
    }

    @Override
    public String getSchemaLocation() {
        return schemaLocation;
    }

    @Override
    public List<Element> disseminateList(Context context, DSpaceObject dso) throws CrosswalkException, IOException, SQLException, AuthorizeException {
        return disseminateListInternal(dso, true);
    }

    @Override
    public Element disseminateElement(Context context, DSpaceObject dso) throws CrosswalkException, IOException, SQLException, AuthorizeException {
        Element root = new Element("mods", MODS_NS);
        root.setAttribute("schemaLocation", schemaLocation, XSI_NS);
        root.addContent(disseminateListInternal(dso, false));
        return root;
    }

    private List<Element> disseminateListInternal(DSpaceObject dso, boolean addSchema) throws CrosswalkException, IOException, SQLException, AuthorizeException {
        List<MetadataValueDTO> dcvs = null;
        if (dso.getType() == Constants.ITEM) {
            dcvs = item2Metadata((Item) dso);
        } else if (dso.getType() == Constants.COLLECTION) {
            dcvs = collection2Metadata((Collection) dso);
        } else if (dso.getType() == Constants.COMMUNITY) {
            dcvs = community2Metadata((Community) dso);
        } else if (dso.getType() == Constants.SITE) {
            dcvs = site2Metadata((Site) dso);
        } else {
            throw new CrosswalkObjectNotSupported("MODSDisseminationCrosswalk can only crosswalk Items, Collections, or Communities");
        }
        initMap();
        List<Element> result = new ArrayList<>(dcvs.size());
        for (MetadataValueDTO dcv : dcvs) {
            String qdc = dcv.getSchema() + "." + dcv.getElement();
            if (dcv.getQualifier() != null) {
                qdc += "." + dcv.getQualifier();
            }
            String value = dcv.getValue();
            modsTriple trip = modsMap.get(qdc);
            if (trip == null) {
                log.warn("WARNING: " + getPluginInstanceName() + ": No MODS mapping for \"" + qdc + "\"");
            } else {
                try {
                    Element me = (Element) trip.xml.clone();
                    if (addSchema) {
                        me.setAttribute("schemaLocation", schemaLocation, XSI_NS);
                    }
                    Iterator ni = trip.xpath.selectNodes(me).iterator();
                    if (!ni.hasNext()) {
                        log.warn("XPath \"" + trip.xpath.getXPath() + "\" found no elements in \"" + outputUgly.outputString(me) + "\", qdc=" + qdc);
                    }
                    while (ni.hasNext()) {
                        Object what = ni.next();
                        if (what instanceof Element) {
                            ((Element) what).setText(checkedString(value));
                        } else if (what instanceof Attribute) {
                            ((Attribute) what).setValue(checkedString(value));
                        } else if (what instanceof Text) {
                            ((Text) what).setText(checkedString(value));
                        } else {
                            log.warn("Got unknown object from XPath, class=" + what.getClass().getName());
                        }
                    }
                    result.add(me);
                } catch (JDOMException je) {
                    log.error("Error following XPath in modsTriple: context=" + outputUgly.outputString(trip.xml) + ", xpath=" + trip.xpath.getXPath() + ", exception=" + je.toString());
                }
            }
        }
        return result;
    }

    @Override
    public boolean canDisseminate(DSpaceObject dso) {
        return (dso.getType() == Constants.ITEM || dso.getType() == Constants.COLLECTION || dso.getType() == Constants.COMMUNITY || dso.getType() == Constants.SITE);
    }

    @Override
    public boolean preferList() {
        return false;
    }

    protected List<MetadataValueDTO> site2Metadata(Site site) {
        List<MetadataValueDTO> metadata = new ArrayList<>();
        String identifier_uri = handleService.getCanonicalPrefix() + site.getHandle();
        String title = site.getName();
        String url = site.getURL();
        if (identifier_uri != null) {
            metadata.add(createDCValue("identifier.uri", null, identifier_uri));
        }
        if (url != null) {
            metadata.add(createDCValue("identifier.uri", null, url));
        }
        if (title != null) {
            metadata.add(createDCValue("title", null, title));
        }
        return metadata;
    }

    protected List<MetadataValueDTO> community2Metadata(Community community) {
        List<MetadataValueDTO> metadata = new ArrayList<>();
        String description = communityService.getMetadataFirstValue(community, CommunityService.MD_INTRODUCTORY_TEXT, Item.ANY);
        String description_abstract = communityService.getMetadataFirstValue(community, CommunityService.MD_SHORT_DESCRIPTION, Item.ANY);
        String description_table = communityService.getMetadataFirstValue(community, CommunityService.MD_SIDEBAR_TEXT, Item.ANY);
        String identifier_uri = handleService.getCanonicalPrefix() + community.getHandle();
        String rights = communityService.getMetadataFirstValue(community, CommunityService.MD_COPYRIGHT_TEXT, Item.ANY);
        String title = communityService.getMetadataFirstValue(community, CommunityService.MD_NAME, Item.ANY);
        metadata.add(createDCValue("description", null, description));
        if (description_abstract != null) {
            metadata.add(createDCValue("description", "abstract", description_abstract));
        }
        if (description_table != null) {
            metadata.add(createDCValue("description", "tableofcontents", description_table));
        }
        if (identifier_uri != null) {
            metadata.add(createDCValue("identifier.uri", null, identifier_uri));
        }
        if (rights != null) {
            metadata.add(createDCValue("rights", null, rights));
        }
        if (title != null) {
            metadata.add(createDCValue("title", null, title));
        }
        return metadata;
    }

    protected List<MetadataValueDTO> collection2Metadata(Collection collection) {
        List<MetadataValueDTO> metadata = new ArrayList<>();
        String description = collectionService.getMetadataFirstValue(collection, CollectionService.MD_INTRODUCTORY_TEXT, Item.ANY);
        String description_abstract = collectionService.getMetadataFirstValue(collection, CollectionService.MD_SHORT_DESCRIPTION, Item.ANY);
        String description_table = collectionService.getMetadataFirstValue(collection, CollectionService.MD_SIDEBAR_TEXT, Item.ANY);
        String identifier_uri = handleService.getCanonicalPrefix() + collection.getHandle();
        String provenance = collectionService.getMetadataFirstValue(collection, CollectionService.MD_PROVENANCE_DESCRIPTION, Item.ANY);
        String rights = collectionService.getMetadataFirstValue(collection, CollectionService.MD_COPYRIGHT_TEXT, Item.ANY);
        String rights_license = collectionService.getMetadataFirstValue(collection, CollectionService.MD_LICENSE, Item.ANY);
        String title = collectionService.getMetadataFirstValue(collection, CollectionService.MD_NAME, Item.ANY);
        if (description != null) {
            metadata.add(createDCValue("description", null, description));
        }
        if (description_abstract != null) {
            metadata.add(createDCValue("description", "abstract", description_abstract));
        }
        if (description_table != null) {
            metadata.add(createDCValue("description", "tableofcontents", description_table));
        }
        if (identifier_uri != null) {
            metadata.add(createDCValue("identifier", "uri", identifier_uri));
        }
        if (provenance != null) {
            metadata.add(createDCValue("provenance", null, provenance));
        }
        if (rights != null) {
            metadata.add(createDCValue("rights", null, rights));
        }
        if (rights_license != null) {
            metadata.add(createDCValue("rights.license", null, rights_license));
        }
        if (title != null) {
            metadata.add(createDCValue("title", null, title));
        }
        return metadata;
    }

    protected List<MetadataValueDTO> item2Metadata(Item item) {
        List<MetadataValue> dcvs = itemService.getMetadata(item, Item.ANY, Item.ANY, Item.ANY, Item.ANY);
        List<MetadataValueDTO> result = new ArrayList<>();
        for (MetadataValue metadataValue : dcvs) {
            result.add(new MetadataValueDTO(metadataValue));
        }
        return result;
    }

    protected MetadataValueDTO createDCValue(String element, String qualifier, String value) {
        MetadataValueDTO dcv = new MetadataValueDTO();
        dcv.setSchema("dc");
        dcv.setElement(element);
        dcv.setQualifier(qualifier);
        dcv.setValue(value);
        return dcv;
    }

    private String checkedString(String value) {
        if (value == null) {
            return null;
        }
        String reason = Verifier.checkCharacterData(value);
        if (reason == null) {
            return value;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Filtering out non-XML characters in string, reason=" + reason);
            }
            StringBuilder result = new StringBuilder(value.length());
            for (int i = 0; i < value.length(); ++i) {
                char c = value.charAt(i);
                if (Verifier.isXMLCharacter((int) c)) {
                    result.append(c);
                }
            }
            return result.toString();
        }
    }
}
