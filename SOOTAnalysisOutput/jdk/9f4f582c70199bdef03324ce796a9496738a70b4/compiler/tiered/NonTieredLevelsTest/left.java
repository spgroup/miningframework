package compiler.tiered;

import java.util.function.IntPredicate;
import compiler.whitebox.CompilerWhiteBoxTest;
import jdk.test.lib.Platform;

public class NonTieredLevelsTest extends CompLevelsTest {

    private static final int AVAILABLE_COMP_LEVEL;

    private static final IntPredicate IS_AVAILABLE_COMPLEVEL;

    static {
        if (Platform.isServer() && !Platform.isEmulatedClient()) {
            AVAILABLE_COMP_LEVEL = COMP_LEVEL_FULL_OPTIMIZATION;
            IS_AVAILABLE_COMPLEVEL = x -> x == COMP_LEVEL_FULL_OPTIMIZATION;
        } else if (Platform.isClient() || Platform.isMinimal() || Platform.isEmulatedClient()) {
            AVAILABLE_COMP_LEVEL = COMP_LEVEL_SIMPLE;
            IS_AVAILABLE_COMPLEVEL = x -> x == COMP_LEVEL_SIMPLE;
        } else {
            throw new Error("TESTBUG: unknown VM: " + Platform.vmName);
        }
    }

    public static void main(String[] args) throws Exception {
        if (CompilerWhiteBoxTest.skipOnTieredCompilation(true)) {
            return;
        }
        CompilerWhiteBoxTest.main(NonTieredLevelsTest::new, args);
    }

    private NonTieredLevelsTest(TestCase testCase) {
        super(testCase);
        WHITE_BOX.testSetDontInlineMethod(method, true);
    }

    @Override
    protected void test() throws Exception {
        if (skipXcompOSR()) {
            return;
        }
        checkNotCompiled();
        compile();
        checkCompiled();
        int compLevel = getCompLevel();
        checkLevel(AVAILABLE_COMP_LEVEL, compLevel);
        int bci = WHITE_BOX.getMethodEntryBci(method);
        deoptimize();
        if (!testCase.isOsr()) {
            for (int level = 1; level <= COMP_LEVEL_MAX; ++level) {
                if (IS_AVAILABLE_COMPLEVEL.test(level)) {
                    testAvailableLevel(level, bci);
                } else {
                    testUnavailableLevel(level, bci);
                }
            }
        } else {
            System.out.println("skip other levels testing in OSR");
            testAvailableLevel(AVAILABLE_COMP_LEVEL, bci);
        }
    }
}
