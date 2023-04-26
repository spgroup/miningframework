package org.springframework.boot.gradle.junit;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.springframework.boot.gradle.testkit.GradleBuild;
import org.springframework.boot.gradle.testkit.GradleBuildExtension;

public final class GradleCompatibilityExtension implements TestTemplateInvocationContextProvider {

    private static final List<String> GRADLE_VERSIONS = Arrays.asList("default", "5.0", "5.1.1", "5.2.1", "5.3.1", "5.4.1", "5.5.1", "5.6.4", "6.0.1", "6.1.1", "6.2.2", "6.3", "6.4");

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
