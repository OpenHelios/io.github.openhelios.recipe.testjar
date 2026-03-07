package io.github.openhelios.recipe.testjar;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;

public class RemoveTestJarPluginRecipe extends Recipe {
    @Override
    public String getDisplayName() {
        return "RemoveTestJarPlugin";
    }

    @Override
    public String getDescription() {
        return "Removes plugin for generating test-jar library";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new RemoveTestJarPluginVisitor();
    }
}
