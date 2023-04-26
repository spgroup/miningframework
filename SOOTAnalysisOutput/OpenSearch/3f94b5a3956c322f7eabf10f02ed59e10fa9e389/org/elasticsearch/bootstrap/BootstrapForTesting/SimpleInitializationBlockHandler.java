package org.elasticsearch.bootstrap;

import org.apache.lucene.util.TestSecurityManager;
import org.elasticsearch.bootstrap.Bootstrap;
import org.elasticsearch.bootstrap.ESPolicy;
import org.elasticsearch.bootstrap.Security;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.PathUtils;
import org.elasticsearch.common.logging.Loggers;
import java.io.FilePermission;
import java.net.URL;
import java.nio.file.Path;
import java.security.Permissions;
import java.security.Policy;
import java.util.Map;
import java.util.Objects;
import static com.carrotsearch.randomizedtesting.RandomizedTest.systemPropertyAsBoolean;

public class BootstrapForTesting {

    static {
        Bootstrap.initializeNatives(true, true);
        Bootstrap.initializeProbes();
        try {
            JarHell.checkJarHell();
        } catch (Exception e) {
                throw new RuntimeException("found jar hell in test classpath", e);
        }
        Path javaTmpDir = PathUtils.get(Objects.requireNonNull(System.getProperty("java.io.tmpdir"), "please set ${java.io.tmpdir} in pom.xml"));
        try {
            Security.ensureDirectoryExists(javaTmpDir);
        } catch (Exception e) {
            throw new RuntimeException("unable to create test temp directory", e);
        }
        if (systemPropertyAsBoolean("tests.security.manager", true)) {
            try {
                Security.setCodebaseProperties();
                Permissions perms = new Permissions();
                for (URL url : JarHell.parseClassPath()) {
                    Path path = PathUtils.get(url.toURI());
                    perms.add(new FilePermission(path.toString(), "read,readlink"));
                    perms.add(new FilePermission(path.toString() + path.getFileSystem().getSeparator() + "-", "read,readlink"));
                    String filename = path.getFileName().toString();
                    if (filename.contains("jython") && filename.endsWith(".jar")) {
                        perms.add(new FilePermission(path.getParent().toString(), "read,readlink"));
                        perms.add(new FilePermission(path.getParent().resolve("Lib").toString(), "read,readlink"));
                    }
                }
                Security.addPath(perms, "java.io.tmpdir", javaTmpDir, "read,readlink,write,delete");
                if (Strings.hasLength(System.getProperty("tests.config"))) {
                    perms.add(new FilePermission(System.getProperty("tests.config"), "read,readlink"));
                }
                if (Boolean.getBoolean("tests.coverage")) {
                    Path coverageDir = PathUtils.get(System.getProperty("tests.coverage.dir"));
                    perms.add(new FilePermission(coverageDir.resolve("jacoco.exec").toString(), "read,write"));
                    perms.add(new FilePermission(coverageDir.resolve("jacoco-it.exec").toString(), "read,write"));
                }
                if (System.getProperty("tests.maven") == null) {
                    perms.add(new RuntimePermission("setIO"));
                }
                final Policy policy;
                String artifact = System.getProperty("tests.artifact");
                if (artifact == null && System.getProperty("tests.maven") == null) {
                    for (Map.Entry<String, String> kv : Security.SPECIAL_PLUGINS.entrySet()) {
                        String resource = kv.getValue().replace('.', '/') + ".class";
                        if (BootstrapForTesting.class.getClassLoader().getResource(resource) != null) {
                            artifact = kv.getKey();
                            break;
                        }
                    }
                }
                String pluginProp = Security.getPluginProperty(artifact);
                if (pluginProp != null) {
                    policy = new MockPluginPolicy(perms, pluginProp);
                } else {
                    policy = new ESPolicy(perms);
                }
                Policy.setPolicy(policy);
                System.setSecurityManager(new TestSecurityManager());
                Security.selfTest();
                if (pluginProp != null) {
                    String clazz = Security.getPluginClass(artifact);
                    Class.forName(clazz);
                }
            } catch (Exception e) {
                throw new RuntimeException("unable to install test security manager", e);
            }
        }
    }

    public static void ensureInitialized() {
    }
}