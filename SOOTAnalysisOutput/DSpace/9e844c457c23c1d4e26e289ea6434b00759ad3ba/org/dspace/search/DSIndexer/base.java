package org.dspace.search;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
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
import org.dspace.content.ItemIterator;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.LogManager;
import org.dspace.handle.HandleManager;

public class DSIndexer {

    private static final Logger log = Logger.getLogger(DSIndexer.class);

    private static final String LAST_INDEXED_FIELD = "DSIndexer.lastIndexed";

    private static final long WRITE_LOCK_TIMEOUT = 30000;

    private static class IndexConfig {

        String indexName;

        String schema;

        String element;

        String qualifier = null;
    }

    private static String index_directory = ConfigurationManager.getProperty("search.dir");

    private static int maxfieldlength = -1;

    private static Analyzer analyzer = null;

    private static IndexConfig[] indexConfigArr = new IndexConfig[0];

    static {
        if (ConfigurationManager.getProperty("search.maxfieldlength") != null) {
            maxfieldlength = ConfigurationManager.getIntProperty("search.maxfieldlength");
        }
        ArrayList indexConfigList = new ArrayList();
        for (int i = 1; ConfigurationManager.getProperty("search.index." + i) != null; i++) {
            indexConfigList.add(ConfigurationManager.getProperty("search.index." + i));
        }
        if (indexConfigList.size() > 0) {
            indexConfigArr = new IndexConfig[indexConfigList.size()];
            for (int i = 0; i < indexConfigList.size(); i++) {
                indexConfigArr[i] = new IndexConfig();
                String index = (String) indexConfigList.get(i);
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
            }
        }
        IndexWriter.setDefaultWriteLockTimeout(WRITE_LOCK_TIMEOUT);
        if (!IndexReader.indexExists(index_directory)) {
            try {
                new File(index_directory).mkdirs();
                openIndex(null, true).close();
            } catch (IOException e) {
                throw new RuntimeException("Could not create search index: " + e.getMessage(), e);
            }
        }
    }

    public static void indexContent(Context context, DSpaceObject dso) throws SQLException, IOException {
        indexContent(context, dso, false);
    }

