package jdk.tools.jaotc.binformat;

import static org.graalvm.compiler.hotspot.meta.HotSpotAOTProfilingPlugin.Options.TieredAOT;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jdk.tools.jaotc.binformat.Symbol.Binding;
import jdk.tools.jaotc.binformat.Symbol.Kind;
import jdk.tools.jaotc.binformat.elf.JELFRelocObject;
import org.graalvm.compiler.hotspot.GraalHotSpotVMConfig;

public class BinaryContainer implements SymbolTable {

    private final int codeSegmentSize;

    private final int codeEntryAlignment;

    private final CodeContainer codeContainer;

    private final CodeContainer extLinkageContainer;

    private final ByteContainer extLinkageGOTContainer;

    private final ByteContainer metaspaceGotContainer;

    private final ByteContainer metadataGotContainer;

    private final ByteContainer methodStateContainer;

    private final ByteContainer oopGotContainer;

    private final ReadOnlyDataContainer configContainer;

    private final ReadOnlyDataContainer metaspaceNamesContainer;

    private final ReadOnlyDataContainer methodsOffsetsContainer;

    private final ReadOnlyDataContainer klassesOffsetsContainer;

    private final ReadOnlyDataContainer klassesDependenciesContainer;

    private final HeaderContainer headerContainer;

    private final ReadOnlyDataContainer stubsOffsetsContainer;

    private final ReadOnlyDataContainer codeSegmentsContainer;

    private final ReadOnlyDataContainer methodMetadataContainer;

    private final ReadOnlyDataContainer constantDataContainer;

    private final Map<String, Integer> offsetStringTable = new HashMap<>();

    private final Map<String, Integer> metaspaceNames = new HashMap<>();

    private final Map<String, Symbol> symbolTable = new HashMap<>();

    private final Map<Symbol, List<Relocation>> relocationTable = new HashMap<>();

    private final Map<Symbol, Relocation> uniqueRelocationTable = new HashMap<>();

    private static final HashMap<String, String> functionNamesToAOTSymbols = new HashMap<>();

