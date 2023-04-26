package org.springframework.boot.loader.tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.zeroturnaround.zip.ZipUtil;
import org.springframework.boot.loader.tools.sample.ClassWithMainMethod;
import org.springframework.boot.loader.tools.sample.ClassWithoutMainMethod;
import org.springframework.util.FileCopyUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class RepackagerTests {

    private static final Libraries NO_LIBRARIES = (callback) -> {
    };

    private static final long JAN_1_1980;

    private static final long JAN_1_1985;

    static {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1980, 0, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        JAN_1_1980 = calendar.getTime().getTime();
        calendar.set(Calendar.YEAR, 1985);
        JAN_1_1985 = calendar.getTime().getTime();
    }

    @TempDir
    File tempDir;

    private TestJarFile testJarFile;

    @BeforeEach
    void setup() throws IOException {
        this.testJarFile = new TestJarFile(this.tempDir);
    }

    @Test
    void nullSource() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Repackager(null));
    }

    @Test
    void missingSource() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Repackager(new File("missing")));
    }

    @Test
    void directorySource() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Repackager(this.tempDir));
    }

    @Test
    void specificMainClass() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithoutMainMethod.class);
        File file = this.testJarFile.getFile();
        Repackager repackager = new Repackager(file);
        repackager.setMainClass("a.b.C");
        repackager.repackage(NO_LIBRARIES);
        Manifest actualManifest = getManifest(file);
        assertThat(actualManifest.getMainAttributes().getValue("Main-Class")).isEqualTo("org.springframework.boot.loader.JarLauncher");
        assertThat(actualManifest.getMainAttributes().getValue("Start-Class")).isEqualTo("a.b.C");
        assertThat(hasLauncherClasses(file)).isTrue();
    }

    @Test
    void mainClassFromManifest() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithoutMainMethod.class);
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        manifest.getMainAttributes().putValue("Main-Class", "a.b.C");
        this.testJarFile.addManifest(manifest);
        File file = this.testJarFile.getFile();
        Repackager repackager = new Repackager(file);
        repackager.repackage(NO_LIBRARIES);
        Manifest actualManifest = getManifest(file);
        assertThat(actualManifest.getMainAttributes().getValue("Main-Class")).isEqualTo("org.springframework.boot.loader.JarLauncher");
        assertThat(actualManifest.getMainAttributes().getValue("Start-Class")).isEqualTo("a.b.C");
        assertThat(hasLauncherClasses(file)).isTrue();
    }

    @Test
    void mainClassFound() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        File file = this.testJarFile.getFile();
        Repackager repackager = new Repackager(file);
        repackager.repackage(NO_LIBRARIES);
        Manifest actualManifest = getManifest(file);
        assertThat(actualManifest.getMainAttributes().getValue("Main-Class")).isEqualTo("org.springframework.boot.loader.JarLauncher");
        assertThat(actualManifest.getMainAttributes().getValue("Start-Class")).isEqualTo("a.b.C");
        assertThat(hasLauncherClasses(file)).isTrue();
    }

    @Test
    void jarIsOnlyRepackagedOnce() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        File file = this.testJarFile.getFile();
        Repackager repackager = new Repackager(file);
        repackager.repackage(NO_LIBRARIES);
        repackager.repackage(NO_LIBRARIES);
        Manifest actualManifest = getManifest(file);
        assertThat(actualManifest.getMainAttributes().getValue("Main-Class")).isEqualTo("org.springframework.boot.loader.JarLauncher");
        assertThat(actualManifest.getMainAttributes().getValue("Start-Class")).isEqualTo("a.b.C");
        assertThat(hasLauncherClasses(file)).isTrue();
    }

    @Test
    void multipleMainClassFound() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        this.testJarFile.addClass("a/b/D.class", ClassWithMainMethod.class);
        File file = this.testJarFile.getFile();
        Repackager repackager = new Repackager(file);
        assertThatIllegalStateException().isThrownBy(() -> repackager.repackage(NO_LIBRARIES)).withMessageContaining("Unable to find a single main class from the following candidates [a.b.C, a.b.D]");
    }

    @Test
    void noMainClass() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithoutMainMethod.class);
        assertThatIllegalStateException().isThrownBy(() -> new Repackager(this.testJarFile.getFile()).repackage(NO_LIBRARIES)).withMessageContaining("Unable to find main class");
    }

    @Test
    void noMainClassAndLayoutIsNone() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        File file = this.testJarFile.getFile();
        Repackager repackager = new Repackager(file);
        repackager.setLayout(new Layouts.None());
        repackager.repackage(file, NO_LIBRARIES);
        Manifest actualManifest = getManifest(file);
        assertThat(actualManifest.getMainAttributes().getValue("Main-Class")).isEqualTo("a.b.C");
        assertThat(hasLauncherClasses(file)).isFalse();
    }

    @Test
    void noMainClassAndLayoutIsNoneWithNoMain() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithoutMainMethod.class);
        File file = this.testJarFile.getFile();
        Repackager repackager = new Repackager(file);
        repackager.setLayout(new Layouts.None());
        repackager.repackage(file, NO_LIBRARIES);
        Manifest actualManifest = getManifest(file);
        assertThat(actualManifest.getMainAttributes().getValue("Main-Class")).isNull();
        assertThat(hasLauncherClasses(file)).isFalse();
    }

    @Test
    void sameSourceAndDestinationWithBackup() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        File file = this.testJarFile.getFile();
        Repackager repackager = new Repackager(file);
        repackager.repackage(NO_LIBRARIES);
        assertThat(new File(file.getParent(), file.getName() + ".original")).exists();
        assertThat(hasLauncherClasses(file)).isTrue();
    }

    @Test
    void sameSourceAndDestinationWithoutBackup() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        File file = this.testJarFile.getFile();
        Repackager repackager = new Repackager(file);
        repackager.setBackupSource(false);
        repackager.repackage(NO_LIBRARIES);
        assertThat(new File(file.getParent(), file.getName() + ".original")).doesNotExist();
        assertThat(hasLauncherClasses(file)).isTrue();
    }

    @Test
    void differentDestination() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        File source = this.testJarFile.getFile();
        File dest = new File(this.tempDir, "different.jar");
        Repackager repackager = new Repackager(source);
        repackager.repackage(dest, NO_LIBRARIES);
        assertThat(new File(source.getParent(), source.getName() + ".original")).doesNotExist();
        assertThat(hasLauncherClasses(source)).isFalse();
        assertThat(hasLauncherClasses(dest)).isTrue();
    }

    @Test
    void nullDestination() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        Repackager repackager = new Repackager(this.testJarFile.getFile());
        assertThatIllegalArgumentException().isThrownBy(() -> repackager.repackage(null, NO_LIBRARIES)).withMessageContaining("Invalid destination");
    }

    @Test
    void destinationIsDirectory() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        Repackager repackager = new Repackager(this.testJarFile.getFile());
        assertThatIllegalArgumentException().isThrownBy(() -> repackager.repackage(this.tempDir, NO_LIBRARIES)).withMessageContaining("Invalid destination");
    }

    @Test
    void overwriteDestination() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        Repackager repackager = new Repackager(this.testJarFile.getFile());
        File dest = new File(this.tempDir, "dest.jar");
        dest.createNewFile();
        repackager.repackage(dest, NO_LIBRARIES);
        assertThat(hasLauncherClasses(dest)).isTrue();
    }

    @Test
    void nullLibraries() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        File file = this.testJarFile.getFile();
        Repackager repackager = new Repackager(file);
        assertThatIllegalArgumentException().isThrownBy(() -> repackager.repackage(file, null)).withMessageContaining("Libraries must not be null");
    }

    @Test
    void libraries() throws Exception {
        TestJarFile libJar = new TestJarFile(this.tempDir);
        libJar.addClass("a/b/C.class", ClassWithoutMainMethod.class, JAN_1_1985);
        File libJarFile = libJar.getFile();
        File libJarFileToUnpack = libJar.getFile();
        File libNonJarFile = new File(this.tempDir, "non-lib.jar");
        FileCopyUtils.copy(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 }, libNonJarFile);
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        this.testJarFile.addFile("BOOT-INF/lib/" + libJarFileToUnpack.getName(), libJarFileToUnpack);
        File file = this.testJarFile.getFile();
        libJarFile.setLastModified(JAN_1_1980);
        Repackager repackager = new Repackager(file);
        repackager.repackage((callback) -> {
            callback.library(new Library(libJarFile, LibraryScope.COMPILE));
            callback.library(new Library(libJarFileToUnpack, LibraryScope.COMPILE, true));
            callback.library(new Library(libNonJarFile, LibraryScope.COMPILE));
        });
        assertThat(hasEntry(file, "BOOT-INF/lib/" + libJarFile.getName())).isTrue();
        assertThat(hasEntry(file, "BOOT-INF/lib/" + libJarFileToUnpack.getName())).isTrue();
        assertThat(hasEntry(file, "BOOT-INF/lib/" + libNonJarFile.getName())).isFalse();
        JarEntry entry = getEntry(file, "BOOT-INF/lib/" + libJarFile.getName());
        assertThat(entry.getTime()).isEqualTo(JAN_1_1985);
        entry = getEntry(file, "BOOT-INF/lib/" + libJarFileToUnpack.getName());
        assertThat(entry.getComment()).startsWith("UNPACK:");
        assertThat(entry.getComment()).hasSize(47);
    }

    @Test
    void duplicateLibraries() throws Exception {
        TestJarFile libJar = new TestJarFile(this.tempDir);
        libJar.addClass("a/b/C.class", ClassWithoutMainMethod.class);
        File libJarFile = libJar.getFile();
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        File file = this.testJarFile.getFile();
        Repackager repackager = new Repackager(file);
        assertThatIllegalStateException().isThrownBy(() -> repackager.repackage((callback) -> {
            callback.library(new Library(libJarFile, LibraryScope.COMPILE, false));
            callback.library(new Library(libJarFile, LibraryScope.COMPILE, false));
        })).withMessageContaining("Duplicate library");
    }

    @Test
    void customLayout() throws Exception {
        TestJarFile libJar = new TestJarFile(this.tempDir);
        libJar.addClass("a/b/C.class", ClassWithoutMainMethod.class);
        File libJarFile = libJar.getFile();
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        File file = this.testJarFile.getFile();
        Repackager repackager = new Repackager(file);
        Layout layout = mock(Layout.class);
        LibraryScope scope = mock(LibraryScope.class);
        given(layout.getLauncherClassName()).willReturn("testLauncher");
        given(layout.getLibraryDestination(anyString(), eq(scope))).willReturn("test/");
        given(layout.getLibraryDestination(anyString(), eq(LibraryScope.COMPILE))).willReturn("test-lib/");
        repackager.setLayout(layout);
        repackager.repackage((callback) -> callback.library(new Library(libJarFile, scope)));
        assertThat(hasEntry(file, "test/" + libJarFile.getName())).isTrue();
        assertThat(getManifest(file).getMainAttributes().getValue("Spring-Boot-Lib")).isEqualTo("test-lib/");
        assertThat(getManifest(file).getMainAttributes().getValue("Main-Class")).isEqualTo("testLauncher");
    }

    @Test
    void customLayoutNoBootLib() throws Exception {
        TestJarFile libJar = new TestJarFile(this.tempDir);
        libJar.addClass("a/b/C.class", ClassWithoutMainMethod.class);
        File libJarFile = libJar.getFile();
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        File file = this.testJarFile.getFile();
        Repackager repackager = new Repackager(file);
        Layout layout = mock(Layout.class);
        LibraryScope scope = mock(LibraryScope.class);
        given(layout.getLauncherClassName()).willReturn("testLauncher");
        repackager.setLayout(layout);
        repackager.repackage((callback) -> callback.library(new Library(libJarFile, scope)));
        assertThat(getManifest(file).getMainAttributes().getValue("Spring-Boot-Lib")).isNull();
        assertThat(getManifest(file).getMainAttributes().getValue("Main-Class")).isEqualTo("testLauncher");
    }

    @Test
    void springBootVersion() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        File file = this.testJarFile.getFile();
        Repackager repackager = new Repackager(file);
        repackager.repackage(NO_LIBRARIES);
        Manifest actualManifest = getManifest(file);
        assertThat(actualManifest.getMainAttributes()).containsKey(new Attributes.Name("Spring-Boot-Version"));
    }

    @Test
    void executableJarLayoutAttributes() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        File file = this.testJarFile.getFile();
        Repackager repackager = new Repackager(file);
        repackager.repackage(NO_LIBRARIES);
        Manifest actualManifest = getManifest(file);
        assertThat(actualManifest.getMainAttributes()).containsEntry(new Attributes.Name("Spring-Boot-Lib"), "BOOT-INF/lib/");
        assertThat(actualManifest.getMainAttributes()).containsEntry(new Attributes.Name("Spring-Boot-Classes"), "BOOT-INF/classes/");
    }

    @Test
    void executableWarLayoutAttributes() throws Exception {
        this.testJarFile.addClass("WEB-INF/classes/a/b/C.class", ClassWithMainMethod.class);
        File file = this.testJarFile.getFile("war");
        Repackager repackager = new Repackager(file);
        repackager.repackage(NO_LIBRARIES);
        Manifest actualManifest = getManifest(file);
        assertThat(actualManifest.getMainAttributes()).containsEntry(new Attributes.Name("Spring-Boot-Lib"), "WEB-INF/lib/");
        assertThat(actualManifest.getMainAttributes()).containsEntry(new Attributes.Name("Spring-Boot-Classes"), "WEB-INF/classes/");
    }

    @Test
    void nullCustomLayout() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithoutMainMethod.class);
        Repackager repackager = new Repackager(this.testJarFile.getFile());
        assertThatIllegalArgumentException().isThrownBy(() -> repackager.setLayout(null)).withMessageContaining("Layout must not be null");
    }

    @Test
    void dontRecompressZips() throws Exception {
        TestJarFile nested = new TestJarFile(this.tempDir);
        nested.addClass("a/b/C.class", ClassWithoutMainMethod.class);
        File nestedFile = nested.getFile();
        this.testJarFile.addFile("test/nested.jar", nestedFile);
        this.testJarFile.addClass("A.class", ClassWithMainMethod.class);
        File file = this.testJarFile.getFile();
        Repackager repackager = new Repackager(file);
        repackager.repackage((callback) -> callback.library(new Library(nestedFile, LibraryScope.COMPILE)));
        try (JarFile jarFile = new JarFile(file)) {
            assertThat(jarFile.getEntry("BOOT-INF/lib/" + nestedFile.getName()).getMethod()).isEqualTo(ZipEntry.STORED);
            assertThat(jarFile.getEntry("BOOT-INF/classes/test/nested.jar").getMethod()).isEqualTo(ZipEntry.STORED);
        }
    }

    @Test
    void addLauncherScript() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        File source = this.testJarFile.getFile();
        File dest = new File(this.tempDir, "dest.jar");
        Repackager repackager = new Repackager(source);
        LaunchScript script = new MockLauncherScript("ABC");
        repackager.repackage(dest, NO_LIBRARIES, script);
        byte[] bytes = FileCopyUtils.copyToByteArray(dest);
        assertThat(new String(bytes)).startsWith("ABC");
        assertThat(hasLauncherClasses(source)).isFalse();
        assertThat(hasLauncherClasses(dest)).isTrue();
        try {
            assertThat(Files.getPosixFilePermissions(dest.toPath())).contains(PosixFilePermission.OWNER_EXECUTE);
        } catch (UnsupportedOperationException ex) {
        }
    }

    @Test
    void unpackLibrariesTakePrecedenceOverExistingSourceEntries() throws Exception {
        TestJarFile nested = new TestJarFile(this.tempDir);
        nested.addClass("a/b/C.class", ClassWithoutMainMethod.class);
        File nestedFile = nested.getFile();
        String name = "BOOT-INF/lib/" + nestedFile.getName();
        this.testJarFile.addFile(name, nested.getFile());
        this.testJarFile.addClass("A.class", ClassWithMainMethod.class);
        File file = this.testJarFile.getFile();
        Repackager repackager = new Repackager(file);
        repackager.repackage((callback) -> callback.library(new Library(nestedFile, LibraryScope.COMPILE, true)));
        try (JarFile jarFile = new JarFile(file)) {
            assertThat(jarFile.getEntry(name).getComment()).startsWith("UNPACK:");
        }
    }

    @Test
    void existingSourceEntriesTakePrecedenceOverStandardLibraries() throws Exception {
        TestJarFile nested = new TestJarFile(this.tempDir);
        nested.addClass("a/b/C.class", ClassWithoutMainMethod.class);
        File nestedFile = nested.getFile();
        this.testJarFile.addFile("BOOT-INF/lib/" + nestedFile.getName(), nested.getFile());
        this.testJarFile.addClass("A.class", ClassWithMainMethod.class);
        File file = this.testJarFile.getFile();
        Repackager repackager = new Repackager(file);
        long sourceLength = nestedFile.length();
        repackager.repackage((callback) -> {
            nestedFile.delete();
            File toZip = new File(this.tempDir, "to-zip");
            toZip.createNewFile();
            ZipUtil.packEntry(toZip, nestedFile);
            callback.library(new Library(nestedFile, LibraryScope.COMPILE));
        });
        try (JarFile jarFile = new JarFile(file)) {
            assertThat(jarFile.getEntry("BOOT-INF/lib/" + nestedFile.getName()).getSize()).isEqualTo(sourceLength);
        }
    }

    @Test
    void metaInfIndexListIsRemovedFromRepackagedJar() throws Exception {
        this.testJarFile.addClass("A.class", ClassWithMainMethod.class);
        File indexList = new File(this.tempDir, "INDEX.LIST");
        indexList.createNewFile();
        this.testJarFile.addFile("META-INF/INDEX.LIST", indexList);
        File source = this.testJarFile.getFile();
        File dest = new File(this.tempDir, "dest.jar");
        Repackager repackager = new Repackager(source);
        repackager.repackage(dest, NO_LIBRARIES);
        try (JarFile jarFile = new JarFile(dest)) {
            assertThat(jarFile.getEntry("META-INF/INDEX.LIST")).isNull();
        }
    }

    @Test
    void customLayoutFactoryWithoutLayout() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        File source = this.testJarFile.getFile();
        Repackager repackager = new Repackager(source, new TestLayoutFactory());
        repackager.repackage(NO_LIBRARIES);
        JarFile jarFile = new JarFile(source);
        assertThat(jarFile.getEntry("test")).isNotNull();
        jarFile.close();
    }

    @Test
    void customLayoutFactoryWithLayout() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        File source = this.testJarFile.getFile();
        Repackager repackager = new Repackager(source, new TestLayoutFactory());
        repackager.setLayout(new Layouts.Jar());
        repackager.repackage(NO_LIBRARIES);
        JarFile jarFile = new JarFile(source);
        assertThat(jarFile.getEntry("test")).isNull();
        jarFile.close();
    }

    @Test
    void metaInfAopXmlIsMovedBeneathBootInfClassesWhenRepackaged() throws Exception {
        this.testJarFile.addClass("A.class", ClassWithMainMethod.class);
        File aopXml = new File(this.tempDir, "aop.xml");
        aopXml.createNewFile();
        this.testJarFile.addFile("META-INF/aop.xml", aopXml);
        File source = this.testJarFile.getFile();
        File dest = new File(this.tempDir, "dest.jar");
        Repackager repackager = new Repackager(source);
        repackager.repackage(dest, NO_LIBRARIES);
        try (JarFile jarFile = new JarFile(dest)) {
            assertThat(jarFile.getEntry("META-INF/aop.xml")).isNull();
            assertThat(jarFile.getEntry("BOOT-INF/classes/META-INF/aop.xml")).isNotNull();
        }
    }

    @Test
    void allEntriesUseUnixPlatformAndUtf8NameEncoding() throws IOException {
        this.testJarFile.addClass("A.class", ClassWithMainMethod.class);
        File source = this.testJarFile.getFile();
        File dest = new File(this.tempDir, "dest.jar");
        Repackager repackager = new Repackager(source);
        repackager.repackage(dest, NO_LIBRARIES);
        try (ZipFile zip = new ZipFile(dest)) {
            Enumeration<ZipArchiveEntry> entries = zip.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                assertThat(entry.getPlatform()).isEqualTo(ZipArchiveEntry.PLATFORM_UNIX);
                assertThat(entry.getGeneralPurposeBit().usesUTF8ForNames()).isTrue();
            }
        }
    }

    @Test
    void loaderIsWrittenFirstThenApplicationClassesThenLibraries() throws IOException {
        this.testJarFile.addClass("com/example/Application.class", ClassWithMainMethod.class);
        File source = this.testJarFile.getFile();
        File dest = new File(this.tempDir, "dest.jar");
        File libraryOne = createLibrary();
        File libraryTwo = createLibrary();
        File libraryThree = createLibrary();
        Repackager repackager = new Repackager(source);
        repackager.repackage(dest, (callback) -> {
            callback.library(new Library(libraryOne, LibraryScope.COMPILE, false));
            callback.library(new Library(libraryTwo, LibraryScope.COMPILE, true));
            callback.library(new Library(libraryThree, LibraryScope.COMPILE, false));
        });
        assertThat(getEntryNames(dest)).containsSubsequence("org/springframework/boot/loader/", "BOOT-INF/classes/com/example/Application.class", "BOOT-INF/lib/" + libraryOne.getName(), "BOOT-INF/lib/" + libraryTwo.getName(), "BOOT-INF/lib/" + libraryThree.getName());
    }

    @Test
    void existingEntryThatMatchesUnpackLibraryIsMarkedForUnpack() throws IOException {
        File library = createLibrary();
        this.testJarFile.addClass("WEB-INF/classes/com/example/Application.class", ClassWithMainMethod.class);
        this.testJarFile.addFile("WEB-INF/lib/" + library.getName(), library);
        File source = this.testJarFile.getFile("war");
        File dest = new File(this.tempDir, "dest.war");
        Repackager repackager = new Repackager(source);
        repackager.setLayout(new Layouts.War());
        repackager.repackage(dest, (callback) -> callback.library(new Library(library, LibraryScope.COMPILE, true)));
        assertThat(getEntryNames(dest)).containsSubsequence("org/springframework/boot/loader/", "WEB-INF/classes/com/example/Application.class", "WEB-INF/lib/" + library.getName());
        JarEntry unpackLibrary = getEntry(dest, "WEB-INF/lib/" + library.getName());
        assertThat(unpackLibrary.getComment()).startsWith("UNPACK:");
    }

    @Test
    void layoutCanOmitLibraries() throws IOException {
        TestJarFile libJar = new TestJarFile(this.tempDir);
        libJar.addClass("a/b/C.class", ClassWithoutMainMethod.class);
        File libJarFile = libJar.getFile();
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        File file = this.testJarFile.getFile();
        Repackager repackager = new Repackager(file);
        Layout layout = mock(Layout.class);
        LibraryScope scope = mock(LibraryScope.class);
        repackager.setLayout(layout);
        repackager.repackage((callback) -> callback.library(new Library(libJarFile, scope)));
        assertThat(getEntryNames(file)).containsExactly("META-INF/", "META-INF/MANIFEST.MF", "a/", "a/b/", "a/b/C.class");
    }

    @Test
    void jarThatUsesCustomCompressionConfigurationCanBeRepackaged() throws IOException {
        File source = new File(this.tempDir, "source.jar");
        ZipOutputStream output = new ZipOutputStream(new FileOutputStream(source)) {

            {
                this.def = new Deflater(Deflater.NO_COMPRESSION, true);
            }
        };
        byte[] data = new byte[1024 * 1024];
        new Random().nextBytes(data);
        ZipEntry entry = new ZipEntry("entry.dat");
        output.putNextEntry(entry);
        output.write(data);
        output.closeEntry();
        output.close();
        File dest = new File(this.tempDir, "dest.jar");
        Repackager repackager = new Repackager(source);
        repackager.setMainClass("com.example.Main");
        repackager.repackage(dest, NO_LIBRARIES);
    }

    @Test
    void moduleInfoClassRemainsInRootOfJarWhenRepackaged() throws Exception {
        this.testJarFile.addClass("A.class", ClassWithMainMethod.class);
        this.testJarFile.addClass("module-info.class", ClassWithoutMainMethod.class);
        File source = this.testJarFile.getFile();
        File dest = new File(this.tempDir, "dest.jar");
        Repackager repackager = new Repackager(source);
        repackager.repackage(dest, NO_LIBRARIES);
        try (JarFile jarFile = new JarFile(dest)) {
            assertThat(jarFile.getEntry("module-info.class")).isNotNull();
            assertThat(jarFile.getEntry("BOOT-INF/classes/module-info.class")).isNull();
        }
    }

    @Test
    void kotlinModuleMetadataMovesBeneathBootInfClassesWhenRepackaged() throws Exception {
        this.testJarFile.addClass("A.class", ClassWithMainMethod.class);
        File kotlinModule = new File(this.tempDir, "test.kotlin_module");
        kotlinModule.createNewFile();
        this.testJarFile.addFile("META-INF/test.kotlin_module", kotlinModule);
        File source = this.testJarFile.getFile();
        File dest = new File(this.tempDir, "dest.jar");
        Repackager repackager = new Repackager(source);
        repackager.repackage(dest, NO_LIBRARIES);
        try (JarFile jarFile = new JarFile(dest)) {
            assertThat(jarFile.getEntry("META-INF/test.kotlin_module")).isNull();
            assertThat(jarFile.getEntry("BOOT-INF/classes/META-INF/test.kotlin_module")).isNotNull();
        }
    }

    private File createLibrary() throws IOException {
        TestJarFile library = new TestJarFile(this.tempDir);
        library.addClass("com/example/library/Library.class", ClassWithoutMainMethod.class);
        return library.getFile();
    }

    private boolean hasLauncherClasses(File file) throws IOException {
        return hasEntry(file, "org/springframework/boot/") && hasEntry(file, "org/springframework/boot/loader/JarLauncher.class");
    }

    private boolean hasEntry(File file, String name) throws IOException {
        return getEntry(file, name) != null;
    }

    private JarEntry getEntry(File file, String name) throws IOException {
        try (JarFile jarFile = new JarFile(file)) {
            return jarFile.getJarEntry(name);
        }
    }

    private Manifest getManifest(File file) throws IOException {
        try (JarFile jarFile = new JarFile(file)) {
            return jarFile.getManifest();
        }
    }

    private List<String> getEntryNames(File file) throws IOException {
        List<String> entryNames = new ArrayList<>();
        try (JarFile jarFile = new JarFile(file)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                entryNames.add(entries.nextElement().getName());
            }
        }
        return entryNames;
    }

    static class MockLauncherScript implements LaunchScript {

        private final byte[] bytes;

        MockLauncherScript(String script) {
            this.bytes = script.getBytes();
        }

        @Override
        public byte[] toByteArray() {
            return this.bytes;
        }
    }

    static class TestLayoutFactory implements LayoutFactory {

        @Override
        public Layout getLayout(File source) {
            return new TestLayout();
        }
    }

    static class TestLayout extends Layouts.Jar implements CustomLoaderLayout {

        @Override
        public void writeLoadedClasses(LoaderClassesWriter writer) throws IOException {
            writer.writeEntry("test", new ByteArrayInputStream("test".getBytes()));
        }
    }
}
