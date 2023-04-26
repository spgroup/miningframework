package java.lang.module;

import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static jdk.internal.module.Checks.*;
import static java.util.Objects.*;
import jdk.internal.module.Checks;
import jdk.internal.module.ModuleInfo;

public class ModuleDescriptor implements Comparable<ModuleDescriptor> {

    public static enum Modifier {

        OPEN, AUTOMATIC, SYNTHETIC, MANDATED
    }

    public final static class Requires implements Comparable<Requires> {

        public static enum Modifier {

            TRANSITIVE, STATIC, SYNTHETIC, MANDATED
        }

        private final Set<Modifier> mods;

        private final String name;

        private final Version compiledVersion;

        private final String rawCompiledVersion;

        private Requires(Set<Modifier> ms, String mn, Version v, String vs) {
            assert v == null || vs == null;
            if (ms.isEmpty()) {
                ms = Collections.emptySet();
            } else {
                ms = Collections.unmodifiableSet(EnumSet.copyOf(ms));
            }
            this.mods = ms;
            this.name = mn;
            this.compiledVersion = v;
            this.rawCompiledVersion = vs;
        }

        private Requires(Set<Modifier> ms, String mn, Version v, boolean unused) {
            this.mods = ms;
            this.name = mn;
            this.compiledVersion = v;
            this.rawCompiledVersion = null;
        }

        public Set<Modifier> modifiers() {
            return mods;
        }

        public String name() {
            return name;
        }

        public Optional<Version> compiledVersion() {
            return Optional.ofNullable(compiledVersion);
        }

        public Optional<String> rawCompiledVersion() {
            if (compiledVersion != null) {
                return Optional.of(compiledVersion.toString());
            } else {
                return Optional.ofNullable(rawCompiledVersion);
            }
        }

        @Override
        public int compareTo(Requires that) {
            if (this == that)
                return 0;
            int c = this.name().compareTo(that.name());
            if (c != 0)
                return c;
            long v1 = modsValue(this.modifiers());
            long v2 = modsValue(that.modifiers());
            c = Long.compare(v1, v2);
            if (c != 0)
                return c;
            c = compare(this.compiledVersion, that.compiledVersion);
            if (c != 0)
                return c;
            c = compare(this.rawCompiledVersion, that.rawCompiledVersion);
            if (c != 0)
                return c;
            return 0;
        }

        @Override
        public boolean equals(Object ob) {
            if (!(ob instanceof Requires))
                return false;
            Requires that = (Requires) ob;
            return name.equals(that.name) && mods.equals(that.mods) && Objects.equals(compiledVersion, that.compiledVersion) && Objects.equals(rawCompiledVersion, that.rawCompiledVersion);
        }

        @Override
        public int hashCode() {
            int hash = name.hashCode() * 43 + mods.hashCode();
            if (compiledVersion != null)
                hash = hash * 43 + compiledVersion.hashCode();
            if (rawCompiledVersion != null)
                hash = hash * 43 + rawCompiledVersion.hashCode();
            return hash;
        }

        @Override
        public String toString() {
            String what;
            if (compiledVersion != null) {
                what = name() + " (@" + compiledVersion + ")";
            } else {
                what = name();
            }
            return ModuleDescriptor.toString(mods, what);
        }
    }

    public final static class Exports implements Comparable<Exports> {

        public static enum Modifier {

            SYNTHETIC, MANDATED
        }

        private final Set<Modifier> mods;

        private final String source;

        private final Set<String> targets;

        private Exports(Set<Modifier> ms, String source, Set<String> targets) {
            if (ms.isEmpty()) {
                ms = Collections.emptySet();
            } else {
                ms = Collections.unmodifiableSet(EnumSet.copyOf(ms));
            }
            this.mods = ms;
            this.source = source;
            this.targets = emptyOrUnmodifiableSet(targets);
        }

        private Exports(Set<Modifier> ms, String source, Set<String> targets, boolean unused) {
            this.mods = ms;
            this.source = source;
            this.targets = targets;
        }

        public Set<Modifier> modifiers() {
            return mods;
        }

        public boolean isQualified() {
            return !targets.isEmpty();
        }

