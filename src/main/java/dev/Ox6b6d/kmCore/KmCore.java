package dev.Ox6b6d.kmCore;

import dev.Ox6b6d.kmCore.config.ConfigManager;
import dev.Ox6b6d.kmCore.event.adapter.BlockListener;
import dev.Ox6b6d.kmCore.event.adapter.PlayerListener;
import dev.Ox6b6d.kmCore.event.hook.command.CommandHook;
import dev.Ox6b6d.kmCore.module.ModuleManager;
import dev.Ox6b6d.kmCore.module.impl.JoinModule;
import dev.Ox6b6d.kmCore.module.impl.ModulesCommandModule;
import dev.Ox6b6d.kmCore.module.impl.QuitModule;
import dev.Ox6b6d.kmCore.module.impl.mechanics.RedstoneObsidianModule;
import dev.Ox6b6d.kmCore.module.impl.mechanics.safe.SafeModule;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@SuppressWarnings("UnstableApiUsage")
public final class KmCore extends JavaPlugin {

    private static KmCore instance;
    private ConfigManager configManager;
    private ModuleManager moduleManager;

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
        this.moduleManager = new ModuleManager(configManager);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);

        moduleManager.register(new JoinModule());
        moduleManager.register(new QuitModule());
        moduleManager.register(new RedstoneObsidianModule());
        moduleManager.register(new SafeModule(this));
        moduleManager.register(new ModulesCommandModule(moduleManager, configManager));

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                moduleManager.getAllModules().stream()
                        .filter(m -> m instanceof CommandHook)
                        .map(m -> (CommandHook) m)
                        .forEach(hook ->
                                event.registrar().register(hook.getCommand(), new BasicCommand() {
                                    @Override
                                    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
                                        moduleManager.dispatchCommand(hook.getCommand(), stack.getSender(), args);
                                    }

                                    @Override
                                    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
                                        return hook.suggest(stack.getSender(), args);
                                    }
                                })
                        )
        );

        getLogger().info("[kmCore] Enabled.");
    }

    @Override
    public void onDisable() {
        moduleManager.disableAll();
        getLogger().info("[kmCore] Disabled.");
    }

    public static KmCore get()              { return instance; }
    public ModuleManager getModuleManager() { return moduleManager; }
    public ConfigManager getConfigManager() { return configManager; }
}