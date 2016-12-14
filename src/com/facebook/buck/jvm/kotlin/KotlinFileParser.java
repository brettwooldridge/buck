package com.facebook.buck.jvm.kotlin;

import com.facebook.buck.jvm.java.JavaFileParser;
import com.facebook.buck.log.Logger;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;

/**
 * Extracts the set of exported symbols (class and enum names) from a Kotlin code file, using the
 * Parser from the Kotlin compiler.
 */
public class KotlinFileParser {

  private static final Logger LOG = Logger.get(JavaFileParser.class);

  private final int klsLevel;
  private final String kotlinVersion;

  private static final ImmutableMap<String, String> kotlinVersionMap =
      ImmutableMap.<String, String>builder()
         .put("1", "1.0")
         .build();

  private KotlinFileParser(int klsLevel, String kotlinVersion) {
    this.klsLevel = klsLevel;
    this.kotlinVersion = kotlinVersion;
  }

  public static KotlinFileParser createKotlinFileParser(KotlinOptions options) {
    String kotlinVersion = Preconditions.checkNotNull(kotlinVersionMap.get(options.getSourceLevel()));
    KotlinFileParser kotlinFileParser = new KotlinFileParser(1, kotlinVersion);
    LOG.debug("KotlinFileParser created (language level %d, version %s.", kotlinFileParser.klsLevel, kotlinFileParser.kotlinVersion);
    return kotlinFileParser;
  }

//  private static enum DependencyType {
//    REQUIRED,
//    EXPORTED,
//  }

  public KotlinFileFeatures extractFeaturesFromKotlinCode(String code) {


    return new KotlinFileFeatures();
//        providedSymbols.build(),
//        ImmutableSortedSet.copyOf(totalRequiredSymbols),
//        totalExportedSymbols);
  }

  public static class KotlinFileFeatures {
    public final ImmutableSortedSet<String> providedSymbols;
    public final ImmutableSortedSet<String> requiredSymbols;

    /**
     * Exported symbols are those that need to be on the classpath when compiling against the
     * providedSymbols. These include:
     * <ul>
     *   <li>Parameter types for non-private methods of non-private types.
     *   <li>Return types for non-private methods of non-private types.
     *   <li>Parent classes of non-private provided types.
     *   <li>Parent interfaces of non-private provided types.
     *   <li>Types of non-private fields of non-private types.
     * </ul>
     */
    public final ImmutableSortedSet<String> exportedSymbols;

    // Remove me!
    private KotlinFileFeatures() {
      this.providedSymbols = ImmutableSortedSet.<String>naturalOrder().build();
      this.requiredSymbols = ImmutableSortedSet.<String>naturalOrder().build();
      this.exportedSymbols = ImmutableSortedSet.<String>naturalOrder().build();
    }

    private KotlinFileFeatures(
        ImmutableSortedSet<String> providedSymbols,
        ImmutableSortedSet<String> requiredSymbols,
        ImmutableSortedSet<String> exportedSymbols) {
      this.providedSymbols = providedSymbols;
      this.requiredSymbols = requiredSymbols;
      this.exportedSymbols = exportedSymbols;
    }

    @Override
    public String toString() {
      return String.format(
          "providedSymbols=%s; requiredSymbols=%s; exportedSymbols=%s",
          providedSymbols,
          requiredSymbols,
          exportedSymbols);
    }
  }

}
