package dev.Ox6b6d.kmCore.module;

import dev.Ox6b6d.kmCore.config.Configurable;
import dev.Ox6b6d.kmCore.config.ConfigManager;
import dev.Ox6b6d.kmCore.event.hook.Hook;
import dev.Ox6b6d.kmCore.event.hook.command.CommandHook;
import org.bukkit.command.CommandSender;

import java.util.*;

public class ModuleManager {

    private final Map<String, Module> allModules  = new LinkedHashMap<>();
    private final List<Module>        activeModules = new ArrayList<>();

    private final Map<Class<? extends Hook>, List<Hook>> hookMap    = new HashMap<>();
    private final Map<String, CommandHook>               commandMap = new HashMap<>();

    private final ConfigManager configManager;

    public ModuleManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void register(Module module) {
        String name = module.getName();
        if (allModules.containsKey(name)) return;
        allModules.put(name, module);
        activate(module);
    }

    public void unregister(Module module) {
        if (!allModules.containsValue(module)) return;
        deactivate(module);
        allModules.remove(module.getName());
    }


    public boolean enableModule(String name) {
        Module module = allModules.get(name);
        if (module == null) return false;
        if (activeModules.contains(module)) return true;
        activate(module);
        return true;
    }

    public boolean disableModule(String name) {
        Module module = allModules.get(name);
        if (module == null) return false;
        if (!activeModules.contains(module)) return true;
        deactivate(module);
        return true;
    }


    public boolean isEnabled(String name) {
        Module module = allModules.get(name);
        return module != null && activeModules.contains(module);
    }

    public Collection<Module> getAllModules() {
        return Collections.unmodifiableCollection(allModules.values());
    }

    public Set<Class<? extends Hook>> getHookTypes(Module module) {
        return collectHookInterfaces(module.getClass());
    }

    public boolean dispatchCommand(String label, CommandSender sender, String[] args) {
        CommandHook hook = commandMap.get(label.toLowerCase());
        if (hook == null) return false;
        return hook.onCommand(sender, args);
    }

    @SuppressWarnings("unchecked")
    public <T extends Hook> List<T> getHooks(Class<T> hookType) {
        List<Hook> hooks = hookMap.getOrDefault(hookType, Collections.emptyList());
        return Collections.unmodifiableList((List<T>) hooks);
    }

    public void disableAll() {
        new ArrayList<>(allModules.values()).forEach(this::unregister);
    }


    private void activate(Module module) {
        // Inject config before enable() so the module has its values ready
        if (module instanceof Configurable configurable) {
            configManager.inject(configurable);
        }

        activeModules.add(module);
        module.enable();
        registerHooks(module);

        if (module instanceof CommandHook commandHook) {
            commandMap.put(commandHook.getCommand().toLowerCase(), commandHook);
        }
    }

    private void deactivate(Module module) {
        activeModules.remove(module);
        module.disable();
        hookMap.values().forEach(list -> list.remove(module));

        if (module instanceof CommandHook commandHook) {
            commandMap.remove(commandHook.getCommand().toLowerCase());
        }
    }

    private void registerHooks(Module module) {
        collectHookInterfaces(module.getClass()).forEach(hookType ->
                hookMap.computeIfAbsent(hookType, k -> new ArrayList<>())
                        .add((Hook) module)
        );
    }

    private Set<Class<? extends Hook>> collectHookInterfaces(Class<?> clazz) {
        Set<Class<? extends Hook>> result = new HashSet<>();
        if (clazz == null || clazz == Object.class) return result;

        for (Class<?> iface : clazz.getInterfaces()) {
            if (Hook.class.isAssignableFrom(iface) && iface != Hook.class) {
                @SuppressWarnings("unchecked")
                Class<? extends Hook> hookType = (Class<? extends Hook>) iface;
                result.add(hookType);
            }
            result.addAll(collectHookInterfaces(iface));
        }

        result.addAll(collectHookInterfaces(clazz.getSuperclass()));
        return result;
    }
}