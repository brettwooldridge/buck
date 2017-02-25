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

import com.facebook.buck.cli.BuckConfig;
import com.facebook.buck.io.ExecutableFinder;
import com.facebook.buck.util.HumanReadableException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import javax.annotation.Nullable;

public class KotlinBuckConfig {
  private static final String SECTION = "kotlin";

  private static final Path DEFAULT_KOTLIN_COMPILER = Paths.get("kotlinc");

  private final BuckConfig delegate;
  private @Nullable Path kotlinHome;

  public KotlinBuckConfig(BuckConfig delegate) {
    this.delegate = delegate;
  }

  public Kotlinc getKotlinc() {
    if (isExternalCompilation()) {
      return new ExternalKotlinc(getCompilerPath());
    } else {
      ImmutableSet<Path> classpathEntries = ImmutableSet.of(
          getPathToRuntimeJar(),
          getPathToCompilerJar());

      return
          new JarBackedReflectedKotlinc(classpathEntries,
              ImmutableList.of());
    }
  }


  Path getCompilerPath() {
    Path compilerPath = getKotlinHome().resolve("kotlinc");
    if (!Files.isExecutable(compilerPath)) {
      compilerPath = getKotlinHome().resolve(Paths.get("bin", "kotlinc"));
      if (!Files.isExecutable(compilerPath)) {
        throw new HumanReadableException("Could not resolve kotlinc location.");
      }
    }

    return new ExecutableFinder().getExecutable(compilerPath, delegate.getEnvironment());
  }

  /**
   * Get the path to the Kotlin runtime jar.
   * @return the Kotlin runtime jar path
   */
  Path getPathToRuntimeJar() {
    Optional<String> value = delegate.getValue(SECTION, "runtime_jar");

    if (value.isPresent()) {
      boolean isAbsolute = Paths.get(value.get()).isAbsolute();
      if (isAbsolute) {
        return delegate.getPath(SECTION, "runtime_jar", false).get().normalize();
      } else {
        return delegate.getPath(SECTION, "runtime_jar", true).get();
      }
    }

    Path runtime = getKotlinHome().resolve("kotlin-runtime.jar");
    if (Files.isRegularFile(runtime)) {
      return runtime.toAbsolutePath().normalize();
    }

    runtime = getKotlinHome().resolve(Paths.get("lib", "kotlin-runtime.jar"));
    if (Files.isRegularFile(runtime)) {
      return runtime.toAbsolutePath().normalize();
    }

    throw new HumanReadableException("Could not resolve kotlin runtime JAR location.");
  }

  /**
   * Get the path to the Kotlin runtime jar.
   * @return the Kotlin runtime jar path
   */
  Path getPathToCompilerJar() {
    Optional<String> value = delegate.getValue(SECTION, "compiler_jar");

    if (value.isPresent()) {
      boolean isAbsolute = Paths.get(value.get()).isAbsolute();
      if (isAbsolute) {
        return delegate.getPath(SECTION, "compiler_jar", false).get().normalize();
      } else {
        return delegate.getPath(SECTION, "compiler_jar", true).get();
      }
    }

    Path compiler = getKotlinHome().resolve("kotlin-compiler.jar");
    if (Files.isRegularFile(compiler)) {
      return compiler.toAbsolutePath().normalize();
    }

    compiler = getKotlinHome().resolve(Paths.get("lib", "kotlin-compiler.jar"));
    if (Files.isRegularFile(compiler)) {
      return compiler.toAbsolutePath().normalize();
    }

    throw new HumanReadableException("Could not resolve kotlin compiler JAR location (kotlin home:" + getKotlinHome() + ".");
  }

  boolean isExternalCompilation() {
    Optional<Boolean> value = delegate.getBoolean(SECTION, "external");
    return value.orElse(false);
  }

  private Path getKotlinHome() {
    if (kotlinHome != null) {
      return kotlinHome;
    }

    try {
      // Check the buck configuration for a specified kotlin compliler
      Optional<String> value = delegate.getValue(SECTION, "compiler");
      boolean isAbsolute = (value.isPresent() && Paths.get(value.get()).isAbsolute());
      Optional<Path> compilerPath = delegate.getPath(SECTION, "compiler", !isAbsolute);
      if (compilerPath.isPresent()) {
        if (Files.isExecutable(compilerPath.get())) {
          kotlinHome = compilerPath.get().toRealPath().getParent().normalize();
          if (kotlinHome != null && kotlinHome.endsWith(Paths.get("bin"))) {
            kotlinHome = kotlinHome.getParent().normalize();
          }
          return kotlinHome;
        } else {
          throw new HumanReadableException(
              "Could not deduce kotlin home directory from path " + compilerPath.toString());
        }
      } else {
        // If the KOTLIN_HOME environment variable is specified we trust it
        String home = delegate.getEnvironment().get("KOTLIN_HOME");
        if (home != null) {
          kotlinHome = Paths.get(home).normalize();
          return kotlinHome;
        } else {
          // Lastly, we try to resolve from the system PATH
          Optional<Path> compiler = new ExecutableFinder()
              .getOptionalExecutable(DEFAULT_KOTLIN_COMPILER, delegate.getEnvironment());
          if (compiler.isPresent()) {
            kotlinHome = compiler.get().toRealPath().getParent().normalize();
            if (kotlinHome != null && kotlinHome.endsWith(Paths.get("bin"))) {
              kotlinHome = kotlinHome.getParent().normalize();
            }
            return kotlinHome;
          } else {
            throw new HumanReadableException(
                "Could not resolve kotlin home directory, Consider setting KOTLIN_HOME.");
          }
        }
      }
    } catch (IOException io) {
      throw new HumanReadableException(
          "Could not resolve kotlin home directory, Consider setting KOTLIN_HOME.", io);
    }
  }

  public boolean shouldSuggestDependencies() {
    return delegate.getBooleanValue(SECTION, "suggest_dependencies", false);
  }
}
