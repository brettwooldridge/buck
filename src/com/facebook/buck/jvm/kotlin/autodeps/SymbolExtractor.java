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

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import com.facebook.buck.jvm.java.autodeps.Symbols;
import com.facebook.buck.jvm.kotlin.KotlinFileParser;
import com.google.common.base.Charsets;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.io.Files;

final class SymbolExtractor {

  private SymbolExtractor() {}

  public static Symbols extractSymbols(
      KotlinFileParser kotlinFileParser,
      boolean shouldRecordRequiredSymbols,
      ImmutableSortedSet<Path> absolutePaths) {
    Set<String> providedSymbols = new HashSet<>();
    Set<String> requiredSymbols = new HashSet<>();
    Set<String> exportedSymbols = new HashSet<>();

    for (Path src : absolutePaths) {
      String code;
      try {
        code = Files.toString(src.toFile(), Charsets.UTF_8);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      KotlinFileParser.KotlinFileFeatures features = kotlinFileParser
          .extractFeaturesFromKotlinCode(code);
      if (shouldRecordRequiredSymbols) {
        requiredSymbols.addAll(features.requiredSymbols);
        exportedSymbols.addAll(features.exportedSymbols);
      }

      providedSymbols.addAll(features.providedSymbols);
    }

    return new Symbols(
        providedSymbols,
        FluentIterable.from(requiredSymbols).filter(SymbolExtractor::isNotABuiltInSymbol),
        FluentIterable.from(exportedSymbols).filter(SymbolExtractor::isNotABuiltInSymbol));
  }

  private static boolean isNotABuiltInSymbol(String symbol) {
    // Ignore things in java.* and kotlin.*
    return !symbol.startsWith("java.") && !symbol.startsWith("kotlin.");
  }
}
