package org.opensolaris.opengrok.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.spell.DirectSpellChecker;
import org.apache.lucene.search.spell.SuggestMode;
import org.apache.lucene.search.spell.SuggestWord;
import org.apache.lucene.store.FSDirectory;
import org.opensolaris.opengrok.OpenGrokLogger;
import org.opensolaris.opengrok.analysis.CompatibleAnalyser;
import org.opensolaris.opengrok.analysis.Definitions;
import org.opensolaris.opengrok.index.IndexDatabase;
import org.opensolaris.opengrok.search.QueryBuilder;
import org.opensolaris.opengrok.search.Summarizer;
import org.opensolaris.opengrok.search.context.Context;
import org.opensolaris.opengrok.search.context.HistoryContext;
import org.opensolaris.opengrok.util.IOUtils;

public class SearchHelper {

    public int SPELLCHECK_SUGGEST_WORD_COUNT = 5;

    public File dataRoot;

    public String contextPath;

    public boolean compressed;

    public File sourceRoot;

    public EftarFileReader desc;

    public int start;

    public int maxItems;

    public QueryBuilder builder;

    public SortOrder order;

    public boolean parallel;

    public boolean isCrossRefSearch;

    public String redirect;

    public String errorMsg;

    public IndexSearcher searcher;

    public ScoreDoc[] hits;

    public int totalHits;

    public Query query;

    protected Sort sort;

    protected DirectSpellChecker checker;

    public SortedSet<String> projects;

    public Context sourceContext = null;

    public Summarizer summerizer = null;

    public HistoryContext historyContext;

    private static final Map<String, String> fileTypeDescription;

    public static final String PARSE_ERROR_MSG = "Unable to parse your query: ";

    private ExecutorService executor = null;

    private static final Logger log = Logger.getLogger(SearchHelper.class.getName());

    static {
        fileTypeDescription = new TreeMap<>();
        fileTypeDescription.put("xml", "XML");
        fileTypeDescription.put("troff", "Troff");
        fileTypeDescription.put("elf", "ELF");
        fileTypeDescription.put("javaclass", "Java class");
        fileTypeDescription.put("image", "Image file");
        fileTypeDescription.put("c", "C");
        fileTypeDescription.put("csharp", "C#");
        fileTypeDescription.put("vb", "Visual Basic");
        fileTypeDescription.put("cxx", "C++");
        fileTypeDescription.put("sh", "Shell script");
        fileTypeDescription.put("java", "Java");
        fileTypeDescription.put("javascript", "JavaScript");
        fileTypeDescription.put("python", "Python");
        fileTypeDescription.put("perl", "Perl");
        fileTypeDescription.put("php", "PHP");
        fileTypeDescription.put("lisp", "Lisp");
        fileTypeDescription.put("tcl", "Tcl");
        fileTypeDescription.put("scala", "Scala");
        fileTypeDescription.put("sql", "SQL");
        fileTypeDescription.put("plsql", "PL/SQL");
        fileTypeDescription.put("fortran", "Fortran");
    }

    public static Set<Map.Entry<String, String>> getFileTypeDescirptions() {
        return fileTypeDescription.entrySet();
    }

    File indexDir;

    public SearchHelper prepareExec(SortedSet<String> projects) {
        if (redirect != null || errorMsg != null) {
            return this;
        }
        try {
            indexDir = new File(dataRoot, IndexDatabase.INDEX_DIR);
            query = builder.build();
            if (projects == null) {
                errorMsg = "No project selected!";
                return this;
            }
            this.projects = projects;
            if (projects.isEmpty()) {
                FSDirectory dir = FSDirectory.open(indexDir);
                searcher = new IndexSearcher(DirectoryReader.open(dir));
            } else if (projects.size() == 1) {
                FSDirectory dir = FSDirectory.open(new File(indexDir, projects.first()));
                searcher = new IndexSearcher(DirectoryReader.open(dir));
            } else {
                IndexReader[] subreaders = new IndexReader[projects.size()];
                int ii = 0;
                for (String proj : projects) {
                    FSDirectory dir = FSDirectory.open(new File(indexDir, proj));
                    subreaders[ii++] = DirectoryReader.open(dir);
                }
                MultiReader searchables = new MultiReader(subreaders, true);
                if (parallel) {
                    int noThreads = 2 + (2 * Runtime.getRuntime().availableProcessors());
                    executor = Executors.newFixedThreadPool(noThreads);
                }
                searcher = parallel ? new IndexSearcher(searchables, executor) : new IndexSearcher(searchables);
            }
            switch(order) {
                case LASTMODIFIED:
                    sort = new Sort(new SortField(QueryBuilder.DATE, SortField.Type.STRING, true));
                    break;
                case BY_PATH:
                    sort = new Sort(new SortField(QueryBuilder.FULLPATH, SortField.Type.STRING));
                    break;
                default:
                    sort = Sort.RELEVANCE;
                    break;
            }
            checker = new DirectSpellChecker();
        } catch (ParseException e) {
            errorMsg = PARSE_ERROR_MSG + e.getMessage();
        } catch (FileNotFoundException e) {
            errorMsg = "Index database(s) not found.";
        } catch (Exception e) {
            errorMsg = e.getMessage();
        }
        return this;
    }

