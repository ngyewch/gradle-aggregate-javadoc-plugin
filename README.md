# gradle-aggregate-javadoc-plugin

![GitHub release (latest by date)](https://img.shields.io/github/v/release/ngyewch/gradle-aggregate-javadoc-plugin)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/ngyewch/gradle-aggregate-javadoc-plugin/Java%20CI)

A Gradle plugin for aggregating Javadoc for multi-project builds. This plugin transitively adds sources from local project dependencies and adds links to Javadoc hosted in [javadoc.io](https://javadoc.io/). 

## Basic usage

Kotlin DSL (`build.gradle.kts`)

```
plugins {
    java
    id("io.github.ngyewch.aggregate-javadoc") version "0.1.2"
}

allprojects {
    repositories {
        mavenCentral()
    }

    tasks.withType<Javadoc> {
        configure(options, closureOf<StandardJavadocDocletOptions> {
            addStringOption("Xdoclint:none", "-quiet")
            links = mutableListOf(
                "https://docs.oracle.com/javase/8/docs/api/",
            )
        })
    }
}

configure<AggregateJavadocPluginExtension> {
    useJavadocIo.set(true)
    excludeLinksForDependencies.set(
        mutableListOf(
            "com.google.code.gson:gson",
        )
    )

    register {
        id.set("client-sdk")
        title.set("Client SDK")
        projects.addAll(
            project(":client-sdk"),
        )
    }
    register {
        id.set("server-sdk")
        title.set("Server SDK")
        projects.addAll(
            project(":server-sdk"),
        )
    }
}
```