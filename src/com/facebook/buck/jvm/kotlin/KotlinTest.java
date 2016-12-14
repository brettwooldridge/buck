package com.facebook.buck.jvm.kotlin;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

import com.facebook.buck.jvm.java.ForkMode;
import com.facebook.buck.jvm.java.JavaLibrary;
import com.facebook.buck.jvm.java.JavaRuntimeLauncher;
import com.facebook.buck.jvm.java.JavaTest;
import com.facebook.buck.jvm.java.TestType;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.Label;
import com.facebook.buck.rules.SourcePathResolver;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class KotlinTest extends JavaTest {

  public KotlinTest(
      BuildRuleParams params,
      SourcePathResolver resolver,
      JavaLibrary compiledTestsLibrary,
      ImmutableSet<Path> additionalClasspathEntries,
      Set<Label> labels,
      Set<String> contacts,
      TestType testType,
      JavaRuntimeLauncher javaRuntimeLauncher,
      List<String> vmArgs,
      Map<String, String> nativeLibsEnvironment,
      Optional<Long> testRuleTimeoutMs,
      Optional<Long> testCaseTimeoutMs,
      ImmutableMap<String, String> env,
      boolean runTestSeparately,
      ForkMode forkMode,
      Optional<Level> stdOutLogLevel,
      Optional<Level> stdErrLogLevel) {

    super(
        params,
        resolver,
        compiledTestsLibrary,
        additionalClasspathEntries,
        labels,
        contacts,
        testType,
        javaRuntimeLauncher,
        vmArgs,
        nativeLibsEnvironment,
        testRuleTimeoutMs,
        testCaseTimeoutMs,
        env,
        runTestSeparately,
        forkMode,
        stdOutLogLevel,
        stdErrLogLevel);
  }

}