    private static final String[][] map = { { "CompilerToVM::Data::SharedRuntime_deopt_blob_unpack", "_aot_deopt_blob_unpack" }, { "CompilerToVM::Data::SharedRuntime_deopt_blob_uncommon_trap", "_aot_deopt_blob_uncommon_trap" }, { "CompilerToVM::Data::SharedRuntime_ic_miss_stub", "_aot_ic_miss_stub" }, { "CompilerToVM::Data::SharedRuntime_handle_wrong_method_stub", "_aot_handle_wrong_method_stub" }, { "SharedRuntime::exception_handler_for_return_address", "_aot_exception_handler_for_return_address" }, { "SharedRuntime::register_finalizer", "_aot_register_finalizer" }, { "SharedRuntime::OSR_migration_end", "_aot_OSR_migration_end" }, { "CompilerRuntime::resolve_string_by_symbol", "_aot_resolve_string_by_symbol" }, { "CompilerRuntime::resolve_klass_by_symbol", "_aot_resolve_klass_by_symbol" }, { "CompilerRuntime::resolve_method_by_symbol_and_load_counters", "_aot_resolve_method_by_symbol_and_load_counters" }, { "CompilerRuntime::initialize_klass_by_symbol", "_aot_initialize_klass_by_symbol" }, { "CompilerRuntime::invocation_event", "_aot_invocation_event" }, { "CompilerRuntime::backedge_event", "_aot_backedge_event" }, { "CompilerToVM::Data::dpow", "_aot_shared_runtime_dpow" }, { "CompilerToVM::Data::dexp", "_aot_shared_runtime_dexp" }, { "CompilerToVM::Data::dcos", "_aot_shared_runtime_dcos" }, { "CompilerToVM::Data::dsin", "_aot_shared_runtime_dsin" }, { "CompilerToVM::Data::dtan", "_aot_shared_runtime_dtan" }, { "CompilerToVM::Data::dlog", "_aot_shared_runtime_dlog" }, { "CompilerToVM::Data::dlog10", "_aot_shared_runtime_dlog10" }, { "StubRoutines::_jbyte_arraycopy", "_aot_stub_routines_jbyte_arraycopy" }, { "StubRoutines::_jshort_arraycopy", "_aot_stub_routines_jshort_arraycopy" }, { "StubRoutines::_jint_arraycopy", "_aot_stub_routines_jint_arraycopy" }, { "StubRoutines::_jlong_arraycopy", "_aot_stub_routines_jlong_arraycopy" }, { "StubRoutines::_oop_arraycopy", "_aot_stub_routines_oop_arraycopy" }, { "StubRoutines::_oop_arraycopy_uninit", "_aot_stub_routines_oop_arraycopy_uninit" }, { "StubRoutines::_jbyte_disjoint_arraycopy", "_aot_stub_routines_jbyte_disjoint_arraycopy" }, { "StubRoutines::_jshort_disjoint_arraycopy", "_aot_stub_routines_jshort_disjoint_arraycopy" }, { "StubRoutines::_jint_disjoint_arraycopy", "_aot_stub_routines_jint_disjoint_arraycopy" }, { "StubRoutines::_jlong_disjoint_arraycopy", "_aot_stub_routines_jlong_disjoint_arraycopy" }, { "StubRoutines::_oop_disjoint_arraycopy", "_aot_stub_routines_oop_disjoint_arraycopy" }, { "StubRoutines::_oop_disjoint_arraycopy_uninit", "_aot_stub_routines_oop_disjoint_arraycopy_uninit" }, { "StubRoutines::_arrayof_jbyte_arraycopy", "_aot_stub_routines_arrayof_jbyte_arraycopy" }, { "StubRoutines::_arrayof_jshort_arraycopy", "_aot_stub_routines_arrayof_jshort_arraycopy" }, { "StubRoutines::_arrayof_jint_arraycopy", "_aot_stub_routines_arrayof_jint_arraycopy" }, { "StubRoutines::_arrayof_jlong_arraycopy", "_aot_stub_routines_arrayof_jlong_arraycopy" }, { "StubRoutines::_arrayof_oop_arraycopy", "_aot_stub_routines_arrayof_oop_arraycopy" }, { "StubRoutines::_arrayof_oop_arraycopy_uninit", "_aot_stub_routines_arrayof_oop_arraycopy_uninit" }, { "StubRoutines::_arrayof_jbyte_disjoint_arraycopy", "_aot_stub_routines_arrayof_jbyte_disjoint_arraycopy" }, { "StubRoutines::_arrayof_jshort_disjoint_arraycopy", "_aot_stub_routines_arrayof_jshort_disjoint_arraycopy" }, { "StubRoutines::_arrayof_jint_disjoint_arraycopy", "_aot_stub_routines_arrayof_jint_disjoint_arraycopy" }, { "StubRoutines::_arrayof_jlong_disjoint_arraycopy", "_aot_stub_routines_arrayof_jlong_disjoint_arraycopy" }, { "StubRoutines::_arrayof_oop_disjoint_arraycopy", "_aot_stub_routines_arrayof_oop_disjoint_arraycopy" }, { "StubRoutines::_arrayof_oop_disjoint_arraycopy_uninit", "_aot_stub_routines_arrayof_oop_disjoint_arraycopy_uninit" }, { "StubRoutines::_checkcast_arraycopy", "_aot_stub_routines_checkcast_arraycopy" }, { "StubRoutines::_aescrypt_encryptBlock", "_aot_stub_routines_aescrypt_encryptBlock" }, { "StubRoutines::_aescrypt_decryptBlock", "_aot_stub_routines_aescrypt_decryptBlock" }, { "StubRoutines::_cipherBlockChaining_encryptAESCrypt", "_aot_stub_routines_cipherBlockChaining_encryptAESCrypt" }, { "StubRoutines::_cipherBlockChaining_decryptAESCrypt", "_aot_stub_routines_cipherBlockChaining_decryptAESCrypt" }, { "StubRoutines::_updateBytesCRC32", "_aot_stub_routines_update_bytes_crc32" }, { "StubRoutines::_crc_table_adr", "_aot_stub_routines_crc_table_adr" }, { "StubRoutines::_sha1_implCompress", "_aot_stub_routines_sha1_implCompress" }, { "StubRoutines::_sha1_implCompressMB", "_aot_stub_routines_sha1_implCompressMB" }, { "StubRoutines::_sha256_implCompress", "_aot_stub_routines_sha256_implCompress" }, { "StubRoutines::_sha256_implCompressMB", "_aot_stub_routines_sha256_implCompressMB" }, { "StubRoutines::_sha512_implCompress", "_aot_stub_routines_sha512_implCompress" }, { "StubRoutines::_sha512_implCompressMB", "_aot_stub_routines_sha512_implCompressMB" }, { "StubRoutines::_multiplyToLen", "_aot_stub_routines_multiplyToLen" }, { "StubRoutines::_counterMode_AESCrypt", "_aot_stub_routines_counterMode_AESCrypt" }, { "StubRoutines::_ghash_processBlocks", "_aot_stub_routines_ghash_processBlocks" }, { "StubRoutines::_crc32c_table_addr", "_aot_stub_routines_crc32c_table_addr" }, { "StubRoutines::_updateBytesCRC32C", "_aot_stub_routines_updateBytesCRC32C" }, { "StubRoutines::_updateBytesAdler32", "_aot_stub_routines_updateBytesAdler32" }, { "StubRoutines::_squareToLen", "_aot_stub_routines_squareToLen" }, { "StubRoutines::_mulAdd", "_aot_stub_routines_mulAdd" }, { "StubRoutines::_montgomeryMultiply", "_aot_stub_routines_montgomeryMultiply" }, { "StubRoutines::_montgomerySquare", "_aot_stub_routines_montgomerySquare" }, { "StubRoutines::_vectorizedMismatch", "_aot_stub_routines_vectorizedMismatch" }, { "StubRoutines::_throw_delayed_StackOverflowError_entry", "_aot_stub_routines_throw_delayed_StackOverflowError_entry" }, { "os::javaTimeMillis", "_aot_os_javaTimeMillis" }, { "os::javaTimeNanos", "_aot_os_javaTimeNanos" }, { "JVMCIRuntime::monitorenter", "_aot_jvmci_runtime_monitorenter" }, { "JVMCIRuntime::monitorexit", "_aot_jvmci_runtime_monitorexit" }, { "JVMCIRuntime::log_object", "_aot_jvmci_runtime_log_object" }, { "JVMCIRuntime::log_printf", "_aot_jvmci_runtime_log_printf" }, { "JVMCIRuntime::vm_message", "_aot_jvmci_runtime_vm_message" }, { "JVMCIRuntime::new_instance", "_aot_jvmci_runtime_new_instance" }, { "JVMCIRuntime::log_primitive", "_aot_jvmci_runtime_log_primitive" }, { "JVMCIRuntime::new_multi_array", "_aot_jvmci_runtime_new_multi_array" }, { "JVMCIRuntime::validate_object", "_aot_jvmci_runtime_validate_object" }, { "JVMCIRuntime::dynamic_new_array", "_aot_jvmci_runtime_dynamic_new_array" }, { "JVMCIRuntime::write_barrier_pre", "_aot_jvmci_runtime_write_barrier_pre" }, { "JVMCIRuntime::identity_hash_code", "_aot_jvmci_runtime_identity_hash_code" }, { "JVMCIRuntime::write_barrier_post", "_aot_jvmci_runtime_write_barrier_post" }, { "JVMCIRuntime::dynamic_new_instance", "_aot_jvmci_runtime_dynamic_new_instance" }, { "JVMCIRuntime::thread_is_interrupted", "_aot_jvmci_runtime_thread_is_interrupted" }, { "JVMCIRuntime::exception_handler_for_pc", "_aot_jvmci_runtime_exception_handler_for_pc" }, { "JVMCIRuntime::test_deoptimize_call_int", "_aot_jvmci_runtime_test_deoptimize_call_int" }, { "JVMCIRuntime::throw_and_post_jvmti_exception", "_aot_jvmci_runtime_throw_and_post_jvmti_exception" }, { "JVMCIRuntime::throw_klass_external_name_exception", "_aot_jvmci_runtime_throw_klass_external_name_exception" }, { "JVMCIRuntime::throw_class_cast_exception", "_aot_jvmci_runtime_throw_class_cast_exception" }, { "JVMCIRuntime::vm_error", "_aot_jvmci_runtime_vm_error" }, { "JVMCIRuntime::new_array", "_aot_jvmci_runtime_new_array" } };

