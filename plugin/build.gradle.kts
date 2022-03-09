plugins {
    `java-gradle-plugin`
    `maven-publish`
    id("ca.cutterslade.analyze") version "1.8.3"
    id("com.asarkar.gradle.build-time-tracker") version "4.0.0"
    id("com.github.ben-manes.versions") version "0.42.0"
    id("com.gradle.plugin-publish") version "0.20.0"
    id("me.qoomon.git-versioning") version "5.1.5"
    id("se.ascp.gradle.gradle-versions-filter") version "0.1.10"
}

group = "io.github.ngyewch.gradle"
version = "0.0.0-SNAPSHOT"
gitVersioning.apply {
    refs {
        tag("v(?<version>.*)") {
            considerTagsOnBranches = true
            version = "\${ref.version}"
        }
        branch(".+") {
            version = "\${ref}-SNAPSHOT"
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(gradleApi())
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("gradle-aggregate-javadoc-plugin") {
            id = "io.github.ngyewch.aggregate-javadoc"
            displayName = "Gradle Aggregate Javadoc plugin"
            description = "Gradle Aggregate Javadoc plugin."
            implementationClass = "com.github.ngyewch.gradle.AggregateJavadocPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/ngyewch/gradle-aggregate-javadoc-plugin"
    vcsUrl = "https://github.com/ngyewch/gradle-aggregate-javadoc-plugin.git"
    tags = listOf("javadoc")
}
