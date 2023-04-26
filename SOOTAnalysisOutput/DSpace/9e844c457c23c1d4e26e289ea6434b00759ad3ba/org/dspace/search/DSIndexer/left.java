package org.dspace.search;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.Vector;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DCValue;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.dao.ItemDAO;
import org.dspace.content.dao.ItemDAOFactory;
import org.dspace.uri.ObjectIdentifier;
import org.dspace.uri.IdentifierService;
import org.dspace.uri.IdentifierException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.LogManager;
import org.dspace.sort.SortOption;
import org.dspace.sort.OrderFormat;

public class DSIndexer {

    private static final Logger log = Logger.getLogger(DSIndexer.class);

    private static final String LAST_INDEXED_FIELD = "DSIndexer.lastIndexed";

    private static final long WRITE_LOCK_TIMEOUT = 30000;

    private static ItemDAO itemDAO;

    private static class IndexConfig {

        String indexName;

        String schema;

        String element;

        String qualifier = null;

        String type = "text";

        IndexConfig() {
        }

        IndexConfig(String indexName, String schema, String element, String qualifier, String type) {
            this.indexName = indexName;
            this.schema = schema;
            this.element = element;
            this.qualifier = qualifier;
            this.type = type;
        }
    }

    private static String index_directory = ConfigurationManager.getProperty("search.dir");

    private static int maxfieldlength = -1;

    private static Analyzer analyzer = null;

    private static IndexConfig[] indexConfigArr = new IndexConfig[] { new IndexConfig("author", "dc", "contributor", Item.ANY, "text"), new IndexConfig("author", "dc", "creator", Item.ANY, "text"), new IndexConfig("author", "dc", "description", "statementofresponsibility", "text"), new IndexConfig("title", "dc", "title", Item.ANY, "text"), new IndexConfig("keyword", "dc", "subject", Item.ANY, "text"), new IndexConfig("abstract", "dc", "description", "abstract", "text"), new IndexConfig("abstract", "dc", "description", "tableofcontents", "text"), new IndexConfig("series", "dc", "relation", "ispartofseries", "text"), new IndexConfig("mimetype", "dc", "format", "mimetype", "text"), new IndexConfig("sponsor", "dc", "description", "sponsorship", "text"), new IndexConfig("identifier", "dc", "identifier", Item.ANY, "text") };

    static {
        if (ConfigurationManager.getProperty("search.maxfieldlength") != null) {
            maxfieldlength = ConfigurationManager.getIntProperty("search.maxfieldlength");
        }
        ArrayList<String> indexConfigList = new ArrayList<String>();
        for (int i = 1; ConfigurationManager.getProperty("search.index." + i) != null; i++) {
            indexConfigList.add(ConfigurationManager.getProperty("search.index." + i));
        }
        if (indexConfigList.size() > 0) {
            indexConfigArr = new IndexConfig[indexConfigList.size()];
            for (int i = 0; i < indexConfigList.size(); i++) {
                indexConfigArr[i] = new IndexConfig();
                String index = indexConfigList.get(i);
                String[] configLine = index.split(":");
                indexConfigArr[i].indexName = configLine[0];
                String[] parts = configLine[1].split("\\.");
                switch(parts.length) {
                    case 3:
                        indexConfigArr[i].qualifier = parts[2];
                    case 2:
                        indexConfigArr[i].schema = parts[0];
                        indexConfigArr[i].element = parts[1];
                        break;
                    default:
                        log.warn("Malformed configuration line: search.index." + i);
                        throw new RuntimeException("Malformed configuration line: search.index." + i);
                }
                if (configLine.length > 2) {
                    indexConfigArr[i].type = configLine[2];
                }
            }
        }
        IndexWriter.setDefaultWriteLockTimeout(WRITE_LOCK_TIMEOUT);
        if (!IndexReader.indexExists(index_directory)) {
            try {
                new File(index_directory).mkdirs();
                openIndex(true).close();
            } catch (IOException e) {
                throw new RuntimeException("Could not create search index: " + e.getMessage(), e);
            }
        }
    }

    public static void indexContent(Context context, DSpaceObject dso) throws IOException {
        indexContent(context, dso, false);
    }