    static {
        for (String[] entry : map) {
            functionNamesToAOTSymbols.put(entry[0], entry[1]);
        }
    }

    public BinaryContainer(GraalHotSpotVMConfig config, String jvmVersion) {
        this.codeSegmentSize = config.codeSegmentSize;
        this.codeEntryAlignment = config.codeEntryAlignment;
        codeContainer = new CodeContainer(".text", this);
        extLinkageContainer = new CodeContainer(".hotspot.linkage.plt", this);
        configContainer = new ReadOnlyDataContainer(".config", this);
        metaspaceNamesContainer = new ReadOnlyDataContainer(".metaspace.names", this);
        methodsOffsetsContainer = new ReadOnlyDataContainer(".methods.offsets", this);
        klassesOffsetsContainer = new ReadOnlyDataContainer(".klasses.offsets", this);
        klassesDependenciesContainer = new ReadOnlyDataContainer(".klasses.dependencies", this);
        headerContainer = new HeaderContainer(jvmVersion, new ReadOnlyDataContainer(".header", this));
        stubsOffsetsContainer = new ReadOnlyDataContainer(".stubs.offsets", this);
        codeSegmentsContainer = new ReadOnlyDataContainer(".code.segments", this);
        constantDataContainer = new ReadOnlyDataContainer(".method.constdata", this);
        methodMetadataContainer = new ReadOnlyDataContainer(".method.metadata", this);
        metaspaceGotContainer = new ByteContainer(".metaspace.got", this);
        metadataGotContainer = new ByteContainer(".metadata.got", this);
        methodStateContainer = new ByteContainer(".method.state", this);
        oopGotContainer = new ByteContainer(".oop.got", this);
        extLinkageGOTContainer = new ByteContainer(".hotspot.linkage.got", this);
        addGlobalSymbols();
        recordConfiguration(config);
    }

