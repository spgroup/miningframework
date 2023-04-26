package org.opensolaris.opengrok.analysis;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.opensolaris.opengrok.web.Util;

public class AnalyzerGuru {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzerGuru.class);

    private static final FileAnalyzerFactory DEFAULT_ANALYZER_FACTORY = new FileAnalyzerFactory();

    private static final Map<String, FileAnalyzerFactory> FILE_NAMES = new HashMap<>();

    private static final Map<String, FileAnalyzerFactory> ext = new HashMap<>();

    private static final Map<String, FileAnalyzerFactory> pre = new HashMap<>();

    private static final SortedMap<String, FileAnalyzerFactory> magics = new TreeMap<>();

    private static final List<FileAnalyzerFactory.Matcher> matchers = new ArrayList<>();

    private static final List<FileAnalyzerFactory> factories = new ArrayList<>();

    public static final Reader dummyR = new StringReader("");

    public static final String dummyS = "";

    public static final FieldType string_ft_stored_nanalyzed_norms = new FieldType(StringField.TYPE_STORED);

    public static final FieldType string_ft_nstored_nanalyzed_norms = new FieldType(StringField.TYPE_NOT_STORED);

    private static final Map<String, String> fileTypeDescriptions = new TreeMap<>();

    static {
        FileAnalyzerFactory[] analyzers = { DEFAULT_ANALYZER_FACTORY, new IgnorantAnalyzerFactory(), new BZip2AnalyzerFactory(), new XMLAnalyzerFactory(), new TroffAnalyzerFactory(), new ELFAnalyzerFactory(), new JavaClassAnalyzerFactory(), new ImageAnalyzerFactory(), JarAnalyzerFactory.DEFAULT_INSTANCE, ZipAnalyzerFactory.DEFAULT_INSTANCE, new TarAnalyzerFactory(), new CAnalyzerFactory(), new CSharpAnalyzerFactory(), new VBAnalyzerFactory(), new CxxAnalyzerFactory(), new ErlangAnalyzerFactory(), new ShAnalyzerFactory(), new PowershellAnalyzerFactory(), PlainAnalyzerFactory.DEFAULT_INSTANCE, new UuencodeAnalyzerFactory(), new GZIPAnalyzerFactory(), new JavaAnalyzerFactory(), new JavaScriptAnalyzerFactory(), new KotlinAnalyzerFactory(), new SwiftAnalyzerFactory(), new JsonAnalyzerFactory(), new PythonAnalyzerFactory(), new RustAnalyzerFactory(), new PerlAnalyzerFactory(), new PhpAnalyzerFactory(), new LispAnalyzerFactory(), new TclAnalyzerFactory(), new ScalaAnalyzerFactory(), new ClojureAnalyzerFactory(), new SQLAnalyzerFactory(), new PLSQLAnalyzerFactory(), new FortranAnalyzerFactory(), new HaskellAnalyzerFactory(), new GolangAnalyzerFactory(), new LuaAnalyzerFactory(), new PascalAnalyzerFactory() };
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
            return getAnalyzer();
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
        factory.writeXref(input, out, defs, annotation, project);
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

    public static FileAnalyzerFactory findFactory(String factoryClassName) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return findFactory(Class.forName(factoryClassName));
    }

    private static FileAnalyzerFactory findFactory(Class<?> factoryClass) throws InstantiationException, IllegalAccessException {
        for (FileAnalyzerFactory f : factories) {
            if (f.getClass() == factoryClass) {
                return f;
            }
        }
        FileAnalyzerFactory f = (FileAnalyzerFactory) factoryClass.newInstance();
        registerAnalyzer(f);
        return f;
    }

    public static FileAnalyzerFactory find(InputStream in, String file) throws IOException {
        FileAnalyzerFactory factory = find(file);
        if (factory != null) {
            return factory;
        }
        return find(in);
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
                    return factory;
                }
            }
            factory = ext.get(path.substring(dotpos + 1).toUpperCase(Locale.getDefault()));
            if (factory != null) {
                return factory;
            }
        }
        return FILE_NAMES.get(path.toUpperCase(Locale.getDefault()));
    }

    public static FileAnalyzerFactory find(InputStream in) throws IOException {
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
        FileAnalyzerFactory factory = find(content);
        if (factory != null) {
            return factory;
        }
        for (FileAnalyzerFactory.Matcher matcher : matchers) {
            FileAnalyzerFactory fac = matcher.isMagic(content, in);
            if (fac != null) {
                return fac;
            }
        }
        return null;
    }

    private static FileAnalyzerFactory find(byte[] signature) throws IOException {
        char[] chars = new char[signature.length > 8 ? 8 : signature.length];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) (0xFF & signature[i]);
        }
        String sig = new String(chars);
        FileAnalyzerFactory a = magics.get(sig);
        if (a == null) {
            String sigWithoutBOM = stripBOM(signature);
            for (Map.Entry<String, FileAnalyzerFactory> entry : magics.entrySet()) {
                if (sig.startsWith(entry.getKey())) {
                    return entry.getValue();
                }
                if (sigWithoutBOM != null && entry.getValue().getGenre() == Genre.PLAIN && sigWithoutBOM.startsWith(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        return a;
    }

    private static final Map<String, byte[]> BOMS = new HashMap<>();

    static {
        BOMS.put("UTF-8", new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
        BOMS.put("UTF-16BE", new byte[] { (byte) 0xFE, (byte) 0xFF });
        BOMS.put("UTF-16LE", new byte[] { (byte) 0xFF, (byte) 0xFE });
    }

    public static String stripBOM(byte[] sig) throws IOException {
        for (Map.Entry<String, byte[]> entry : BOMS.entrySet()) {
            String encoding = entry.getKey();
            byte[] bom = entry.getValue();
            if (sig.length > bom.length) {
                int i = 0;
                while (i < bom.length && sig[i] == bom[i]) {
                    i++;
                }
                if (i == bom.length) {
                    return new String(sig, bom.length, sig.length - bom.length, encoding);
                }
            }
        }
        return null;
    }
}
