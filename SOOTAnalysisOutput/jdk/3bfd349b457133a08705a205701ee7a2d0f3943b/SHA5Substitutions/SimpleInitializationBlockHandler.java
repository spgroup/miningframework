package org.graalvm.compiler.hotspot.replacements;

import static org.graalvm.compiler.core.common.util.Util.Java8OrEarlier;
import static jdk.vm.ci.hotspot.HotSpotJVMCIRuntimeProvider.getArrayBaseOffset;
import org.graalvm.compiler.api.replacements.ClassSubstitution;
import org.graalvm.compiler.api.replacements.MethodSubstitution;
import org.graalvm.compiler.core.common.LocationIdentity;
import org.graalvm.compiler.debug.GraalError;
import org.graalvm.compiler.hotspot.HotSpotBackend;
import org.graalvm.compiler.hotspot.nodes.ComputeObjectAddressNode;
import org.graalvm.compiler.nodes.PiNode;
import org.graalvm.compiler.nodes.extended.UnsafeLoadNode;
import org.graalvm.compiler.word.Word;
import jdk.vm.ci.meta.JavaKind;

@ClassSubstitution(className = "sun.security.provider.SHA5", optional = true)
public class SHA5Substitutions {

    static final long stateOffset;

    static final Class<?> shaClass;

    public static final String implCompressName = Java8OrEarlier ? "implCompress" : "implCompress0";

    static {
        try {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            shaClass = Class.forName("sun.security.provider.SHA5", true, cl);
            stateOffset = UnsafeAccess.UNSAFE.objectFieldOffset(shaClass.getDeclaredField("state"));
        } catch (Exception ex) {
            throw new GraalError(ex);
        }
    }

    @MethodSubstitution(isStatic = false)
    static void implCompress0(Object receiver, byte[] buf, int ofs) {
        Object realReceiver = PiNode.piCastNonNull(receiver, shaClass);
        Object state = UnsafeLoadNode.load(realReceiver, stateOffset, JavaKind.Object, LocationIdentity.any());
        Word bufAddr = Word.unsigned(ComputeObjectAddressNode.get(buf, getArrayBaseOffset(JavaKind.Byte) + ofs));
        Word stateAddr = Word.unsigned(ComputeObjectAddressNode.get(state, getArrayBaseOffset(JavaKind.Int)));
        HotSpotBackend.sha5ImplCompressStub(bufAddr, stateAddr);
    }
}