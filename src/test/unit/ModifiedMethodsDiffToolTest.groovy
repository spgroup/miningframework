package unit

import org.junit.Test
import services.dataCollectors.modifiedLinesCollector.ModifiedMethod
import services.dataCollectors.modifiedLinesCollector.ModifiedMethodsHelper

import static org.junit.Assert.assertEquals

/**
 * The methods (and fields and initializer blocks) that ModifiedMethodsHelper finds changed from a base to a target
 * Java file, with the default method diff tool (MethodDiffTools), and the type and the number of their modified lines
 * as the textual diff (diff) gives them. The first tests are changes diffj already reports; the ones after
 * "diffj misses" are changes found in the miningframework experiments that diffj does not report.
 */
class ModifiedMethodsDiffToolTest {

    // signature -> "type number" of each modified line
    private static Map<String, List<String>> modifiedMethods(String base, String target) {
        File folder = File.createTempDir()
        File baseFile = new File(folder, 'base/C.java')
        File targetFile = new File(folder, 'target/C.java')
        baseFile.parentFile.mkdirs()
        targetFile.parentFile.mkdirs()
        baseFile.text = base
        targetFile.text = target

        Set<ModifiedMethod> methods = new ModifiedMethodsHelper().getModifiedMethods(baseFile, targetFile)
        folder.deleteDir()
        return methods.collectEntries { method ->
            [(method.getSignature()): method.getModifiedLines().collect { "${it.getType()} ${it.getNumber()}".toString() }.sort()]
        }
    }

    @Test
    void changedStatement() {
        String base = '''\
public class C {
    void m() {
        int x = 0;
    }
}
'''
        assertEquals(['m()': ['Changed 3']], modifiedMethods(base, base.replace('x = 0', 'x = 1')))
    }

    @Test
    void addedStatement() {
        String base = '''\
public class C {
    void m() {
        int x = 0;
    }
}
'''
        String target = '''\
public class C {
    void m() {
        int x = 0;
        x++;
    }
}
'''
        assertEquals(['m()': ['Added 4']], modifiedMethods(base, target))
    }

    @Test
    void constructorAndMethodSignatures() {
        // varargs as its type, generics without spaces, no final: as diffj names them
        String base = '''\
import java.util.*;

public class C {
    public C(String s, int... xs) {
        System.out.println(0);
    }

    <T> List<T> gen(Map<String, List<T>> m, String[] a, final int x) {
        return new ArrayList<T>(0);
    }
}
'''
        assertEquals(['C(String, int)': ['Changed 5'], 'gen(Map<String,List<T>>, String[], int)': ['Changed 9']],
                modifiedMethods(base, base.replace('(0)', '(1)')))
    }

    @Test
    void fieldsAndStaticBlock() {
        String base = '''\
public class C {
    static final int[] TABLE = {1, 2, 3};
    int a = 0, b = 2;

    static {
        System.out.println(0);
    }
}
'''
        String target = base.replace('{1, 2, 3}', '{1, 2, 4}').replace('a = 0', 'a = 1').replace('println(0)', 'println(1)')
        assertEquals(['TABLE': ['Changed 2'], 'a': ['Changed 3'], 'static block': ['Changed 6']], modifiedMethods(base, target))
    }

    @Test
    void anonymousAndInnerClasses() {
        // a change in an anonymous class belongs to the method that creates it; one in an inner class, to its method
        String base = '''\
public class C {
    Runnable anon() {
        return new Runnable() {
            public void run() {
                System.out.println(0);
            }
        };
    }

    class Inner {
        void inner(java.util.List<String> q) {
            System.out.println(0);
        }
    }
}
'''
        assertEquals(['anon()': ['Changed 5'], 'inner(java.util.List<String>)': ['Changed 12']],
                modifiedMethods(base, base.replace('println(0)', 'println(1)')))
    }

    @Test
    void changedSignatureKeepsTheAncestorName() {
        // a method keeps the name it has in the base, so that left, right and merge name it alike (Lang 4589bbf2)
        String base = '''\
public class C {
    void m(int x) {
        System.out.println(0);
    }
}
'''
        String target = '''\
public class C {
    void m(int x, int y) {
        System.out.println(1);
    }
}
'''
        assertEquals(['m(int)'] as Set, modifiedMethods(base, target).keySet())
    }

    @Test
    void reorderedStatements() {
        // the fix moves a statement after another one: diff removes and adds it back (Closure aa15a401)
        String base = '''\
public class C {
    void m() {
        a();
        b();
    }

    void a() {
    }

    void b() {
    }
}
'''
        String target = base.replace('        a();\n        b();\n', '        b();\n        a();\n')
        assertEquals(['m()'] as Set, modifiedMethods(base, target).keySet())
    }

    @Test
    void onlyCommentChanged() {
        String base = '''\
public class C {
    void m() {
        int x = 0; // zero
    }
}
'''
        assertEquals([:], modifiedMethods(base, base.replace('// zero', '// nothing')))
    }

    // diffj misses

    @Test
    void thisArgumentChanged() {
        // diffj does not compare this(...) (a Major mutant in Codec da622fd9)
        String base = '''\
public class C {
    C(int x) {
    }

    C() {
        this(0);
    }
}
'''
        assertEquals(['C()': ['Changed 6']], modifiedMethods(base, base.replace('this(0)', 'this(1)')))
    }

    @Test
    void superArgumentChanged() {
        String base = '''\
public class C extends Exception {
    C() {
        super("a");
    }
}
'''
        assertEquals(['C()': ['Changed 3']], modifiedMethods(base, base.replace('"a"', '"b"')))
    }

    @Test
    void removedStatement() {
        // diff names the removal 3d2 and diffj 3d3, so the line was lost (Jsoup b1856400)
        String base = '''\
public class C {
    void m(String s) {
        check(s);
        use(s);
    }

    void check(String s) {
    }

    void use(String s) {
    }
}
'''
        assertEquals(['m(String)': ['Removed 2']], modifiedMethods(base, base.replace('        check(s);\n', '')))
    }

    @Test
    void removedArrayElement() {
        // diffj takes the change for a removal at the next line, so the line was lost (Codec da622fd9)
        String base = '''\
public class C {
    static final int[] TABLE = {
        1, 2, 3,
    };
}
'''
        assertEquals(['TABLE': ['Changed 3']], modifiedMethods(base, base.replace('1, 2, 3,', '1, 2,          // 1-2')))
    }

    @Test
    void annotatedConstructorParameter() {
        // diffj fails on the annotated parameter and skips the inner class (the Closure scenarios)
        String base = '''\
public class C {
    public C(@Deprecated String s) {
    }

    class Inner {
        void m() {
            System.out.println(0);
        }
    }
}
'''
        assertEquals(['m()': ['Changed 7']], modifiedMethods(base, base.replace('println(0)', 'println(1)')))
    }
}
