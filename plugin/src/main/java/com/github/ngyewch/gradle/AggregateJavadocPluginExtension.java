package com.github.ngyewch.gradle;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public abstract class AggregateJavadocPluginExtension {

  private final ObjectFactory objectFactory;

  private final List<AggregateJavadocSetOptions> setOptions = new ArrayList<>();

  public abstract ListProperty<Project> getExcludeProjects();

  public abstract Property<Boolean> getUseJavadocIo();

  public abstract ListProperty<String> getExcludeLinksForDependencies();

  @Inject
  public AggregateJavadocPluginExtension(ObjectFactory objectFactory) {
    super();

    this.objectFactory = objectFactory;
  }

  public List<AggregateJavadocSetOptions> getSetOptions() {
    return setOptions;
  }

  public void register(Action<AggregateJavadocSetOptions> action) {
    final AggregateJavadocSetOptions aggregateJavadocSetOptions = objectFactory
        .newInstance(AggregateJavadocSetOptions.class);
    action.execute(aggregateJavadocSetOptions);
    setOptions.add(aggregateJavadocSetOptions);
  }
}
