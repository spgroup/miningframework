package org.springframework.boot.loader.jar;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import org.springframework.boot.loader.data.RandomAccessData;

class JarFileEntries implements CentralDirectoryVisitor, Iterable<JarEntry> {

    private static final Runnable NO_VALIDATION = () -> {
    };

    private static final String META_INF_PREFIX = "META-INF/";

    private static final Name MULTI_RELEASE = new Name("Multi-Release");

    private static final int BASE_VERSION = 8;

    private static final int RUNTIME_VERSION;

    static {
        int version;
        try {
            Object runtimeVersion = Runtime.class.getMethod("version").invoke(null);
            version = (int) runtimeVersion.getClass().getMethod("major").invoke(runtimeVersion);
        } catch (Throwable ex) {
            version = BASE_VERSION;
        }
        RUNTIME_VERSION = version;
    }

    private static final long LOCAL_FILE_HEADER_SIZE = 30;

    private static final char SLASH = '/';

    private static final char NO_SUFFIX = 0;

    protected static final int ENTRY_CACHE_SIZE = 25;

    private final JarFile jarFile;

    private final JarEntryFilter filter;

    private RandomAccessData centralDirectoryData;

    private int size;

    private int[] hashCodes;

    private int[] centralDirectoryOffsets;

    private int[] positions;

    private Boolean multiReleaseJar;

