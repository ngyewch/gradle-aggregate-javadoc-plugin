package com.github.ngyewch.gradle;

import org.gradle.api.Project;
import org.gradle.api.artifacts.*;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

import java.io.File;
import java.util.*;

public class AggregateJavadoc {

  private final Project project;
  private final ConfigurableFileCollection aggregateClasspath;
  private final TaskProvider<Javadoc> aggregateJavadoc;
  private final List<Project> excludeProjects;
  private final boolean useJavadocIo;
  private final List<String> excludeLinksForDependencies;
  private final Set<Project> handledProjects = new HashSet<>();

  public AggregateJavadoc(Project project, AggregateJavadocPluginExtension extension,
                          AggregateJavadocSetOptions options) {
    super();

    this.project = project;
    this.excludeProjects = extension.getExcludeProjects().getOrElse(new ArrayList<>());
    this.useJavadocIo = extension.getUseJavadocIo().getOrElse(false);
    this.excludeLinksForDependencies = extension.getExcludeLinksForDependencies().getOrElse(new ArrayList<>());

    final String id = options.getId().get();
    final String title = options.getId().get();
    final List<Project> projects = options.getProjects().getOrElse(new ArrayList<>());

    aggregateClasspath = project.files();
    aggregateJavadoc = project.getTasks()
        .register(String.format("javadoc-%s", id), Javadoc.class, aj -> {
          aj.setTitle(title);
          aj.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);
          aj.getConventionMapping().map("destinationDir", () -> {
            final File docsDir = Optional.ofNullable(project.getExtensions().findByType(JavaPluginExtension.class))
                .map(JavaPluginExtension::getDocsDir)
                .map(directoryProperty -> directoryProperty.get().getAsFile())
                .orElse(new File(project.getBuildDir(), "docs"));
            return new File(docsDir, id);
          });
          aj.setClasspath(aggregateClasspath);
        });
    projects.forEach(this::handleSubproject);
  }

  private boolean isLinkExcluded(ModuleDependency moduleDependency) {
    for (final String excludeLinkForDependency : excludeLinksForDependencies) {
      final Dependency dep = project.getDependencies().create(excludeLinkForDependency);
      boolean exclude = ((dep.getGroup() == null) || dep.getGroup().equals(moduleDependency.getGroup()))
          && ((dep.getName() == null) || dep.getName().equals("*") || dep.getName().equals(moduleDependency.getName()))
          && ((dep.getVersion() == null) || dep.getVersion().equals(moduleDependency.getVersion()));
      if (exclude) {
        return true;
      }
    }
    return false;
  }

  private void handleSubproject(Project subproject) {
    if (excludeProjects.contains(subproject)) {
      return;
    }
    if (handledProjects.contains(subproject)) {
      return;
    }
    handledProjects.add(subproject);

    final List<Dependency> dependencies = getDependencies(subproject,"implementation", "api", "compileOnly");

    final List<String> links = new ArrayList<>();
    if (useJavadocIo) {
      dependencies.forEach(dependency -> {
        if (dependency instanceof ExternalModuleDependency) {
          final ExternalModuleDependency externalModuleDependency = (ExternalModuleDependency) dependency;
          if (!isLinkExcluded(externalModuleDependency)) {
            links.add(String.format("https://javadoc.io/doc/%s/%s/%s/",
                externalModuleDependency.getGroup(), externalModuleDependency.getName(),
                externalModuleDependency.getVersion()));
          }
        }
      });
    }

    subproject.getPlugins().withType(JavaPlugin.class, jp -> {
      final AggregateJavadocClientPlugin clientPlugin = subproject.getPlugins()
          .apply(AggregateJavadocClientPlugin.class);
      aggregateClasspath.from(clientPlugin.getJavadocClasspath());

      aggregateJavadoc.configure(aj -> {
        final SourceSet main = subproject.getExtensions().getByType(JavaPluginExtension.class).getSourceSets()
            .getByName("main");
        final Javadoc javadoc = subproject.getTasks().named(main.getJavadocTaskName(), Javadoc.class).get();

        aj.dependsOn(String.format(":%s:compileJava", subproject.getName()));

        final JavaCompile javaCompile = (JavaCompile) subproject.getTasks().getByName(main.getCompileJavaTaskName());
        aj.source(javaCompile.getSource());

        StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) javadoc.getOptions();
        StandardJavadocDocletOptions aggregateOptions = (StandardJavadocDocletOptions) aj.getOptions();

        addAll(aggregateOptions.getLinks(), options.getLinks());
        addAll(aggregateOptions.getLinks(), links);
        addAll(aggregateOptions.getLinksOffline(), options.getLinksOffline());
        addAll(aggregateOptions.getJFlags(), options.getJFlags());
      });
    });

    try {
      dependencies.forEach(dependency -> {
        if (dependency instanceof ProjectDependency) {
          final ProjectDependency projectDependency = (ProjectDependency) dependency;
          handleSubproject(projectDependency.getDependencyProject());
        }
      });
    } catch (UnknownConfigurationException e) {
      // ignore exception
    }
  }

  private static List<Dependency> getDependencies(Project project, String... configurationNames) {
    final List<Dependency> dependencies = new ArrayList<>();
    for (final String configurationName : configurationNames) {
      try {
        dependencies.addAll(project.getConfigurations().getByName(configurationName).getDependencies());
      } catch (UnknownConfigurationException e) {
        // ignore exception
      }
    }
    return dependencies;
  }

  private static <T> void addAll(List<T> list, List<T> newElements) {
    if (newElements == null) {
      return;
    }
    newElements.forEach(element -> {
      if ((element != null) && !list.contains(element)) {
        list.add(element);
      }
    });
  }
}
