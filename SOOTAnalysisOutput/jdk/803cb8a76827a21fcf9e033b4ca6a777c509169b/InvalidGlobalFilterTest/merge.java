import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.util.Map;

@Test
public class InvalidGlobalFilterTest {

    private static final String serialPropName = "jdk.serialFilter";

    private static final String serialFilter = System.getProperty(serialPropName);

    static {
        System.setProperty("java.util.logging.config.file", System.getProperty("test.src", ".") + "/logging.properties");
    }

    private static final Map<String, String> invalidMessages = Map.of(".*", "Invalid jdk.serialFilter: package missing in: \".*\"", ".**", "Invalid jdk.serialFilter: package missing in: \".**\"", "!", "Invalid jdk.serialFilter: class or package missing in: \"!\"", "/java.util.Hashtable", "Invalid jdk.serialFilter: module name is missing in: \"/java.util.Hashtable\"", "java.base/", "Invalid jdk.serialFilter: class or package missing in: \"java.base/\"", "/", "Invalid jdk.serialFilter: module name is missing in: \"/\"");

    @DataProvider(name = "MethodsToCall")
    private Object[][] cases() {
        return new Object[][] { { serialFilter, "getSerialFilter", (Assert.ThrowingRunnable) () -> ObjectInputFilter.Config.getSerialFilter() }, { serialFilter, "setSerialFilter", (Assert.ThrowingRunnable) () -> ObjectInputFilter.Config.setSerialFilter(new NoopFilter()) }, { serialFilter, "new ObjectInputStream(is)", (Assert.ThrowingRunnable) () -> new ObjectInputStream(new ByteArrayInputStream(new byte[0])) }, { serialFilter, "new OISSubclass()", (Assert.ThrowingRunnable) () -> new OISSubclass() } };
    }

    @Test(dataProvider = "MethodsToCall")
    public void initFaultTest(String pattern, String method, Assert.ThrowingRunnable runnable) {
        IllegalStateException ex = Assert.expectThrows(IllegalStateException.class, runnable);
        String expected = invalidMessages.get(serialFilter);
        if (expected == null) {
            Assert.fail("No expected message for filter: " + serialFilter);
        }
        System.out.println(ex.getMessage());
        Assert.assertEquals(ex.getMessage(), expected, "wrong message");
    }

    private static class NoopFilter implements ObjectInputFilter {

        public ObjectInputFilter.Status checkInput(FilterInfo filter) {
            return ObjectInputFilter.Status.UNDECIDED;
        }

        public String toString() {
            return "NoopFilter";
        }
    }

    private static class OISSubclass extends ObjectInputStream {

        protected OISSubclass() throws IOException {
        }
    }
}