    private void recordConfiguration(GraalHotSpotVMConfig config) {
        boolean[] booleanFlags = { config.cAssertions, config.useCompressedOops, config.useCompressedClassPointers, config.compactFields, config.useG1GC, config.useCMSGC, config.useTLAB, config.useBiasedLocking, TieredAOT.getValue(), config.enableContended, config.restrictContended };
        int[] intFlags = { config.narrowOopShift, config.narrowKlassShift, config.contendedPaddingWidth, config.fieldsAllocationStyle, config.objectAlignment, config.codeSegmentSize };
        byte[] booleanFlagsAsBytes = flagsToByteArray(booleanFlags);
        int size0 = configContainer.getByteStreamSize();
        int computedSize = booleanFlagsAsBytes.length * Byte.BYTES + intFlags.length * Integer.BYTES + Integer.BYTES;
        configContainer.appendInt(computedSize).appendInts(intFlags).appendBytes(booleanFlagsAsBytes);
        int size = configContainer.getByteStreamSize() - size0;
        assert size == computedSize;
    }

    private static byte[] flagsToByteArray(boolean[] flags) {
        byte[] byteArray = new byte[flags.length];
        for (int i = 0; i < flags.length; ++i) {
            byteArray[i] = boolToByte(flags[i]);
        }
        return byteArray;
    }

    private static byte boolToByte(boolean flag) {
        return (byte) (flag ? 1 : 0);
    }

    public void freeMemory() {
        offsetStringTable.clear();
        metaspaceNames.clear();
    }

    public String getCardTableAddressSymbolName() {
        return "_aot_card_table_address";
    }

    public String getHeapTopAddressSymbolName() {
        return "_aot_heap_top_address";
    }

    public String getHeapEndAddressSymbolName() {
        return "_aot_heap_end_address";
    }

    public String getCrcTableAddressSymbolName() {
        return "_aot_stub_routines_crc_table_adr";
    }

    public String getPollingPageSymbolName() {
        return "_aot_polling_page";
    }

    public String getResolveStaticEntrySymbolName() {
        return "_resolve_static_entry";
    }

    public String getResolveVirtualEntrySymbolName() {
        return "_resolve_virtual_entry";
    }

    public String getResolveOptVirtualEntrySymbolName() {
        return "_resolve_opt_virtual_entry";
    }

    public String getNarrowKlassBaseAddressSymbolName() {
        return "_aot_narrow_klass_base_address";
    }

    public String getLogOfHeapRegionGrainBytesSymbolName() {
        return "_aot_log_of_heap_region_grain_bytes";
    }

    public String getInlineContiguousAllocationSupportedSymbolName() {
        return "_aot_inline_contiguous_allocation_supported";
    }