        public String source() {
            return source;
        }

        public Set<String> targets() {
            return targets;
        }

        @Override
        public int compareTo(Exports that) {
            if (this == that)
                return 0;
            int c = source.compareTo(that.source);
            if (c != 0)
                return c;
            long v1 = modsValue(this.modifiers());
            long v2 = modsValue(that.modifiers());
            c = Long.compare(v1, v2);
            if (c != 0)
                return c;
            c = compare(targets, that.targets);
            if (c != 0)
                return c;
            return 0;
        }

        @Override
        public int hashCode() {
            int hash = mods.hashCode();
            hash = hash * 43 + source.hashCode();
            return hash * 43 + targets.hashCode();
        }

        @Override
        public boolean equals(Object ob) {
            if (!(ob instanceof Exports))
                return false;
            Exports other = (Exports) ob;
            return Objects.equals(this.mods, other.mods) && Objects.equals(this.source, other.source) && Objects.equals(this.targets, other.targets);
        }

        @Override
        public String toString() {
            String s = ModuleDescriptor.toString(mods, source);
            if (targets.isEmpty())
                return s;
            else
                return s + " to " + targets;
        }
    }

    public final static class Opens implements Comparable<Opens> {

        public static enum Modifier {

            SYNTHETIC, MANDATED
        }

        private final Set<Modifier> mods;

        private final String source;

        private final Set<String> targets;

        private Opens(Set<Modifier> ms, String source, Set<String> targets) {
            if (ms.isEmpty()) {
                ms = Collections.emptySet();
            } else {
                ms = Collections.unmodifiableSet(EnumSet.copyOf(ms));
            }
            this.mods = ms;
            this.source = source;
            this.targets = emptyOrUnmodifiableSet(targets);
        }

        private Opens(Set<Modifier> ms, String source, Set<String> targets, boolean unused) {
            this.mods = ms;
            this.source = source;
            this.targets = targets;
        }

        public Set<Modifier> modifiers() {
            return mods;
        }

        public boolean isQualified() {
            return !targets.isEmpty();
        }

        public String source() {
            return source;
        }

        public Set<String> targets() {
            return targets;
        }

        @Override
        public int compareTo(Opens that) {
            if (this == that)
                return 0;
            int c = source.compareTo(that.source);
            if (c != 0)
                return c;
            long v1 = modsValue(this.modifiers());
            long v2 = modsValue(that.modifiers());
            c = Long.compare(v1, v2);
            if (c != 0)
                return c;
            c = compare(targets, that.targets);
            if (c != 0)
                return c;
            return 0;
        }

        @Override
        public int hashCode() {
            int hash = mods.hashCode();
            hash = hash * 43 + source.hashCode();
            return hash * 43 + targets.hashCode();
        }

        @Override
        public boolean equals(Object ob) {
            if (!(ob instanceof Opens))
                return false;
            Opens other = (Opens) ob;
            return Objects.equals(this.mods, other.mods) && Objects.equals(this.source, other.source) && Objects.equals(this.targets, other.targets);
        }

        @Override
        public String toString() {
            String s = ModuleDescriptor.toString(mods, source);
            if (targets.isEmpty())
                return s;
            else
                return s + " to " + targets;
        }
    }

    public final static class Provides implements Comparable<Provides> {

        private final String service;

        private final List<String> providers;

        private Provides(String service, List<String> providers) {
            this.service = service;
            this.providers = Collections.unmodifiableList(providers);
        }

        private Provides(String service, List<String> providers, boolean unused) {
            this.service = service;
            this.providers = providers;
        }

        public String service() {
            return service;
        }

        public List<String> providers() {
            return providers;
        }

        public int compareTo(Provides that) {
            if (this == that)
                return 0;
            int c = service.compareTo(that.service);
            if (c != 0)
                return c;
            int size1 = this.providers.size();
            int size2 = that.providers.size();
            for (int index = 0; index < Math.min(size1, size2); index++) {
                String e1 = this.providers.get(index);
                String e2 = that.providers.get(index);
                c = e1.compareTo(e2);
                if (c != 0)
                    return c;
            }
            if (size1 == size2) {
                return 0;
            } else {
                return (size1 > size2) ? 1 : -1;
            }
        }

