package build.tools.jigsaw;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import static java.util.stream.Collectors.*;
import static java.lang.module.ModuleDescriptor.Requires.Modifier.TRANSITIVE;

public class GenGraphs {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("ERROR: specify the output directory");
            System.exit(1);
        }
        Path dir = Paths.get(args[0]);
        Files.createDirectories(dir);
        ModuleFinder finder = ModuleFinder.ofSystem();
        Set<ModuleDescriptor> javaSEModules = new TreeSet<>(finder.findAll().stream().map(ModuleReference::descriptor).filter(m -> (m.name().startsWith("java.") && !m.name().equals("java.smartcardio"))).collect(toSet()));
        Set<ModuleDescriptor> jdkModules = new TreeSet<>(finder.findAll().stream().map(ModuleReference::descriptor).filter(m -> !javaSEModules.contains(m)).collect(toSet()));
        GenGraphs genGraphs = new GenGraphs(dir, javaSEModules, jdkModules);
        Set<String> mods = new HashSet<>();
        for (ModuleReference mref : finder.findAll()) {
            mods.add(mref.descriptor().name());
            genGraphs.genDotFile(mref);
        }
        genGraphs.genDotFile("jdk", mods);
    }

    private static final String ORANGE = "#e76f00";

    private static final String BLUE = "#437291";

    private static final String GRAY = "#dddddd";

    private static final String REEXPORTS = "";

    private static final String REQUIRES = "style=\"dashed\"";

    private static final String REQUIRES_BASE = "color=\"" + GRAY + "\"";

    private static final Map<String, Integer> weights = new HashMap<>();

    private static final List<Set<String>> ranks = new ArrayList<>();

    private static void weight(String s, String t, int w) {
        weights.put(s + ":" + t, w);
    }

    private static int weightOf(String s, String t) {
        int w = weights.getOrDefault(s + ":" + t, 1);
        if (w != 1)
            return w;
        if (s.startsWith("java.") && t.startsWith("java."))
            return 10;
        return 1;
    }

    static {
        int h = 1000;
        weight("java.se", "java.sql.rowset", h * 10);
        weight("java.sql.rowset", "java.sql", h * 10);
        weight("java.sql", "java.xml", h * 10);
        weight("java.xml", "java.base", h * 10);
        ranks.add(Set.of("java.logging", "java.scripting", "java.xml"));
        ranks.add(Set.of("java.sql"));
        ranks.add(Set.of("java.compiler", "java.instrument"));
        ranks.add(Set.of("java.desktop", "java.management"));
        ranks.add(Set.of("java.corba", "java.xml.ws"));
        ranks.add(Set.of("java.xml.bind", "java.xml.ws.annotation"));
    }

    private final Path dir;

    private final Set<ModuleDescriptor> javaGroup;

    private final Set<ModuleDescriptor> jdkGroup;

    GenGraphs(Path dir, Set<ModuleDescriptor> javaGroup, Set<ModuleDescriptor> jdkGroup) {
        this.dir = dir;
        this.javaGroup = Collections.unmodifiableSet(javaGroup);
        this.jdkGroup = Collections.unmodifiableSet(jdkGroup);
    }

    void genDotFile(ModuleReference mref) throws IOException {
        String name = mref.descriptor().name();
        genDotFile(name, Set.of(name));
    }

    void genDotFile(String name, Set<String> roots) throws IOException {
        Configuration cf = Configuration.empty().resolveRequires(ModuleFinder.ofSystem(), ModuleFinder.of(), roots);
        Set<ModuleDescriptor> mds = cf.modules().stream().map(ResolvedModule::reference).map(ModuleReference::descriptor).collect(toSet());
        try (OutputStream os = Files.newOutputStream(dir.resolve(name + ".dot"));
            PrintStream out = new PrintStream(os)) {
            printGraph(out, name, gengraph(cf), mds.stream().collect(toMap(ModuleDescriptor::name, Function.identity())));
        }
        if (name.equals("java.se") || name.equals("java.se.ee")) {
            try (OutputStream os = Files.newOutputStream(dir.resolve(name + "-spec.dot"));
                PrintStream out = new PrintStream(os)) {
                Graph<String> graph = requiresTransitiveGraph(cf, true);
                printGraph(out, name, graph, mds.stream().filter(md -> !md.name().startsWith("jdk.") && graph.nodes().contains(md.name())).collect(toMap(ModuleDescriptor::name, Function.identity())));
            }
        }
    }

    private void printGraph(PrintStream out, String name, Graph<String> graph, Map<String, ModuleDescriptor> nameToModule) throws IOException {
        Set<ModuleDescriptor> descriptors = new TreeSet<>(nameToModule.values());
        out.format("digraph \"%s\" {%n", name);
        out.format("size=\"25,25\";");
        out.format("nodesep=.5;%n");
        out.format("ranksep=1.5;%n");
        out.format("pencolor=transparent;%n");
        out.format("node [shape=plaintext, fontname=\"DejaVuSans\", fontsize=36, margin=\".2,.2\"];%n");
        out.format("edge [penwidth=4, color=\"#999999\", arrowhead=open, arrowsize=2];%n");
        out.format("subgraph %sse {%n", name.equals("jdk") ? "cluster_" : "");
        descriptors.stream().filter(javaGroup::contains).map(ModuleDescriptor::name).forEach(mn -> out.format("  \"%s\" [fontcolor=\"%s\", group=%s];%n", mn, ORANGE, "java"));
        out.format("}%n");
        ranks.stream().map(group -> descriptors.stream().map(ModuleDescriptor::name).filter(group::contains).map(mn -> "\"" + mn + "\"").collect(joining(","))).filter(group -> group.length() > 0).forEach(group -> out.format("{rank=same %s}%n", group));
        descriptors.stream().filter(jdkGroup::contains).map(ModuleDescriptor::name).forEach(mn -> out.format("  \"%s\" [fontcolor=\"%s\", group=%s];%n", mn, BLUE, "jdk"));
        descriptors.stream().forEach(md -> {
            String mn = md.name();
            Set<String> requiresTransitive = md.requires().stream().filter(d -> d.modifiers().contains(TRANSITIVE)).map(d -> d.name()).collect(toSet());
            graph.adjacentNodes(mn).stream().filter(nameToModule::containsKey).forEach(dn -> {
                String attr = dn.equals("java.base") ? REQUIRES_BASE : (requiresTransitive.contains(dn) ? REEXPORTS : REQUIRES);
                int w = weightOf(mn, dn);
                if (w > 1)
                    attr += "weight=" + w;
                out.format("  \"%s\" -> \"%s\" [%s];%n", mn, dn, attr);
            });
        });
        out.println("}");
    }

    private Graph<String> gengraph(Configuration cf) {
        Graph.Builder<String> builder = new Graph.Builder<>();
        for (ResolvedModule resolvedModule : cf.modules()) {
            String mn = resolvedModule.reference().descriptor().name();
            builder.addNode(mn);
            resolvedModule.reads().stream().map(ResolvedModule::name).forEach(target -> builder.addEdge(mn, target));
        }
        Graph<String> rpg = requiresTransitiveGraph(cf, false);
        return builder.build().reduce(rpg);
    }

    private Graph<String> requiresTransitiveGraph(Configuration cf, boolean includeBase) {
        Graph.Builder<String> builder = new Graph.Builder<>();
        for (ResolvedModule resolvedModule : cf.modules()) {
            ModuleDescriptor descriptor = resolvedModule.reference().descriptor();
            String mn = descriptor.name();
            descriptor.requires().stream().filter(d -> d.modifiers().contains(TRANSITIVE) || (includeBase && d.name().equals("java.base"))).map(d -> d.name()).forEach(d -> builder.addEdge(mn, d));
        }
        return builder.build().reduce();
    }
}
