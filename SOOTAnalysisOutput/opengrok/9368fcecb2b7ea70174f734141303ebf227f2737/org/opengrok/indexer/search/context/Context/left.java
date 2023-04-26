package org.opengrok.indexer.search.context;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.opengrok.indexer.analysis.AbstractAnalyzer;
import org.opengrok.indexer.analysis.Definitions;
import org.opengrok.indexer.analysis.Scopes;
import org.opengrok.indexer.analysis.Scopes.Scope;
import org.opengrok.indexer.analysis.plain.PlainAnalyzerFactory;
import org.opengrok.indexer.configuration.RuntimeEnvironment;
import org.opengrok.indexer.logger.LoggerFactory;
import org.opengrok.indexer.search.Hit;
import org.opengrok.indexer.search.QueryBuilder;
import org.opengrok.indexer.util.IOUtils;
import org.opengrok.indexer.web.Util;

public class Context {

    static final int MAXFILEREAD = 1024 * 1024;

    private static final Logger LOGGER = LoggerFactory.getLogger(Context.class);

    private final Query query;

    private final QueryBuilder qbuilder;

    private final LineMatcher[] m;

    private final String queryAsURI;

    private static final Map<String, Boolean> TOKEN_FIELDS = Map.of(QueryBuilder.FULL, Boolean.TRUE, QueryBuilder.REFS, Boolean.FALSE, QueryBuilder.DEFS, Boolean.FALSE);

    public Context(Query query, QueryBuilder qbuilder) {
        if (qbuilder == null) {
            throw new IllegalArgumentException("qbuilder is null");
        }
        this.query = query;
        this.qbuilder = qbuilder;
        QueryMatchers qm = new QueryMatchers();
        m = qm.getMatchers(query, TOKEN_FIELDS);
        if (m != null) {
            queryAsURI = buildQueryAsURI(qbuilder.getQueries());
        } else {
            queryAsURI = "";
        }
    }

    public void toggleAlt() {
        alt = !alt;
    }

    public boolean isEmpty() {
        return m == null;
    }

