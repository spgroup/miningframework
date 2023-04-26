package java.lang.module;

import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
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

    public final static class Requires implements Comparable<Requires> {

        public static enum Modifier {

            TRANSITIVE, STATIC, SYNTHETIC, MANDATED
        }

        private final Set<Modifier> mods;

        private final String name;

        private final Version compiledVersion;

        private Requires(Set<Modifier> ms, String mn, Version v) {
            if (ms.isEmpty()) {
                ms = Collections.emptySet();
            } else {
                ms = Collections.unmodifiableSet(EnumSet.copyOf(ms));
            }
            this.mods = ms;
            this.name = mn;
            this.compiledVersion = v;
        }

        private Requires(Set<Modifier> ms, String mn, Version v, boolean unused) {
            this.mods = ms;
            this.name = mn;
            this.compiledVersion = v;
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

        @Override
        public int compareTo(Requires that) {
            int c = this.name().compareTo(that.name());
            if (c != 0)
                return c;
            c = Long.compare(this.modsValue(), that.modsValue());
            if (c != 0)
                return c;
            if (this.compiledVersion != null) {
                if (that.compiledVersion != null)
                    c = this.compiledVersion.compareTo(that.compiledVersion);
                else
                    c = 1;
            } else {
                if (that.compiledVersion != null)
                    c = -1;
            }
            return c;
        }

        private long modsValue() {
            long value = 0;
            for (Modifier m : mods) {
                value += 1 << m.ordinal();
            }
            return value;
        }

        @Override
        public boolean equals(Object ob) {
            if (!(ob instanceof Requires))
                return false;
            Requires that = (Requires) ob;
            return name.equals(that.name) && mods.equals(that.mods) && Objects.equals(compiledVersion, that.compiledVersion);
        }

        @Override
        public int hashCode() {
            int hash = name.hashCode() * 43 + mods.hashCode();
            if (compiledVersion != null)
                hash = hash * 43 + compiledVersion.hashCode();
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

    public final static class Exports {

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

    public final static class Opens {

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

    public final static class Provides {

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

    private final boolean open;

    private final boolean automatic;

    private final boolean synthetic;

    private final Set<Requires> requires;

    private final Set<Exports> exports;

    private final Set<Opens> opens;

    private final Set<String> uses;

    private final Set<Provides> provides;

    private final Set<String> packages;

    private final String mainClass;

    private final String osName;

    private final String osArch;

    private final String osVersion;

    private ModuleDescriptor(String name, Version version, boolean open, boolean automatic, boolean synthetic, Set<Requires> requires, Set<Exports> exports, Set<Opens> opens, Set<String> uses, Set<Provides> provides, Set<String> packages, String mainClass, String osName, String osArch, String osVersion) {
        this.name = name;
        this.version = version;
        this.open = open;
        this.automatic = automatic;
        this.synthetic = synthetic;
        assert (requires.stream().map(Requires::name).distinct().count() == requires.size());
        this.requires = emptyOrUnmodifiableSet(requires);
        this.exports = emptyOrUnmodifiableSet(exports);
        this.opens = emptyOrUnmodifiableSet(opens);
        this.uses = emptyOrUnmodifiableSet(uses);
        this.provides = emptyOrUnmodifiableSet(provides);
        this.packages = emptyOrUnmodifiableSet(packages);
        this.mainClass = mainClass;
        this.osName = osName;
        this.osArch = osArch;
        this.osVersion = osVersion;
    }

    ModuleDescriptor(ModuleDescriptor md, Set<String> pkgs) {
        this.name = md.name;
        this.version = md.version;
        this.open = md.open;
        this.automatic = md.automatic;
        this.synthetic = md.synthetic;
        this.requires = md.requires;
        this.exports = md.exports;
        this.opens = md.opens;
        this.uses = md.uses;
        this.provides = md.provides;
        Set<String> packages = new HashSet<>(md.packages);
        packages.addAll(pkgs);
        this.packages = emptyOrUnmodifiableSet(packages);
        this.mainClass = md.mainClass;
        this.osName = md.osName;
        this.osArch = md.osArch;
        this.osVersion = md.osVersion;
    }

    ModuleDescriptor(String name, Version version, boolean open, boolean automatic, boolean synthetic, Set<Requires> requires, Set<Exports> exports, Set<Opens> opens, Set<String> uses, Set<Provides> provides, Set<String> packages, String mainClass, String osName, String osArch, String osVersion, int hashCode, boolean unused) {
        this.name = name;
        this.version = version;
        this.open = open;
        this.automatic = automatic;
        this.synthetic = synthetic;
        this.requires = requires;
        this.exports = exports;
        this.opens = opens;
        this.uses = uses;
        this.provides = provides;
        this.packages = packages;
        this.mainClass = mainClass;
        this.osName = osName;
        this.osArch = osArch;
        this.osVersion = osVersion;
        this.hash = hashCode;
    }

    public String name() {
        return name;
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isAutomatic() {
        return automatic;
    }

    public boolean isSynthetic() {
        return synthetic;
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

    public Optional<String> osName() {
        return Optional.ofNullable(osName);
    }

    public Optional<String> osArch() {
        return Optional.ofNullable(osArch);
    }

    public Optional<String> osVersion() {
        return Optional.ofNullable(osVersion);
    }

    public Set<String> packages() {
        return packages;
    }

    public static final class Builder {

        final String name;

        final boolean strict;

        final boolean open;

        final boolean synthetic;

        boolean automatic;

        final Map<String, Requires> requires = new HashMap<>();

        final Map<String, Exports> exports = new HashMap<>();

        final Map<String, Opens> opens = new HashMap<>();

        final Set<String> concealedPackages = new HashSet<>();

        final Set<String> uses = new HashSet<>();

        final Map<String, Provides> provides = new HashMap<>();

        Version version;

        String osName;

        String osArch;

        String osVersion;

        String mainClass;

        Builder(String name, boolean strict, boolean open, boolean synthetic) {
            this.name = (strict) ? requireModuleName(name) : name;
            this.strict = strict;
            this.open = open;
            this.synthetic = synthetic;
        }

        Builder automatic(boolean automatic) {
            this.automatic = automatic;
            return this;
        }

        Set<String> exportedPackages() {
            return exports.keySet();
        }

        Set<String> openPackages() {
            return opens.keySet();
        }

        public Builder requires(Requires req) {
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
            return requires(new Requires(ms, mn, compiledVersion));
        }

        public Builder requires(Set<Requires.Modifier> ms, String mn) {
            if (strict)
                mn = requireModuleName(mn);
            return requires(new Requires(ms, mn, null));
        }

        public Builder requires(String mn) {
            return requires(EnumSet.noneOf(Requires.Modifier.class), mn);
        }

        public Builder exports(Exports e) {
            String source = e.source();
            if (concealedPackages.contains(source)) {
                throw new IllegalStateException("Package " + source + " already declared");
            }
            if (exports.containsKey(source)) {
                throw new IllegalStateException("Exported package " + source + " already declared");
            }
            exports.put(source, e);
            return this;
        }

        public Builder exports(Set<Exports.Modifier> ms, String pn, Set<String> targets) {
            Exports e = new Exports(ms, requirePackageName(pn), targets);
            targets = e.targets();
            if (targets.isEmpty())
                throw new IllegalArgumentException("Empty target set");
            if (strict)
                targets.stream().forEach(Checks::requireModuleName);
            return exports(e);
        }

        public Builder exports(Set<Exports.Modifier> ms, String pn) {
            Exports e = new Exports(ms, requirePackageName(pn), Collections.emptySet());
            return exports(e);
        }

        public Builder exports(String pn, Set<String> targets) {
            return exports(Collections.emptySet(), pn, targets);
        }

        public Builder exports(String pn) {
            return exports(Collections.emptySet(), pn);
        }

        public Builder opens(Opens obj) {
            if (open) {
                throw new IllegalStateException("open modules cannot declare" + " open packages");
            }
            String source = obj.source();
            if (concealedPackages.contains(source)) {
                throw new IllegalStateException("Package " + source + " already declared");
            }
            if (opens.containsKey(source)) {
                throw new IllegalStateException("Open package " + source + " already declared");
            }
            opens.put(source, obj);
            return this;
        }

        public Builder opens(Set<Opens.Modifier> ms, String pn, Set<String> targets) {
            Opens e = new Opens(ms, requirePackageName(pn), targets);
            targets = e.targets();
            if (targets.isEmpty())
                throw new IllegalArgumentException("Empty target set");
            if (strict)
                targets.stream().forEach(Checks::requireModuleName);
            return opens(e);
        }

        public Builder opens(Set<Opens.Modifier> ms, String pn) {
            Opens e = new Opens(ms, requirePackageName(pn), Collections.emptySet());
            return opens(e);
        }

        public Builder opens(String pn, Set<String> targets) {
            return opens(Collections.emptySet(), pn, targets);
        }

        public Builder opens(String pn) {
            return opens(Collections.emptySet(), pn);
        }

        public Builder uses(String service) {
            if (uses.contains(requireServiceTypeName(service)))
                throw new IllegalStateException("Dependence upon service " + service + " already declared");
            uses.add(service);
            return this;
        }

        public Builder provides(Provides p) {
            String st = p.service();
            if (provides.containsKey(st))
                throw new IllegalStateException("Providers of service " + st + " already declared");
            provides.put(st, p);
            return this;
        }

        public Builder provides(String service, List<String> providers) {
            if (provides.containsKey(service))
                throw new IllegalStateException("Providers of service " + service + " already declared by " + name);
            Provides p = new Provides(requireServiceTypeName(service), providers);
            List<String> providerNames = p.providers();
            if (providerNames.isEmpty())
                throw new IllegalArgumentException("Empty providers set");
            providerNames.forEach(Checks::requireServiceProviderName);
            provides.put(service, p);
            return this;
        }

        public Builder provides(String service, String provider) {
            if (provider == null)
                throw new IllegalArgumentException("'provider' is null");
            return provides(service, List.of(provider));
        }

        public Builder contains(Set<String> pns) {
            pns.forEach(this::contains);
            return this;
        }

        public Builder contains(String pn) {
            Checks.requirePackageName(pn);
            if (concealedPackages.contains(pn)) {
                throw new IllegalStateException("Package " + pn + " already declared");
            }
            if (exports.containsKey(pn)) {
                throw new IllegalStateException("Exported package " + pn + " already declared");
            }
            if (opens.containsKey(pn)) {
                throw new IllegalStateException("Open package " + pn + " already declared");
            }
            concealedPackages.add(pn);
            return this;
        }

        public Builder version(Version v) {
            version = requireNonNull(v);
            return this;
        }

        public Builder version(String v) {
            return version(Version.parse(v));
        }

        public Builder mainClass(String mc) {
            mainClass = requireBinaryName("main class name", mc);
            return this;
        }

        public Builder osName(String name) {
            if (name == null || name.isEmpty())
                throw new IllegalArgumentException("OS name is null or empty");
            osName = name;
            return this;
        }

        public Builder osArch(String arch) {
            if (arch == null || arch.isEmpty())
                throw new IllegalArgumentException("OS arch is null or empty");
            osArch = arch;
            return this;
        }

        public Builder osVersion(String version) {
            if (version == null || version.isEmpty())
                throw new IllegalArgumentException("OS version is null or empty");
            osVersion = version;
            return this;
        }

        public ModuleDescriptor build() {
            Set<Requires> requires = new HashSet<>(this.requires.values());
            Set<String> packages = new HashSet<>();
            packages.addAll(exports.keySet());
            packages.addAll(opens.keySet());
            packages.addAll(concealedPackages);
            Set<Exports> exports = new HashSet<>(this.exports.values());
            Set<Opens> opens = new HashSet<>(this.opens.values());
            Set<Provides> provides = new HashSet<>(this.provides.values());
            return new ModuleDescriptor(name, version, open, automatic, synthetic, requires, exports, opens, uses, provides, packages, mainClass, osName, osArch, osVersion);
        }
    }

    @Override
    public int compareTo(ModuleDescriptor that) {
        int c = this.name().compareTo(that.name());
        if (c != 0)
            return c;
        if (version == null) {
            if (that.version == null)
                return 0;
            return -1;
        }
        if (that.version == null)
            return +1;
        return version.compareTo(that.version);
    }

    @Override
    public boolean equals(Object ob) {
        if (ob == this)
            return true;
        if (!(ob instanceof ModuleDescriptor))
            return false;
        ModuleDescriptor that = (ModuleDescriptor) ob;
        return (name.equals(that.name) && open == that.open && automatic == that.automatic && synthetic == that.synthetic && requires.equals(that.requires) && exports.equals(that.exports) && opens.equals(that.opens) && uses.equals(that.uses) && provides.equals(that.provides) && Objects.equals(version, that.version) && Objects.equals(mainClass, that.mainClass) && Objects.equals(osName, that.osName) && Objects.equals(osArch, that.osArch) && Objects.equals(osVersion, that.osVersion) && Objects.equals(packages, that.packages));
    }

    private transient int hash;

    @Override
    public int hashCode() {
        int hc = hash;
        if (hc == 0) {
            hc = name.hashCode();
            hc = hc * 43 + Boolean.hashCode(open);
            hc = hc * 43 + Boolean.hashCode(automatic);
            hc = hc * 43 + Boolean.hashCode(synthetic);
            hc = hc * 43 + requires.hashCode();
            hc = hc * 43 + exports.hashCode();
            hc = hc * 43 + opens.hashCode();
            hc = hc * 43 + uses.hashCode();
            hc = hc * 43 + provides.hashCode();
            hc = hc * 43 + Objects.hashCode(version);
            hc = hc * 43 + Objects.hashCode(mainClass);
            hc = hc * 43 + Objects.hashCode(osName);
            hc = hc * 43 + Objects.hashCode(osArch);
            hc = hc * 43 + Objects.hashCode(osVersion);
            hc = hc * 43 + Objects.hashCode(packages);
            if (hc == 0)
                hc = -1;
            hash = hc;
        }
        return hc;
    }

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

    public static Builder module(String name) {
        return new Builder(name, true, false, false);
    }

    public static Builder openModule(String name) {
        return new Builder(name, true, true, false);
    }

    public static Builder automaticModule(String name) {
        return new Builder(name, true, false, false).automatic(true);
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

    private static <M> String toString(Set<M> mods, String what) {
        return (Stream.concat(mods.stream().map(e -> e.toString().toLowerCase()), Stream.of(what))).collect(Collectors.joining(" "));
    }

    static {
        jdk.internal.misc.SharedSecrets.setJavaLangModuleAccess(new jdk.internal.misc.JavaLangModuleAccess() {

            @Override
            public Builder newModuleBuilder(String mn, boolean strict, boolean open, boolean synthetic) {
                return new Builder(mn, strict, open, synthetic);
            }

            @Override
            public Set<String> exportedPackages(ModuleDescriptor.Builder builder) {
                return builder.exportedPackages();
            }

            @Override
            public Set<String> openPackages(ModuleDescriptor.Builder builder) {
                return builder.openPackages();
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
            public Version newVersion(String v) {
                return new Version(v);
            }

            @Override
            public ModuleDescriptor newModuleDescriptor(ModuleDescriptor md, Set<String> pkgs) {
                return new ModuleDescriptor(md, pkgs);
            }

            @Override
            public ModuleDescriptor newModuleDescriptor(String name, Version version, boolean open, boolean automatic, boolean synthetic, Set<Requires> requires, Set<Exports> exports, Set<Opens> opens, Set<String> uses, Set<Provides> provides, Set<String> packages, String mainClass, String osName, String osArch, String osVersion, int hashCode) {
                return new ModuleDescriptor(name, version, open, automatic, synthetic, requires, exports, opens, uses, provides, packages, mainClass, osName, osArch, osVersion, hashCode, false);
            }

            @Override
            public Configuration resolveRequiresAndUses(ModuleFinder finder, Collection<String> roots, boolean check, PrintStream traceOutput) {
                return Configuration.resolveRequiresAndUses(finder, roots, check, traceOutput);
            }
        });
    }
}
