package io.github.openhelios.recipe.testjar.module;

import io.github.openhelios.recipe.testjar.util.TestJarUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.maven.MavenIsoVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.Set;

class ScanPomVisitor extends MavenIsoVisitor<ExecutionContext> {

    private final Set<TestJarPom> poms;

    public ScanPomVisitor(Set<TestJarPom> poms) {
        this.poms = poms;
    }

    @Override
    public Xml.Document visitDocument(Xml.Document document, ExecutionContext ctx) {
        var resolution = getResolutionResult();
        var pom = resolution.getPom();
        if ("pom".equals(pom.getPackaging())) {
            return super.visitDocument(document, ctx);
        }
        if (TestJarUtil.generatesTestJar(resolution.getPom())) {
            poms.add(new TestJarPom(resolution, document.getSourcePath()));
        }
        return document;
    }
}
