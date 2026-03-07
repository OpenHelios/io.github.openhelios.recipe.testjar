package io.github.openhelios.recipe.testjar.move;

import io.github.openhelios.recipe.testjar.util.TestJarUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import java.util.HashSet;

/**
 * 1. phase: Fill {@link MoveCuData} except {@link MoveCuData#moveClasses()}.
 */
public class ScanCuVisitor extends JavaIsoVisitor<ExecutionContext> {

    private final MoveCuData data;

    public ScanCuVisitor(MoveCuData data) {
        this.data = data;
    }

    @Override
    public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, ExecutionContext ctx) {
        if (!cu.getSourcePath().toString().contains("src/test/java")) {
            return cu;
        }
        for (J.ClassDeclaration clazz : cu.getClasses()) {
            if (clazz.getType() instanceof JavaType.FullyQualified classType) {
                String typeName = classType.getFullyQualifiedName();
                data.allTestClasses().add(typeName);
                data.class2dependencies().putIfAbsent(typeName, new HashSet<>());
                data.class2cu().put(typeName, cu);
                String className = clazz.getSimpleName();
                if (!TestJarUtil.isTestClass(className)) {
                    data.initialCandidates().add(typeName);
                }
            }
        }
        return super.visitCompilationUnit(cu, ctx);
    }

    @Override
    public J.Identifier visitIdentifier(J.Identifier identifier, ExecutionContext ctx) {
        if (identifier.getType() instanceof JavaType.FullyQualified usedClass) {
            if (!(usedClass instanceof JavaType.Unknown)) {
                String usedTypeName = usedClass.getFullyQualifiedName();
                J.CompilationUnit cu =
                        getCursor().firstEnclosing(J.CompilationUnit.class);
                if (null != cu) {
                    for (var containedClass : cu.getClasses()) {
                        if (containedClass.getType() instanceof JavaType.FullyQualified clazz) {
                            String typeName =
                                    clazz.getFullyQualifiedName();
                            if (!typeName.equals(usedTypeName)) {
                                data.class2dependencies()
                                        .computeIfAbsent(typeName,
                                                _ -> new HashSet<>())
                                        .add(usedTypeName);
                            }
                        }
                    }
                }
            }
        }
        return super.visitIdentifier(identifier, ctx);
    }
}