    private final Map<Integer, FileHeader> entriesCache = Collections.synchronizedMap(new LinkedHashMap<Integer, FileHeader>(16, 0.75f, true) {

        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, FileHeader> eldest) {
            if (JarFileEntries.this.jarFile.isSigned()) {
                return false;
            }
            return size() >= ENTRY_CACHE_SIZE;
        }
    });

    JarFileEntries(JarFile jarFile, JarEntryFilter filter) {
        this.jarFile = jarFile;
        this.filter = filter;
        if (RUNTIME_VERSION == BASE_VERSION) {
            this.multiReleaseJar = false;
        }
    }

    @Override
    public void visitStart(CentralDirectoryEndRecord endRecord, RandomAccessData centralDirectoryData) {
        int maxSize = endRecord.getNumberOfRecords();
        this.centralDirectoryData = centralDirectoryData;
        this.hashCodes = new int[maxSize];
        this.centralDirectoryOffsets = new int[maxSize];
        this.positions = new int[maxSize];
    }

    @Override
    public void visitFileHeader(CentralDirectoryFileHeader fileHeader, int dataOffset) {
        AsciiBytes name = applyFilter(fileHeader.getName());
        if (name != null) {
            add(name, dataOffset);
        }
    }

    private void add(AsciiBytes name, int dataOffset) {
        this.hashCodes[this.size] = name.hashCode();
        this.centralDirectoryOffsets[this.size] = dataOffset;
        this.positions[this.size] = this.size;
        this.size++;
    }

    @Override
    public void visitEnd() {
        sort(0, this.size - 1);
        int[] positions = this.positions;
        this.positions = new int[positions.length];
        for (int i = 0; i < this.size; i++) {
            this.positions[positions[i]] = i;
        }
    }

    int getSize() {
        return this.size;
    }

    private void sort(int left, int right) {
        if (left < right) {
            int pivot = this.hashCodes[left + (right - left) / 2];
            int i = left;
            int j = right;
            while (i <= j) {
                while (this.hashCodes[i] < pivot) {
                    i++;
                }
                while (this.hashCodes[j] > pivot) {
                    j--;
                }
                if (i <= j) {
                    swap(i, j);
                    i++;
                    j--;
                }
            }
            if (left < j) {
                sort(left, j);
            }
            if (right > i) {
                sort(i, right);
            }
        }
    }

    private void swap(int i, int j) {
        swap(this.hashCodes, i, j);
        swap(this.centralDirectoryOffsets, i, j);
        swap(this.positions, i, j);
    }

    private void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    @Override
    public Iterator<JarEntry> iterator() {
        return new EntryIterator(NO_VALIDATION);
    }

    Iterator<JarEntry> iterator(Runnable validator) {
        return new EntryIterator(validator);
    }

    boolean containsEntry(CharSequence name) {
        return getEntry(name, FileHeader.class, true) != null;
    }

    JarEntry getEntry(CharSequence name) {
        return getEntry(name, JarEntry.class, true);
    }

    InputStream getInputStream(String name) throws IOException {
        FileHeader entry = getEntry(name, FileHeader.class, false);
        return getInputStream(entry);
    }

    InputStream getInputStream(FileHeader entry) throws IOException {
        if (entry == null) {
            return null;
        }
        InputStream inputStream = getEntryData(entry).getInputStream();
        if (entry.getMethod() == ZipEntry.DEFLATED) {
            inputStream = new ZipInflaterInputStream(inputStream, (int) entry.getSize());
        }
        return inputStream;
    }

    RandomAccessData getEntryData(String name) throws IOException {
        FileHeader entry = getEntry(name, FileHeader.class, false);
        if (entry == null) {
            return null;
        }
        return getEntryData(entry);
    }

    private RandomAccessData getEntryData(FileHeader entry) throws IOException {
        RandomAccessData data = this.jarFile.getData();
        byte[] localHeader = data.read(entry.getLocalHeaderOffset(), LOCAL_FILE_HEADER_SIZE);
        long nameLength = Bytes.littleEndianValue(localHeader, 26, 2);
        long extraLength = Bytes.littleEndianValue(localHeader, 28, 2);
        return data.getSubsection(entry.getLocalHeaderOffset() + LOCAL_FILE_HEADER_SIZE + nameLength + extraLength, entry.getCompressedSize());
    }

    private <T extends FileHeader> T getEntry(CharSequence name, Class<T> type, boolean cacheEntry) {
        T entry = doGetEntry(name, type, cacheEntry, null);
        if (!isMetaInfEntry(name) && isMultiReleaseJar()) {
            int version = RUNTIME_VERSION;
            AsciiBytes nameAlias = (entry instanceof JarEntry) ? ((JarEntry) entry).getAsciiBytesName() : new AsciiBytes(name.toString());
            while (version > BASE_VERSION) {
                T versionedEntry = doGetEntry("META-INF/versions/" + version + "/" + name, type, cacheEntry, nameAlias);
                if (versionedEntry != null) {
                    return versionedEntry;
                }
                version--;
            }
        }
        return entry;
    }

    private boolean isMetaInfEntry(CharSequence name) {
        return name.toString().startsWith(META_INF_PREFIX);
    }

    private boolean isMultiReleaseJar() {
        Boolean multiRelease = this.multiReleaseJar;
        if (multiRelease != null) {
            return multiRelease;
        }
        try {
            Manifest manifest = this.jarFile.getManifest();
            if (manifest == null) {
                multiRelease = false;
            } else {
                Attributes attributes = manifest.getMainAttributes();
                multiRelease = attributes.containsKey(MULTI_RELEASE);
            }
        } catch (IOException ex) {
            multiRelease = false;
        }
        this.multiReleaseJar = multiRelease;
        return multiRelease;
    }

    private <T extends FileHeader> T doGetEntry(CharSequence name, Class<T> type, boolean cacheEntry, AsciiBytes nameAlias) {
        int hashCode = AsciiBytes.hashCode(name);
        T entry = getEntry(hashCode, name, NO_SUFFIX, type, cacheEntry, nameAlias);
        if (entry == null) {
            hashCode = AsciiBytes.hashCode(hashCode, SLASH);
            entry = getEntry(hashCode, name, SLASH, type, cacheEntry, nameAlias);
        }
        return entry;
    }

    private <T extends FileHeader> T getEntry(int hashCode, CharSequence name, char suffix, Class<T> type, boolean cacheEntry, AsciiBytes nameAlias) {
        int index = getFirstIndex(hashCode);
        while (index >= 0 && index < this.size && this.hashCodes[index] == hashCode) {
            T entry = getEntry(index, type, cacheEntry, nameAlias);
            if (entry.hasName(name, suffix)) {
                return entry;
            }
            index++;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T extends FileHeader> T getEntry(int index, Class<T> type, boolean cacheEntry, AsciiBytes nameAlias) {
        try {
            FileHeader cached = this.entriesCache.get(index);
            FileHeader entry = (cached != null) ? cached : CentralDirectoryFileHeader.fromRandomAccessData(this.centralDirectoryData, this.centralDirectoryOffsets[index], this.filter);
            if (CentralDirectoryFileHeader.class.equals(entry.getClass()) && type.equals(JarEntry.class)) {
                entry = new JarEntry(this.jarFile, (CentralDirectoryFileHeader) entry, nameAlias);
            }
            if (cacheEntry && cached != entry) {
                this.entriesCache.put(index, entry);
            }
            return (T) entry;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private int getFirstIndex(int hashCode) {
        int index = Arrays.binarySearch(this.hashCodes, 0, this.size, hashCode);
        if (index < 0) {
            return -1;
        }
        while (index > 0 && this.hashCodes[index - 1] == hashCode) {
            index--;
        }
        return index;
    }

    void clearCache() {
        this.entriesCache.clear();
    }

    private AsciiBytes applyFilter(AsciiBytes name) {
        return (this.filter != null) ? this.filter.apply(name) : name;
    }

    private final class EntryIterator implements Iterator<JarEntry> {

        private final Runnable validator;

        private int index = 0;

        private EntryIterator(Runnable validator) {
            this.validator = validator;
            validator.run();
        }

        @Override
        public boolean hasNext() {
            this.validator.run();
            return this.index < JarFileEntries.this.size;
        }

        @Override
        public JarEntry next() {
            this.validator.run();
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            int entryIndex = JarFileEntries.this.positions[this.index];
            this.index++;
            return getEntry(entryIndex, JarEntry.class, false, null);
        }
    }
}