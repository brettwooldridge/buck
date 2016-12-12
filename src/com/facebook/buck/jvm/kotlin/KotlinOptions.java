package com.facebook.buck.jvm.kotlin;

import java.util.List;

import javax.annotation.Generated;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import com.facebook.buck.jvm.java.JavacOptions;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@Generated({"Immutables.generator"})
@Immutable
public class KotlinOptions {


  private final String sourceLevel;

  KotlinOptions(KotlinOptions.Builder builder) {
    this.sourceLevel = builder.sourceLevel;
  }

  public String getSourceLevel() {
    return sourceLevel;
  }

  @NotThreadSafe
  public static final class Builder {

    private static final long INIT_BIT_SOURCE_LEVEL = 0x1L;
    private static final long OPT_BIT_VERBOSE = 0x2L;

    private long initBits = 0x3L;
    private @Nullable String sourceLevel;

    private Builder() {
    }

    /**
     * Initializes the value for the {@link AbstractJavacOptions#getSourceLevel() sourceLevel} attribute.
     * @param sourceLevel The value for sourceLevel 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder setSourceLevel(String sourceLevel) {
      this.sourceLevel = Preconditions.checkNotNull(sourceLevel, "sourceLevel");
      initBits &= ~INIT_BIT_SOURCE_LEVEL;
      return this;
    }

    /**
     * Builds a new {@link JavacOptions JavacOptions}.
     * @return An immutable instance of JavacOptions
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public KotlinOptions build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new KotlinOptions(this);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = Lists.newArrayList();
      if ((initBits & INIT_BIT_SOURCE_LEVEL) != 0) attributes.add("sourceLevel");
      return "Cannot build KotlinOptions, some of required attributes are not set " + attributes;
    }
  }
}
