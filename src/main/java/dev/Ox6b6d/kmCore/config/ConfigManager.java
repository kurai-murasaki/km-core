package dev.Ox6b6d.kmCore.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final JavaPlugin plugin;
    private final File       configFile;
    private YamlConfiguration yaml;

    private final Map<String, Configurable> registered = new LinkedHashMap<>();

    public ConfigManager(JavaPlugin plugin) {
        this.plugin     = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        load();
    }

    private void load() {
        plugin.getDataFolder().mkdirs();
        if (!configFile.exists()) {
            yaml = new YamlConfiguration();
            return;
        }
        yaml = YamlConfiguration.loadConfiguration(configFile);
    }

    private void save() {
        try {
            yaml.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("[kmCore][ConfigManager] Failed to save config: " + e.getMessage());
        }
    }

    public void reload() {
        load();
        for (Configurable module : registered.values()) {
            inject(module);
        }
        plugin.getLogger().info("[kmCore][ConfigManager] Config reloaded.");
    }

    public void inject(Configurable module) {
        registered.put(module.getConfigSection(), module);

        ConfigurationSection section = getOrCreateSection(module.getConfigSection());

        boolean dirty = false;

        for (Field field : collectFields(module.getClass())) {
            ConfigValue annotation = field.getAnnotation(ConfigValue.class);
            if (annotation == null) continue;

            field.setAccessible(true);
            String key = annotation.key();

            try {
                if (!section.contains(key)) {
                    // Write default from field's current value
                    Object defaultValue = field.get(module);
                    section.set(key, defaultValue);
                    dirty = true;
                } else {
                    // Read from config and inject into field
                    Object value = readAs(section, key, field.getType());
                    if (value != null) {
                        field.set(module, value);
                    }
                }
            } catch (IllegalAccessException e) {
                plugin.getLogger().warning("[kmCore][ConfigManager] Could not access field '"
                        + field.getName() + "' in " + module.getConfigSection() + ": " + e.getMessage());
            }
        }

        if (dirty) save();
    }

    private ConfigurationSection getOrCreateSection(String path) {
        ConfigurationSection section = yaml.getConfigurationSection(path);
        if (section == null) {
            section = yaml.createSection(path);
        }
        return section;
    }

    private List<Field> collectFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            for (Field f : clazz.getDeclaredFields()) {
                if (f.isAnnotationPresent(ConfigValue.class)) {
                    fields.add(f);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private Object readAs(ConfigurationSection section, String key, Class<?> type) {
        if (type == String.class)                          return section.getString(key);
        if (type == int.class || type == Integer.class)    return section.getInt(key);
        if (type == double.class || type == Double.class)  return section.getDouble(key);
        if (type == boolean.class || type == Boolean.class) return section.getBoolean(key);
        if (type == List.class)                            return section.getStringList(key);
        plugin.getLogger().warning("[kmCore][ConfigManager] Unsupported config field type: " + type.getSimpleName());
        return null;
    }
    /**
     * Updates a single key inside a {@link Configurable} module's config section,
     * persists it to disk, and re-injects all registered values back into the module
     * so the running instance reflects the change immediately.
     *
     * <p>The {@code value} type must be one already supported by
     * {@link #readAs(ConfigurationSection, String, Class)} —
     * {@code String}, {@code int}, {@code double}, {@code boolean}, or {@code List<String>}.
     *
     * @param module the configurable module that owns the key
     * @param key    the key within the module's config section (e.g. {@code "sword-attack-speed"})
     * @param value  the new value to write
     */
    public void set(Configurable module, String key, Object value) {
        ConfigurationSection section = getOrCreateSection(module.getConfigSection());
        section.set(key, value);
        save();
        inject(module); // re-inject so the field reflects the new value immediately
    }
}