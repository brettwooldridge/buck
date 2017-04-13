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

package com.facebook.buck.jvm.kotlin;

import com.facebook.buck.jvm.java.DefaultJavaLibraryBuilder;
import com.facebook.buck.jvm.java.ForkMode;
import com.facebook.buck.jvm.java.HasJavaAbi;
import com.facebook.buck.jvm.java.JavaOptions;
import com.facebook.buck.jvm.java.JavaTest;
import com.facebook.buck.jvm.java.JavacOptions;
import com.facebook.buck.jvm.java.TestType;
import com.facebook.buck.parser.NoSuchBuildTargetException;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.BuildRuleResolver;
import com.facebook.buck.rules.CellPathResolver;
import com.facebook.buck.rules.Description;
import com.facebook.buck.rules.SourcePathResolver;
import com.facebook.buck.rules.SourcePathRuleFinder;
import com.facebook.buck.rules.TargetGraph;
import com.facebook.infer.annotation.SuppressFieldNotInitialized;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import java.util.Optional;
import java.util.logging.Level;

public class KotlinTestDescription implements Description<KotlinTestDescription.Arg> {

  private final KotlinBuckConfig kotlinBuckConfig;
  private final JavaOptions javaOptions;
  private final JavacOptions templateJavacOptions;
  private final Optional<Long> defaultTestRuleTimeoutMs;

  public KotlinTestDescription(
      KotlinBuckConfig kotlinBuckConfig,
      JavaOptions javaOptions,
      JavacOptions templateOptions,
      Optional<Long> defaultTestRuleTimeoutMs) {
    this.kotlinBuckConfig = kotlinBuckConfig;
    this.javaOptions = javaOptions;
    this.templateJavacOptions = templateOptions;
    this.defaultTestRuleTimeoutMs = defaultTestRuleTimeoutMs;
  }

  @Override
  public Arg createUnpopulatedConstructorArg() {
    return new Arg();
  }

  @Override
  public <A extends Arg> BuildRule createBuildRule(
      TargetGraph targetGraph,
      BuildRuleParams params,
      BuildRuleResolver resolver,
      CellPathResolver cellRoots,
      A args) throws NoSuchBuildTargetException {
    BuildRuleParams testsLibraryParams = params
        .withAppendedFlavor(JavaTest.COMPILED_TESTS_LIBRARY_FLAVOR);

    DefaultJavaLibraryBuilder defaultJavaLibraryBuilder = new DefaultKotlinLibraryBuilder(
        testsLibraryParams,
        resolver,
        kotlinBuckConfig)
        .setArgs(args)
        .setGeneratedSourceFolder(templateJavacOptions.getGeneratedSourceFolderName());

    if (HasJavaAbi.isAbiTarget(params.getBuildTarget())) {
      return defaultJavaLibraryBuilder.buildAbi();
    }

    DefaultKotlinLibrary testsLibrary = (DefaultKotlinLibrary) resolver.addToIndex(defaultJavaLibraryBuilder.build());

//    Kotlinc kotlinc = kotlinBuckConfig.getKotlinc();
//    KotlincToJarStepFactory stepFactory = new KotlincToJarStepFactory(
//        kotlinc,
//        args.extraKotlincArguments);

//    BuildRuleParams testsLibraryParams = stepFactory.addInputs(params, ruleFinder)
//         .withAppendedFlavor(JavaTest.COMPILED_TESTS_LIBRARY_FLAVOR);

    SourcePathRuleFinder ruleFinder = new SourcePathRuleFinder(resolver);
    SourcePathResolver pathResolver = new SourcePathResolver(ruleFinder);
    return new KotlinTest(
        params.copyReplacingDeclaredAndExtraDeps(
            Suppliers.ofInstance(ImmutableSortedSet.of(testsLibrary)),
            Suppliers.ofInstance(ImmutableSortedSet.of())),
        pathResolver,
        testsLibrary,
        ImmutableSet.of(kotlinBuckConfig.getPathToRuntimeJar(ruleFinder)),
        args.labels,
        args.contacts,
        args.testType.orElse(TestType.JUNIT),
        javaOptions.getJavaRuntimeLauncher(),
        args.vmArgs,
        ImmutableMap.of(), /* nativeLibsEnvironment */
        args.testRuleTimeoutMs.map(Optional::of).orElse(defaultTestRuleTimeoutMs),
        args.testCaseTimeoutMs,
        args.env,
        args.getRunTestSeparately(),
        args.getForkMode(),
        args.stdOutLogLevel,
        args.stdErrLogLevel);
  }

  @SuppressFieldNotInitialized
  public static class Arg extends KotlinLibraryDescription.Arg {
    public ImmutableSortedSet<String> contacts = ImmutableSortedSet.of();
    public ImmutableList<String> vmArgs = ImmutableList.of();
    public Optional<TestType> testType;
    public Optional<Boolean> runTestSeparately;
    public Optional<ForkMode> forkMode;
    public Optional<Level> stdErrLogLevel;
    public Optional<Level> stdOutLogLevel;
    public Optional<Long> testRuleTimeoutMs;
    public Optional<Long> testCaseTimeoutMs;
    public ImmutableMap<String, String> env = ImmutableMap.of();

    public boolean getRunTestSeparately() {
      return runTestSeparately.orElse(false);
    }
    public ForkMode getForkMode() {
      return forkMode.orElse(ForkMode.NONE);
    }
  }
}
