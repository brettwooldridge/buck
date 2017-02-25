/*
 * Copyright 2016-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.buck.jvm.kotlin;

import com.facebook.buck.io.ProjectFilesystem;
import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.rules.BuildContext;
import com.facebook.buck.rules.SourcePathResolver;
import com.facebook.buck.rules.SourcePathRuleFinder;
import com.facebook.buck.step.ExecutionContext;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.StepExecutionResult;
import com.facebook.buck.util.CapturingPrintStream;
import com.facebook.buck.util.Verbosity;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

@SuppressWarnings("unused")
public class KotlincStep implements Step {

  private final Kotlinc kotlinc;
  private final SourcePathResolver resolver;
  private final ImmutableSortedSet<Path> declaredClassPathEntries;
  private final Path outputDirectory;
  private final ImmutableList<String> extraArguments;
  private final ImmutableSortedSet<Path> sourceFilePaths;
  private final ProjectFilesystem filesystem;
  private final Path pathToSrcsList;
  private final SourcePathRuleFinder ruleFinder;
  private final BuildContext buildContext;
  private final BuildTarget invokingRule;

  KotlincStep(
      BuildContext buildContext,
      BuildTarget invokingRule,
      Path outputDirectory,
      ImmutableSortedSet<Path> sourceFilePaths,
      Path pathToSrcsList,
      ImmutableSortedSet<Path> declaredClassPathEntries,
      Kotlinc kotlinc,
      ImmutableList<String> extraArguments,
      SourcePathResolver resolver,
      SourcePathRuleFinder ruleFinder,
      ImmutableSortedSet<Path> extraClassPathEntries,
      ProjectFilesystem filesystem) {
    this.buildContext = buildContext;
    this.invokingRule = invokingRule;
    this.outputDirectory = outputDirectory;
    this.sourceFilePaths = sourceFilePaths;
    this.pathToSrcsList = pathToSrcsList;
    this.kotlinc = kotlinc;
    this.resolver = resolver;
    this.ruleFinder = ruleFinder;
    this.declaredClassPathEntries = declaredClassPathEntries;
    this.extraArguments = extraArguments;
    this.filesystem = filesystem;
  }

  @Override
  public String getShortName() {
    return getKotlinc().getShortName();
  }

  @Override
  public StepExecutionResult execute(ExecutionContext context) throws IOException, InterruptedException {
    Verbosity verbosity =
            context.getVerbosity().isSilent() ? Verbosity.STANDARD_INFORMATION : context.getVerbosity();

    try (CapturingPrintStream stdout = new CapturingPrintStream();
         CapturingPrintStream stderr = new CapturingPrintStream();
         ExecutionContext firstOrderContext = context.createSubContext(
              stdout,
              stderr,
              Optional.of(verbosity))) {

      int declaredDepsBuildResult = kotlinc.buildWithClasspath(
          firstOrderContext,
          outputDirectory,
          invokingRule,
          ImmutableList.of(),
          sourceFilePaths,
          pathToSrcsList,
          extraArguments,
          declaredClassPathEntries,
          Optional.empty(),
          filesystem);

      String firstOrderStdout = stdout.getContentsAsString(Charsets.UTF_8);
      String firstOrderStderr = stderr.getContentsAsString(Charsets.UTF_8);
      Optional<String> returnedStderr;
      if (declaredDepsBuildResult != 0) {
        returnedStderr = Optional.of(firstOrderStderr); //processBuildFailure(context, firstOrderStdout, firstOrderStderr);
      } else {
        returnedStderr = Optional.empty();
      }
      return StepExecutionResult.of(declaredDepsBuildResult, returnedStderr);
    }
  }

  @VisibleForTesting
  Kotlinc getKotlinc() {
    return kotlinc;
  }

  @Override
  public String getDescription(ExecutionContext context) {
    return getKotlinc().getDescription(
        ImmutableList.of(), // TODO getOptions(context, getClasspathEntries()),
        sourceFilePaths,
        pathToSrcsList);
  }

  /**
   * @return The classpath entries used to invoke javac.
   */
  @VisibleForTesting
  ImmutableSortedSet<Path> getClasspathEntries() {
    return declaredClassPathEntries;
  }

  @VisibleForTesting
  ImmutableSortedSet<Path> getSrcs() {
    return sourceFilePaths;
  }
}
