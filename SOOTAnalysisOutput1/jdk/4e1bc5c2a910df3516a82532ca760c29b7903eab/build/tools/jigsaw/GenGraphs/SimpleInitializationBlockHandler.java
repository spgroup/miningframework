package build.tools.jigsaw;

import com.sun.tools.jdeps.ModuleDotGraph;
import com.sun.tools.jdeps.ModuleDotGraph.DotGraphBuilder;
import java.io.IOException;
import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GenGraphs {

    public static void main(String[] args) throws Exception {
        Path dir = null;
        boolean spec = false;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--spec")) {
                spec = true;
            } else if (arg.equals("--output")) {
                i++;
                dir = i < args.length ? Paths.get(args[i]) : null;
            } else if (arg.startsWith("-")) {
                throw new IllegalArgumentException("Invalid option: " + arg);
            }
        }
        if (dir == null) {
            System.err.println("ERROR: must specify --output argument");
            System.exit(1);
        }
        initDotGraphAttributes();
        Files.createDirectories(dir);
        GenGraphs genGraphs = new GenGraphs(dir, spec);
        Map<String, Configuration> configurations = new HashMap<>();
        Set<String> modules = new HashSet<>();
        ModuleFinder finder = ModuleFinder.ofSystem();
        for (ModuleReference mref : finder.findAll()) {
            String name = (mref.descriptor().name());
            modules.add(name);
            if (genGraphs.accept(name, mref.descriptor())) {
                configurations.put(name, Configuration.empty().resolve(finder, ModuleFinder.of(), Set.of(name)));
            }
        }
        if (genGraphs.accept("jdk", null)) {
            configurations.put("jdk", Configuration.empty().resolve(finder, ModuleFinder.of(), modules));
        }
        genGraphs.genDotFiles(configurations);
    }

    static void initDotGraphAttributes() {
        int h = 1000;
        DotGraphBuilder.weight("java.se", "java.sql.rowset", h * 10);
        DotGraphBuilder.weight("java.sql.rowset", "java.sql", h * 10);
        DotGraphBuilder.weight("java.sql", "java.xml", h * 10);
        DotGraphBuilder.weight("java.xml", "java.base", h * 10);
        DotGraphBuilder.sameRankNodes(Set.of("java.logging", "java.scripting", "java.xml"));
        DotGraphBuilder.sameRankNodes(Set.of("java.sql"));
        DotGraphBuilder.sameRankNodes(Set.of("java.compiler", "java.instrument"));
        DotGraphBuilder.sameRankNodes(Set.of("java.desktop", "java.management"));
        DotGraphBuilder.sameRankNodes(Set.of("java.corba", "java.xml.ws"));
        DotGraphBuilder.sameRankNodes(Set.of("java.xml.bind", "java.xml.ws.annotation"));
        DotGraphBuilder.setRankSep(0.7);
        DotGraphBuilder.setFontSize(12);
        DotGraphBuilder.setArrowSize(1);
        DotGraphBuilder.setArrowWidth(2);
    }

    private final Path dir;

    private final boolean spec;

    GenGraphs(Path dir, boolean spec) {
        this.dir = dir;
        this.spec = spec;
    }

    void genDotFiles(Map<String, Configuration> configurations) throws IOException {
        ModuleDotGraph dotGraph = new ModuleDotGraph(configurations, spec);
        dotGraph.genDotFiles(dir);
    }

    boolean accept(String name, ModuleDescriptor descriptor) {
        if (!spec)
            return true;
        if (name.equals("jdk"))
            return false;
        if (name.equals("java.se") || name.equals("java.se.ee"))
            return true;
        return descriptor.exports().stream().filter(e -> !e.isQualified()).findAny().isPresent();
    }
}