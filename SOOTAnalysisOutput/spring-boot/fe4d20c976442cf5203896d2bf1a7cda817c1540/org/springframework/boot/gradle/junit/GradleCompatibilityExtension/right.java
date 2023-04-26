package org.springframework.boot.gradle.junit;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.gradle.api.JavaVersion;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.springframework.boot.gradle.testkit.GradleBuild;
import org.springframework.boot.gradle.testkit.GradleBuildExtension;

public final class GradleCompatibilityExtension implements TestTemplateInvocationContextProvider {

    private static final List<String> GRADLE_VERSIONS;

    static {
        JavaVersion javaVersion = JavaVersion.current();
        if (javaVersion.isCompatibleWith(JavaVersion.VERSION_14) || javaVersion.isCompatibleWith(JavaVersion.VERSION_13)) {
            GRADLE_VERSIONS = Arrays.asList("6.3", "6.4.1", "6.5.1", "6.6.1", "6.7.1", "6.8.3", "default");
        } else {
            GRADLE_VERSIONS = Arrays.asList("5.6.4", "6.3", "6.4.1", "6.5.1", "6.6.1", "6.7.1", "6.8.3", "default");
        }
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        return GRADLE_VERSIONS.stream().map(GradleVersionTestTemplateInvocationContext::new);
    }

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return true;
    }

    private static final class GradleVersionTestTemplateInvocationContext implements TestTemplateInvocationContext {

        private final String gradleVersion;

        GradleVersionTestTemplateInvocationContext(String gradleVersion) {
            this.gradleVersion = gradleVersion;
        }

        @Override
        public String getDisplayName(int invocationIndex) {
            return "Gradle " + this.gradleVersion;
        }

        @Override
        public List<Extension> getAdditionalExtensions() {
            GradleBuild gradleBuild = new GradleBuild();
            if (!this.gradleVersion.equals("default")) {
                gradleBuild.gradleVersion(this.gradleVersion);
            }
            return Arrays.asList(new GradleBuildFieldSetter(gradleBuild), new GradleBuildExtension());
        }
    }
}