    public int getCodeSegmentSize() {
        return codeSegmentSize;
    }

    public int getCodeEntryAlignment() {
        return codeEntryAlignment;
    }

    public String getAOTSymbolForVMFunctionName(String functionName) {
        return functionNamesToAOTSymbols.get(functionName);
    }

    private void addGlobalSymbols() {
        createContainerSymbol(codeContainer);
        createContainerSymbol(configContainer);
        createContainerSymbol(methodsOffsetsContainer);
        createContainerSymbol(klassesOffsetsContainer);
        createContainerSymbol(klassesDependenciesContainer);
        createContainerSymbol(metaspaceGotContainer);
        createContainerSymbol(metadataGotContainer);
        createContainerSymbol(methodStateContainer);
        createContainerSymbol(oopGotContainer);
        createContainerSymbol(metaspaceNamesContainer);
        createContainerSymbol(methodMetadataContainer);
        createContainerSymbol(stubsOffsetsContainer);
        createContainerSymbol(headerContainer.getContainer());
        createContainerSymbol(codeSegmentsContainer);
        createGotSymbol(getResolveStaticEntrySymbolName());
        createGotSymbol(getResolveVirtualEntrySymbolName());
        createGotSymbol(getResolveOptVirtualEntrySymbolName());
        createGotSymbol(getCardTableAddressSymbolName());
        createGotSymbol(getHeapTopAddressSymbolName());
        createGotSymbol(getHeapEndAddressSymbolName());
        createGotSymbol(getNarrowKlassBaseAddressSymbolName());
        createGotSymbol(getPollingPageSymbolName());
        createGotSymbol(getLogOfHeapRegionGrainBytesSymbolName());
        createGotSymbol(getInlineContiguousAllocationSupportedSymbolName());
        for (HashMap.Entry<String, String> entry : functionNamesToAOTSymbols.entrySet()) {
            createGotSymbol(entry.getValue());
        }
    }

    private static void createContainerSymbol(ByteContainer container) {
        container.createSymbol(0, Kind.OBJECT, Binding.GLOBAL, 0, "JVM" + container.getContainerName());
    }

    private void createGotSymbol(String name) {
        String s = "got." + name;
        Symbol gotSymbol = extLinkageGOTContainer.createGotSymbol(s);
        extLinkageGOTContainer.createSymbol(gotSymbol.getOffset(), Kind.OBJECT, Binding.GLOBAL, 8, name);
    }

    public void createBinary(String outputFileName, String aotVersion) throws IOException {
        String osName = System.getProperty("os.name");
        switch(osName) {
            case "Linux":
            case "SunOS":
                JELFRelocObject elfso = new JELFRelocObject(this, outputFileName, aotVersion);
                elfso.createELFRelocObject(relocationTable, symbolTable.values());
                break;
            default:
                throw new InternalError("Unsupported platform: " + osName);
        }
    }

    public void addSymbol(Symbol symInfo) {
        if (symInfo.getName().startsWith("got.") && !(symInfo instanceof GotSymbol)) {
            throw new InternalError("adding got. without being GotSymbol");
        }
        if (symbolTable.containsKey(symInfo.getName())) {
            throw new InternalError("Symbol: " + symInfo.getName() + " already exists in SymbolTable");
        } else {
            symbolTable.put(symInfo.getName(), symInfo);
        }
    }

    public boolean addStringOffset(String name, Integer offset) {
        offsetStringTable.put(name, offset);
        return true;
    }

    public void addRelocation(Relocation info) {
        if (relocationTable.containsKey(info.getSymbol())) {
            relocationTable.get(info.getSymbol()).add(info);
        } else if (uniqueRelocationTable.containsKey(info.getSymbol())) {
            ArrayList<Relocation> list = new ArrayList<>(2);
            list.add(uniqueRelocationTable.get(info.getSymbol()));
            list.add(info);
            relocationTable.put(info.getSymbol(), list);
            uniqueRelocationTable.remove(info.getSymbol());
        } else {
            uniqueRelocationTable.put(info.getSymbol(), info);
        }
    }

    @Override
    public Symbol getSymbol(String symName) {
        return symbolTable.get(symName);
    }

