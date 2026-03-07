package io.github.openhelios.recipe.testjar.move;

import io.github.openhelios.recipe.testjar.util.TestJarUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import java.nio.file.Path;

class MoveCuVisitor extends JavaIsoVisitor<ExecutionContext> {

    private final MoveCuData data;

    public MoveCuVisitor(MoveCuData data) {
        this.data = data;
    }

    @Override
    public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, ExecutionContext ctx) {
        if (!cu.getSourcePath().toString().contains("src/test/java")) {
            return cu;
        }
        var classes = cu.getClasses();
        if (classes.isEmpty()) {
            return cu;
        }
        var clazz = classes.getFirst().getType();
        if (null == clazz) {
            return cu;
        }
        final String typeName = clazz.getFullyQualifiedName();
        if (!data.moveClasses().contains(typeName)) {
            return cu;
        }
        final Path originalPath = cu.getSourcePath();
        final Path moduleRoot = findModuleRoot(originalPath);
        final String moduleName = moduleRoot.getFileName().toString();
        final String newModuleName = moduleName + "-test";
        final Path relative = moduleRoot.relativize(originalPath);
        final boolean isTestFile = TestJarUtil.isTestFile(originalPath.toString());
        final String sourcePath = isTestFile ? "src/test/java" : "src/main/java";
        Path newPath = moduleRoot
                .resolve("..")
                .resolve(newModuleName)
                .resolve(sourcePath)
                .resolve(relative.toString()
                        .replace("src/test/java/", ""));
        return cu.withSourcePath(newPath);
    }

    private static Path findModuleRoot(Path path) {
        Path current = path;
        while (current != null) {
            if (current.resolve("pom.xml").toFile().exists()) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("no pom.xml found for compilation unit " + path);
    }
}
