package io.github.openhelios.recipe.testjar.module;

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.SourceFile;
import org.openrewrite.TreeVisitor;
import org.openrewrite.xml.XmlParser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CreateTestPomsRecipe extends ScanningRecipe<Set<TestJarPom>> {

    @Override
    public String getDisplayName() {
        return "CreateTestModuleRecipe";
    }

    @Override
    public String getDescription() {
        return "Create -test module for test-jar and add it to parent module.";
    }

    @Override
    public Set<TestJarPom> getInitialValue(ExecutionContext ctx) {
        return new HashSet<>();
    }

    /**
     * 1. phase: Collect test jar POMs.
     *
     * @param poms The test jar POMs.
     * @return The scanner visitor.
     */
    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(Set<TestJarPom> poms) {
        return new ScanPomVisitor(poms);
    }

    /**
     * 2. phase: Generate poms.
     *
     * @param poms The test jar POMs.
     * @param ctx  The context.
     * @return The created pom files.
     */
    @Override
    public List<SourceFile> generate(Set<TestJarPom> poms, ExecutionContext ctx) {
        XmlParser parser = new XmlParser();
        List<SourceFile> generated = new ArrayList<>();
        for (TestJarPom module : poms) {
            var pom = module.resolution().getPom();
            var gav = pom.getGav();
            String newArtifactId = gav.getArtifactId() + "-test";
            var parent = pom.getRequested().getParent();
            if (null == parent) {
                throw new IllegalStateException("expected parent for " + gav);
            }
            String relativePath = "";
            if (null != parent.getRelativePath()) {
                relativePath = "\n    <relativePath>%s</relativePath>".formatted(
                        parent.getRelativePath());
            }
            String parentSection = """
                      <parent>
                        <groupId>%s</groupId>
                        <artifactId>%s</artifactId>
                        <version>%s</version>%s
                      </parent>
                    """
                    .formatted(
                            parent.getGroupId(),
                            parent.getArtifactId(),
                            parent.getVersion(),
                            relativePath
                    );
            StringBuilder dependenciesSection = new StringBuilder();
            dependenciesSection.append("""

                        <dependency>
                          <groupId>%s</groupId>
                          <artifactId>%s</artifactId>
                          <version>%s</version>
                        </dependency>
                    """
                    .formatted(
                            gav.getGroupId(),
                            gav.getArtifactId(),
                            gav.getVersion()
                    )
            );
            for (var dependency : pom.getRequestedDependencies()) {
                if ("test".equals(dependency.getScope()) || "provided".equals(dependency.getScope())) {
                    dependenciesSection.append("""
                                <dependency>
                                  <groupId>%s</groupId>
                                  <artifactId>%s</artifactId>%s%s%s
                                </dependency>
                            """
                            .formatted(
                                    dependency.getGroupId(),
                                    dependency.getArtifactId(),
                                    tagOrEmpty("version", dependency.getVersion()),
                                    tagOrEmpty("classifier", dependency.getClassifier()),
                                    tagOrEmpty("scope", "test".equals(dependency.getScope()) ? null : dependency.getScope())
                            )
                    );
                }
            }
            String pomContent = """
                    <project xmlns="http://maven.apache.org/POM/4.0.0"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                        https://maven.apache.org/xsd/maven-4.0.0.xsd">
                      <modelVersion>4.0.0</modelVersion>%s
                      <artifactId>%s</artifactId>
                      <dependencies>%s
                      </dependencies>
                    </project>
                    """
                    .formatted(
                            parentSection,
                            newArtifactId,
                            dependenciesSection.toString()
                    );
            Path newPomPath = module.path()
                    .getParent()
                    .resolveSibling(newArtifactId)
                    .resolve("pom.xml");
            var content = parser.parse(pomContent).findFirst().orElseThrow();
            generated.add(content.withSourcePath(newPomPath));
        }
        return generated;
    }

    private static String tagOrEmpty(String tagName, @Nullable String value) {
        if (null == value) {
            return "";
        }
        return "\n%s<%s>%s</%s>".formatted(" ".repeat(6), tagName, value, tagName);
    }

    /**
     * 3. phase: update parent with new poms
     *
     * @param poms The test jar POMs.
     * @return The Maven visitor.
     */
    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor(Set<TestJarPom> poms) {
        return new ParentPomVisitor(poms);
    }

}
