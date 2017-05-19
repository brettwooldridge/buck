package com.facebook.buck.jvm.kotlin;

import static com.google.common.collect.Iterables.transform;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import com.facebook.buck.io.ProjectFilesystem;
import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.RuleKeyObjectSink;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.rules.SourcePathResolver;
import com.facebook.buck.rules.SourcePathRuleFinder;
import com.facebook.buck.step.ExecutionContext;
import com.facebook.buck.util.Console;
import com.facebook.buck.util.DefaultProcessExecutor;
import com.facebook.buck.util.ProcessExecutor;
import com.facebook.buck.util.ProcessExecutorParams;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;

public class ExternalKotlinc implements Kotlinc {

  private static final KotlincVersion DEFAULT_VERSION = KotlincVersion.of("unknown version");
  private static final String CLASSPATH_FLAG = "-cp";
  private static final String DESTINATION_FLAG = "-d";
  private static final String INCLUDE_RUNTIME_FLAG = "-include-runtime";

  private final Path pathToKotlinc;
  private final Supplier<KotlincVersion> version;

  public ExternalKotlinc(final Path pathToKotlinc) {
    this.pathToKotlinc = pathToKotlinc;

    this.version = Suppliers.memoize(
        () -> {
          ProcessExecutorParams params = ProcessExecutorParams.builder()
              .setCommand(
                  ImmutableList.of(pathToKotlinc.toString(),
                      "-version"))
              .build();
          ProcessExecutor.Result result;
          try {
            result = createProcessExecutor().launchAndExecute(params);
          } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
          }
          Optional<String> stderr = result.getStderr();
          String output = stderr.orElse("").trim();
          if (Strings.isNullOrEmpty(output)) {
            return DEFAULT_VERSION;
          } else {
            return KotlincVersion.of(output);
          }
        });
  }

  @Override
  public void appendToRuleKey(RuleKeyObjectSink sink) {
    if (DEFAULT_VERSION.equals(getVersion())) {
      // What we really want to do here is use a VersionedTool, however, this will suffice for now.
      sink.setReflectively("kotlinc", getShortName());
    } else {
      sink.setReflectively("kotlinc.version", getVersion().toString());
    }
  }

  @Override
  public ImmutableCollection<BuildRule> getDeps(SourcePathRuleFinder ruleFinder) {
    return ruleFinder.filterBuildRuleInputs(getInputs());
  }

  @Override
  public ImmutableCollection<SourcePath> getInputs() {
    return ImmutableSortedSet.of();
  }

  @Override
  public ImmutableList<String> getCommandPrefix(SourcePathResolver resolver) {
    return ImmutableList.of(pathToKotlinc.toString());
  }

  @Override
  public KotlincVersion getVersion() {
    return version.get();
  }

  @Override
  public int buildWithClasspath(
      ExecutionContext context,
      Path outputDirectory,
      BuildTarget invokingRule,
      ImmutableList<String> options,
      ImmutableSortedSet<Path> kotlinSourceFilePaths,
      Path pathToSrcsList,
      ImmutableList<String> extraArguments,
      ImmutableSortedSet<Path> declaredClassPathEntries,
      Optional<Path> workingDirectory,
      ProjectFilesystem projectFilesystem)
      throws InterruptedException {

    final ImmutableList.Builder<String> command = ImmutableList.builder();
    command.add(pathToKotlinc.toString());

    String classpath =
        Joiner.on(File.pathSeparator).join(transform(declaredClassPathEntries, Object::toString));
    command
        .add(INCLUDE_RUNTIME_FLAG)
        .add(CLASSPATH_FLAG)
        .add(classpath.isEmpty() ? "''" : classpath)
        .add(DESTINATION_FLAG)
        .add(outputDirectory.toString());

    command
        .addAll(extraArguments)
        .addAll(transform(kotlinSourceFilePaths, Object::toString));

    // Run the command
    int exitCode = -1;
    try {
      ProcessExecutorParams params = ProcessExecutorParams.builder()
          .setCommand(command.build())
          .setEnvironment(context.getEnvironment())
          .setDirectory(projectFilesystem.getRootPath().toAbsolutePath())
          .build();
      ProcessExecutor.Result result = context.getProcessExecutor().launchAndExecute(params);
      exitCode = result.getExitCode();
    } catch (IOException e) {
      e.printStackTrace(context.getStdErr());
      return exitCode;
    }

    return exitCode;
  }

  @VisibleForTesting
  ProcessExecutor createProcessExecutor() {
    return new DefaultProcessExecutor(Console.createNullConsole());
  }

  @Override
  public String getDescription(
      ImmutableList<String> options,
      ImmutableSortedSet<Path> kotlinSourceFilePaths,
      Path pathToSrcsList) {
    StringBuilder builder = new StringBuilder(getShortName());
    builder.append(" ");
    Joiner.on(" ").appendTo(builder, options);
    builder.append(" ");
    builder.append("@").append(pathToSrcsList);

    return builder.toString();
  }

  @Override
  public String getShortName() {
    return pathToKotlinc.toString();
  }

  @Override
  public ImmutableMap<String, String> getEnvironment(SourcePathResolver resolver) {
    return ImmutableMap.of();
  }
}
