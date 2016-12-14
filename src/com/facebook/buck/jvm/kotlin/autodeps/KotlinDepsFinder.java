package com.facebook.buck.jvm.kotlin.autodeps;

import com.facebook.buck.jvm.java.JavaFileParser;
import com.facebook.buck.jvm.java.autodeps.JavaDepsFinder;
import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.rules.BuildEngine;
import com.facebook.buck.rules.BuildEngineBuildContext;
import com.facebook.buck.step.ExecutionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSortedMap;

public class KotlinDepsFinder extends JavaDepsFinder {

  public KotlinDepsFinder(
      ImmutableSortedMap<String,
      BuildTarget> javaPackageMapping,
      JavaFileParser javaFileParser,
      ObjectMapper objectMapper,
      BuildEngineBuildContext buildContext,
      ExecutionContext executionContext,
      BuildEngine buildEngine) {

    super(
        javaPackageMapping,
        javaFileParser,
        objectMapper,
        buildContext,
        executionContext,
        buildEngine);
  }
}
