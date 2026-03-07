package io.github.openhelios.recipe.testjar.util;

import org.openrewrite.maven.tree.ResolvedPom;

/**
 * Utility class to shore in all visitors.
 */
public class TestJarUtil {

    public static final String TEST_JAR = "test-jar";

    public static final String MAVEN_JAR_PLUGIN = "maven-jar-plugin";

    /**
     * @param pom The POM.
     * @return True, if the given POM generates a test-jar file.
     */
    public static boolean generatesTestJar(ResolvedPom pom) {
        return pom.getPlugins().stream()
                .filter(p -> MAVEN_JAR_PLUGIN.equals(p.getArtifactId()))
                .flatMap(p -> p.getExecutions().stream())
                .filter( e -> null != e.getGoals())
                .flatMap(e -> e.getGoals().stream())
                .anyMatch(TEST_JAR::equals);
    }

    public static boolean isTestClass(String className) {
        return className.endsWith("Test") || className.endsWith("IT");
    }

    public static boolean isTestFile(String fileName) {
        if (fileName.endsWith(".java")) {
            var baseName = fileName.substring(0, fileName.length() - 5);
            return baseName.endsWith("Test") || baseName.endsWith("IT");
        }
        return false;
    }
}
