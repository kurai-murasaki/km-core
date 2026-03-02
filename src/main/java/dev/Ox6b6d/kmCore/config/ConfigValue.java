package dev.Ox6b6d.kmCore.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as a config entry.
 * The field will be read from (and written to) the module's config section.
 *
 * Supported field types: String, int, double, boolean, List<String>
 *
 * Example:
 *   @ConfigValue(key = "allow-crowbar", comment = "Whether crowbar cracking is enabled")
 *   private boolean allowCrowbar = true;
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigValue {

    /** Key within the module's config section. */
    String key();

    /** Shown as a comment in config.yml above the entry. */
    String comment() default "";
}