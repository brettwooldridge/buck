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

import static java.io.File.pathSeparator;
import static org.junit.Assert.assertEquals;

import com.facebook.buck.cli.BuckConfig;
import com.facebook.buck.cli.FakeBuckConfig;
import com.facebook.buck.io.MoreFiles;
import com.facebook.buck.io.ProjectFilesystem;
import com.facebook.buck.log.Logger;
import com.facebook.buck.testutil.integration.ProjectWorkspace;
import com.facebook.buck.testutil.integration.TemporaryPaths;
import com.facebook.buck.testutil.integration.TestDataHelper;
import com.facebook.buck.util.HumanReadableException;
import com.google.common.collect.ImmutableMap;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

public class KotlinBuckConfigTest {

  @Rule public TemporaryPaths tmp = new TemporaryPaths();

  private ProjectWorkspace workspace;
  private Path testDataDirectory;

  @Before
  public void setUp() throws InterruptedException, IOException {
    KotlinTestAssumptions.assumeUnixLike();
    workspace = TestDataHelper
        .createProjectWorkspaceForScenario(this, "faux_kotlin_home", tmp);
    testDataDirectory = TestDataHelper.getTestDataDirectory(KotlinBuckConfigTest.class);
  }

  //@Test
  public void testFindsKotlinCompilerInPath() throws HumanReadableException, IOException {
    // Get faux kotlinc binary location in project
    Path kotlinHome = workspace.resolve("../faux_kotlin_home/bin").normalize();
    Path kotlinCompiler = kotlinHome.resolve("kotlinc");
    MoreFiles.makeExecutable(kotlinCompiler);

    BuckConfig buckConfig = FakeBuckConfig.builder()
      .setSections(
          ImmutableMap.of("kotlin", ImmutableMap.of("external", "true")))
      .setEnvironment(
        ImmutableMap.of("PATH",
            kotlinHome.toString() + pathSeparator +
            System.getenv("PATH")))
      .build();

    KotlinBuckConfig kotlinBuckConfig = new KotlinBuckConfig(buckConfig);
    String command = kotlinBuckConfig.getCompilerPath().toString();
    assertEquals(command, kotlinCompiler.toString());
  }

  //@Test
  public void testFindsKotlinCompilerInHomeEnvironment() throws HumanReadableException, IOException {
    // Get faux kotlinc binary location in project
    Path kotlinCompiler = workspace.resolve("bin").resolve("kotlinc");
    MoreFiles.makeExecutable(kotlinCompiler);

    BuckConfig buckConfig = FakeBuckConfig.builder()
        .setSections(
            ImmutableMap.of("kotlin", ImmutableMap.of("external", "true")))
        .setEnvironment(
          ImmutableMap.of("KOTLIN_HOME", workspace.getPath(".").normalize().toString()))
        .build();

    KotlinBuckConfig kotlinBuckConfig = new KotlinBuckConfig(buckConfig);
    String command = kotlinBuckConfig.getCompilerPath().toString();
    assertEquals(command, kotlinCompiler.toString());
  }

  //@Test
  public void testFindsKotlinCompilerInConfigWithRelativePath()
      throws HumanReadableException, InterruptedException, IOException {
    // Get faux kotlinc binary location in project
    Path kotlinHome = workspace.resolve("../faux_kotlin_home").normalize();
    Path kotlinCompiler = kotlinHome.resolve("bin").resolve("kotlinc");
    MoreFiles.makeExecutable(kotlinCompiler);

    ProjectFilesystem filesystem = new ProjectFilesystem(workspace.resolve("."));
    BuckConfig buckConfig = FakeBuckConfig.builder()
        .setFilesystem(filesystem)
        .setSections(ImmutableMap.of(
            "kotlin", ImmutableMap.of("kotlin_home", kotlinHome.toString(),
                                      "external", "true")))
        .build();

    KotlinBuckConfig kotlinBuckConfig = new KotlinBuckConfig(buckConfig);
    String command = kotlinBuckConfig.getCompilerPath().toString();
    assertEquals(command, kotlinCompiler.toString());
  }

  //@Test
  public void testFindsKotlinRuntimeLibraryInPath() throws IOException {
    // Get faux kotlinc binary location in project
    Path kotlinPath = workspace.resolve("bin");
    Path kotlinCompiler = kotlinPath.resolve("kotlinc");
    MoreFiles.makeExecutable(kotlinCompiler);

    BuckConfig buckConfig = FakeBuckConfig.builder()
      .setSections(
          ImmutableMap.of("kotlin", ImmutableMap.of("external", "true")))
      .setEnvironment(
        ImmutableMap.of("PATH",
            kotlinPath.toString() + pathSeparator +
            System.getenv("PATH")))
      .build();

    KotlinBuckConfig kotlinBuckConfig = new KotlinBuckConfig(buckConfig);
    Path runtimeJar = kotlinBuckConfig.getPathToStdlibJar();
    Assert.assertThat(runtimeJar.toString(),
                      Matchers.containsString(workspace.getPath(".").normalize().toString()));
  }

  @Test
  public void testFindsKotlinRuntimeJarInConfigWithAbsolutePath()
      throws HumanReadableException, InterruptedException, IOException {

    Path kotlinRuntime = testDataDirectory
        .resolve("faux_kotlin_home")
        .resolve("libexec")
        .resolve("kotlin-runtime.jar");

    Logger.get(KotlinBuckConfigTest.class).warn(testDataDirectory.resolve("faux_kotlin_home").toAbsolutePath().toString());
    BuckConfig buckConfig = FakeBuckConfig.builder()
        .setSections(ImmutableMap.of(
            "kotlin",
            ImmutableMap.of("kotlin_home", testDataDirectory.resolve("faux_kotlin_home").toAbsolutePath().toString(),
                            "external", "true")))
        .build();

    KotlinBuckConfig kotlinBuckConfig = new KotlinBuckConfig(buckConfig);
    Path runtimeJar = kotlinBuckConfig.getPathToStdlibJar();
    assertEquals(kotlinRuntime, runtimeJar);
  }

  //@Test
  public void testFindsKotlinStdlibJarInConfigWithRelativePath()
      throws HumanReadableException, InterruptedException, IOException {

    ProjectFilesystem filesystem = new ProjectFilesystem(workspace.resolve("testdata/faux_kotlin_home"));
    BuckConfig buckConfig = FakeBuckConfig.builder()
        .setFilesystem(filesystem)
        .setSections(ImmutableMap.of(
            "kotlin",
            ImmutableMap.of("kotlin_home", filesystem.toString(),
                            "external", "true")))
        .build();

    KotlinBuckConfig kotlinBuckConfig = new KotlinBuckConfig(buckConfig);
    Path runtimeJar = kotlinBuckConfig.getPathToStdlibJar();
    assertEquals("lib/kotlin-runtime.jar", runtimeJar);
  }
}
