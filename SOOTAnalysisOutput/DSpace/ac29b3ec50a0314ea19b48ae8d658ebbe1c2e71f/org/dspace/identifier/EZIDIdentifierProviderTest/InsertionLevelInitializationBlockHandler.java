package org.dspace.identifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.dspace.AbstractUnitTest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.WorkspaceItem;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.WorkspaceItemService;
import org.dspace.identifier.ezid.DateToYear;
import org.dspace.identifier.ezid.Transform;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.workflow.WorkflowException;
import org.dspace.workflow.WorkflowItem;
import org.dspace.workflow.factory.WorkflowServiceFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class EZIDIdentifierProviderTest extends AbstractUnitTest {

    private static final String TEST_SHOULDER = "10.5072/FK2";

    private static final Map<String, String> aCrosswalk = new HashMap<>();

    static {
        aCrosswalk.put("datacite.creator", "dc.contributor.author");
        aCrosswalk.put("datacite.title", "dc.title");
        aCrosswalk.put("datacite.publisher", "dc.publisher");
        aCrosswalk.put("datacite.publicationyear", "dc.date.issued");
    }

    private static final Map<String, Transform> crosswalkTransforms = new HashMap();

    static {
        crosswalkTransforms.put("datacite.publicationyear", new DateToYear());
    }

    private static ConfigurationService config = null;

    private static Community community;

    private static Collection collection;

    protected CommunityService communityService = ContentServiceFactory.getInstance().getCommunityService();

    protected CollectionService collectionService = ContentServiceFactory.getInstance().getCollectionService();

    protected ItemService itemService = ContentServiceFactory.getInstance().getItemService();

    protected WorkspaceItemService workspaceItemService = ContentServiceFactory.getInstance().getWorkspaceItemService();

    private static Item item;

    public EZIDIdentifierProviderTest() {
    }

    private void dumpMetadata(Item eyetem) {
        List<MetadataValue> metadata = itemService.getMetadata(eyetem, "dc", Item.ANY, Item.ANY, Item.ANY);
        for (MetadataValue metadatum : metadata) {
            System.out.printf("Metadata:  %s.%s.%s(%s) = %s\n", metadatum.getMetadataField().getMetadataSchema().getName(), metadatum.getMetadataField().getElement(), metadatum.getMetadataField().getQualifier(), metadatum.getLanguage(), metadatum.getValue());
        }
    }

    private Item newItem() throws SQLException, AuthorizeException, IOException, WorkflowException {
        context.turnOffAuthorisationSystem();
        WorkspaceItem wsItem = workspaceItemService.create(context, collection, false);
        WorkflowItem wfItem = WorkflowServiceFactory.getInstance().getWorkflowService().start(context, wsItem);
        item = wfItem.getItem();
        itemService.addMetadata(context, item, "dc", "contributor", "author", null, "Author, A. N.");
        itemService.addMetadata(context, item, "dc", "title", null, null, "A Test Object");
        itemService.addMetadata(context, item, "dc", "publisher", null, null, "DSpace Test Harness");
        itemService.update(context, item);
        context.restoreAuthSystemState();
        return item;
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        config = DSpaceServicesFactory.getInstance().getConfigurationService();
        config.setProperty(EZIDIdentifierProvider.CFG_SHOULDER, TEST_SHOULDER);
        config.setProperty(EZIDIdentifierProvider.CFG_USER, "apitest");
        config.setProperty(EZIDIdentifierProvider.CFG_PASSWORD, "apitest");
        config.setProperty("mail.server.disabled", "true");
        EZIDIdentifierProvider instance = new EZIDIdentifierProvider();
        instance.setConfigurationService(config);
        instance.setCrosswalk(aCrosswalk);
        instance.setCrosswalkTransform(crosswalkTransforms);
        instance.setItemService(ContentServiceFactory.getInstance().getItemService());
        DSpaceServicesFactory.getInstance().getServiceManager().registerServiceNoAutowire(EZIDIdentifierProvider.class.getName(), instance);
        assertNotNull(DSpaceServicesFactory.getInstance().getServiceManager().getServiceByName(EZIDIdentifierProvider.class.getName(), EZIDIdentifierProvider.class));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        DSpaceServicesFactory.getInstance().getServiceManager().unregisterService(EZIDIdentifierProvider.class.getName());
        System.out.print("Tearing down\n\n");
    }

    @Before
    public void setUp() throws Exception {
        context.turnOffAuthorisationSystem();
        community = communityService.create(community, context);
        communityService.setMetadataSingleValue(context, community, CommunityService.MD_NAME, null, "A Test Community");
        communityService.update(context, community);
        collection = collectionService.create(context, community);
        collectionService.setMetadataSingleValue(context, collection, CollectionService.MD_NAME, null, "A Test Collection");
        collectionService.update(context, collection);
    }

    @After
    public void tearDown() throws SQLException {
        context.restoreAuthSystemState();
        dumpMetadata(item);
    }

    @Test
    public void testSupports_Class() {
        System.out.println("supports Class");
        EZIDIdentifierProvider instance = DSpaceServicesFactory.getInstance().getServiceManager().getServiceByName(EZIDIdentifierProvider.class.getName(), EZIDIdentifierProvider.class);
        Class<? extends Identifier> identifier = DOI.class;
        boolean result = instance.supports(identifier);
        assertTrue("DOI is supported", result);
    }

    @Test
    public void testSupports_String() {
        System.out.println("supports String");
        EZIDIdentifierProvider instance = DSpaceServicesFactory.getInstance().getServiceManager().getServiceByName(EZIDIdentifierProvider.class.getName(), EZIDIdentifierProvider.class);
        String identifier = "doi:" + TEST_SHOULDER;
        boolean result = instance.supports(identifier);
        assertTrue(identifier + " is supported", result);
    }

    @Test
    public void testCrosswalkMetadata() throws Exception {
        try {
            System.out.println("crosswalkMetadata");
            EZIDIdentifierProvider instance = DSpaceServicesFactory.getInstance().getServiceManager().getServiceByName(EZIDIdentifierProvider.class.getName(), EZIDIdentifierProvider.class);
            DSpaceObject dso = newItem();
            String handle = dso.getHandle();
            Map<String, String> metadata = instance.crosswalkMetadata(context, dso);
            String target = (String) metadata.get("_target");
            assertEquals("Generates correct _target metadatum", config.getProperty("dspace.ui.url") + "/handle/" + handle, target);
            assertTrue("Has title", metadata.containsKey("datacite.title"));
            assertTrue("Has publication year", metadata.containsKey("datacite.publicationyear"));
            assertTrue("Has publisher", metadata.containsKey("datacite.publisher"));
            assertTrue("Has creator", metadata.containsKey("datacite.creator"));
            System.out.println("Results:");
            for (Entry metadatum : metadata.entrySet()) {
                System.out.printf("  %s : %s\n", metadatum.getKey(), metadatum.getValue());
            }
        } catch (NullPointerException ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            System.out.println(sw.toString());
            org.apache.logging.log4j.LogManager.getLogger(EZIDIdentifierProviderTest.class).fatal("Caught NPE", ex);
            throw ex;
        }
    }
}