package io.github.openhelios.recipe.testjar.move;

import org.openrewrite.ExecutionContext;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.SourceFile;
import org.openrewrite.TreeVisitor;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class MoveCuRecipe extends ScanningRecipe<MoveCuData> {

    @Override
    public String getDisplayName() {
        return "CreateRecipe";
    }

    @Override
    public String getDescription() {
        return "Move non-test classes and test classes using them from src/test/java into -test module";
    }

    @Override
    public MoveCuData getInitialValue(ExecutionContext ctx) {
        return new MoveCuData();
    }

    /**
     * 1. phase: Fill {@link MoveCuData} except {@link MoveCuData#moveClasses()}.
     *
     * @param data The move data.
     * @return The scan visitor.
     */
    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(MoveCuData data) {
        return new ScanCuVisitor(data);
    }

    /**
     * 2. phase: Fill {@link MoveCuData#moveClasses()}.
     *
     * @param data The move data.
     * @param ctx The execution context.
     * @return Not used - is always an empty set.
     */
    @Override
    public Collection<? extends SourceFile> generate(MoveCuData data, ExecutionContext ctx) {
        var moveClasses = data.moveClasses();
        moveClasses.addAll(data.initialCandidates());
        boolean changed = true;
        while (changed) {
            changed = false;
            for (String clazz : data.allTestClasses()) {
                if (!moveClasses.contains(clazz)) {
                    Set<String> deps = data.class2dependencies().getOrDefault(clazz,
                            Collections.emptySet());
                    for (String dep : deps) {
                        if (moveClasses.contains(dep)) {
                            moveClasses.add(clazz);
                            changed = true;
                            break;
                        }
                    }
                }
            }
        }
        return Collections.emptySet();
    }

    // 3. phase: Move {@link MoveData#moveClasses} compilation units.
    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor(MoveCuData data) {
        return new MoveCuVisitor(data);
    }

}