        @Override
        public int hashCode() {
            return service.hashCode() * 43 + providers.hashCode();
        }

        @Override
        public boolean equals(Object ob) {
            if (!(ob instanceof Provides))
                return false;
            Provides other = (Provides) ob;
            return Objects.equals(this.service, other.service) && Objects.equals(this.providers, other.providers);
        }

        @Override
        public String toString() {
            return service + " with " + providers;
        }
    }

    public final static class Version implements Comparable<Version> {

        private final String version;

        private final List<Object> sequence;

        private final List<Object> pre;

        private final List<Object> build;

        private static int takeNumber(String s, int i, List<Object> acc) {
            char c = s.charAt(i);
            int d = (c - '0');
            int n = s.length();
            while (++i < n) {
                c = s.charAt(i);
                if (c >= '0' && c <= '9') {
                    d = d * 10 + (c - '0');
                    continue;
                }
                break;
            }
            acc.add(d);
            return i;
        }

        private static int takeString(String s, int i, List<Object> acc) {
            int b = i;
            int n = s.length();
            while (++i < n) {
                char c = s.charAt(i);
                if (c != '.' && c != '-' && c != '+' && !(c >= '0' && c <= '9'))
                    continue;
                break;
            }
            acc.add(s.substring(b, i));
            return i;
        }

        private Version(String v) {
            if (v == null)
                throw new IllegalArgumentException("Null version string");
            int n = v.length();
            if (n == 0)
                throw new IllegalArgumentException("Empty version string");
            int i = 0;
            char c = v.charAt(i);
            if (!(c >= '0' && c <= '9'))
                throw new IllegalArgumentException(v + ": Version string does not start" + " with a number");
            List<Object> sequence = new ArrayList<>(4);
            List<Object> pre = new ArrayList<>(2);
            List<Object> build = new ArrayList<>(2);
            i = takeNumber(v, i, sequence);
            while (i < n) {
                c = v.charAt(i);
                if (c == '.') {
                    i++;
                    continue;
                }
                if (c == '-' || c == '+') {
                    i++;
                    break;
                }
                if (c >= '0' && c <= '9')
                    i = takeNumber(v, i, sequence);
                else
                    i = takeString(v, i, sequence);
            }
            if (c == '-' && i >= n)
                throw new IllegalArgumentException(v + ": Empty pre-release");
            while (i < n) {
                c = v.charAt(i);
                if (c >= '0' && c <= '9')
                    i = takeNumber(v, i, pre);
                else
                    i = takeString(v, i, pre);
                if (i >= n)
                    break;
                c = v.charAt(i);
                if (c == '.' || c == '-') {
                    i++;
                    continue;
                }
                if (c == '+') {
                    i++;
                    break;
                }
            }
            if (c == '+' && i >= n)
                throw new IllegalArgumentException(v + ": Empty pre-release");
            while (i < n) {
                c = v.charAt(i);
                if (c >= '0' && c <= '9')
                    i = takeNumber(v, i, build);
                else
                    i = takeString(v, i, build);
                if (i >= n)
                    break;
                c = v.charAt(i);
                if (c == '.' || c == '-' || c == '+') {
                    i++;
                    continue;
                }
            }
            this.version = v;
            this.sequence = sequence;
            this.pre = pre;
            this.build = build;
        }

        public static Version parse(String v) {
            return new Version(v);
        }

        @SuppressWarnings("unchecked")
        private int cmp(Object o1, Object o2) {
            return ((Comparable) o1).compareTo(o2);
        }

        private int compareTokens(List<Object> ts1, List<Object> ts2) {
            int n = Math.min(ts1.size(), ts2.size());
            for (int i = 0; i < n; i++) {
                Object o1 = ts1.get(i);
                Object o2 = ts2.get(i);
                if ((o1 instanceof Integer && o2 instanceof Integer) || (o1 instanceof String && o2 instanceof String)) {
                    int c = cmp(o1, o2);
                    if (c == 0)
                        continue;
                    return c;
                }
                int c = o1.toString().compareTo(o2.toString());
                if (c == 0)
                    continue;
                return c;
            }
            List<Object> rest = ts1.size() > ts2.size() ? ts1 : ts2;
            int e = rest.size();
            for (int i = n; i < e; i++) {
                Object o = rest.get(i);
                if (o instanceof Integer && ((Integer) o) == 0)
                    continue;
                return ts1.size() - ts2.size();
            }
            return 0;
        }

