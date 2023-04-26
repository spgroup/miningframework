package org.springframework.boot.loader.tools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.junit.jupiter.api.Test;
import org.springframework.boot.loader.tools.sample.ClassWithMainMethod;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class RepackagerTests extends AbstractPackagerTests<Repackager> {

    private File destination;

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
    void jarIsOnlyRepackagedOnce() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        Repackager repackager = createRepackager(this.testJarFile.getFile(), false);
        repackager.repackage(NO_LIBRARIES);
        repackager.repackage(NO_LIBRARIES);
        Manifest actualManifest = getPackagedManifest();
        assertThat(actualManifest.getMainAttributes().getValue("Main-Class")).isEqualTo("org.springframework.boot.loader.JarLauncher");
        assertThat(actualManifest.getMainAttributes().getValue("Start-Class")).isEqualTo("a.b.C");
        assertThat(hasPackagedLauncherClasses()).isTrue();
    }

    @Test
    void sameSourceAndDestinationWithoutBackup() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        File file = this.testJarFile.getFile();
        Repackager repackager = createRepackager(file, false);
        repackager.setBackupSource(false);
        repackager.repackage(NO_LIBRARIES);
        assertThat(new File(file.getParent(), file.getName() + ".original")).doesNotExist();
        assertThat(hasPackagedLauncherClasses()).isTrue();
    }

    @Test
    void sameSourceAndDestinationWithBackup() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        File file = this.testJarFile.getFile();
        Repackager repackager = createRepackager(file, false);
        repackager.repackage(NO_LIBRARIES);
        assertThat(new File(file.getParent(), file.getName() + ".original")).exists();
        assertThat(hasPackagedLauncherClasses()).isTrue();
    }

    @Test
    void differentDestination() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        File source = this.testJarFile.getFile();
        Repackager repackager = createRepackager(source, true);
        execute(repackager, NO_LIBRARIES);
        assertThat(new File(source.getParent(), source.getName() + ".original")).doesNotExist();
        assertThat(hasLauncherClasses(source)).isFalse();
        assertThat(hasPackagedLauncherClasses()).isTrue();
    }

    @Test
    void nullDestination() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        Repackager repackager = createRepackager(this.testJarFile.getFile(), true);
        assertThatIllegalArgumentException().isThrownBy(() -> repackager.repackage(null, NO_LIBRARIES)).withMessageContaining("Invalid destination");
    }

    @Test
    void destinationIsDirectory() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        Repackager repackager = createRepackager(this.testJarFile.getFile(), true);
        assertThatIllegalArgumentException().isThrownBy(() -> repackager.repackage(this.tempDir, NO_LIBRARIES)).withMessageContaining("Invalid destination");
    }

    @Test
    void overwriteDestination() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        Repackager repackager = createRepackager(this.testJarFile.getFile(), true);
        this.destination.createNewFile();
        repackager.repackage(this.destination, NO_LIBRARIES);
        assertThat(hasLauncherClasses(this.destination)).isTrue();
    }

    @Test
    void addLauncherScript() throws Exception {
        this.testJarFile.addClass("a/b/C.class", ClassWithMainMethod.class);
        File source = this.testJarFile.getFile();
        Repackager repackager = createRepackager(source, true);
        LaunchScript script = new MockLauncherScript("ABC");
        repackager.repackage(this.destination, NO_LIBRARIES, script);
        byte[] bytes = FileCopyUtils.copyToByteArray(this.destination);
        assertThat(new String(bytes)).startsWith("ABC");
        assertThat(hasLauncherClasses(source)).isFalse();
        assertThat(hasLauncherClasses(this.destination)).isTrue();
        try {
            assertThat(Files.getPosixFilePermissions(this.destination.toPath())).contains(PosixFilePermission.OWNER_EXECUTE);
        } catch (UnsupportedOperationException ex) {
        }
    }

    @Test
    void allLoaderDirectoriesAndFilesUseSameTimestamp() throws IOException {
        this.testJarFile.addClass("A.class", ClassWithMainMethod.class);
        Repackager repackager = createRepackager(this.testJarFile.getFile(), true);
        Long timestamp = null;
        repackager.repackage(this.destination, NO_LIBRARIES);
        for (ZipArchiveEntry entry : getAllPackagedEntries()) {
            if (entry.getName().startsWith("org/springframework/boot/loader")) {
                if (timestamp == null) {
                    timestamp = entry.getTime();
                } else {
                    assertThat(entry.getTime()).withFailMessage("Expected time %d to be equal to %d for entry %s", entry.getTime(), timestamp, entry.getName()).isEqualTo(timestamp);
                }
            }
        }
    }

    @Test
    void allEntriesUseProvidedTimestamp() throws IOException {
        this.testJarFile.addClass("A.class", ClassWithMainMethod.class);
        Repackager repackager = createRepackager(this.testJarFile.getFile(), true);
        long timestamp = OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant().toEpochMilli();
        repackager.repackage(this.destination, NO_LIBRARIES, null, FileTime.fromMillis(timestamp));
        for (ZipArchiveEntry entry : getAllPackagedEntries()) {
            assertThat(entry.getTime()).isEqualTo(timestamp);
        }
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

    @Override
    protected Repackager createPackager(File source) {
        return createRepackager(source, true);
    }

    private Repackager createRepackager(File source, boolean differentDest) {
        String ext = StringUtils.getFilenameExtension(source.getName());
        this.destination = differentDest ? new File(this.tempDir, "dest." + ext) : source;
        return new Repackager(source);
    }

    @Override
    protected void execute(Repackager packager, Libraries libraries) throws IOException {
        packager.repackage(this.destination, libraries);
    }

    @Override
    protected Collection<ZipArchiveEntry> getAllPackagedEntries() throws IOException {
        List<ZipArchiveEntry> result = new ArrayList<>();
        try (ZipFile zip = new ZipFile(this.destination)) {
            Enumeration<ZipArchiveEntry> entries = zip.getEntries();
            while (entries.hasMoreElements()) {
                result.add(entries.nextElement());
            }
        }
        return result;
    }

    @Override
    protected Manifest getPackagedManifest() throws IOException {
        try (JarFile jarFile = new JarFile(this.destination)) {
            return jarFile.getManifest();
        }
    }

    @Override
    protected String getPackagedEntryContent(String name) throws IOException {
        try (ZipFile zip = new ZipFile(this.destination)) {
            ZipArchiveEntry entry = zip.getEntry(name);
            if (entry == null) {
                return null;
            }
            byte[] bytes = FileCopyUtils.copyToByteArray(zip.getInputStream(entry));
            return new String(bytes, StandardCharsets.UTF_8);
        }
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
}