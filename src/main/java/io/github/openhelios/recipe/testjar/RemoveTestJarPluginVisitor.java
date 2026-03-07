package io.github.openhelios.recipe.testjar;

import io.github.openhelios.recipe.testjar.util.TestJarUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.maven.MavenIsoVisitor;
import org.openrewrite.xml.RemoveContentVisitor;
import org.openrewrite.xml.tree.Xml;

public class RemoveTestJarPluginVisitor extends MavenIsoVisitor<ExecutionContext> {

    @Override
    public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext ctx) {
        if (!"plugin".equals(tag.getName())) {
            return super.visitTag(tag, ctx);
        }
        var artifactIdTag = tag.getChild("artifactId")
                .orElse(null);
        if (null == artifactIdTag ||
                !TestJarUtil.MAVEN_JAR_PLUGIN.equals(artifactIdTag.getValue().orElse(null))) {
            return tag;
        }
        var executionsTag = tag.getChild("executions")
                .orElse(null);
        if (null == executionsTag) {
            return tag;
        }
        var executions = executionsTag.getChildren();
        if (executions.isEmpty()) {
            return tag;
        }
        var countExecutions = 0;
        for (var execution : executions) {
            var goalsTag = execution.getChild("goals");
            if (goalsTag.isPresent()) {
                var goals = goalsTag.get().getChildren();
                var countGoals = 0;
                for (var goal : goals) {
                    if ("test-jar".equals(goal.getValue().orElse(null))) {
                        doAfterVisit(new RemoveContentVisitor<>(goal, true, true));
                        countGoals++;
                    }
                }
                if (countGoals == goals.size()) {
                    doAfterVisit(new RemoveContentVisitor<>(execution, true, true));
                    countExecutions++;
                }
            }
        }
        if (countExecutions == executions.size()) {
            doAfterVisit(new RemoveContentVisitor<>(tag, true, true));
        }
        return tag;
    }

}
