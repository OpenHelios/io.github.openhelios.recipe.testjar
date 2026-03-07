package io.github.openhelios.recipe.testjar.module;

import org.openrewrite.maven.tree.MavenResolutionResult;

import java.nio.file.Path;

public record TestJarPom(
        MavenResolutionResult resolution,
        Path path
) {
    @Override
    public int hashCode() {
        return resolution.getPom().getGav().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TestJarPom other //
                && resolution.getPom().getGav().equals(other.resolution.getPom().getGav());
    }
}
