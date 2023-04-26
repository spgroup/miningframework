package org.opensolaris.opengrok.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.util.BytesRef;
import org.opensolaris.opengrok.analysis.FileAnalyzer.Genre;
import org.opensolaris.opengrok.analysis.ada.AdaAnalyzerFactory;
import org.opensolaris.opengrok.analysis.archive.BZip2AnalyzerFactory;
import org.opensolaris.opengrok.analysis.archive.GZIPAnalyzerFactory;
import org.opensolaris.opengrok.analysis.archive.TarAnalyzerFactory;
import org.opensolaris.opengrok.analysis.archive.ZipAnalyzerFactory;
import org.opensolaris.opengrok.analysis.c.CAnalyzerFactory;
import org.opensolaris.opengrok.analysis.c.CxxAnalyzerFactory;
import org.opensolaris.opengrok.analysis.csharp.CSharpAnalyzerFactory;
import org.opensolaris.opengrok.analysis.data.IgnorantAnalyzerFactory;
import org.opensolaris.opengrok.analysis.data.ImageAnalyzerFactory;
import org.opensolaris.opengrok.analysis.document.TroffAnalyzerFactory;
import org.opensolaris.opengrok.analysis.erlang.ErlangAnalyzerFactory;
import org.opensolaris.opengrok.analysis.executables.ELFAnalyzerFactory;
import org.opensolaris.opengrok.analysis.executables.JarAnalyzerFactory;
import org.opensolaris.opengrok.analysis.executables.JavaClassAnalyzerFactory;
import org.opensolaris.opengrok.analysis.fortran.FortranAnalyzerFactory;
import org.opensolaris.opengrok.analysis.golang.GolangAnalyzerFactory;
import org.opensolaris.opengrok.analysis.haskell.HaskellAnalyzerFactory;
import org.opensolaris.opengrok.analysis.lua.LuaAnalyzerFactory;
import org.opensolaris.opengrok.analysis.java.JavaAnalyzerFactory;
import org.opensolaris.opengrok.analysis.javascript.JavaScriptAnalyzerFactory;
import org.opensolaris.opengrok.analysis.lisp.LispAnalyzerFactory;
import org.opensolaris.opengrok.analysis.pascal.PascalAnalyzerFactory;
import org.opensolaris.opengrok.analysis.perl.PerlAnalyzerFactory;
import org.opensolaris.opengrok.analysis.php.PhpAnalyzerFactory;
import org.opensolaris.opengrok.analysis.plain.PlainAnalyzerFactory;
import org.opensolaris.opengrok.analysis.plain.XMLAnalyzerFactory;
import org.opensolaris.opengrok.analysis.python.PythonAnalyzerFactory;
import org.opensolaris.opengrok.analysis.rust.RustAnalyzerFactory;
import org.opensolaris.opengrok.analysis.scala.ScalaAnalyzerFactory;
import org.opensolaris.opengrok.analysis.clojure.ClojureAnalyzerFactory;
import org.opensolaris.opengrok.analysis.json.JsonAnalyzerFactory;
import org.opensolaris.opengrok.analysis.kotlin.KotlinAnalyzerFactory;
import org.opensolaris.opengrok.analysis.sh.ShAnalyzerFactory;
import org.opensolaris.opengrok.analysis.powershell.PowershellAnalyzerFactory;
import org.opensolaris.opengrok.analysis.sql.PLSQLAnalyzerFactory;
import org.opensolaris.opengrok.analysis.sql.SQLAnalyzerFactory;
import org.opensolaris.opengrok.analysis.swift.SwiftAnalyzerFactory;
import org.opensolaris.opengrok.analysis.tcl.TclAnalyzerFactory;
import org.opensolaris.opengrok.analysis.uue.UuencodeAnalyzerFactory;
import org.opensolaris.opengrok.analysis.vb.VBAnalyzerFactory;
import org.opensolaris.opengrok.configuration.Project;
import org.opensolaris.opengrok.configuration.RuntimeEnvironment;
import org.opensolaris.opengrok.history.Annotation;
import org.opensolaris.opengrok.history.HistoryException;
import org.opensolaris.opengrok.history.HistoryGuru;
import org.opensolaris.opengrok.history.HistoryReader;
import org.opensolaris.opengrok.logger.LoggerFactory;
import org.opensolaris.opengrok.search.QueryBuilder;
import org.opensolaris.opengrok.util.IOUtils;
import org.opensolaris.opengrok.web.Util;

