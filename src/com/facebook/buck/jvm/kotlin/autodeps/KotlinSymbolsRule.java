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

package com.facebook.buck.jvm.kotlin.autodeps;

import com.facebook.buck.io.ProjectFilesystem;
import com.facebook.buck.jvm.java.autodeps.JavaSymbolsRule;
import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.model.Flavor;
import com.facebook.buck.model.ImmutableFlavor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSortedSet;

final class KotlinSymbolsRule extends JavaSymbolsRule {

  private static final String TYPE = "kotlin_symbols";
  public static final Flavor KOTLIN_SYMBOLS = ImmutableFlavor.of(TYPE);

  KotlinSymbolsRule(
      BuildTarget libraryBuildTarget,
      SymbolsFinder symbolsFinder,
      ImmutableSortedSet<String> generatedSymbols,
      ObjectMapper objectMapper,
      ProjectFilesystem projectFilesystem) {

    super(
        libraryBuildTarget,
        symbolsFinder,
        generatedSymbols,
        objectMapper,
        projectFilesystem);
  }

  @Override
  protected BuildTarget getFlavoredBuildTarget(BuildTarget buildTarget) {
    return buildTarget.withFlavors(KOTLIN_SYMBOLS);
  }
}
