package com.github.ngyewch.gradle;

import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

public abstract class AggregateJavadocSetOptions {

  public abstract Property<String> getId();

  public abstract Property<String> getTitle();

  public abstract ListProperty<Project> getProjects();
}
