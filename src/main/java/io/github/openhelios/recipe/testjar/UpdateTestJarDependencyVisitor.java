package io.github.openhelios.recipe.testjar;

import org.openrewrite.ExecutionContext;
import org.openrewrite.maven.MavenIsoVisitor;
import org.openrewrite.xml.RemoveContentVisitor;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.ArrayList;
import java.util.List;

public class UpdateTestJarDependencyVisitor extends MavenIsoVisitor<ExecutionContext> {

    @Override
    public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext ctx) {
        if (!"dependency".equals(tag.getName())) {
            return super.visitTag(tag, ctx);
        }
        var typeTag = tag.getChild("type").orElse(null);
        if (null == typeTag) {
            return tag;
        }
        String typeValue = typeTag.getValue().orElse(null);
        if (!"test-jar".equals(typeValue)) {
            return tag;
        }
        doAfterVisit(new RemoveContentVisitor<>(typeTag, false, true));
        var contends = tag.getContent();
        if (null == contends) {
            throw new IllegalStateException("expected tag dependency with content: " + tag);
        }
        List<Content> newContents = new ArrayList<>(contends);
        var artifactId = tag.getChild("artifactId").orElseThrow();
        String artifactIdValue = artifactId.getValue().orElseThrow();
        if (!artifactIdValue.endsWith("-test")) {
            var newArtifactId =
                    artifactId.withValue(artifactIdValue + "-test");
            replaceChild(newContents, artifactId, newArtifactId);
        }
        return tag.withContent(newContents);
    }

    private static void replaceChild(List<Content> content,
                                     Xml.Tag oldTag,
                                     Xml.Tag newTag) {
        for (int i = 0; i < content.size(); i++) {
            if (content.get(i) == oldTag) {
                content.set(i, newTag);
                return;
            }
        }
    }
}
