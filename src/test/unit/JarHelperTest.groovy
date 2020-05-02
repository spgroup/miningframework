package unit

import org.junit.BeforeClass
import util.JarHelper

import org.junit.Test
import org.junit.Assert

class JarHelperTest {

    private static File jarFile = null

    @BeforeClass
    static void setup () {
        jarFile = new File("./src/test/assets/test.jar")
    }

    @Test
    void withExistingClass () {
        boolean result = JarHelper.classExistsInJarFile(jarFile, "app.MiningFramework")

        Assert.assertTrue(result)
    }

    @Test
    void withClassThatDoesNotExist() {
        boolean result = JarHelper.classExistsInJarFile(jarFile, "app.MiningFramework")

        Assert.assertTrue(result)
    }

}