        @Override
        public int compareTo(Version that) {
            int c = compareTokens(this.sequence, that.sequence);
            if (c != 0)
                return c;
            if (this.pre.isEmpty()) {
                if (!that.pre.isEmpty())
                    return +1;
            } else {
                if (that.pre.isEmpty())
                    return -1;
            }
            c = compareTokens(this.pre, that.pre);
            if (c != 0)
                return c;
            return compareTokens(this.build, that.build);
        }

        @Override
        public boolean equals(Object ob) {
            if (!(ob instanceof Version))
                return false;
            return compareTo((Version) ob) == 0;
        }

        @Override
        public int hashCode() {
            return version.hashCode();
        }

        @Override
        public String toString() {
            return version;
        }
    }

    private final String name;

    private final Version version;

    private final String rawVersionString;

    private final Set<Modifier> modifiers;

    private final boolean open;

    private final boolean automatic;

    private final Set<Requires> requires;

    private final Set<Exports> exports;

    private final Set<Opens> opens;

    private final Set<String> uses;

    private final Set<Provides> provides;

    private final Set<String> packages;

    private final String mainClass;

    private ModuleDescriptor(String name, Version version, String rawVersionString, Set<Modifier> modifiers, Set<Requires> requires, Set<Exports> exports, Set<Opens> opens, Set<String> uses, Set<Provides> provides, Set<String> packages, String mainClass) {
        assert version == null || rawVersionString == null;
        this.name = name;
        this.version = version;
        this.rawVersionString = rawVersionString;
        this.modifiers = emptyOrUnmodifiableSet(modifiers);
        this.open = modifiers.contains(Modifier.OPEN);
        this.automatic = modifiers.contains(Modifier.AUTOMATIC);
        assert (requires.stream().map(Requires::name).distinct().count() == requires.size());
        this.requires = emptyOrUnmodifiableSet(requires);
        this.exports = emptyOrUnmodifiableSet(exports);
        this.opens = emptyOrUnmodifiableSet(opens);
        this.uses = emptyOrUnmodifiableSet(uses);
        this.provides = emptyOrUnmodifiableSet(provides);
        this.packages = emptyOrUnmodifiableSet(packages);
        this.mainClass = mainClass;
    }

    ModuleDescriptor(String name, Version version, Set<Modifier> modifiers, Set<Requires> requires, Set<Exports> exports, Set<Opens> opens, Set<String> uses, Set<Provides> provides, Set<String> packages, String mainClass, int hashCode, boolean unused) {
        this.name = name;
        this.version = version;
        this.rawVersionString = null;
        this.modifiers = modifiers;
        this.open = modifiers.contains(Modifier.OPEN);
        this.automatic = modifiers.contains(Modifier.AUTOMATIC);
        this.requires = requires;
        this.exports = exports;
        this.opens = opens;
        this.uses = uses;
        this.provides = provides;
        this.packages = packages;
        this.mainClass = mainClass;
        this.hash = hashCode;
    }

    public String name() {
        return name;
    }

