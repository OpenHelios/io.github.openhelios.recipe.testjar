package io.github.openhelios.recipe.testjar;

import io.github.openhelios.recipe.testjar.module.CreateTestPomsRecipe;
import io.github.openhelios.recipe.testjar.move.MoveCuRecipe;
import org.openrewrite.Recipe;

import java.util.List;

public class MoveTestJarIntoNewModule extends Recipe {

    @Override
    public String getDisplayName() {
        return "MoveTestJarIntoNewModule";
    }

    @Override
    public String getDescription() {
        return "Move all needed Java files from a module generating a test-jar into a new module.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return List.of(
                new CreateTestPomsRecipe(),
                new MoveCuRecipe(),
                new UpdateTestJarDependencyRecipe(),
                new RemoveTestJarPluginRecipe()
        );
    }

}
