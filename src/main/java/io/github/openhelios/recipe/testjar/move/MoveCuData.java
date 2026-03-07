package io.github.openhelios.recipe.testjar.move;

import org.openrewrite.java.tree.J;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 1. phase: allTestClasses, initialCandidates, class2dependencies, class2cu</br>
 * 2. phase: moveClasses
 * @param allTestClasses All classes stored in src/test/java folder of module.
 * @param initialCandidates All classes stored in src/test/java folder of module, which ends with Test or TI.
 * @param class2dependencies A class stored in src/test/java folder of module to direct dependency classes map.
 * @param class2cu A class stored in src/test/java folder of module to compilation unit map.
 * @param moveClasses All classes stored in src/test/java folder of module, which will be moved to -test module.
 */
public record MoveCuData(
        Set<String> allTestClasses,
        Set<String> initialCandidates,
        Map<String, Set<String>> class2dependencies,
        Map<String, J.CompilationUnit> class2cu,
        Set<String> moveClasses) {
    public MoveCuData() {
        this(
                new HashSet<>(),
                new HashSet<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashSet<>()
        );
    }
}

