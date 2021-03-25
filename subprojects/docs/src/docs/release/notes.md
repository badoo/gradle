The Gradle team is excited to announce Gradle @version@.

This release significantly improves the [performance of Kotlin DSL build scripts compilation](#kotlin-dsl-performance), adds several [improvements to Java toolchain support](#java-toolchain-improvements), including vendor selection, and makes it easy to [execute any tasks in a composite build directly from the command line](#composite-builds). This release also introduces new [dependency management APIs for consistent resolution](#dm-features).

The [experimental configuration cache](#configuration-cache) has added support for composite builds and more core plugins shipped with Gradle.

Several other [improvements](#other-improvements) were added in this release.

We don't expect many builds to be affected, but this release [disables outdated TLS v1.0 and v1.1 protocols](#security-tls) to improve the security of builds resolving dependencies from external repositories.

We would like to thank the following community contributors for their contributions to this release of Gradle:

[Marcono1234](https://github.com/Marcono1234),
[Björn Sundahl](https://github.com/Ranzdo),
[Roberto Perez Alcolea](https://github.com/rpalcolea),
[Danny Thomas](https://github.com/DanielThomas),
[Jeff](https://github.com/mathjeff),
[Mattia Tommasone](https://github.com/Raibaz),
[jdai8](https://github.com/jdai8),
[David Burström](https://github.com/davidburstrom),
[Björn Kautler](https://github.com/Vampire),
[Stefan Oehme](https://github.com/oehme),
[Thad House](https://github.com/ThadHouse),
[knittl](https://github.com/knittl),
[hywelbennett](https://github.com/hywelbennett),
[wboult](https://github.com/wboult),
[Gregorios Leach](https://github.com/simtel12).

## Upgrade instructions

Switch your build to use Gradle @version@ by updating your wrapper:

`./gradlew wrapper --gradle-version=@version@`

See the [Gradle 6.x upgrade guide](userguide/upgrading_version_6.html#changes_@baseVersion@) to learn about deprecations, breaking changes and other considerations when upgrading to Gradle @version@.

NOTE: Gradle 6.8 has had **three** patch releases, which fix several issues from the original release.
We recommend always using the latest patch release.

For Java, Groovy, Kotlin and Android compatibility, see the [full compatibility notes](userguide/compatibility.html).

<!-- Do not add breaking changes or deprecations here! Add them to the upgrade guide instead. -->

<a name="kotlin-dsl-performance"></a>
## Performance improvements

### Kotlin DSL script compilation improvements

This release makes compilation of [Gradle Kotlin DSL](userguide/kotlin_dsl.html) scripts (`*.gradle.kts`) faster, reduces the amount of memory consumed, and introduces compilation avoidance that can eliminate the need to recompile Kotlin build scripts altogether.

On a sample build with 100 subprojects, the cumulative script compilation time goes from [~50 seconds](https://scans.gradle.com/s/3bg67eccnya5i/performance/configuration?showScriptCompilationTimes) down to [~21 seconds](https://scans.gradle.com/s/y7dw5ekes24ag/performance/configuration?showScriptCompilationTimes) with cold caches and cold daemons.
Garbage collection time goes from [2.6 seconds](https://scans.gradle.com/s/3bg67eccnya5i/performance/build#garbage-collection) down to [1.3 seconds](https://scans.gradle.com/s/y7dw5ekes24ag/performance/build#garbage-collection). 
This improvement also reduces memory pressure.
On top of that, a non-ABI change can [eliminate build script recompilation altogether now](https://scans.gradle.com/s/exxa2y22shld6/performance/configuration#summary-script-compile), saving those 21 seconds.

<img src="data:image/png;base64,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CxYopZRSSin1mlbk0l4b5GP4TIyPL4hx8VdddVXuZa/UIx+BvtTq1avT8OHD09VXX53GjRuXjhw54iMYAAB65LtLDIWJ4F4I54UgH73sTU1NadCgQe3Tbt++PV133XUnPUeMhy8Mt4ne+hEjRqSZM2facgAACPLdKYL3I488kq9ME0NrYpx8IdTHya7z58/P902cODGfyFoqrnYzZMiQtHv37vwBYNSoUfl5AABAkO9GMYzmzjvvzMNibrvtttzzXrBx48Yc7AvXkY+gXs706dNT//7983PEibO1jJUHAABBHgAAEOQBAABBHgAABHkAAECQBwAABHkAAECQBwAAQR4AABDkAQAAQR4AAAR5AABAkId6ffGLX0xvetObOtSOHTvyfdOnT08f+MAH0lve8pb0iU98Ii1durTi87zwwgvps5/9bHrb296W1q9fb8UCAII8dKcPfvCD6WMf+1h6+OGH2+vw4cNpxYoV6Zxzzkmf+9zn0pQpU9I73vGOdOmll6a2traTnmPmzJl52o985CNpzJgxad++fVYsACDIQ3eKHvSbb775pNvvvffedMkll6QNGzbkv2+55ZbcW799+/YO07W0tOSQ/8lPfjL/v5Ljx4+nkSNH5g8DF198cRo0aFA6ePCgDQAACPJQr+g5j3D+7ne/O731rW9N73vf+9Ls2bPLhvAYWvP2t7/9pLDe0NCQn+Mzn/lMuuyyy9K73vWu9NBDD530HPG8Md1dd92Vpk6dmv8/fPhwGwEAEOShK0F+yJAhOVA//fTTeZhNjIffuXNnh+liuEwE78mTJ5/0HE8++WS+70Mf+lB64IEH8r8xzGbTpk0dpnv88cfzdKNGjcqvGz39pb37AACCPHTBE088kcP2M888037bjBkzcjAfMGBA2cc89thj+TFz587Nfz/33HP57+h1LxY9+fGh4bzzzkvnnntuuvzyywV5AECQh65Ys2ZNuvXWW9OqVavy34888kgO4dE7HxYvXpze/OY35xNejx07VvY5CkNrHn300fz3nDlzygb5uJJNTBu98XFffDgYPHiwjQAACPJQr+gRP//889NHP/rRfLWa97///enCCy9Mzc3NaePGjemiiy7K98dVa5566qlcmzdvTnv27ElXXXVVmjVrVu5pj8e95z3vSQ8++GD68Ic/nC644IK0bdu2Dq81evTo9oAfPf4R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" alt="Gradle Kotlin DSL script compilation performance improvements"/> 

Until now, any change to build logic in [buildSrc](userguide/organizing_gradle_projects.html#sec:build_sources) required all of the build scripts to be recompiled.
This release introduces compilation avoidance for [Gradle Kotlin DSL](userguide/kotlin_dsl.html) scripts.

Compilation avoidance will cause Gradle to only recompile build scripts when a change to shared build logic impacts the
ABI (application binary interface) of the classpath of the build script.
Changes to private implementation details of build logic, such as private methods or classes,
bodies of non-private methods or classes, as well as internal changes to [precompiled script plugins](userguide/custom_plugins.html#sec:precompiled_plugins),
will no longer trigger recompilation of the project's build scripts.

Compilation avoidance also applies to changes in any JAR on the build script's classpath.
That includes JARs added by plugins define in included builds and JARs added directly via the `buildscript {}` block.

While the impact on your build may vary, most builds can expect a noticeably shorter feedback loop when editing Kotlin DSL build logic.

**Note**: Kotlin's public [inline functions](https://kotlinlang.org/docs/reference/inline-functions.html#inline-functions) are not supported with compilation avoidance. 
If such functions appear in the public API of a JAR on the buildscript's classpath, changes to classes in that JAR will cause Gradle to fallback to its old behavior.
For example, if `buildSrc` contains a class with a public inline function, then any change to a class in `buildSrc` will cause all build scripts to be recompiled.

### More cache hits for tasks with runtime classpaths

For [up-to-date checks](userguide/more_about_tasks.html#sec:up_to_date_checks) and the [build cache](userguide/build_cache.html), Gradle needs to determine if two task input properties have the same value. 
In order to do so, Gradle first [normalizes](userguide/more_about_tasks.html#sec:configure_input_normalization) both inputs and then compares the result.

Runtime classpath analysis now smartly inspects all properties files, ignoring changes to comments, whitespace, and differences in property order.  Moreover, you can selectively ignore properties that don't impact the runtime classpath.

```
normalization {
    properties('**/build-info.properties') {
        ignoreProperty('timestamp')
    }
}
```

This improves the likelihood of up-to-date and build cache hits when a properties file on the classpath is regenerated or only differs by unimportant values.

See [the user manual](userguide/more_about_tasks.html#sec:property_file_normalization) for further information.

### More cache hits when empty directories are present

For [up-to-date checks](userguide/more_about_tasks.html#sec:up_to_date_checks) and the [build cache](userguide/build_cache.html), Gradle needs to determine if two directory structures contain the same contents.  When a directory contains an empty directory, it is considered to have different contents than an identical directory where the empty directory does not exist.

This may not always be desirable. There are many cases where only the files in a directory structure may be significant, and an empty directory will have no impact on the outputs of a task. In such cases, re-executing the task because an empty directory exists is unnecessary as it will only produce the same outputs.

A new annotation has been introduced to address this scenario. Inputs annotated with [@InputFiles](javadoc/org/gradle/api/tasks/InputFiles.html) or [@InputDirectory](javadoc/org/gradle/api/tasks/InputDirectory.html) can additionally be annotated with [@IgnoreEmptyDirectories](javadoc/org/gradle/api/tasks/IgnoreEmptyDirectories.html) to specify that directories should not be considered during build cache and up-to-date checks.
For inputs annotated in this way, only changes to files (including the file path) will be treated as differences in the input values.

```
class MyTask extends DefaultTask {
    @InputFiles
    @PathSensitive(@PathSensitivity.RELATIVE)
    @IgnoreEmptyDirectories
    FileCollection inputFiles;
}
```

Similarly, there is a corresponding runtime API equivalent:

```
tasks.register("myTask") {
    ext.inputFiles = files()
    inputs.files(inputFiles)
          .withPropertyName('inputFiles')
          .withPathSensitivity(PathSensitivity.RELATIVE)
          .ignoreEmptyDirectories()
}
```

[SourceTask](javadoc/org/gradle/api/tasks/SourceTask.html), [JavaCompile](javadoc/org/gradle/api/tasks/compile/JavaCompile.html), [GroovyCompile](javadoc/org/gradle/api/tasks/compile/GroovyCompile.html), and [AntlrTask](javadoc/org/gradle/api/plugins/antlr/AntlrTask.html) have all been updated to now ignore empty directories when doing up-to-date checks and build cache key calculations.

See [the user manual](userguide/more_about_tasks.html#sec:up_to_date_checks) for more information.

<a name="configuration-cache"></a>
### Configuration cache improvements

The [configuration cache](userguide/configuration_cache.html) improves build performance by caching the result of the configuration phase. Using the configuration cache, Gradle can skip the configuration phase entirely when nothing that affects the build configuration has changed.

Read about [this feature and its impact](https://blog.gradle.org/introducing-configuration-caching) on the Gradle blog. You can also track progress of configuration cache support in [core plugins](https://github.com/gradle/gradle/issues/13454) and [community plugins](https://github.com/gradle/gradle/issues/13490).

#### Support for composite builds

Starting with this release, [composite builds](userguide/composite_builds.html) are fully supported with the configuration cache.

#### More supported core plugins

In this release all core code analysis plugins received full support for the configuration cache:

* [`checkstyle`](userguide/checkstyle_plugin.html)
* [`pmd`](userguide/pmd_plugin.html)
* [`codenarc`](userguide/codenarc_plugin.html)
* [`jacoco`](userguide/jacoco_plugin.html)

See the [matrix of supported core plugins](userguide/configuration_cache.html#config_cache:plugins:core) in the user manual.

<a name="java-toolchain-improvements"></a>
## Java toolchain improvements 

[Java toolchain support](userguide/toolchains.html) provides an easy way to declare what Java version the project should be built with. 
By default, Gradle will [auto-detect installed JDKs](userguide/toolchains.html#sec:auto_detection) that can be used as toolchain.

With this release, toolchain support has been added to the Groovy compile task along with the following improvements.

### Selecting toolchain by vendor and implementation

In case your build has specific requirements from the used JRE/JDK, you may want to define the vendor for the toolchain as well.
`JvmVendorSpec` has a list of well-known JVM vendors recognized by Gradle. 

```
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
        vendor = JvmVendorSpec.ADOPTOPENJDK

        // alternativly, use custom matching
        // vendor = JvmVendorSpec.matching("customString")
    }
}
```

If the vendor is not enough to select the appropriate toolchain, you may as well filter by the implementation of the virtual machine.
For example, to use an [Open J9](https://www.eclipse.org/openj9/) JVM, distributed via [AdoptOpenJDK](https://adoptopenjdk.net/), you can filter by the implementation as shown in the example below. 

```
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
        vendor = JvmVendorSpec.ADOPTOPENJDK
        implementation = JvmImplementation.J9
    }
}
```

Please refer to [the documentation](userguide/toolchains.html#using_toolchains_by_specific_vendors) for more detailed information.

### Viewing all available toolchains

In order to see which toolchains got detected and their corresponding metadata, Gradle 6.8 now provides some insight with the `javaToolchains` task.

Output of `gradle -q javaToolchains`:
```
 + Options
     | Auto-detection:     Enabled
     | Auto-download:      Enabled

 + AdoptOpenJDK 1.8.0_242
     | Location:           /path/to/8.0.242.hs-adpt/jre
     | Language Version:   8
     | Vendor:             AdoptOpenJDK
     | Is JDK:             true
     | Detected by:        SDKMAN!

 + OpenJDK 15-ea
     | Location:           /path/to/java/15.ea.21-open
     | Language Version:   15
     | Vendor:             AdoptOpenJDK
     | Is JDK:             true
     | Detected by:        SDKMAN!

 + Oracle JDK 1.7.0_80
     | Location:           /Library/Java/jdk1.7.0_80.jdk/jre
     | Language Version:   7
     | Vendor:             Oracle
     | Is JDK:             true
     | Detected by:        macOS java_home
```

This can help to debug which toolchains are available to the build and if the expected toolchain got detected or [requires manual setup](userguide/toolchains.html#sec:custom_loc).
See the [toolchain documentation](userguide/toolchains.html) for more in-depth information on toolchain detection and usage.

<a name="composite-builds"></a>
## Composite builds improvements

[Composite builds](userguide/composite_builds.html) are a way of combining separate Gradle builds into a single build. Each build can have a separate purpose (build logic, backend code, frontend code) and can be worked on independently. 

### Tasks can be executed for included builds

Gradle now allows users to execute tasks from included builds directly from the command line. For example, if your build includes `my-other-project` as an included build and it has a subproject `sub` with a task `foo`, then you can execute `foo` with the following command:

    gradle :my-other-project:sub:foo

Note, unlike a multi-project build, running `gradle build` will _not_ run the `build` task in all of the included builds.
You could introduce [task dependencies](userguide/composite_builds.html#included_build_task_dependencies) to [lifecycle tasks](userguide/more_about_tasks.html#sec:lifecycle_tasks) in included builds if you wanted to recreate this behavior for included builds.

IDE support for executing tasks from included builds may not yet fully work depending on the IDE.
Updates for IntelliJ IDEA and Eclipse Buildship are planned to support this fully. 
Today, in IntelliJ IDEA, you can create a [Gradle run configuration](https://www.jetbrains.com/help/idea/create-run-debug-configuration-gradle-tasks.html) to execute a task directly (like you would on the command line).

### Desired cycles between builds are now fully supported

There are cases, where a cycle between included builds are desired.
For example, if two builds contain end-to-end tests that require the production code of both builds.
Such setups are possible with subprojects of a single build, but were not fully supported between projects of different builds.
With this release, this is possible and Gradle will only fail if there is a cycle between _tasks_.
Issues with importing such builds in IDEs are also fixed. 

### New documentation for composite builds and structuring software projects

Gradle's documentation now contains a [sample](samples/sample_structuring_software_projects.html) for structuring software projects with composite builds and a new a chapter on [structuring software projects](userguide/structuring_software_products.html) using composite builds.

<a name="dm-features"></a>
## Dependency management improvements

### Consistent dependency resolution

Dependency resolution in Gradle happens a lot during a build.
From the classpath to compile code or run tests, to the tools used for static analysis, they all resolve a configuration to a set of dependencies at some point.

However, these resolutions happen in isolation.
Sometimes, the dependencies resolved for the runtime classpath have different versions than the dependencies resolved for the compile classpath.
This typically happens when a transitive dependency that is only present at runtime brings in a higher version of a first level dependency.
Similarly, the runtime classpath of tests could use different versions than the compile classpath of production code.

To mitigate this problem, Gradle now lets you declare consistency between dependency configurations.
For example, in the Java ecosystem, you can write:

```
java {
    consistentResolution {
        useCompileClasspathVersions()
    }
}
```

This tells Gradle that the common dependencies between the runtime classpath and the compile classpath should be aligned to the versions used at compile time.

There are many options to configure this feature, including using it outside of the Java ecosystem, which are described in the [user manual](userguide/resolution_strategy_tuning.html#resolution_consistency).

### Central declaration of repositories

In previous Gradle versions, repositories used for dependency resolution had to be declared for every (sub)project individually.
However, in most cases, the same repositories should be used in every project.

In Gradle 6.8, repositories can now conveniently be defined for the whole build in `settings.gradle(.kts)`:

```
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
```

This allows Gradle to ensure that you use the same repositories for resolving dependencies in all projects of the build.
Learn more by reading how to [declare repositories for the whole build](userguide/declaring_repositories.html#sub:centralized-repository-declaration).

### Central declaration of component metadata rules

[Component metadata rules](userguide/component_metadata_rules.html) are a powerful tool to fix bad metadata published on remote repositories.
However, similarly to repositories, rules traditionally had to be applied on each project.
Starting from this release, it is possible to declare component metadata rules in a central place in `settings.gradle(.kts)`:

```
dependencyResolutionManagement {
    components {
        withModule('com.google.guava:guava', GuavaRule)
    }
}
```

You can learn more about declaring rules globally in the [user manual](userguide/component_metadata_rules.html#sec:rules_in_settings).

### Locking of Gradle settings dependencies

[Dependency locking](userguide/dependency_locking.html) makes builds using dynamic versions deterministic.

So far you could lock dependency configurations from your project or from the project buildscript classpath.
This release adds support to lock configurations from the [Gradle `settings.gradle(.kts)`](userguide/build_lifecycle.html#sec:settings_file):

```
buildscript {
    configurations.classpath {
        resolutionStrategy.activateDependencyLocking()
    }
}
```

See the documentation for more details on [locking settings configurations](userguide/dependency_locking.html#locking_settings_classpath_configuration).

<a name="other-improvements"></a>
## Other improvements

### Test re-run JUnit XML reporting enhancements

The `Test` task, used for executing JVM tests, reports test results as HTML and as a set of XML files in the “JUnit XML” pseudo standard.
It is common for CI servers and other tooling to observe test results via the XML files.
A new `mergeReruns` option has been added that changes how tests that are executed more than once are reported in the XML files.

```
test {
    reports.junitXml.mergeReruns = true
}
```

When this new option is enabled, if a test fails but is then retried and succeeds, its failures will be recorded as `<flakyFailure>` instead of `<failure>`, within one `<testcase>`.
This is the same as the reporting produced by the [surefire plugin of Apache Maven™](https://maven.apache.org/components/surefire/maven-surefire-plugin/examples/rerun-failing-tests.html), when enabling reruns.
If your CI server understands this format, it will indicate that the test was flaky.

This option is disabled by default, causing each test execution to be listed as a separate `<testcase>` in the XML.
This means that when a test is executed multiple times, due to a retry-on-failure mechanism for example, it is listed multiple times.
This is also the behavior for all previous Gradle versions.

If you are using [build scans](https://scans.gradle.com) or [Gradle Enterprise](https://gradle.com/gradle-enterprise-solution-overview/failure-analytics/), flaky tests will be detected regardless of this setting.

Learn more about this new feature in the [Java testing documentation](userguide/java_testing.html#communicating_test_results_to_CI_servers_and_other_tools_via_xml_files).

### `@Inject` is an implicit import

When using [dependency injection](userguide/custom_gradle_types.html#service_injection) when developing plugins, tasks or project extensions, it is now possible to use the `@Inject` annotation without explicitly importing it into your build scripts the
same way it works for other Gradle API classes.

### Importing projects with custom source sets into Eclipse

This version of Gradle fixes problems with projects that use custom source sets, like additional functional test source sets.

Custom source sets are now imported into Eclipse automatically and no longer require manual configuration in the build.

This does not require a separate upgrade to Eclipse Buildship.

<a name="security-tls"></a>
## Security improvements

### Outdated TLS versions are no longer enabled by default

This version of Gradle removes TLS protocols v1.0 and v1.1 from the default list of allowed protocols. Gradle will no longer fallback to TLS v1.0 or v1.1 by default when resolving dependencies. Only TLS v1.2 or TLS v1.3 are allowed by default.

These TLS versions can be re-enabled by manually specifying the system property `https.protocols` with
a comma separated list of protocols required by your build.

The vast majority of builds should not need to change in any way. [Maven Central](https://central.sonatype.org/articles/2018/May/04/discontinued-support-for-tlsv11-and-below/) and [JCenter/Bintray](https://jfrog.com/knowledge-base/why-am-i-failing-to-work-with-jfrog-cloud-services-with-tls-1-0-1-1/) dropped support for TLS v1.0 and TLS v1.1 in 2018. Java has had TLS v1.2 available since Java 7. Disabling these protocols in Gradle protects builds from downgrade attacks.

Depending on the version of Java you use, Gradle will negotiate TLS v1.2 or TLS v1.3 when communicating with remote repositories.

**Note**: Early versions of JDK 11 & JDK 12 contained [race condition bug in the `TLSv1.3` handling logic](https://bugs.openjdk.java.net/browse/JDK-8213202)
which causes the exception `javax.net.ssl.SSLException: No PSK available. Unable to resume`. If you run into this issue,
we recommend updating to the latest minor JDK version.

## Fixed issues

## Known issues

Known issues are problems that were discovered post release that are directly related to changes made in this release.

## External contributions

We love getting contributions from the Gradle community. For information on contributing, please see [gradle.org/contribute](https://gradle.org/contribute).

## Reporting Problems

If you find a problem with this release, please file a bug on [GitHub Issues](https://github.com/gradle/gradle/issues) adhering to our issue guidelines.
If you're not sure you're encountering a bug, please use the [forum](https://discuss.gradle.org/c/help-discuss).

We hope you will build happiness with Gradle, and we look forward to your feedback via [Twitter](https://twitter.com/gradle) or on [GitHub](https://github.com/gradle).
