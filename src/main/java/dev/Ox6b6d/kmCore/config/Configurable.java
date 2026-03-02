package dev.Ox6b6d.kmCore.config;

/**
 * Implemented by modules that want to participate in the config system.
 * The section name is used as the top-level key in config.yml.
 *
 * Example config.yml layout:
 *   safe:
 *     allow-crowbar: true
 *   redstone-obsidian:
 *     play-sound: true
 */
public interface Configurable {

    /**
     * The section name this module writes to in config.yml.
     * Should be lowercase-kebab-case
     */
    String getConfigSection();
}