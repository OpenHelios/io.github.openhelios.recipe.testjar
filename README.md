# io.github.openhelios.recipe.testjar

This is a third party [OpenRewrite](https://github.com/openrewrite/rewrite)
recipe to remove a generated
test JAR of type `test-jar` by creating a new `-test` module.
All Maven modules are scanned for this process. It
contains the following steps and is processed for all Maven
`pom.xml` files:

1. The new `-test` module contains sharable Java files and
   other test Java files only if it is needed:
   1. All Java files not having suffix `Test` or `IT` are
      moved into folder `src/main/java` of the new module.
   2. All test Java files identified by suffix `Test` and `IT`,
      which need one of the moved classes, are moved into
      folder `src/test/java`.
   3. The test dependencies are added to the new module as
      normal dependencies.
2. The new `-test` module is added to its parent module.
3. A dependency to the old `test-jar` is replaced
   with the dependency to the new module.
4. The `maven-jar-plugin` to generate the `test-jar` is removed. 

## Getting Started

Compile, install and configure this recipe. Then it can
be executed easily.

### Requirements

The following versions or newer have to be installed:

* [JDK 25](https://en.wikipedia.org/wiki/Java_Development_Kit)
* [Maven 3](https://en.wikipedia.org/wiki/Apache_Maven)

Of course, you need a Maven project generating a JAR with
goal `test-jar` by plugin `maven-jar-plugin`.

### Clone and Install

Clone this repository and execute `mvn install` in the
repository folder, e.g.

```shell
git clone https://github.com/OpenHelios/io.github.openhelios.recipe.testjar.git
cd io.github.openhelios.recipe.testjar
mvn install
```

### Configure Maven for OpenRewrite

A convenient way is to add the OpenRewrite plugin to your
root `pom.xml` file in the plugin management, i.e.

```xml
<pluginManagement>
    <plugins>
        <plugin>
            <groupId>org.openrewrite.maven</groupId>
            <artifactId>rewrite-maven-plugin</artifactId>
            <version>6.30.0</version>
            <dependencies>
                <dependency>
                    <groupId>io.github.openhelios.recipe</groupId>
                    <artifactId>testjar</artifactId>
                    <version>1.0.0</version>
                </dependency>
            </dependencies>
            <configuration>
                <activeRecipes>
                    <recipe>MyRecipes</recipe>
                </activeRecipes>
            </configuration>
        </plugin>
        [...]
    </plugins>
</pluginManagement>
```

It already contains a reference to `MyRecipes`, which we
define in the file named `rewrite.yml`. It must be stored in
the project root folder:

```yaml
# start with "mvn rewrite:run"
type: specs.openrewrite.org/v1beta/recipe
name: MyRecipes
displayName: A list of recipes to be executed.
recipeList:
  - io.github.openhelios.recipe.testjar.MoveTestJarIntoNewModule
```

Then start the process by executing:
```shell
mvn rewrite:run
```
It looks automatically into `rewrite.yml` and then executes
the recipe list named `MyRecipes`.

## FAQ

### Not enough Heap Space Error

Depending on your machine and your project it
is useful to increase the Java heap space by
setting the environment variable `MAVEN_OPTS`
to avoid Java heap space errors, e.g.

```shell
export MAVEN_OPTS="-Xmx10g"
```