    @Override
    public Symbol createSymbol(int offset, Kind kind, Binding binding, int size, String name) {
        if (kind != Kind.NATIVE_FUNCTION) {
            throw new UnsupportedOperationException("Must be external functions: " + name);
        }
        Symbol symbol = new Symbol(offset, kind, binding, null, size, name);
        addSymbol(symbol);
        return symbol;
    }

    public Integer getStringOffset(String name) {
        return offsetStringTable.get(name);
    }

    private static void appendBytes(ByteContainer byteContainer, byte[] targetCode, int offset, int size) {
        byteContainer.appendBytes(targetCode, offset, size);
    }

    public void appendCodeBytes(byte[] targetCode, int offset, int size) {
        appendBytes(codeContainer, targetCode, offset, size);
    }

    public void appendIntToCode(int value) {
        codeContainer.appendInt(value);
    }

    public int appendExtLinkageGotBytes(byte[] bytes, int offset, int size) {
        int startOffset = extLinkageGOTContainer.getByteStreamSize();
        appendBytes(extLinkageGOTContainer, bytes, offset, size);
        return startOffset;
    }

    public int appendMetaspaceGotBytes(byte[] bytes, int offset, int size) {
        int startOffset = metaspaceGotContainer.getByteStreamSize();
        appendBytes(metaspaceGotContainer, bytes, offset, size);
        return startOffset;
    }

    public void addMetadataGotEntry(int offset) {
        metadataGotContainer.appendLong(offset);
    }