    public Set<Modifier> modifiers() {
        return modifiers;
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isAutomatic() {
        return automatic;
    }

    public Set<Requires> requires() {
        return requires;
    }

    public Set<Exports> exports() {
        return exports;
    }

    public Set<Opens> opens() {
        return opens;
    }

    public Set<String> uses() {
        return uses;
    }

    public Set<Provides> provides() {
        return provides;
    }

    public Optional<Version> version() {
        return Optional.ofNullable(version);
    }

    public Optional<String> rawVersion() {
        if (version != null) {
            return Optional.of(version.toString());
        } else {
            return Optional.ofNullable(rawVersionString);
        }
    }

    public String toNameAndVersion() {
        if (version != null) {
            return name() + "@" + version;
        } else {
            return name();
        }
    }

    public Optional<String> mainClass() {
        return Optional.ofNullable(mainClass);
    }

    public Set<String> packages() {
        return packages;
    }

    public static final class Builder {

        final String name;

        final boolean strict;

        final Set<Modifier> modifiers;

        final boolean open;

        final boolean automatic;

        final Set<String> packages = new HashSet<>();

        final Map<String, Requires> requires = new HashMap<>();

        final Map<String, Exports> exports = new HashMap<>();

        final Map<String, Opens> opens = new HashMap<>();

        final Set<String> uses = new HashSet<>();

        final Map<String, Provides> provides = new HashMap<>();

        Version version;

        String rawVersionString;

        String mainClass;

        Builder(String name, boolean strict, Set<Modifier> modifiers) {
            this.name = (strict) ? requireModuleName(name) : name;
            this.strict = strict;
            this.modifiers = modifiers;
            this.open = modifiers.contains(Modifier.OPEN);
            this.automatic = modifiers.contains(Modifier.AUTOMATIC);
            assert !open || !automatic;
        }

        Set<String> packages() {
            return Collections.unmodifiableSet(packages);
        }

        public Builder requires(Requires req) {
            if (automatic)
                throw new IllegalStateException("Automatic modules cannot declare" + " dependences");
            String mn = req.name();
            if (name.equals(mn))
                throw new IllegalArgumentException("Dependence on self");
            if (requires.containsKey(mn))
                throw new IllegalStateException("Dependence upon " + mn + " already declared");
            requires.put(mn, req);
            return this;
        }

        public Builder requires(Set<Requires.Modifier> ms, String mn, Version compiledVersion) {
            Objects.requireNonNull(compiledVersion);
            if (strict)
                mn = requireModuleName(mn);
            return requires(new Requires(ms, mn, compiledVersion, null));
        }

        Builder requires(Set<Requires.Modifier> ms, String mn, String rawCompiledVersion) {
            Requires r;
            try {
                Version v = Version.parse(rawCompiledVersion);
                r = new Requires(ms, mn, v, null);
            } catch (IllegalArgumentException e) {
                if (strict)
                    throw e;
                r = new Requires(ms, mn, null, rawCompiledVersion);
            }
            return requires(r);
        }

        public Builder requires(Set<Requires.Modifier> ms, String mn) {
            if (strict)
                mn = requireModuleName(mn);
            return requires(new Requires(ms, mn, null, null));
        }

        public Builder requires(String mn) {
            return requires(EnumSet.noneOf(Requires.Modifier.class), mn);
        }

        public Builder exports(Exports e) {
            if (automatic) {
                throw new IllegalStateException("Automatic modules cannot declare" + " exported packages");
            }
            String source = e.source();
            if (exports.containsKey(source)) {
                throw new IllegalStateException("Exported package " + source + " already declared");
            }
            exports.put(source, e);
            packages.add(source);
            return this;
        }

        public Builder exports(Set<Exports.Modifier> ms, String pn, Set<String> targets) {
            Exports e = new Exports(ms, pn, targets);
            targets = e.targets();
            if (targets.isEmpty())
                throw new IllegalArgumentException("Empty target set");
            if (strict) {
                requirePackageName(e.source());
                targets.stream().forEach(Checks::requireModuleName);
            }
            return exports(e);
        }

        public Builder exports(Set<Exports.Modifier> ms, String pn) {
            if (strict) {
                requirePackageName(pn);
            }
            Exports e = new Exports(ms, pn, Collections.emptySet());
            return exports(e);
        }

        public Builder exports(String pn, Set<String> targets) {
            return exports(Collections.emptySet(), pn, targets);
        }

        public Builder exports(String pn) {
            return exports(Collections.emptySet(), pn);
        }

        public Builder opens(Opens obj) {
            if (open || automatic) {
                throw new IllegalStateException("Open or automatic modules cannot" + " declare open packages");
            }
            String source = obj.source();
            if (opens.containsKey(source)) {
                throw new IllegalStateException("Open package " + source + " already declared");
            }
            opens.put(source, obj);
            packages.add(source);
            return this;
        }

        public Builder opens(Set<Opens.Modifier> ms, String pn, Set<String> targets) {
            Opens opens = new Opens(ms, pn, targets);
            targets = opens.targets();
            if (targets.isEmpty())
                throw new IllegalArgumentException("Empty target set");
            if (strict) {
                requirePackageName(opens.source());
                targets.stream().forEach(Checks::requireModuleName);
            }
            return opens(opens);
        }

        public Builder opens(Set<Opens.Modifier> ms, String pn) {
            if (strict) {
                requirePackageName(pn);
            }
            Opens e = new Opens(ms, pn, Collections.emptySet());
            return opens(e);
        }

        public Builder opens(String pn, Set<String> targets) {
            return opens(Collections.emptySet(), pn, targets);
        }

        public Builder opens(String pn) {
            return opens(Collections.emptySet(), pn);
        }

        public Builder uses(String service) {
            if (automatic)
                throw new IllegalStateException("Automatic modules can not declare" + " service dependences");
            if (uses.contains(requireServiceTypeName(service)))
                throw new IllegalStateException("Dependence upon service " + service + " already declared");
            uses.add(service);
            return this;
        }

        public Builder provides(Provides p) {
            String service = p.service();
            if (provides.containsKey(service))
                throw new IllegalStateException("Providers of service " + service + " already declared");
            provides.put(service, p);
            p.providers().forEach(name -> packages.add(packageName(name)));
            return this;
        }

        public Builder provides(String service, List<String> providers) {
            Provides p = new Provides(service, providers);
            List<String> providerNames = p.providers();
            if (providerNames.isEmpty())
                throw new IllegalArgumentException("Empty providers set");
            if (strict) {
                requireServiceTypeName(p.service());
                providerNames.forEach(Checks::requireServiceProviderName);
            } else {
                String pn = packageName(service);
                if (pn.isEmpty()) {
                    throw new IllegalArgumentException(service + ": unnamed package");
                }
                for (String name : providerNames) {
                    pn = packageName(name);
                    if (pn.isEmpty()) {
                        throw new IllegalArgumentException(name + ": unnamed package");
                    }
                }
            }
            return provides(p);
        }

        public Builder packages(Set<String> pns) {
            if (strict) {
                pns = new HashSet<>(pns);
                pns.forEach(Checks::requirePackageName);
            }
            this.packages.addAll(pns);
            return this;
        }

        public Builder version(Version v) {
            version = requireNonNull(v);
            rawVersionString = null;
            return this;
        }

        public Builder version(String vs) {
            try {
                version = Version.parse(vs);
                rawVersionString = null;
            } catch (IllegalArgumentException e) {
                if (strict)
                    throw e;
                version = null;
                rawVersionString = vs;
            }
            return this;
        }

        public Builder mainClass(String mc) {
            String pn;
            if (strict) {
                mc = requireQualifiedClassName("main class name", mc);
                pn = packageName(mc);
                assert !pn.isEmpty();
            } else {
                pn = packageName(mc);
                if (pn.isEmpty()) {
                    throw new IllegalArgumentException(mc + ": unnamed package");
                }
            }
            mainClass = mc;
            packages.add(pn);
            return this;
        }

        public ModuleDescriptor build() {
            Set<Requires> requires = new HashSet<>(this.requires.values());
            Set<Exports> exports = new HashSet<>(this.exports.values());
            Set<Opens> opens = new HashSet<>(this.opens.values());
            if (strict && !name.equals("java.base") && !this.requires.containsKey("java.base")) {
                requires.add(new Requires(Set.of(Requires.Modifier.MANDATED), "java.base", null, null));
            }
            Set<Provides> provides = new HashSet<>(this.provides.values());
            return new ModuleDescriptor(name, version, rawVersionString, modifiers, requires, exports, opens, uses, provides, packages, mainClass);
        }
    }

    @Override
    public int compareTo(ModuleDescriptor that) {
        if (this == that)
            return 0;
        int c = this.name().compareTo(that.name());
        if (c != 0)
            return c;
        c = compare(this.version, that.version);
        if (c != 0)
            return c;
        c = compare(this.rawVersionString, that.rawVersionString);
        if (c != 0)
            return c;
        long v1 = modsValue(this.modifiers());
        long v2 = modsValue(that.modifiers());
        c = Long.compare(v1, v2);
        if (c != 0)
            return c;
        c = compare(this.requires, that.requires);
        if (c != 0)
            return c;
        c = compare(this.packages, that.packages);
        if (c != 0)
            return c;
        c = compare(this.exports, that.exports);
        if (c != 0)
            return c;
        c = compare(this.opens, that.opens);
        if (c != 0)
            return c;
        c = compare(this.uses, that.uses);
        if (c != 0)
            return c;
        c = compare(this.provides, that.provides);
        if (c != 0)
            return c;
        c = compare(this.mainClass, that.mainClass);
        if (c != 0)
            return c;
        return 0;
    }

    @Override
    public boolean equals(Object ob) {
        if (ob == this)
            return true;
        if (!(ob instanceof ModuleDescriptor))
            return false;
        ModuleDescriptor that = (ModuleDescriptor) ob;
        return (name.equals(that.name) && modifiers.equals(that.modifiers) && requires.equals(that.requires) && Objects.equals(packages, that.packages) && exports.equals(that.exports) && opens.equals(that.opens) && uses.equals(that.uses) && provides.equals(that.provides) && Objects.equals(version, that.version) && Objects.equals(rawVersionString, that.rawVersionString) && Objects.equals(mainClass, that.mainClass));
    }

    @Override
    public int hashCode() {
        int hc = hash;
        if (hc == 0) {
            hc = name.hashCode();
            hc = hc * 43 + Objects.hashCode(modifiers);
            hc = hc * 43 + requires.hashCode();
            hc = hc * 43 + Objects.hashCode(packages);
            hc = hc * 43 + exports.hashCode();
            hc = hc * 43 + opens.hashCode();
            hc = hc * 43 + uses.hashCode();
            hc = hc * 43 + provides.hashCode();
            hc = hc * 43 + Objects.hashCode(version);
            hc = hc * 43 + Objects.hashCode(rawVersionString);
            hc = hc * 43 + Objects.hashCode(mainClass);
            if (hc == 0)
                hc = -1;
            hash = hc;
        }
        return hc;
    }

    private transient int hash;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isOpen())
            sb.append("open ");
        sb.append("module { name: ").append(toNameAndVersion());
        if (!requires.isEmpty())
            sb.append(", ").append(requires);
        if (!uses.isEmpty())
            sb.append(", uses: ").append(uses);
        if (!exports.isEmpty())
            sb.append(", exports: ").append(exports);
        if (!opens.isEmpty())
            sb.append(", opens: ").append(opens);
        if (!provides.isEmpty()) {
            sb.append(", provides: ").append(provides);
        }
        sb.append(" }");
        return sb.toString();
    }

    public static Builder newModule(String name, Set<Modifier> ms) {
        Set<Modifier> mods = new HashSet<>(ms);
        if (mods.contains(Modifier.AUTOMATIC) && mods.size() > 1)
            throw new IllegalArgumentException("AUTOMATIC cannot be used with" + " other modifiers");
        return new Builder(name, true, mods);
    }

    public static Builder newModule(String name) {
        return new Builder(name, true, Set.of());
    }

    public static Builder newOpenModule(String name) {
        return new Builder(name, true, Set.of(Modifier.OPEN));
    }

    public static Builder newAutomaticModule(String name) {
        return new Builder(name, true, Set.of(Modifier.AUTOMATIC));
    }

    public static ModuleDescriptor read(InputStream in, Supplier<Set<String>> packageFinder) throws IOException {
        return ModuleInfo.read(in, requireNonNull(packageFinder)).descriptor();
    }

    public static ModuleDescriptor read(InputStream in) throws IOException {
        return ModuleInfo.read(in, null).descriptor();
    }

    public static ModuleDescriptor read(ByteBuffer bb, Supplier<Set<String>> packageFinder) {
        return ModuleInfo.read(bb, requireNonNull(packageFinder)).descriptor();
    }

    public static ModuleDescriptor read(ByteBuffer bb) {
        return ModuleInfo.read(bb, null).descriptor();
    }

    private static <K, V> Map<K, V> emptyOrUnmodifiableMap(Map<K, V> map) {
        if (map.isEmpty()) {
            return Collections.emptyMap();
        } else if (map.size() == 1) {
            Map.Entry<K, V> entry = map.entrySet().iterator().next();
            return Collections.singletonMap(entry.getKey(), entry.getValue());
        } else {
            return Collections.unmodifiableMap(map);
        }
    }

    private static <T> Set<T> emptyOrUnmodifiableSet(Set<T> set) {
        if (set.isEmpty()) {
            return Collections.emptySet();
        } else if (set.size() == 1) {
            return Collections.singleton(set.iterator().next());
        } else {
            return Collections.unmodifiableSet(set);
        }
    }

    private static String packageName(String cn) {
        int index = cn.lastIndexOf('.');
        return (index == -1) ? "" : cn.substring(0, index);
    }

    private static <M> String toString(Set<M> mods, String what) {
        return (Stream.concat(mods.stream().map(e -> e.toString().toLowerCase()), Stream.of(what))).collect(Collectors.joining(" "));
    }

    private static <T extends Object & Comparable<? super T>> int compare(T obj1, T obj2) {
        if (obj1 != null) {
            return (obj2 != null) ? obj1.compareTo(obj2) : 1;
        } else {
            return (obj2 == null) ? 0 : -1;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Object & Comparable<? super T>> int compare(Set<T> s1, Set<T> s2) {
        T[] a1 = (T[]) s1.toArray();
        T[] a2 = (T[]) s2.toArray();
        Arrays.sort(a1);
        Arrays.sort(a2);
        return Arrays.compare(a1, a2);
    }

    private static <E extends Enum<E>> long modsValue(Set<E> set) {
        long value = 0;
        for (Enum<E> e : set) {
            value += 1 << e.ordinal();
        }
        return value;
    }

    static {
        jdk.internal.misc.SharedSecrets.setJavaLangModuleAccess(new jdk.internal.misc.JavaLangModuleAccess() {

            @Override
            public Builder newModuleBuilder(String mn, boolean strict, Set<ModuleDescriptor.Modifier> modifiers) {
                return new Builder(mn, strict, modifiers);
            }

            @Override
            public Set<String> packages(ModuleDescriptor.Builder builder) {
                return builder.packages();
            }

            @Override
            public void requires(ModuleDescriptor.Builder builder, Set<Requires.Modifier> ms, String mn, String rawCompiledVersion) {
                builder.requires(ms, mn, rawCompiledVersion);
            }

            @Override
            public Requires newRequires(Set<Requires.Modifier> ms, String mn, Version v) {
                return new Requires(ms, mn, v, true);
            }

            @Override
            public Exports newExports(Set<Exports.Modifier> ms, String source) {
                return new Exports(ms, source, Collections.emptySet(), true);
            }

            @Override
            public Exports newExports(Set<Exports.Modifier> ms, String source, Set<String> targets) {
                return new Exports(ms, source, targets, true);
            }

            @Override
            public Opens newOpens(Set<Opens.Modifier> ms, String source, Set<String> targets) {
                return new Opens(ms, source, targets, true);
            }

            @Override
            public Opens newOpens(Set<Opens.Modifier> ms, String source) {
                return new Opens(ms, source, Collections.emptySet(), true);
            }

            @Override
            public Provides newProvides(String service, List<String> providers) {
                return new Provides(service, providers, true);
            }

            @Override
            public ModuleDescriptor newModuleDescriptor(String name, Version version, Set<ModuleDescriptor.Modifier> modifiers, Set<Requires> requires, Set<Exports> exports, Set<Opens> opens, Set<String> uses, Set<Provides> provides, Set<String> packages, String mainClass, int hashCode) {
                return new ModuleDescriptor(name, version, modifiers, requires, exports, opens, uses, provides, packages, mainClass, hashCode, false);
            }

            @Override
            public Configuration resolveAndBind(ModuleFinder finder, Collection<String> roots, boolean check, PrintStream traceOutput) {
                return Configuration.resolveAndBind(finder, roots, check, traceOutput);
            }
        });
    }
}
