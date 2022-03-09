package com.github.ngyewch.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.javadoc.Javadoc;

class AggregateJavadocClientPlugin
    implements Plugin<Project> {

  private ConfigurableFileCollection javadocClasspath;

  private TaskProvider<Task> collectJavadocClasspath;

  public ConfigurableFileCollection getJavadocClasspath() {
    return javadocClasspath;
  }

  @Override
  public void apply(Project project) {
    collectJavadocClasspath = project.getTasks().register("collectJavadocClasspath");
    javadocClasspath = project.files().builtBy(collectJavadocClasspath);

    project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
      collectJavadocClasspath.configure(c -> {
        c.doFirst(t -> {
          Javadoc javadoc = (Javadoc) project.getTasks().getByName(JavaPlugin.JAVADOC_TASK_NAME);
          javadocClasspath
              .from(javadoc.getClasspath().getFiles())
              .builtBy(javadoc.getClasspath());
        });
      });
    });
  }
}