    public int addMetaspaceName(String name) {
        Integer value = metaspaceNames.get(name);
        if (value != null) {
            return value.intValue();
        }
        int nameOffset = alignUp(metaspaceNamesContainer, 8);
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bout);
            int len = name.length();
            if (name.startsWith("Stub")) {
                out.writeUTF(name);
            } else {
                int parenthesesIndex = name.lastIndexOf('(', len - 1);
                if (parenthesesIndex > 0) {
                    int dotIndex = name.lastIndexOf('.', parenthesesIndex - 1);
                    assert dotIndex > 0 : "method's full name should have '.' : " + name;
                    String klassName = name.substring(0, dotIndex);
                    out.writeUTF(klassName);
                    String methodName = name.substring(dotIndex + 1, parenthesesIndex);
                    out.writeUTF(methodName);
                    String signature = name.substring(parenthesesIndex, len);
                    out.writeUTF(signature);
                } else {
                    out.writeUTF(name);
                }
            }
            out.writeShort(0);
            byte[] b = bout.toByteArray();
            metaspaceNamesContainer.appendBytes(b, 0, b.length);
            metaspaceNames.put(name, nameOffset);
            return nameOffset;
        } catch (IOException e) {
            throw new InternalError("Failed to append bytes to stubs sections", e);
        }
    }

    public Integer addOopSymbol(String oopName) {
        Integer oopGotOffset = getStringOffset(oopName);
        if (oopGotOffset != null) {
            return oopGotOffset;
        }
        return newOopSymbol(oopName);
    }

    private Integer newOopSymbol(String oopName) {
        int offset = oopGotContainer.getByteStreamSize();
        String gotName = "got.ldc." + offset;
        Symbol relocationSymbol = oopGotContainer.createGotSymbol(gotName);
        if (offset != relocationSymbol.getOffset()) {
            throw new InternalError("offset must equal! (" + offset + " vs " + relocationSymbol.getOffset());
        }
        addStringOffset(oopName, relocationSymbol.getOffset());
        return relocationSymbol.getOffset();
    }

    public int addMetaspaceSymbol(String metaspaceName) {
        String gotName = "got." + metaspaceName;
        Symbol relocationSymbol = getGotSymbol(gotName);
        int metaspaceOffset = -1;
        if (relocationSymbol == null) {
            metaspaceGotContainer.createGotSymbol(gotName);
        }
        return metaspaceOffset;
    }

    public Symbol getGotSymbol(String name) {
        assert name.startsWith("got.");
        return symbolTable.get(name);
    }

    public int addTwoSlotMetaspaceSymbol(String metaspaceName) {
        String gotName = "got." + metaspaceName;
        Symbol previous = getGotSymbol(gotName);
        assert previous == null : "should be called only once for: " + metaspaceName;
        String gotInitName = "got.init." + metaspaceName;
        GotSymbol slot1Symbol = metaspaceGotContainer.createGotSymbol(gotInitName);
        GotSymbol slot2Symbol = metaspaceGotContainer.createGotSymbol(gotName);
        slot1Symbol.getIndex();
        return slot2Symbol.getIndex();
    }

    public int addMethodsCount(int count, ReadOnlyDataContainer container) {
        return appendInt(count, container);
    }

    private static int appendInt(int count, ReadOnlyDataContainer container) {
        int offset = container.getByteStreamSize();
        container.appendInt(count);
        return offset;
    }

    public int addConstantData(byte[] data, int alignment) {
        int constantDataOffset = alignUp(constantDataContainer, alignment);
        constantDataContainer.appendBytes(data, 0, data.length);
        alignUp(constantDataContainer, alignment);
        return constantDataOffset;
    }

    public int alignUp(ByteContainer container, int alignment) {
        if (Integer.bitCount(alignment) != 1) {
            throw new IllegalArgumentException("Must be a power of 2");
        }
        int offset = container.getByteStreamSize();
        int aligned = (offset + (alignment - 1)) & -alignment;
        if (aligned < offset || (aligned & (alignment - 1)) != 0) {
            throw new RuntimeException("Error aligning: " + offset + " -> " + aligned);
        }
        if (aligned != offset) {
            int nullArraySz = aligned - offset;
            byte[] nullArray = new byte[nullArraySz];
            container.appendBytes(nullArray, 0, nullArraySz);
            offset = aligned;
        }
        return offset;
    }

    public void addCodeSegments(int start, int end) {
        assert (start % codeSegmentSize) == 0 : "not aligned code";
        int currentOffset = codeSegmentsContainer.getByteStreamSize();
        int offset = start / codeSegmentSize;
        int emptySize = offset - currentOffset;
        if (emptySize > 0) {
            byte[] emptyArray = new byte[emptySize];
            for (int i = 0; i < emptySize; i++) {
                emptyArray[i] = (byte) 0xff;
            }
            appendBytes(codeSegmentsContainer, emptyArray, 0, emptySize);
        }
        int alignedEnd = (end + (codeSegmentSize - 1)) & -codeSegmentSize;
        int segmentsCount = (alignedEnd / codeSegmentSize) - offset;
        byte[] segments = new byte[segmentsCount];
        int idx = 0;
        for (int i = 0; i < segmentsCount; i++) {
            segments[i] = (byte) idx;
            idx = (idx == 0xfe) ? 1 : (idx + 1);
        }
        appendBytes(codeSegmentsContainer, segments, 0, segmentsCount);
    }

    public CodeContainer getExtLinkageContainer() {
        return extLinkageContainer;
    }

    public ByteContainer getExtLinkageGOTContainer() {
        return extLinkageGOTContainer;
    }

    public ByteContainer getMethodMetadataContainer() {
        return methodMetadataContainer;
    }

    public ReadOnlyDataContainer getMetaspaceNamesContainer() {
        return metaspaceNamesContainer;
    }

    public ReadOnlyDataContainer getMethodsOffsetsContainer() {
        return methodsOffsetsContainer;
    }

    public ReadOnlyDataContainer getKlassesOffsetsContainer() {
        return klassesOffsetsContainer;
    }

    public ReadOnlyDataContainer getKlassesDependenciesContainer() {
        return klassesDependenciesContainer;
    }

    public ReadOnlyDataContainer getStubsOffsetsContainer() {
        return stubsOffsetsContainer;
    }

    public ReadOnlyDataContainer getCodeSegmentsContainer() {
        return codeSegmentsContainer;
    }

    public ReadOnlyDataContainer getConstantDataContainer() {
        return constantDataContainer;
    }

    public ByteContainer getMetaspaceGotContainer() {
        return metaspaceGotContainer;
    }

    public ByteContainer getMetadataGotContainer() {
        return metadataGotContainer;
    }

    public ByteContainer getMethodStateContainer() {
        return methodStateContainer;
    }

    public ByteContainer getOopGotContainer() {
        return oopGotContainer;
    }

    public CodeContainer getCodeContainer() {
        return codeContainer;
    }

    public ReadOnlyDataContainer getConfigContainer() {
        return configContainer;
    }

    public Map<Symbol, Relocation> getUniqueRelocationTable() {
        return uniqueRelocationTable;
    }

    public HeaderContainer getHeaderContainer() {
        return headerContainer;
    }
}