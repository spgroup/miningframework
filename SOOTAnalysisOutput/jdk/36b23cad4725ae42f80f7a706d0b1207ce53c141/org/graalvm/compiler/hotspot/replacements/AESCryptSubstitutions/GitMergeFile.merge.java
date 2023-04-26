package org.graalvm.compiler.hotspot.replacements;

import static org.graalvm.compiler.hotspot.HotSpotBackend.DECRYPT_BLOCK;
import static org.graalvm.compiler.hotspot.HotSpotBackend.DECRYPT_BLOCK_WITH_ORIGINAL_KEY;
import static org.graalvm.compiler.hotspot.HotSpotBackend.ENCRYPT_BLOCK;
import static org.graalvm.compiler.nodes.extended.BranchProbabilityNode.VERY_SLOW_PATH_PROBABILITY;
import static org.graalvm.compiler.nodes.extended.BranchProbabilityNode.probability;
import static jdk.vm.ci.hotspot.HotSpotJVMCIRuntimeProvider.getArrayBaseOffset;
import org.graalvm.compiler.api.replacements.ClassSubstitution;
import org.graalvm.compiler.api.replacements.MethodSubstitution;
import org.graalvm.compiler.core.common.LocationIdentity;
import org.graalvm.compiler.core.common.spi.ForeignCallDescriptor;
import org.graalvm.compiler.debug.GraalError;
import org.graalvm.compiler.graph.Node.ConstantNodeParameter;
import org.graalvm.compiler.graph.Node.NodeIntrinsic;
import org.graalvm.compiler.hotspot.nodes.ComputeObjectAddressNode;
import org.graalvm.compiler.nodes.DeoptimizeNode;
import org.graalvm.compiler.nodes.PiNode;
import org.graalvm.compiler.nodes.extended.ForeignCallNode;
import org.graalvm.compiler.nodes.extended.UnsafeLoadNode;
import org.graalvm.compiler.word.Pointer;
import org.graalvm.compiler.word.Word;
import jdk.vm.ci.meta.DeoptimizationAction;
import jdk.vm.ci.meta.DeoptimizationReason;
import jdk.vm.ci.meta.JavaKind;

@ClassSubstitution(className = "com.sun.crypto.provider.AESCrypt", optional = true)
public class AESCryptSubstitutions {

    static final long kOffset;

    static final long lastKeyOffset;

    static final Class<?> AESCryptClass;

    static final int AES_BLOCK_SIZE_IN_BYTES;

    static {
        try {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            AESCryptClass = Class.forName("com.sun.crypto.provider.AESCrypt", true, cl);
            kOffset = UnsafeAccess.UNSAFE.objectFieldOffset(AESCryptClass.getDeclaredField("K"));
            lastKeyOffset = UnsafeAccess.UNSAFE.objectFieldOffset(AESCryptClass.getDeclaredField("lastKey"));
            AES_BLOCK_SIZE_IN_BYTES = 16;
        } catch (Exception ex) {
            throw new GraalError(ex);
        }
    }

    @MethodSubstitution(isStatic = false)
    static void encryptBlock(Object rcvr, byte[] in, int inOffset, byte[] out, int outOffset) {
        crypt(rcvr, in, inOffset, out, outOffset, true, false);
    }

    @MethodSubstitution(isStatic = false)
    static void implEncryptBlock(Object rcvr, byte[] in, int inOffset, byte[] out, int outOffset) {
        crypt(rcvr, in, inOffset, out, outOffset, true, false);
    }

    @MethodSubstitution(isStatic = false)
    static void decryptBlock(Object rcvr, byte[] in, int inOffset, byte[] out, int outOffset) {
        crypt(rcvr, in, inOffset, out, outOffset, false, false);
    }

    @MethodSubstitution(isStatic = false)
    static void implDecryptBlock(Object rcvr, byte[] in, int inOffset, byte[] out, int outOffset) {
        crypt(rcvr, in, inOffset, out, outOffset, false, false);
    }

    @MethodSubstitution(value = "decryptBlock", isStatic = false)
    static void decryptBlockWithOriginalKey(Object rcvr, byte[] in, int inOffset, byte[] out, int outOffset) {
        crypt(rcvr, in, inOffset, out, outOffset, false, true);
    }

    @MethodSubstitution(value = "implDecryptBlock", isStatic = false)
    static void implDecryptBlockWithOriginalKey(Object rcvr, byte[] in, int inOffset, byte[] out, int outOffset) {
        crypt(rcvr, in, inOffset, out, outOffset, false, true);
    }

    private static void crypt(Object rcvr, byte[] in, int inOffset, byte[] out, int outOffset, boolean encrypt, boolean withOriginalKey) {
        checkArgs(in, inOffset, out, outOffset);
        Object realReceiver = PiNode.piCastNonNull(rcvr, AESCryptClass);
        Object kObject = UnsafeLoadNode.load(realReceiver, kOffset, JavaKind.Object, LocationIdentity.any());
        Pointer kAddr = Word.objectToTrackedPointer(kObject).add(getArrayBaseOffset(JavaKind.Int));
        Word inAddr = Word.unsigned(ComputeObjectAddressNode.get(in, getArrayBaseOffset(JavaKind.Byte) + inOffset));
        Word outAddr = Word.unsigned(ComputeObjectAddressNode.get(out, getArrayBaseOffset(JavaKind.Byte) + outOffset));
        if (encrypt) {
            encryptBlockStub(ENCRYPT_BLOCK, inAddr, outAddr, kAddr);
        } else {
            if (withOriginalKey) {
                Object lastKeyObject = UnsafeLoadNode.load(realReceiver, lastKeyOffset, JavaKind.Object, LocationIdentity.any());
                Pointer lastKeyAddr = Word.objectToTrackedPointer(lastKeyObject).add(getArrayBaseOffset(JavaKind.Byte));
                decryptBlockWithOriginalKeyStub(DECRYPT_BLOCK_WITH_ORIGINAL_KEY, inAddr, outAddr, kAddr, lastKeyAddr);
            } else {
                decryptBlockStub(DECRYPT_BLOCK, inAddr, outAddr, kAddr);
            }
        }
    }

    static void checkArgs(byte[] in, int inOffset, byte[] out, int outOffset) {
        if (probability(VERY_SLOW_PATH_PROBABILITY, inOffset < 0 || in.length - AES_BLOCK_SIZE_IN_BYTES < inOffset || outOffset < 0 || out.length - AES_BLOCK_SIZE_IN_BYTES < outOffset)) {
            DeoptimizeNode.deopt(DeoptimizationAction.None, DeoptimizationReason.RuntimeConstraint);
        }
    }

    @NodeIntrinsic(ForeignCallNode.class)
    public static native void encryptBlockStub(@ConstantNodeParameter ForeignCallDescriptor descriptor, Word in, Word out, Pointer key);

    @NodeIntrinsic(ForeignCallNode.class)
    public static native void decryptBlockStub(@ConstantNodeParameter ForeignCallDescriptor descriptor, Word in, Word out, Pointer key);

    @NodeIntrinsic(ForeignCallNode.class)
    public static native void decryptBlockWithOriginalKeyStub(@ConstantNodeParameter ForeignCallDescriptor descriptor, Word in, Word out, Pointer key, Pointer originalKey);
}
