package io.github.openhelios.recipe.testjar;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;

public class UpdateTestJarDependencyRecipe extends Recipe {

    @Override
    public String getDisplayName() {
        return "UpdateTestJarDependency";
    }

    @Override
    public String getDescription() {
        return "Migrate class2dependencies with test-jar type to test module";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new UpdateTestJarDependencyVisitor();
    }
}
