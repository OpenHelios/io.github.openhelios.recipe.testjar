package io.github.openhelios.recipe.testjar.module;

import org.openrewrite.ExecutionContext;
import org.openrewrite.maven.MavenIsoVisitor;
import org.openrewrite.xml.AddToTagVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.List;
import java.util.Objects;
import java.util.Set;

class ParentPomVisitor extends MavenIsoVisitor<ExecutionContext> {

    private final Set<TestJarPom> poms;

    public ParentPomVisitor(Set<TestJarPom> poms) {
        this.poms = poms;
    }

    @Override
    public Xml.Document visitDocument(Xml.Document document, ExecutionContext ctx) {
        Xml.Tag root = document.getRoot();
        Xml.Tag modules = root.getChild("modules").orElse(null);
        if (null == modules) {
            return document;
        }
        List<String> existing = modules.getChildren().stream()
                .map(t -> t.getValue().orElse(null))
                .filter(Objects::nonNull)
                .toList();
        for (TestJarPom module : poms) {
            String artifactId = module.resolution()
                    .getPom()
                    .getGav()
                    .getArtifactId();
            if (!existing.contains(artifactId)) {
                continue;
            }
            String newModule = artifactId + "-test";
            if (!existing.contains(newModule)) {
                Xml.Tag newModuleTag =
                        Xml.Tag.build(" ".repeat(8) + "<module>" + newModule + "</module>");
                doAfterVisit(new AddToTagVisitor<>(modules, newModuleTag));
            }
        }
        return document;
    }
}
