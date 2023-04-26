package com.oracle.truffle.regex.tregex.parser.ast;

import java.util.Objects;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.regex.RegexLanguage;
import com.oracle.truffle.regex.result.PreCalculatedResultFactory;
import com.oracle.truffle.regex.tregex.nfa.ASTTransition;
import com.oracle.truffle.regex.tregex.nfa.NFAStateTransition;
import com.oracle.truffle.regex.tregex.util.json.Json;
import com.oracle.truffle.regex.tregex.util.json.JsonArray;
import com.oracle.truffle.regex.tregex.util.json.JsonConvertible;
import com.oracle.truffle.regex.tregex.util.json.JsonValue;
import com.oracle.truffle.regex.util.TBitSet;

public class GroupBoundaries implements JsonConvertible {

    private static final byte[] EMPTY_BYTE_ARRAY = {};

    private static final short[] EMPTY_SHORT_ARRAY = {};

    private final TBitSet updateIndices;

    private final TBitSet clearIndices;

    private final int cachedHash;

    @CompilationFinal(dimensions = 1)
    private byte[] updateArrayByte;

    @CompilationFinal(dimensions = 1)
    private byte[] clearArrayByte;

    @CompilationFinal(dimensions = 1)
    private short[] updateArray;

    @CompilationFinal(dimensions = 1)
    private short[] clearArray;

    GroupBoundaries(TBitSet updateIndices, TBitSet clearIndices) {
        this.updateIndices = updateIndices;
        this.clearIndices = clearIndices;
        this.cachedHash = Objects.hashCode(updateIndices) * 31 + Objects.hashCode(clearIndices);
    }

    public static GroupBoundaries[] createCachedGroupBoundaries() {
        GroupBoundaries[] instances = new GroupBoundaries[TBitSet.getNumberOfStaticInstances()];
        for (int i = 0; i < instances.length; i++) {
            instances[i] = new GroupBoundaries(TBitSet.getStaticInstance(i), TBitSet.getEmptyInstance());
        }
        return instances;
    }

    public static GroupBoundaries getStaticInstance(RegexLanguage language, TBitSet updateIndices, TBitSet clearIndices) {
        if (clearIndices.isEmpty()) {
            int key = updateIndices.getStaticCacheKey();
            if (key >= 0) {
                return language.getCachedGroupBoundaries()[key];
            }
        }
        return null;
    }

    public static GroupBoundaries getEmptyInstance(RegexLanguage language) {
        return language.getCachedGroupBoundaries()[0];
    }

    public boolean isEmpty() {
        return updateIndices.isEmpty() && clearIndices.isEmpty();
    }

    public byte[] updatesToByteArray() {
        if (updateArrayByte == null) {
            updateArrayByte = indicesToByteArray(updateIndices);
        }
        return updateArrayByte;
    }

    public byte[] clearsToByteArray() {
        if (clearArrayByte == null) {
            clearArrayByte = indicesToByteArray(clearIndices);
        }
        return clearArrayByte;
    }

    private static byte[] indicesToByteArray(TBitSet indices) {
        if (indices.isEmpty()) {
            return EMPTY_BYTE_ARRAY;
        }
        final byte[] array = new byte[indices.numberOfSetBits()];
        int i = 0;
        for (int j : indices) {
            assert j < 256;
            array[i++] = (byte) j;
        }
        return array;
    }

    public void materializeArrays() {
        if (updateArray == null) {
            updateArray = indicesToShortArray(updateIndices);
            clearArray = indicesToShortArray(clearIndices);
        }
    }

    private static short[] indicesToShortArray(TBitSet indices) {
        if (indices.isEmpty()) {
            return EMPTY_SHORT_ARRAY;
        }
        final short[] array = new short[indices.numberOfSetBits()];
        writeIndicesToArray(indices, array, 0);
        return array;
    }

    private static void writeIndicesToArray(TBitSet indices, final short[] array, int offset) {
        int i = offset;
        for (int j : indices) {
            assert j < (1 << 16);
            array[i++] = (short) j;
        }
    }

    public TBitSet getUpdateIndices() {
        return updateIndices;
    }

    public TBitSet getClearIndices() {
        return clearIndices;
    }

    public boolean hasIndexUpdates() {
        return !updateIndices.isEmpty();
    }

