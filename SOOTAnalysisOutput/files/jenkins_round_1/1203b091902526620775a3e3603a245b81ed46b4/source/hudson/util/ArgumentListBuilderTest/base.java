/*
 * The MIT License
 * 
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., Kohsuke Kawaguchi, Yahoo! Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class ArgumentListBuilderTest extends Assert {

    public static void assertArrayEquals(String msg, boolean[] expected, boolean[] actual) {
        assertArrayEquals(msg,box(expected),box(actual));
    }

    private static Boolean[] box(boolean[] a) {
        if(a==null)     return null;
        Boolean[] r = new Boolean[a.length];
        for (int i = 0; i < a.length; i++)
            r[i] = a[i];
        return r;
    }


    @Test
    public void assertEmptyMask() {
        ArgumentListBuilder builder = new ArgumentListBuilder();
        builder.add("arg");
        builder.add("other", "arguments");

        assertFalse("There shouldnt be any masked arguments", builder.hasMaskedArguments());
        boolean[] array = builder.toMaskArray();
        assertNotNull("The mask array should not be null", array);
        assertArrayEquals("The mask array was incorrect", new boolean[]{false,false,false}, array);
    }

    @Test
    public void assertLastArgumentIsMasked() {
        ArgumentListBuilder builder = new ArgumentListBuilder();
        builder.add("arg");
        builder.addMasked("ismasked");

        assertTrue("There should be masked arguments", builder.hasMaskedArguments());
        boolean[] array = builder.toMaskArray();
        assertNotNull("The mask array should not be null", array);
        assertArrayEquals("The mask array was incorrect", new boolean[]{false,true}, array);
    }

    @Test
    public void assertSeveralMaskedArguments() {
        ArgumentListBuilder builder = new ArgumentListBuilder();
        builder.add("arg");
        builder.addMasked("ismasked");
        builder.add("non masked arg");
        builder.addMasked("ismasked2");

        assertTrue("There should be masked arguments", builder.hasMaskedArguments());
        boolean[] array = builder.toMaskArray();
        assertNotNull("The mask array should not be null", array);
        assertArrayEquals("The mask array was incorrect", new boolean[]{false,true, false, true}, array);
    }

    @Test
    public void assertPrependAfterAddingMasked() {
        ArgumentListBuilder builder = new ArgumentListBuilder();
        builder.addMasked("ismasked");
        builder.add("arg");
        builder.prepend("first", "second");

        assertTrue("There should be masked arguments", builder.hasMaskedArguments());
        boolean[] array = builder.toMaskArray();
        assertNotNull("The mask array should not be null", array);
        assertArrayEquals("The mask array was incorrect", new boolean[]{false,false,true,false}, array);
    }

    @Test
    public void assertPrependBeforeAddingMasked() {
        ArgumentListBuilder builder = new ArgumentListBuilder();
        builder.prepend("first", "second");
        builder.addMasked("ismasked");
        builder.add("arg");

        assertTrue("There should be masked arguments", builder.hasMaskedArguments());
        boolean[] array = builder.toMaskArray();
        assertNotNull("The mask array should not be null", array);
        assertArrayEquals("The mask array was incorrect", new boolean[]{false,false,true,false}, array);
    }

    @Test
    public void testToWindowsCommand() {
        ArgumentListBuilder builder = new ArgumentListBuilder(
                "ant.bat", "-Dfoo1=abc",  // nothing special, no quotes
                "-Dfoo2=foo bar", "-Dfoo3=/u*r", "-Dfoo4=/us?",  // add quotes
                                                 "-Dfoo10=bar,baz",
                "-Dfoo5=foo;bar^baz", "-Dfoo6=<xml>&here;</xml>", // add quotes
                "-Dfoo7=foo|bar\"baz", // add quotes and "" for "
                "-Dfoo8=% %QED% %comspec% %-%(%.%", // add quotes, and extra quotes for %Q and %c
                "-Dfoo9=%'''%%@%"); // no quotes as none of the % are followed by a letter
        // By default, does not escape %VAR%
        assertArrayEquals(new String[] { "cmd.exe", "/C",
                "\"ant.bat -Dfoo1=abc \"-Dfoo2=foo bar\""
                + " \"-Dfoo3=/u*r\" \"-Dfoo4=/us?\" \"-Dfoo10=bar,baz\" \"-Dfoo5=foo;bar^baz\""
                + " \"-Dfoo6=<xml>&here;</xml>\" \"-Dfoo7=foo|bar\"\"baz\""
                + " \"-Dfoo8=% %QED% %comspec% %-%(%.%\""
                + " -Dfoo9=%'''%%@% && exit %%ERRORLEVEL%%\"" },
                builder.toWindowsCommand().toCommandArray());
        // Pass flag to escape %VAR%
        assertArrayEquals(new String[] { "cmd.exe", "/C",
                "\"ant.bat -Dfoo1=abc \"-Dfoo2=foo bar\""
                + " \"-Dfoo3=/u*r\" \"-Dfoo4=/us?\" \"-Dfoo10=bar,baz\" \"-Dfoo5=foo;bar^baz\""
                + " \"-Dfoo6=<xml>&here;</xml>\" \"-Dfoo7=foo|bar\"\"baz\""
                + " \"-Dfoo8=% %\"Q\"ED% %\"c\"omspec% %-%(%.%\""
                + " -Dfoo9=%'''%%@% && exit %%ERRORLEVEL%%\"" },
                builder.toWindowsCommand(true).toCommandArray());
    }

    @Test
    public void assertMaskOnClone() {
        ArgumentListBuilder builder = new ArgumentListBuilder();
        builder.add("arg1");
        builder.addMasked("masked1");
        builder.add("arg2");

        ArgumentListBuilder clone = builder.clone();
        assertTrue("There should be masked arguments", clone.hasMaskedArguments());
        boolean[] array = clone.toMaskArray();
        assertNotNull("The mask array should not be null", array);
        assertArrayEquals("The mask array was incorrect", builder.toMaskArray(), array);
    }
    
    private static final Map<String, String> KEY_VALUES = new HashMap<String, String>() {{
        put("key1", "value1");
        put("key2", "value2");
        put("key3", "value3");
    }};

    private static final Set<String> MASKS = new HashSet<String>() {{
        add("key2");
    }};
    
    @Test
    public void assertKeyValuePairsWithMask() {
        ArgumentListBuilder builder = new ArgumentListBuilder();
        builder.addKeyValuePairs(null, KEY_VALUES, MASKS);

        assertTrue("There should be masked arguments", builder.hasMaskedArguments());
        boolean[] array = builder.toMaskArray();
        assertNotNull("The mask array should not be null", array);
        assertArrayEquals("The mask array was incorrect", new boolean[]{false,true,false}, array);

    }

    @Test
    public void assertKeyValuePairs() {
        ArgumentListBuilder builder = new ArgumentListBuilder();
        builder.addKeyValuePairs(null, KEY_VALUES);

        assertFalse("There shouldnt be any masked arguments", builder.hasMaskedArguments());
        boolean[] array = builder.toMaskArray();
        assertNotNull("The mask array should not be null", array);
        assertArrayEquals("The mask array was incorrect", new boolean[]{false,false,false}, array);
    }
}