    public SearchHelper executeQuery() {
        if (redirect != null || errorMsg != null) {
            return this;
        }
        try {
            TopFieldDocs fdocs = searcher.search(query, null, start + maxItems, sort);
            totalHits = fdocs.totalHits;
            hits = fdocs.scoreDocs;
            boolean isSingleDefinitionSearch = (query instanceof TermQuery) && (builder.getDefs() != null);
            boolean uniqueDefinition = false;
            if (isSingleDefinitionSearch && hits != null && hits.length == 1) {
                Document doc = searcher.doc(hits[0].doc);
                if (doc.getField(QueryBuilder.TAGS) != null) {
                    byte[] rawTags = doc.getField(QueryBuilder.TAGS).binaryValue().bytes;
                    Definitions tags = Definitions.deserialize(rawTags);
                    String symbol = ((TermQuery) query).getTerm().text();
                    if (tags.occurrences(symbol) == 1) {
                        uniqueDefinition = true;
                    }
                }
            }
            if (uniqueDefinition && hits != null && hits.length > 0 && isCrossRefSearch) {
                redirect = contextPath + Prefix.XREF_P + Util.URIEncodePath(searcher.doc(hits[0].doc).get(QueryBuilder.PATH)) + '#' + Util.URIEncode(((TermQuery) query).getTerm().text());
            }
        } catch (BooleanQuery.TooManyClauses e) {
            errorMsg = "Too many results for wildcard!";
        } catch (Exception e) {
            errorMsg = e.getMessage();
        }
        return this;
    }

    private static final Pattern TABSPACE = Pattern.compile("[\t ]+");

    private void getSuggestion(Term term, IndexReader ir, List<String> result) throws IOException {
        if (term == null) {
            return;
        }
        String[] toks = TABSPACE.split(term.text(), 0);
        for (int j = 0; j < toks.length; j++) {
            SuggestWord[] words = checker.suggestSimilar(new Term(term.field(), toks[j]), SPELLCHECK_SUGGEST_WORD_COUNT, ir, SuggestMode.SUGGEST_ALWAYS);
            for (SuggestWord w : words) {
                result.add(w.string);
            }
        }
    }

    public List<Suggestion> getSuggestions() {
        if (projects == null) {
            return new ArrayList<>(0);
        }
        String[] name;
        if (projects.isEmpty()) {
            name = new String[] { "/" };
        } else if (projects.size() == 1) {
            name = new String[] { projects.first() };
        } else {
            name = new String[projects.size()];
            int ii = 0;
            for (String proj : projects) {
                name[ii++] = proj;
            }
        }
        List<Suggestion> res = new ArrayList<>();
        List<String> dummy = new ArrayList<>();
        FSDirectory dir;
        IndexReader ir = null;
        Term t;
        for (int idx = 0; idx < name.length; idx++) {
            Suggestion s = new Suggestion(name[idx]);
            try {
                dir = FSDirectory.open(new File(indexDir, name[idx]));
                ir = DirectoryReader.open(dir);
                if (builder.getFreetext() != null && !builder.getFreetext().isEmpty()) {
                    t = new Term(QueryBuilder.FULL, builder.getFreetext());
                    getSuggestion(t, ir, dummy);
                    s.freetext = dummy.toArray(new String[dummy.size()]);
                    dummy.clear();
                }
                if (builder.getRefs() != null && !builder.getRefs().isEmpty()) {
                    t = new Term(QueryBuilder.REFS, builder.getRefs());
                    getSuggestion(t, ir, dummy);
                    s.refs = dummy.toArray(new String[dummy.size()]);
                    dummy.clear();
                }
                if (builder.getDefs() != null && !builder.getDefs().isEmpty()) {
                    t = new Term(QueryBuilder.DEFS, builder.getDefs());
                    getSuggestion(t, ir, dummy);
                    s.defs = dummy.toArray(new String[dummy.size()]);
                    dummy.clear();
                }
                if ((s.freetext != null && s.freetext.length > 0) || (s.defs != null && s.defs.length > 0) || (s.refs != null && s.refs.length > 0)) {
                    res.add(s);
                }
            } catch (IOException e) {
                log.log(Level.WARNING, "Got exception while getting " + "spelling suggestions: ", e);
            } finally {
                if (ir != null) {
                    try {
                        ir.close();
                    } catch (IOException ex) {
                        log.log(Level.WARNING, "Got exception while " + "getting spelling suggestions: ", ex);
                    }
                }
            }
        }
        return res;
    }

    public SearchHelper prepareSummary() {
        if (redirect != null || errorMsg != null) {
            return this;
        }
        try {
            sourceContext = new Context(query, builder.getQueries());
            summerizer = new Summarizer(query, new CompatibleAnalyser());
        } catch (Exception e) {
            OpenGrokLogger.getLogger().log(Level.WARNING, "Summerizer: {0}", e.getMessage());
        }
        try {
            historyContext = new HistoryContext(query);
        } catch (Exception e) {
            OpenGrokLogger.getLogger().log(Level.WARNING, "HistoryContext: {0}", e.getMessage());
        }
        return this;
    }

    public void destroy() {
        if (searcher != null) {
            IOUtils.close(searcher.getIndexReader());
        }
        if (executor != null) {
            try {
                executor.shutdown();
            } catch (SecurityException se) {
                log.warning(se.getLocalizedMessage());
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "destroy", se);
                }
            }
        }
    }
}