    public boolean hasIndexClears() {
        return !clearIndices.isEmpty();
    }

    public void updateBitSets(TBitSet foreignUpdateIndices, TBitSet foreignClearIndices) {
        foreignUpdateIndices.union(updateIndices);
        foreignClearIndices.subtract(updateIndices);
        foreignClearIndices.union(clearIndices);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof GroupBoundaries)) {
            return false;
        }
        GroupBoundaries o = (GroupBoundaries) obj;
        return Objects.equals(updateIndices, o.updateIndices) && Objects.equals(clearIndices, o.clearIndices);
    }

    @Override
    public int hashCode() {
        return cachedHash;
    }

    public void applyToResultFactory(PreCalculatedResultFactory resultFactory, int index) {
        if (hasIndexUpdates()) {
            resultFactory.updateIndices(updateIndices, index);
        }
    }

    @ExplodeLoop
    public void applyExploded(int[] array, int offset, int index) {
        CompilerAsserts.partialEvaluationConstant(this);
        CompilerAsserts.partialEvaluationConstant(clearArray);
        CompilerAsserts.partialEvaluationConstant(updateArray);
        for (int i = 0; i < clearArray.length; i++) {
            array[offset + Short.toUnsignedInt(clearArray[i])] = -1;
        }
        for (int i = 0; i < updateArray.length; i++) {
            array[offset + Short.toUnsignedInt(updateArray[i])] = index;
        }
    }

    public void apply(int[] array, int offset, int index) {
        for (int i = 0; i < clearArray.length; i++) {
            array[offset + Short.toUnsignedInt(clearArray[i])] = -1;
        }
        for (int i = 0; i < updateArray.length; i++) {
            array[offset + Short.toUnsignedInt(updateArray[i])] = index;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (hasIndexUpdates()) {
            appendBitSet(sb, updateIndices, false).append(")(");
            appendBitSet(sb, updateIndices, true);
        }
        if (hasIndexClears()) {
            sb.append(" clr{");
            appendBitSet(sb, clearIndices, false).append(")(");
            appendBitSet(sb, clearIndices, true);
            sb.append("}");
        }
        return sb.toString();
    }

    @TruffleBoundary
    private static StringBuilder appendBitSet(StringBuilder sb, TBitSet gbBitSet, boolean entries) {
        boolean first = true;
        if (gbBitSet != null) {
            for (int i : gbBitSet) {
                if ((i & 1) == (entries ? 0 : 1)) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(",");
                    }
                    sb.append(Json.val(i / 2));
                }
            }
        }
        return sb;
    }

    @TruffleBoundary
    @Override
    public JsonValue toJson() {
        return Json.obj(Json.prop("updateEnter", gbBitSetGroupEntriesToJsonArray(updateIndices)), Json.prop("updateExit", gbBitSetGroupExitsToJsonArray(updateIndices)), Json.prop("clearEnter", gbBitSetGroupEntriesToJsonArray(clearIndices)), Json.prop("clearExit", gbBitSetGroupExitsToJsonArray(clearIndices)));
    }

    @TruffleBoundary
    private static JsonArray gbBitSetGroupEntriesToJsonArray(TBitSet gbArray) {
        return gbBitSetGroupPartToJsonArray(gbArray, true);
    }

    @TruffleBoundary
    private static JsonArray gbBitSetGroupExitsToJsonArray(TBitSet gbArray) {
        return gbBitSetGroupPartToJsonArray(gbArray, false);
    }

    @TruffleBoundary
    private static JsonArray gbBitSetGroupPartToJsonArray(TBitSet gbBitSet, boolean entries) {
        JsonArray array = Json.array();
        if (gbBitSet != null) {
            for (int i : gbBitSet) {
                if ((i & 1) == (entries ? 0 : 1)) {
                    array.append(Json.val(i / 2));
                }
            }
        }
        return array;
    }

    @TruffleBoundary
    public JsonArray indexUpdateSourceSectionsToJson(RegexAST ast) {
        if (!hasIndexUpdates()) {
            return Json.array();
        }
        return RegexAST.sourceSectionsToJson(getUpdateIndices().stream().mapToObj(x -> ast.getSourceSections(ast.getGroupByBoundaryIndex(x)).get(x & 1)));
    }
}