public class AnalyzerGuru {

    private static final int OPENING_MAX_CHARS = 100;

    private static final int MARK_READ_LIMIT = 1024 * 16;

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzerGuru.class);

    private static final FileAnalyzerFactory DEFAULT_ANALYZER_FACTORY = new FileAnalyzerFactory();

    private static final Map<String, FileAnalyzerFactory> FILE_NAMES = new HashMap<>();

    private static final Map<String, FileAnalyzerFactory> ext = new HashMap<>();

    private static final Map<String, FileAnalyzerFactory> pre = new HashMap<>();

    private static Comparator<String> descStrlenComparator = new Comparator<String>() {

        @Override
        public int compare(String s1, String s2) {
            int cmp = Integer.compare(s2.length(), s1.length());
            if (cmp != 0)
                return cmp;
            cmp = s1.compareTo(s2);
            return cmp;
        }
    };

    private static final SortedMap<String, FileAnalyzerFactory> magics = new TreeMap<>(descStrlenComparator);

    private static final List<FileAnalyzerFactory.Matcher> matchers = new ArrayList<>();

    private static final List<FileAnalyzerFactory> factories = new ArrayList<>();

    private static final List<String> analysisPkgNames = new ArrayList<>();

    public static final Reader dummyR = new StringReader("");

    public static final String dummyS = "";

    public static final FieldType string_ft_stored_nanalyzed_norms = new FieldType(StringField.TYPE_STORED);

    public static final FieldType string_ft_nstored_nanalyzed_norms = new FieldType(StringField.TYPE_NOT_STORED);

    private static final Map<String, String> fileTypeDescriptions = new TreeMap<>();

    static {
        FileAnalyzerFactory[] analyzers = { DEFAULT_ANALYZER_FACTORY, new IgnorantAnalyzerFactory(), new BZip2AnalyzerFactory(), new XMLAnalyzerFactory(), new TroffAnalyzerFactory(), new ELFAnalyzerFactory(), new JavaClassAnalyzerFactory(), new ImageAnalyzerFactory(), JarAnalyzerFactory.DEFAULT_INSTANCE, ZipAnalyzerFactory.DEFAULT_INSTANCE, new TarAnalyzerFactory(), new CAnalyzerFactory(), new CSharpAnalyzerFactory(), new VBAnalyzerFactory(), new CxxAnalyzerFactory(), new ErlangAnalyzerFactory(), new ShAnalyzerFactory(), new PowershellAnalyzerFactory(), PlainAnalyzerFactory.DEFAULT_INSTANCE, new UuencodeAnalyzerFactory(), new GZIPAnalyzerFactory(), new JavaAnalyzerFactory(), new JavaScriptAnalyzerFactory(), new KotlinAnalyzerFactory(), new SwiftAnalyzerFactory(), new JsonAnalyzerFactory(), new PythonAnalyzerFactory(), new RustAnalyzerFactory(), new PerlAnalyzerFactory(), new PhpAnalyzerFactory(), new LispAnalyzerFactory(), new TclAnalyzerFactory(), new ScalaAnalyzerFactory(), new ClojureAnalyzerFactory(), new SQLAnalyzerFactory(), new PLSQLAnalyzerFactory(), new FortranAnalyzerFactory(), new HaskellAnalyzerFactory(), new GolangAnalyzerFactory(), new LuaAnalyzerFactory(), new PascalAnalyzerFactory(), new AdaAnalyzerFactory() };
        for (FileAnalyzerFactory analyzer : analyzers) {
            registerAnalyzer(analyzer);
        }
        for (FileAnalyzerFactory analyzer : analyzers) {
            if (analyzer.getName() != null && !analyzer.getName().isEmpty()) {
                fileTypeDescriptions.put(analyzer.getAnalyzer().getFileTypeName(), analyzer.getName());
            }
        }
        string_ft_stored_nanalyzed_norms.setOmitNorms(false);
        string_ft_nstored_nanalyzed_norms.setOmitNorms(false);
    }

    public static Map<String, String> getfileTypeDescriptions() {
        return fileTypeDescriptions;
    }

    public List<FileAnalyzerFactory> getAnalyzerFactories() {
        return factories;
    }

    private static void registerAnalyzer(FileAnalyzerFactory factory) {
        for (String name : factory.getFileNames()) {
            FileAnalyzerFactory old = FILE_NAMES.put(name, factory);
            assert old == null : "name '" + name + "' used in multiple analyzers";
        }
        for (String prefix : factory.getPrefixes()) {
            FileAnalyzerFactory old = pre.put(prefix, factory);
            assert old == null : "prefix '" + prefix + "' used in multiple analyzers";
        }
        for (String suffix : factory.getSuffixes()) {
            FileAnalyzerFactory old = ext.put(suffix, factory);
            assert old == null : "suffix '" + suffix + "' used in multiple analyzers";
        }
        for (String magic : factory.getMagicStrings()) {
            FileAnalyzerFactory old = magics.put(magic, factory);
            assert old == null : "magic '" + magic + "' used in multiple analyzers";
        }
        matchers.addAll(factory.getMatchers());
        factories.add(factory);
    }

    public static void addPrefix(String prefix, FileAnalyzerFactory factory) {
        if (factory == null) {
            pre.remove(prefix);
        } else {
            pre.put(prefix, factory);
        }
    }

    public static void addExtension(String extension, FileAnalyzerFactory factory) {
        if (factory == null) {
            ext.remove(extension);
        } else {
            ext.put(extension, factory);
        }
    }

    public static FileAnalyzer getAnalyzer() {
        return DEFAULT_ANALYZER_FACTORY.getAnalyzer();
    }

    public static FileAnalyzer getAnalyzer(InputStream in, String file) throws IOException {
        FileAnalyzerFactory factory = find(in, file);
        if (factory == null) {
            FileAnalyzer defaultAnalyzer = getAnalyzer();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "{0}: fallback {1}", new Object[] { file, defaultAnalyzer.getClass().getSimpleName() });
            }
            return defaultAnalyzer;
        }
        return factory.getAnalyzer();
    }

    public void populateDocument(Document doc, File file, String path, FileAnalyzer fa, Writer xrefOut) throws IOException {
        String date = DateTools.timeToString(file.lastModified(), DateTools.Resolution.MILLISECOND);
        doc.add(new Field(QueryBuilder.U, Util.path2uid(path, date), string_ft_stored_nanalyzed_norms));
        doc.add(new Field(QueryBuilder.FULLPATH, file.getAbsolutePath(), string_ft_nstored_nanalyzed_norms));
        doc.add(new SortedDocValuesField(QueryBuilder.FULLPATH, new BytesRef(file.getAbsolutePath())));
        if (RuntimeEnvironment.getInstance().isHistoryEnabled()) {
            try {
                HistoryReader hr = HistoryGuru.getInstance().getHistoryReader(file);
                if (hr != null) {
                    doc.add(new TextField(QueryBuilder.HIST, hr));
                }
            } catch (HistoryException e) {
                LOGGER.log(Level.WARNING, "An error occurred while reading history: ", e);
            }
        }
        doc.add(new Field(QueryBuilder.DATE, date, string_ft_stored_nanalyzed_norms));
        doc.add(new SortedDocValuesField(QueryBuilder.DATE, new BytesRef(date)));
        if (path != null) {
            doc.add(new TextField(QueryBuilder.PATH, path, Store.YES));
            Project project = Project.getProject(path);
            if (project != null) {
                doc.add(new TextField(QueryBuilder.PROJECT, project.getPath(), Store.YES));
            }
        }
        if (fa != null) {
            Genre g = fa.getGenre();
            if (g == Genre.PLAIN || g == Genre.XREFABLE || g == Genre.HTML) {
                doc.add(new Field(QueryBuilder.T, g.typeName(), string_ft_stored_nanalyzed_norms));
            }
            fa.analyze(doc, StreamSource.fromFile(file), xrefOut);
            String type = fa.getFileTypeName();
            doc.add(new StringField(QueryBuilder.TYPE, type, Store.YES));
        }
    }

    public static String getContentType(InputStream in, String file) throws IOException {
        FileAnalyzerFactory factory = find(in, file);
        String type = null;
        if (factory != null) {
            type = factory.getContentType();
        }
        return type;
    }

    public static void writeXref(FileAnalyzerFactory factory, Reader in, Writer out, Definitions defs, Annotation annotation, Project project) throws IOException {
        Reader input = in;
        if (factory.getGenre() == Genre.PLAIN) {
            input = ExpandTabsReader.wrap(in, project);
        }
        WriteXrefArgs args = new WriteXrefArgs(input, out);
        args.setDefs(defs);
        args.setAnnotation(annotation);
        args.setProject(project);
        FileAnalyzer analyzer = factory.getAnalyzer();
        analyzer.writeXref(args);
    }

    public static Genre getGenre(String file) {
        return getGenre(find(file));
    }

    public static Genre getGenre(InputStream in) throws IOException {
        return getGenre(find(in));
    }

    public static Genre getGenre(FileAnalyzerFactory factory) {
        if (factory != null) {
            return factory.getGenre();
        }
        return null;
    }

    public static FileAnalyzerFactory findFactory(String factoryClassName) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Class<?> fcn = null;
        try {
            fcn = Class.forName(factoryClassName);
        } catch (ClassNotFoundException e) {
            fcn = getFactoryClass(factoryClassName);
            if (fcn == null) {
                throw new ClassNotFoundException("Unable to locate class " + factoryClassName);
            }
        }
        return findFactory(fcn);
    }

    public static Class<?> getFactoryClass(String simpleName) {
        Class<?> factoryClass = null;
        if (analysisPkgNames.isEmpty()) {
            Package[] p = Package.getPackages();
            for (Package pp : p) {
                String pname = pp.getName();
                if (pname.indexOf(".analysis.") != -1) {
                    analysisPkgNames.add(pname);
                }
            }
        }
        if (simpleName.indexOf("Analyzer") == -1) {
            simpleName += "Analyzer";
        }
        if (simpleName.indexOf("Factory") == -1) {
            simpleName += "Factory";
        }
        for (String aPackage : analysisPkgNames) {
            try {
                String fqn = aPackage + "." + simpleName;
                factoryClass = Class.forName(fqn);
                break;
            } catch (ClassNotFoundException e) {
            }
        }
        return factoryClass;
    }

    private static FileAnalyzerFactory findFactory(Class<?> factoryClass) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        for (FileAnalyzerFactory f : factories) {
            if (f.getClass() == factoryClass) {
                return f;
            }
        }
        FileAnalyzerFactory f = (FileAnalyzerFactory) factoryClass.getDeclaredConstructor().newInstance();
        registerAnalyzer(f);
        return f;
    }

    public static FileAnalyzerFactory find(InputStream in, String file) throws IOException {
        FileAnalyzerFactory factory = find(file);
        if (factory != null) {
            return factory;
        }
        return findForStream(in, file);
    }

    public static FileAnalyzerFactory find(String file) {
        String path = file;
        int i;
        if (((i = path.lastIndexOf(File.separatorChar)) > 0) && (i + 1 < path.length())) {
            path = path.substring(i + 1);
        }
        int dotpos = path.lastIndexOf('.');
        if (dotpos >= 0) {
            FileAnalyzerFactory factory;
            if (dotpos > 0) {
                factory = pre.get(path.substring(0, dotpos).toUpperCase(Locale.getDefault()));
                if (factory != null) {
                    if (LOGGER.isLoggable(Level.FINER)) {
                        LOGGER.log(Level.FINER, "{0}: chosen by prefix: {1}", new Object[] { file, factory.getClass().getSimpleName() });
                    }
                    return factory;
                }
            }
            factory = ext.get(path.substring(dotpos + 1).toUpperCase(Locale.getDefault()));
            if (factory != null) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.log(Level.FINER, "{0}: chosen by suffix: {1}", new Object[] { file, factory.getClass().getSimpleName() });
                }
                return factory;
            }
        }
        return FILE_NAMES.get(path.toUpperCase(Locale.getDefault()));
    }

    public static FileAnalyzerFactory find(InputStream in) throws IOException {
        return findForStream(in, "<anonymous>");
    }

    private static FileAnalyzerFactory findForStream(InputStream in, String file) throws IOException {
        in.mark(8);
        byte[] content = new byte[8];
        int len = in.read(content);
        in.reset();
        if (len < 8) {
            if (len < 4) {
                return null;
            }
            content = Arrays.copyOf(content, len);
        }
        FileAnalyzerFactory fac;
        for (FileAnalyzerFactory.Matcher matcher : matchers) {
            if (matcher.getIsPreciseMagic()) {
                fac = matcher.isMagic(content, in);
                if (fac != null) {
                    if (LOGGER.isLoggable(Level.FINER)) {
                        LOGGER.log(Level.FINER, "{0}: chosen by precise magic: {1}", new Object[] { file, fac.getClass().getSimpleName() });
                    }
                    return fac;
                }
            }
        }
        String opening = readOpening(in, content);
        fac = findMagicString(opening, file);
        if (fac != null) {
            return fac;
        }
        for (FileAnalyzerFactory.Matcher matcher : matchers) {
            if (!matcher.getIsPreciseMagic()) {
                fac = matcher.isMagic(content, in);
                if (fac != null) {
                    if (LOGGER.isLoggable(Level.FINER)) {
                        LOGGER.log(Level.FINER, "{0}: chosen by imprecise magic: {1}", new Object[] { file, fac.getClass().getSimpleName() });
                    }
                    return fac;
                }
            }
        }
        return null;
    }

    private static FileAnalyzerFactory findMagicString(String opening, String file) throws IOException {
        String fragment = getWords(opening, 2);
        FileAnalyzerFactory fac = magics.get(fragment);
        if (fac != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINER, "{0}: chosen by magic {2}: {1}", new Object[] { file, fac.getClass().getSimpleName(), fragment });
            }
            return fac;
        }
        fragment = getWords(opening, 1);
        fac = magics.get(fragment);
        if (fac != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINER, "{0}: chosen by magic {2}: {1}", new Object[] { file, fac.getClass().getSimpleName(), fragment });
            }
            return fac;
        }
        for (Map.Entry<String, FileAnalyzerFactory> entry : magics.entrySet()) {
            String magic = entry.getKey();
            if (opening.startsWith(magic)) {
                fac = entry.getValue();
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.log(Level.FINER, "{0}: chosen by magic(substr) {2}: {1}", new Object[] { file, fac.getClass().getSimpleName(), magic });
                }
                return fac;
            }
        }
        return null;
    }

    private static String getWords(String value, int n) {
        if (n < 1)
            return "";
        int l = 0;
        while (n-- > 0) {
            int o = l > 0 ? l + 1 : l;
            int i = value.indexOf(' ', o);
            if (i == -1)
                return value;
            l = i;
        }
        return value.substring(0, l);
    }

    private static String readOpening(InputStream in, byte[] sig) throws IOException {
        in.mark(MARK_READ_LIMIT);
        String encoding = IOUtils.findBOMEncoding(sig);
        if (encoding == null) {
            encoding = "UTF-8";
        } else {
            int skipForBOM = IOUtils.skipForBOM(sig);
            if (in.skip(skipForBOM) < skipForBOM) {
                in.reset();
                return "";
            }
        }
        int nRead = 0;
        boolean sawNonWhitespace = false;
        boolean lastWhitespace = false;
        boolean postHashbang = false;
        int r;
        StringBuilder opening = new StringBuilder();
        BufferedReader readr = new BufferedReader(new InputStreamReader(in, encoding), OPENING_MAX_CHARS);
        while ((r = readr.read()) != -1) {
            if (++nRead > OPENING_MAX_CHARS)
                break;
            char c = (char) r;
            boolean isWhitespace = Character.isWhitespace(c);
            if (!sawNonWhitespace) {
                if (isWhitespace)
                    continue;
                sawNonWhitespace = true;
            }
            if (c == '\n')
                break;
            if (isWhitespace) {
                if (!lastWhitespace && !postHashbang)
                    opening.append(' ');
            } else {
                opening.append(c);
                postHashbang = false;
            }
            lastWhitespace = isWhitespace;
            if (opening.length() == 2) {
                if (opening.charAt(0) == '#' && opening.charAt(1) == '!') {
                    postHashbang = true;
                }
            }
        }
        in.reset();
        return opening.toString();
    }
}
