package com.github.ngyewch.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class AggregateJavadocPlugin
    implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    final AggregateJavadocPluginExtension extension = project.getExtensions()
        .create("aggregateJavadoc", AggregateJavadocPluginExtension.class);

    final Set<Project> allProjects = new ConcurrentSkipListSet<>(project.getAllprojects());
    project.getAllprojects().forEach(p ->
        p.afterEvaluate(p1 -> {
          allProjects.remove(p1);
          if (allProjects.isEmpty()) {
            for (final AggregateJavadocSetOptions setOptions : extension.getSetOptions()) {
              final AggregateJavadoc aggregateJavadoc = new AggregateJavadoc(project, extension, setOptions);
            }
          }
        })
    );
  }
}
