package com.facebook.buck.jvm.kotlin;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import com.facebook.buck.jvm.java.CompileToJarStepFactory;
import com.facebook.buck.jvm.java.DefaultJavaLibrary;
import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.rules.SourcePathResolver;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

public class DefaultKotlinLibrary extends DefaultJavaLibrary implements KotlinLibrary {

  public DefaultKotlinLibrary(
      BuildRuleParams params,
      SourcePathResolver resolver,
      Set<? extends SourcePath> srcs,
      Set<? extends SourcePath> resources,
      Optional<Path> generatedSourceFolder,
      Optional<SourcePath> proguardConfig,
      ImmutableList<String> postprocessClassesCommands,
      ImmutableSortedSet<BuildRule> exportedDeps,
      ImmutableSortedSet<BuildRule> providedDeps,
      BuildTarget abiJar,
      ImmutableSortedSet<SourcePath> abiInputs,
      boolean trackClassUsage,
      ImmutableSet<Path> additionalClasspathEntries,
      CompileToJarStepFactory compileStepFactory,
      Optional<Path> resourcesRoot,
      Optional<SourcePath> manifestFile,
      Optional<String> mavenCoords,
      ImmutableSortedSet<BuildTarget> tests,
      ImmutableSet<Pattern> classesToRemoveFromJar) {

    super(
        params,
        resolver,
        srcs,
        resources,
        generatedSourceFolder,
        proguardConfig,
        postprocessClassesCommands,
        exportedDeps,
        providedDeps,
        abiJar,
        abiInputs,
        trackClassUsage,
        additionalClasspathEntries,
        compileStepFactory,
        resourcesRoot,
        manifestFile,
        mavenCoords,
        tests,
        classesToRemoveFromJar);
  }
}