    public static void indexContent(Context context, DSpaceObject dso, boolean force) throws IOException {
        String uri = dso.getIdentifier().getCanonicalForm();
        Term t = new Term("uri", uri);
        try {
            switch(dso.getType()) {
                case Constants.ITEM:
                    Item item = (Item) dso;
                    if (item.isArchived() && !item.isWithdrawn()) {
                        if (requiresIndexing(t, ((Item) dso).getLastModified()) || force) {
                            buildDocument(context, (Item) dso, t);
                        }
                    } else {
                        DSIndexer.unIndexContent(context, uri);
                        log.info("Removed Item: " + uri + " from Index");
                    }
                    break;
                case Constants.COLLECTION:
                    buildDocument(context, (Collection) dso, t);
                    log.info("Wrote Collection: " + uri + " to Index");
                    break;
                case Constants.COMMUNITY:
                    buildDocument(context, (Community) dso, t);
                    log.info("Wrote Community: " + uri + " to Index");
                    break;
                default:
                    log.error("Only Items, Collections and Communities can be Indexed");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void unIndexContent(Context context, DSpaceObject dso) throws IOException {
        try {
            unIndexContent(context, dso.getIdentifier().getCanonicalForm());
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            emailException(exception);
        }
    }

    public static void unIndexContent(Context context, String uri) throws IOException {
        IndexWriter writer = openIndex(false);
        try {
            if (uri != null) {
                Term t = new Term("uri", uri);
                writer.deleteDocuments(t);
            } else {
                log.warn("unindex of content with null uri attempted");
            }
        } finally {
            writer.close();
        }
    }

    public static void reIndexContent(Context context, DSpaceObject dso) throws IOException {
        try {
            indexContent(context, dso);
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            emailException(exception);
        }
    }

    public static void createIndex(Context c) throws IOException {
        itemDAO = ItemDAOFactory.getInstance(c);
        openIndex(true).close();
        DSIndexer.updateIndex(c, true);
    }

    public static void optimizeIndex(Context c) throws IOException {
        IndexWriter writer = openIndex(false);
        try {
            writer.optimize();
        } finally {
            writer.close();
        }
    }

    public static void main(String[] args) throws IOException, SQLException {
        Context context = new Context();
        context.setIgnoreAuthorization(true);
        String usage = "org.dspace.search.DSIndexer [-cbhof[r <item uri>]] or nothing to update/clean an existing index.";
        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine line = null;
        options.addOption(OptionBuilder.withArgName("item uri").hasArg(true).withDescription("remove an Item, Collection or Community from index based on its uri").create("r"));
        options.addOption(OptionBuilder.isRequired(false).withDescription("optimize existing index").create("o"));
        options.addOption(OptionBuilder.isRequired(false).withDescription("clean existing index removing any documents that no longer exist in the db").create("c"));
        options.addOption(OptionBuilder.isRequired(false).withDescription("(re)build index, wiping out current one if it exists").create("b"));
        options.addOption(OptionBuilder.isRequired(false).withDescription("if updating existing index, force each document to be reindexed even if uptodate").create("f"));
        options.addOption(OptionBuilder.isRequired(false).withDescription("print this help message").create("h"));
        try {
            line = new PosixParser().parse(options, args);
        } catch (Exception e) {
            formatter.printHelp(usage, e.getMessage(), options, "");
            System.exit(1);
        }
        if (line.hasOption("h")) {
            formatter.printHelp(usage, options);
            System.exit(1);
        }
        if (line.hasOption("r")) {
            log.info("Removing " + line.getOptionValue("r") + " from Index");
            unIndexContent(context, line.getOptionValue("r"));
        } else if (line.hasOption("o")) {
            log.info("Optimizing Index");
            optimizeIndex(context);
        } else if (line.hasOption("c")) {
            log.info("Cleaning Index");
            cleanIndex(context);
        } else if (line.hasOption("b")) {
            log.info("(Re)building index from scratch.");
            createIndex(context);
        } else {
            log.info("Updating and Cleaning Index");
            cleanIndex(context);
            updateIndex(context, line.hasOption("f"));
        }
        log.info("Done with indexing");
    }

    public static void updateIndex(Context context) {
        updateIndex(context, false);
    }

    public static void updateIndex(Context context, boolean force) {
        try {
            for (Item item : itemDAO.getItems()) {
                indexContent(context, item, force);
                item.decache();
            }
            Collection[] collections = Collection.findAll(context);
            for (int i = 0; i < collections.length; i++) {
                indexContent(context, collections[i], force);
                context.removeCached(collections[i], collections[i].getID());
            }
            Community[] communities = Community.findAll(context);
            for (int i = 0; i < communities.length; i++) {
                indexContent(context, communities[i], force);
                context.removeCached(communities[i], communities[i].getID());
            }
            optimizeIndex(context);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void cleanIndex(Context context) throws IOException {
        try {
            ObjectIdentifier oi = null;
            IndexReader reader = DSQuery.getIndexReader();
            for (int i = 0; i < reader.numDocs(); i++) {
                if (!reader.isDeleted(i)) {
                    Document doc = reader.document(i);
                    String uri = doc.get("uri");
                    oi = ObjectIdentifier.parseCanonicalForm(uri);
                    DSpaceObject o = (DSpaceObject) IdentifierService.getResource(context, oi);
                    if (o == null) {
                        log.info("Deleting: " + uri);
                        DSIndexer.unIndexContent(context, uri);
                    } else {
                        context.removeCached(o, o.getID());
                        log.debug("Keeping: " + uri);
                    }
                } else {
                    log.debug("Encountered deleted doc: " + i);
                }
            }
        } catch (IdentifierException e) {
            log.error("caught exception: ", e);
            throw new RuntimeException(e);
        }
    }

    static Analyzer getAnalyzer() throws IllegalStateException {
        if (analyzer == null) {
            String analyzerClassName = ConfigurationManager.getProperty("search.analyzer");
            if (analyzerClassName == null) {
                analyzerClassName = "org.dspace.search.DSAnalyzer";
            }
            try {
                Class analyzerClass = Class.forName(analyzerClassName);
                analyzer = (Analyzer) analyzerClass.newInstance();
            } catch (Exception e) {
                log.fatal(LogManager.getHeader(null, "no_search_analyzer", "search.analyzer=" + analyzerClassName), e);
                throw new IllegalStateException(e.toString());
            }
        }
        return analyzer;
    }

    private static void emailException(Exception exception) {
        try {
            String recipient = ConfigurationManager.getProperty("alert.recipient");
            if (recipient != null) {
                Email email = ConfigurationManager.getEmail("internal_error");
                email.addRecipient(recipient);
                email.addArgument(ConfigurationManager.getProperty("dspace.url"));
                email.addArgument(new Date());
                String stackTrace;
                if (exception != null) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    exception.printStackTrace(pw);
                    pw.flush();
                    stackTrace = sw.toString();
                } else {
                    stackTrace = "No exception";
                }
                email.addArgument(stackTrace);
                email.send();
            }
        } catch (Exception e) {
            log.warn("Unable to send email alert", e);
        }
    }

    private static boolean requiresIndexing(Term t, Date lastModified) throws SQLException, IOException {
        boolean reindexItem = false;
        boolean inIndex = false;
        IndexReader ir = DSQuery.getIndexReader();
        TermDocs docs = ir.termDocs(t);
        while (docs.next()) {
            inIndex = true;
            int id = docs.doc();
            Document doc = ir.document(id);
            Field lastIndexed = doc.getField(LAST_INDEXED_FIELD);
            if (lastIndexed == null || Long.parseLong(lastIndexed.stringValue()) < lastModified.getTime()) {
                reindexItem = true;
            }
        }
        return reindexItem || !inIndex;
    }

    private static IndexWriter openIndex(boolean wipe_existing) throws IOException {
        IndexWriter writer = new IndexWriter(index_directory, getAnalyzer(), wipe_existing);
        if (maxfieldlength == -1) {
            writer.setMaxFieldLength(Integer.MAX_VALUE);
        } else {
            writer.setMaxFieldLength(maxfieldlength);
        }
        return writer;
    }

    private static String buildItemLocationString(Context c, Item myitem) {
        Community[] communities = myitem.getCommunities();
        Collection[] collections = myitem.getCollections();
        String location = "";
        int i = 0;
        for (i = 0; i < communities.length; i++) location = location + " m" + communities[i].getID();
        for (i = 0; i < collections.length; i++) location = location + " l" + collections[i].getID();
        return location;
    }

    private static String buildCollectionLocationString(Context c, Collection target) {
        Community[] communities = target.getCommunities();
        String location = "";
        int i = 0;
        for (i = 0; i < communities.length; i++) location = location + " m" + communities[i].getID();
        return location;
    }

    private static void writeDocument(Term t, Document doc) throws IOException {
        IndexWriter writer = null;
        try {
            writer = openIndex(false);
            writer.updateDocument(t, doc);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static void buildDocument(Context context, Community community, Term t) throws SQLException, IOException {
        String uri = community.getIdentifier().getCanonicalForm();
        Document doc = buildDocument(Constants.COMMUNITY, community.getID(), uri, null);
        String name = community.getMetadata("name");
        if (name != null) {
            doc.add(new Field("name", name, Field.Store.NO, Field.Index.TOKENIZED));
            doc.add(new Field("default", name, Field.Store.NO, Field.Index.TOKENIZED));
        }
        writeDocument(t, doc);
    }

    private static void buildDocument(Context context, Collection collection, Term t) throws SQLException, IOException {
        String location_text = buildCollectionLocationString(context, collection);
        String uri = collection.getIdentifier().getCanonicalForm();
        Document doc = buildDocument(Constants.COLLECTION, collection.getID(), uri, location_text);
        String name = collection.getMetadata("name");
        if (name != null) {
            doc.add(new Field("name", name, Field.Store.NO, Field.Index.TOKENIZED));
            doc.add(new Field("default", name, Field.Store.NO, Field.Index.TOKENIZED));
        }
        writeDocument(t, doc);
    }

    private static void buildDocument(Context context, Item item, Term t) throws SQLException, IOException {
        String location = buildItemLocationString(context, item);
        String uri = item.getIdentifier().getCanonicalForm();
        Document doc = buildDocument(Constants.ITEM, item.getID(), uri, location);
        log.debug("Building Item: " + uri);
        int j;
        int k = 0;
        if (indexConfigArr.length > 0) {
            DCValue[] mydc;
            for (int i = 0; i < indexConfigArr.length; i++) {
                if (indexConfigArr[i].qualifier != null && indexConfigArr[i].qualifier.equals("*")) {
                    mydc = item.getMetadata(indexConfigArr[i].schema, indexConfigArr[i].element, Item.ANY, Item.ANY);
                } else {
                    mydc = item.getMetadata(indexConfigArr[i].schema, indexConfigArr[i].element, indexConfigArr[i].qualifier, Item.ANY);
                }
                for (j = 0; j < mydc.length; j++) {
                    if (!StringUtils.isEmpty(mydc[j].value)) {
                        if ("timestamp".equalsIgnoreCase(indexConfigArr[i].type)) {
                            Date d = toDate(mydc[j].value);
                            if (d != null) {
                                doc.add(new Field(indexConfigArr[i].indexName, DateTools.dateToString(d, DateTools.Resolution.SECOND), Field.Store.NO, Field.Index.UN_TOKENIZED));
                                doc.add(new Field(indexConfigArr[i].indexName + ".year", DateTools.dateToString(d, DateTools.Resolution.YEAR), Field.Store.NO, Field.Index.UN_TOKENIZED));
                            }
                        } else if ("date".equalsIgnoreCase(indexConfigArr[i].type)) {
                            Date d = toDate(mydc[j].value);
                            if (d != null) {
                                doc.add(new Field(indexConfigArr[i].indexName, DateTools.dateToString(d, DateTools.Resolution.DAY), Field.Store.NO, Field.Index.UN_TOKENIZED));
                                doc.add(new Field(indexConfigArr[i].indexName + ".year", DateTools.dateToString(d, DateTools.Resolution.YEAR), Field.Store.NO, Field.Index.UN_TOKENIZED));
                            }
                        } else {
                            doc.add(new Field(indexConfigArr[i].indexName, mydc[j].value, Field.Store.NO, Field.Index.TOKENIZED));
                        }
                        doc.add(new Field("default", mydc[j].value, Field.Store.NO, Field.Index.TOKENIZED));
                    }
                }
            }
        }
        log.debug("  Added Metadata");
        try {
            for (SortOption so : SortOption.getSortOptions()) {
                String[] somd = so.getMdBits();
                DCValue[] dcv = item.getMetadata(somd[0], somd[1], somd[2], Item.ANY);
                if (dcv.length > 0) {
                    String value = OrderFormat.makeSortString(dcv[0].value, dcv[0].language, so.getType());
                    doc.add(new Field("sort_" + so.getName(), value, Field.Store.NO, Field.Index.UN_TOKENIZED));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.debug("  Added Sorting");
        Vector<InputStreamReader> readers = new Vector<InputStreamReader>();
        try {
            Bundle[] myBundles = item.getBundles();
            for (int i = 0; i < myBundles.length; i++) {
                if ((myBundles[i].getName() != null) && myBundles[i].getName().equals("TEXT")) {
                    Bitstream[] myBitstreams = myBundles[i].getBitstreams();
                    for (j = 0; j < myBitstreams.length; j++) {
                        try {
                            InputStreamReader is = new InputStreamReader(myBitstreams[j].retrieve());
                            readers.add(is);
                            doc.add(new Field("default", is));
                            log.debug("  Added BitStream: " + myBitstreams[j].getStoreNumber() + "	" + myBitstreams[j].getSequenceID() + "   " + myBitstreams[j].getName());
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        try {
            writeDocument(t, doc);
            log.info("Wrote Item: " + uri + " to Index");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            Iterator<InputStreamReader> itr = readers.iterator();
            while (itr.hasNext()) {
                InputStreamReader reader = itr.next();
                if (reader != null) {
                    reader.close();
                }
            }
            log.debug("closed " + readers.size() + " readers");
        }
    }

    private static Document buildDocument(int type, int id, String uri, String location) {
        Document doc = new Document();
        doc.add(new Field(LAST_INDEXED_FIELD, Long.toString(System.currentTimeMillis()), Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("type", Integer.toString(type), Field.Store.YES, Field.Index.NO));
        doc.add(new Field("search.resourcetype", Integer.toString(type), Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("search.resourceid", Integer.toString(id), Field.Store.YES, Field.Index.NO));
        if (uri != null) {
            doc.add(new Field("handletext", uri, Field.Store.YES, Field.Index.TOKENIZED));
            doc.add(new Field("uri", uri, Field.Store.YES, Field.Index.UN_TOKENIZED));
            doc.add(new Field("default", uri, Field.Store.NO, Field.Index.TOKENIZED));
        }
        if (location != null) {
            doc.add(new Field("location", location, Field.Store.NO, Field.Index.TOKENIZED));
            doc.add(new Field("default", location, Field.Store.NO, Field.Index.TOKENIZED));
        }
        return doc;
    }

    private static Date toDate(String t) {
        SimpleDateFormat[] dfArr;
        switch(t.length()) {
            case 4:
                dfArr = new SimpleDateFormat[] { new SimpleDateFormat("yyyy") };
                break;
            case 6:
                dfArr = new SimpleDateFormat[] { new SimpleDateFormat("yyyyMM") };
                break;
            case 7:
                dfArr = new SimpleDateFormat[] { new SimpleDateFormat("yyyy-MM") };
                break;
            case 8:
                dfArr = new SimpleDateFormat[] { new SimpleDateFormat("yyyyMMdd"), new SimpleDateFormat("yyyy MMM") };
                break;
            case 10:
                dfArr = new SimpleDateFormat[] { new SimpleDateFormat("yyyy-MM-dd") };
                break;
            case 11:
                dfArr = new SimpleDateFormat[] { new SimpleDateFormat("yyyy MMM dd") };
                break;
            case 20:
                dfArr = new SimpleDateFormat[] { new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'") };
                break;
            default:
                dfArr = new SimpleDateFormat[] { new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") };
                break;
        }
        for (SimpleDateFormat df : dfArr) {
            try {
                df.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
                df.setLenient(false);
                return df.parse(t);
            } catch (ParseException pe) {
                log.error("Unable to parse date format", pe);
            }
        }
        return null;
    }
}