    public static void indexContent(Context context, DSpaceObject dso, boolean force) throws SQLException, IOException {
        String handle = dso.getHandle();
        if (handle == null) {
            handle = HandleManager.findHandle(context, dso);
        }
        Term t = new Term("handle", handle);
        IndexWriter writer = null;
        try {
            switch(dso.getType()) {
                case Constants.ITEM:
                    Item item = (Item) dso;
                    if (item.isArchived() && !item.isWithdrawn()) {
                        if (requiresIndexing(handle, ((Item) dso).getLastModified()) || force) {
                            Document doc = buildDocument(context, (Item) dso);
                            writer = openIndex(context, false);
                            writer.updateDocument(t, doc);
                            log.info("Wrote Item: " + handle + " to Index");
                        }
                    }
                    break;
                case Constants.COLLECTION:
                    writer = openIndex(context, false);
                    writer.updateDocument(t, buildDocument(context, (Collection) dso));
                    log.info("Wrote Collection: " + handle + " to Index");
                    break;
                case Constants.COMMUNITY:
                    writer = openIndex(context, false);
                    writer.updateDocument(t, buildDocument(context, (Community) dso));
                    log.info("Wrote Community: " + handle + " to Index");
                    break;
                default:
                    log.error("Only Items, Collections and Communities can be Indexed");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (writer != null)
                writer.close();
        }
    }

    public static void unIndexContent(Context context, DSpaceObject dso) throws SQLException, IOException {
        try {
            unIndexContent(context, dso.getHandle());
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            emailException(exception);
        }
    }

    public static void unIndexContent(Context context, String handle) throws SQLException, IOException {
        IndexWriter writer = openIndex(context, false);
        try {
            if (handle != null) {
                Term t = new Term("handle", handle);
                writer.deleteDocuments(t);
            } else {
                log.warn("unindex of content with null handle attempted");
            }
        } finally {
            writer.close();
        }
    }

    public static void reIndexContent(Context context, DSpaceObject dso) throws SQLException, IOException {
        try {
            indexContent(context, dso);
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            emailException(exception);
        }
    }

    public static void createIndex(Context c) throws SQLException, IOException {
        openIndex(c, true).close();
        DSIndexer.updateIndex(c, true);
    }

    public static void optimizeIndex(Context c) throws SQLException, IOException {
        IndexWriter writer = openIndex(c, false);
        try {
            writer.optimize();
        } finally {
            writer.close();
        }
    }

    public static void main(String[] args) throws SQLException, IOException {
        Context context = new Context();
        context.setIgnoreAuthorization(true);
        String usage = "org.dspace.search.DSIndexer [-cbhouf[d <item handle>]] or nothing to update/clean an existing index.";
        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine line = null;
        options.addOption(OptionBuilder.withArgName("item handle").hasArg(true).withDescription("delete an Item, Collection or Community from index based on its handle").create("d"));
        options.addOption(OptionBuilder.isRequired(false).withDescription("optimize existing index").create("o"));
        options.addOption(OptionBuilder.isRequired(false).withDescription("clean existing index removing any documents that no longer exist in the db").create("c"));
        options.addOption(OptionBuilder.isRequired(false).withDescription("(re)build index, wiping out current one if it exists").create("b"));
        options.addOption(OptionBuilder.isRequired(false).withDescription("if updating existing index, force each handle to be reindexed even if uptodate").create("f"));
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
        } else if (line.hasOption("u")) {
            log.info("Updating Index");
            updateIndex(context, line.hasOption("f"));
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
            for (ItemIterator i = Item.findAll(context); i.hasNext(); ) {
                Item item = (Item) i.next();
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

    public static void cleanIndex(Context context) throws IOException, SQLException {
        IndexReader reader = DSQuery.getIndexReader();
        for (int i = 0; i < reader.numDocs(); i++) {
            if (!reader.isDeleted(i)) {
                Document doc = reader.document(i);
                String handle = doc.get("handle");
                DSpaceObject o = HandleManager.resolveToObject(context, handle);
                if (o == null) {
                    log.info("Deleting: " + handle);
                    DSIndexer.unIndexContent(context, handle);
                } else {
                    context.removeCached(o, o.getID());
                    log.debug("Keeping: " + handle);
                }
            } else {
                log.debug("Encountered deleted doc: " + i);
            }
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

    private static boolean requiresIndexing(String handle, Date lastModified) throws SQLException, IOException {
        boolean reindexItem = false;
        boolean inIndex = false;
        IndexReader ir = DSQuery.getIndexReader();
        Term t = new Term("handle", handle);
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

    private static IndexWriter openIndex(Context c, boolean wipe_existing) throws IOException {
        IndexWriter writer = new IndexWriter(index_directory, getAnalyzer(), wipe_existing);
        if (maxfieldlength == -1) {
            writer.setMaxFieldLength(Integer.MAX_VALUE);
        } else {
            writer.setMaxFieldLength(maxfieldlength);
        }
        return writer;
    }

    private static String buildItemLocationString(Context c, Item myitem) throws SQLException {
        Community[] communities = myitem.getCommunities();
        Collection[] collections = myitem.getCollections();
        String location = "";
        int i = 0;
        for (i = 0; i < communities.length; i++) location = new String(location + " m" + communities[i].getID());
        for (i = 0; i < collections.length; i++) location = new String(location + " l" + collections[i].getID());
        return location;
    }

    private static String buildCollectionLocationString(Context c, Collection target) throws SQLException {
        Community[] communities = target.getCommunities();
        String location = "";
        int i = 0;
        for (i = 0; i < communities.length; i++) location = new String(location + " m" + communities[i].getID());
        return location;
    }

    private static Document buildDocument(Context context, Community community) throws SQLException, IOException {
        Document doc = buildDocument(Constants.COMMUNITY, community.getHandle(), null);
        String name = community.getMetadata("name");
        if (name != null) {
            doc.add(new Field("name", name, Field.Store.YES, Field.Index.TOKENIZED));
            doc.add(new Field("default", name, Field.Store.YES, Field.Index.TOKENIZED));
        }
        return doc;
    }

    private static Document buildDocument(Context context, Collection collection) throws SQLException, IOException {
        String location_text = buildCollectionLocationString(context, collection);
        Document doc = buildDocument(Constants.COLLECTION, collection.getHandle(), location_text);
        String name = collection.getMetadata("name");
        if (name != null) {
            doc.add(new Field("name", name, Field.Store.YES, Field.Index.TOKENIZED));
            doc.add(new Field("default", name, Field.Store.YES, Field.Index.TOKENIZED));
        }
        return doc;
    }

    private static Document buildDocument(Context context, Item item) throws SQLException, IOException {
        String handle = item.getHandle();
        if (handle == null) {
            handle = HandleManager.findHandle(context, item);
        }
        String location = buildItemLocationString(context, item);
        Document doc = buildDocument(Constants.ITEM, handle, location);
        log.debug("Building Item: " + handle);
        int j;
        int k = 0;
        if (indexConfigArr.length > 0) {
            ArrayList fields = new ArrayList();
            ArrayList content = new ArrayList();
            DCValue[] mydc;
            for (int i = 0; i < indexConfigArr.length; i++) {
                if (indexConfigArr[i].qualifier != null && indexConfigArr[i].qualifier.equals("*")) {
                    mydc = item.getMetadata(indexConfigArr[i].schema, indexConfigArr[i].element, Item.ANY, Item.ANY);
                } else {
                    mydc = item.getMetadata(indexConfigArr[i].schema, indexConfigArr[i].element, indexConfigArr[i].qualifier, Item.ANY);
                }
                String content_text = "";
                for (j = 0; j < mydc.length; j++) {
                    content_text = new String(content_text + mydc[j].value + " ");
                }
                k = fields.indexOf(indexConfigArr[i].indexName);
                if (k < 0) {
                    fields.add(indexConfigArr[i].indexName);
                    content.add(content_text);
                } else {
                    content_text = new String(content_text + (String) content.get(k) + " ");
                    content.set(k, content_text);
                }
            }
            for (int i = 0; i < fields.size(); i++) {
                doc.add(new Field((String) fields.get(i), (String) content.get(i), Field.Store.YES, Field.Index.TOKENIZED));
                doc.add(new Field("default", (String) content.get(i), Field.Store.YES, Field.Index.TOKENIZED));
            }
        } else {
            DCValue[] authors = item.getDC("contributor", Item.ANY, Item.ANY);
            for (j = 0; j < authors.length; j++) {
                doc.add(new Field("author", authors[j].value, Field.Store.YES, Field.Index.TOKENIZED));
                doc.add(new Field("default", authors[j].value, Field.Store.YES, Field.Index.TOKENIZED));
            }
            DCValue[] creators = item.getDC("creator", Item.ANY, Item.ANY);
            for (j = 0; j < creators.length; j++) {
                doc.add(new Field("author", creators[j].value, Field.Store.YES, Field.Index.TOKENIZED));
                doc.add(new Field("default", creators[j].value, Field.Store.YES, Field.Index.TOKENIZED));
            }
            DCValue[] sors = item.getDC("description", "statementofresponsibility", Item.ANY);
            for (j = 0; j < sors.length; j++) {
                doc.add(new Field("author", sors[j].value, Field.Store.YES, Field.Index.TOKENIZED));
                doc.add(new Field("default", sors[j].value, Field.Store.YES, Field.Index.TOKENIZED));
            }
            DCValue[] titles = item.getDC("title", Item.ANY, Item.ANY);
            for (j = 0; j < titles.length; j++) {
                doc.add(new Field("title", titles[j].value, Field.Store.YES, Field.Index.TOKENIZED));
                doc.add(new Field("default", titles[j].value, Field.Store.YES, Field.Index.TOKENIZED));
            }
            DCValue[] keywords = item.getDC("subject", Item.ANY, Item.ANY);
            for (j = 0; j < keywords.length; j++) {
                doc.add(new Field("keyword", keywords[j].value, Field.Store.YES, Field.Index.TOKENIZED));
                doc.add(new Field("default", keywords[j].value, Field.Store.YES, Field.Index.TOKENIZED));
            }
            DCValue[] abstracts = item.getDC("description", "abstract", Item.ANY);
            for (j = 0; j < abstracts.length; j++) {
                doc.add(new Field("abstract", abstracts[j].value, Field.Store.YES, Field.Index.TOKENIZED));
                doc.add(new Field("default", abstracts[j].value, Field.Store.YES, Field.Index.TOKENIZED));
            }
            DCValue[] tocs = item.getDC("description", "tableofcontents", Item.ANY);
            for (j = 0; j < tocs.length; j++) {
                doc.add(new Field("abstract", tocs[j].value, Field.Store.YES, Field.Index.TOKENIZED));
                doc.add(new Field("default", tocs[j].value, Field.Store.YES, Field.Index.TOKENIZED));
            }
            DCValue[] series = item.getDC("relation", "ispartofseries", Item.ANY);
            for (j = 0; j < series.length; j++) {
                doc.add(new Field("series", series[j].value, Field.Store.YES, Field.Index.TOKENIZED));
                doc.add(new Field("default", series[j].value, Field.Store.YES, Field.Index.TOKENIZED));
            }
            DCValue[] mimetypes = item.getDC("format", "mimetype", Item.ANY);
            for (j = 0; j < mimetypes.length; j++) {
                doc.add(new Field("mimetype", mimetypes[j].value, Field.Store.YES, Field.Index.TOKENIZED));
                doc.add(new Field("default", mimetypes[j].value, Field.Store.YES, Field.Index.TOKENIZED));
            }
            DCValue[] sponsors = item.getDC("description", "sponsorship", Item.ANY);
            for (j = 0; j < sponsors.length; j++) {
                doc.add(new Field("sponsor", sponsors[j].value, Field.Store.YES, Field.Index.TOKENIZED));
                doc.add(new Field("default", sponsors[j].value, Field.Store.YES, Field.Index.TOKENIZED));
            }
            DCValue[] identifiers = item.getDC("identifier", Item.ANY, Item.ANY);
            for (j = 0; j < identifiers.length; j++) {
                doc.add(new Field("identifier", identifiers[j].value, Field.Store.YES, Field.Index.TOKENIZED));
                doc.add(new Field("default", identifiers[j].value, Field.Store.YES, Field.Index.TOKENIZED));
            }
        }
        log.debug("  Added Metadata");
        try {
            Bundle[] myBundles = item.getBundles();
            for (int i = 0; i < myBundles.length; i++) {
                if ((myBundles[i].getName() != null) && myBundles[i].getName().equals("TEXT")) {
                    Bitstream[] myBitstreams = myBundles[i].getBitstreams();
                    for (j = 0; j < myBitstreams.length; j++) {
                        try {
                            InputStreamReader is = new InputStreamReader(myBitstreams[j].retrieve());
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
        return doc;
    }

    private static Document buildDocument(int type, String handle, String location) {
        Document doc = new Document();
        doc.add(new Field(LAST_INDEXED_FIELD, Long.toString(System.currentTimeMillis()), Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("type", Integer.toString(type), Field.Store.YES, Field.Index.NO));
        if (handle != null) {
            doc.add(new Field("handletext", handle, Field.Store.YES, Field.Index.TOKENIZED));
            doc.add(new Field("handle", handle, Field.Store.YES, Field.Index.UN_TOKENIZED));
            doc.add(new Field("default", handle, Field.Store.YES, Field.Index.TOKENIZED));
        }
        if (location != null) {
            doc.add(new Field("location", location, Field.Store.YES, Field.Index.TOKENIZED));
            doc.add(new Field("default", location, Field.Store.YES, Field.Index.TOKENIZED));
        }
        return doc;
    }
}