    public boolean getContext2(RuntimeEnvironment env, IndexSearcher searcher, int docId, Appendable dest, String urlPrefix, String morePrefix, boolean limit, int tabSize) {
        if (isEmpty()) {
            return false;
        }
        Document doc;
        try {
            doc = searcher.doc(docId);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "ERROR getting searcher doc(int)", e);
            return false;
        }
        Definitions tags = null;
        try {
            IndexableField tagsField = doc.getField(QueryBuilder.TAGS);
            if (tagsField != null) {
                tags = Definitions.deserialize(tagsField.binaryValue().bytes);
            }
        } catch (ClassNotFoundException | IOException e) {
            LOGGER.log(Level.WARNING, "ERROR Definitions.deserialize(...)", e);
            return false;
        }
        Scopes scopes;
        try {
            IndexableField scopesField = doc.getField(QueryBuilder.SCOPES);
            if (scopesField != null) {
                scopes = Scopes.deserialize(scopesField.binaryValue().bytes);
            } else {
                scopes = new Scopes();
            }
        } catch (ClassNotFoundException | IOException e) {
            LOGGER.log(Level.WARNING, "ERROR Scopes.deserialize(...)", e);
            return false;
        }
        PlainAnalyzerFactory fac = PlainAnalyzerFactory.DEFAULT_INSTANCE;
        AbstractAnalyzer anz = fac.getAnalyzer();
        String path = doc.get(QueryBuilder.PATH);
        String pathE = Util.URIEncodePath(path);
        String urlPrefixE = urlPrefix == null ? "" : Util.URIEncodePath(urlPrefix);
        String moreURL = morePrefix == null ? null : Util.URIEncodePath(morePrefix) + pathE + "?" + queryAsURI;
        ContextArgs args = new ContextArgs(env.getContextSurround(), env.getContextLimit());
        int linelimit = limit ? args.getContextLimit() : Short.MAX_VALUE;
        ContextFormatter formatter = new ContextFormatter(args);
        formatter.setUrl(urlPrefixE + pathE);
        formatter.setDefs(tags);
        formatter.setScopes(scopes);
        formatter.setMoreUrl(moreURL);
        formatter.setMoreLimit(linelimit);
        OGKUnifiedHighlighter uhi = new OGKUnifiedHighlighter(env, searcher, anz);
        uhi.setBreakIterator(StrictLineBreakIterator::new);
        uhi.setFormatter(formatter);
        uhi.setTabSize(tabSize);
        try {
            List<String> fieldList = qbuilder.getContextFields();
            String[] fields = fieldList.toArray(new String[0]);
            String res = uhi.highlightFieldsUnion(fields, query, docId, linelimit);
            if (res != null) {
                dest.append(res);
                return true;
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "ERROR highlightFieldsUnion(...)", e);
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, "ERROR highlightFieldsUnion(...)", e);
            throw e;
        }
        return false;
    }

    private String buildQueryAsURI(Map<String, String> subqueries) {
        if (subqueries.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : subqueries.entrySet()) {
            String field = entry.getKey();
            String queryText = entry.getValue();
            sb.append(field).append("=").append(Util.URIEncode(queryText)).append('&');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    private boolean alt = true;

    public boolean getContext(Reader in, Writer out, String urlPrefix, String morePrefix, String path, Definitions tags, boolean limit, boolean isDefSearch, List<Hit> hits) {
        return getContext(in, out, urlPrefix, morePrefix, path, tags, limit, isDefSearch, hits, null);
    }

    public boolean getContext(Reader in, Writer out, String urlPrefix, String morePrefix, String path, Definitions tags, boolean limit, boolean isDefSearch, List<Hit> hits, Scopes scopes) {
        if (m == null) {
            IOUtils.close(in);
            return false;
        }
        boolean anything = false;
        TreeMap<Integer, String[]> matchingTags = null;
        String urlPrefixE = (urlPrefix == null) ? "" : Util.URIEncodePath(urlPrefix);
        String pathE = Util.URIEncodePath(path);
        if (tags != null) {
            matchingTags = new TreeMap<>();
            try {
                for (Definitions.Tag tag : tags.getTags()) {
                    for (LineMatcher lineMatcher : m) {
                        if (lineMatcher.match(tag.symbol) == LineMatcher.MATCHED) {
                            String scope = null;
                            String scopeUrl = null;
                            if (scopes != null) {
                                Scope scp = scopes.getScope(tag.line);
                                scope = scp.getName() + "()";
                                scopeUrl = "<a href=\"" + urlPrefixE + pathE + "#" + scp.getLineFrom() + "\">" + scope + "</a>";
                            }
                            String[] desc = { tag.symbol, Integer.toString(tag.line), tag.type, tag.text, scope };
                            if (in == null) {
                                if (out == null) {
                                    Hit hit = new Hit(path, Util.htmlize(desc[3]).replace(desc[0], "<b>" + desc[0] + "</b>"), desc[1], false, alt);
                                    hits.add(hit);
                                } else {
                                    out.write("<a class=\"s\" href=\"");
                                    out.write(urlPrefixE);
                                    out.write(pathE);
                                    out.write("#");
                                    out.write(desc[1]);
                                    out.write("\"><span class=\"l\">");
                                    out.write(desc[1]);
                                    out.write("</span> ");
                                    out.write(Util.htmlize(desc[3]).replace(desc[0], "<b>" + desc[0] + "</b>"));
                                    out.write("</a> ");
                                    if (desc[4] != null) {
                                        out.write("<span class=\"scope\"><a href\"");
                                        out.write(scopeUrl);
                                        out.write("\">in ");
                                        out.write(desc[4]);
                                        out.write("</a></span> ");
                                    }
                                    out.write("<i>");
                                    out.write(desc[2]);
                                    out.write("</i><br/>");
                                }
                                anything = true;
                            } else {
                                matchingTags.put(tag.line, desc);
                            }
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                if (hits != null) {
                    LOGGER.log(Level.WARNING, "Could not get context for " + path, e);
                }
            }
        }
        if (in == null) {
            return anything;
        }
        PlainLineTokenizer tokens = new PlainLineTokenizer(null);
        boolean truncated = false;
        boolean lim = limit;
        RuntimeEnvironment env = RuntimeEnvironment.getInstance();
        if (!env.isQuickContextScan()) {
            lim = false;
        }
        if (lim) {
            char[] buffer = new char[MAXFILEREAD];
            int charsRead;
            try {
                charsRead = in.read(buffer);
                if (charsRead == MAXFILEREAD) {
                    truncated = true;
                    for (int i = charsRead - 1; i > charsRead - 100; i--) {
                        if (buffer[i] == '\n') {
                            charsRead = i;
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "An error occurred while reading data", e);
                return anything;
            }
            if (charsRead == 0) {
                return anything;
            }
            tokens.reInit(buffer, charsRead, out, urlPrefixE + pathE + "#", matchingTags, scopes);
        } else {
            tokens.reInit(in, out, urlPrefixE + pathE + "#", matchingTags, scopes);
        }
        if (hits != null) {
            tokens.setAlt(alt);
            tokens.setHitList(hits);
            tokens.setFilename(path);
        }
        int limit_max_lines = env.getContextLimit();
        try {
            String token;
            int matchState;
            int matchedLines = 0;
            while ((token = tokens.yylex()) != null && (!lim || matchedLines < limit_max_lines)) {
                for (LineMatcher lineMatcher : m) {
                    matchState = lineMatcher.match(token);
                    if (matchState == LineMatcher.MATCHED) {
                        if (!isDefSearch) {
                            tokens.printContext();
                        } else if (tokens.tags.containsKey(tokens.markedLine)) {
                            tokens.printContext();
                        }
                        matchedLines++;
                        break;
                    } else if (matchState == LineMatcher.WAIT) {
                        tokens.holdOn();
                    } else {
                        tokens.neverMind();
                    }
                }
            }
            anything = matchedLines > 0;
            tokens.dumpRest();
            if (lim && (truncated || matchedLines == limit_max_lines) && out != null) {
                out.write("<a href=\"" + Util.URIEncodePath(morePrefix) + pathE + "?" + queryAsURI + "\">[all...]</a>");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not get context for " + path, e);
        } finally {
            IOUtils.close(in);
            if (out != null) {
                try {
                    out.flush();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to flush stream: ", e);
                }
            }
        }
        return anything;
    }
}
