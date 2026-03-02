package dev.Ox6b6d.kmCore.module.impl;

import dev.Ox6b6d.kmCore.config.Configurable;
import dev.Ox6b6d.kmCore.config.ConfigManager;
import dev.Ox6b6d.kmCore.config.ConfigValue;
import dev.Ox6b6d.kmCore.event.hook.Hook;
import dev.Ox6b6d.kmCore.module.Module;
import dev.Ox6b6d.kmCore.module.ModuleManager;
import dev.Ox6b6d.kmCore.event.hook.command.CommandHook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ModulesCommandModule implements Module, Configurable, CommandHook {

    private static final MiniMessage MM = MiniMessage.miniMessage();


    @ConfigValue(key = "msg.no-permission",      comment = "Sent when player lacks kmcore.modules.")
    private String msgNoPermission = "<red>You don't have permission to use this command.";

    @ConfigValue(key = "msg.unknown-module",     comment = "Sent when a module name is not found. Use {name}.")
    private String msgUnknownModule = "<red>Unknown module: <gray>{name}";

    @ConfigValue(key = "msg.already-enabled",    comment = "Sent when enabling an already-enabled module. Use {name}.")
    private String msgAlreadyEnabled = "<gray>{name} <dark_gray>is already enabled.";

    @ConfigValue(key = "msg.already-disabled",   comment = "Sent when disabling an already-disabled module. Use {name}.")
    private String msgAlreadyDisabled = "<gray>{name} <dark_gray>is already disabled.";

    @ConfigValue(key = "msg.enabled",            comment = "Sent on successful module enable. Use {name}.")
    private String msgEnabled = "<dark_gray>[<green>✔<dark_gray>] <gray>{name} <dark_gray>enabled.";

    @ConfigValue(key = "msg.disabled",           comment = "Sent on successful module disable. Use {name}.")
    private String msgDisabled = "<dark_gray>[<red>✘<dark_gray>] <gray>{name} <dark_gray>disabled.";

    @ConfigValue(key = "msg.cannot-disable-self", comment = "Sent when trying to disable ModulesCommandModule.")
    private String msgCannotDisableSelf = "<dark_gray>You cannot disable the modules command handler.";

    @ConfigValue(key = "msg.reloaded",           comment = "Sent on successful config reload.")
    private String msgReloaded = "<dark_gray>[<green>✔<dark_gray>] <gray>Config reloaded.";

    @ConfigValue(key = "msg.none",               comment = "Shown when a module list is empty.")
    private String msgNone = "<dark_gray>  None.";


    @ConfigValue(key = "format.header",          comment = "Header for module lists. Use {title}.")
    private String formatHeader = "<dark_gray>— <light_purple><bold>{title}</bold> <dark_gray>—";

    @ConfigValue(key = "format.row-enabled",     comment = "Row for an enabled module. Use {name} and optionally {hooks}.")
    private String formatRowEnabled = "<dark_gray> [<green>✔<dark_gray>] <gray>{name}{hooks}";

    @ConfigValue(key = "format.row-disabled",    comment = "Row for a disabled module. Use {name} and optionally {hooks}.")
    private String formatRowDisabled = "<dark_gray> [<red>✘<dark_gray>] <gray>{name}{hooks}";

    @ConfigValue(key = "format.hooks-suffix",    comment = "Appended after module name when hooks are shown. Use {hooks}.")
    private String formatHooksSuffix = "<dark_gray>  →  <dark_purple>{hooks}";

    @ConfigValue(key = "format.usage-line",      comment = "A usage line. Use {cmd} and {desc}.")
    private String formatUsageLine = "<gray>  {cmd} <dark_gray>— {desc}";


    private final ModuleManager moduleManager;
    private final ConfigManager configManager;

    public ModulesCommandModule(ModuleManager moduleManager, ConfigManager configManager) {
        this.moduleManager = moduleManager;
        this.configManager = configManager;
    }

    @Override public String getName()          { return "ModulesCommandModule"; }
    @Override public String getConfigSection() { return "kmcore-command"; }
    @Override public void enable()             {}
    @Override public void disable()            {}
    @Override public String getCommand()       { return "kmcore"; }


    @Override
    public Collection<String> suggest(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kmcore.modules")) return List.of();

        if (args.length == 1) {
            return List.of("modules", "reload").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("modules")) {
            return List.of("list", "all", "enable", "disable").stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("modules")) {
            return switch (args[1].toLowerCase()) {
                case "enable" -> moduleManager.getAllModules().stream()
                        .map(Module::getName)
                        .filter(name -> !moduleManager.isEnabled(name))
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
                case "disable" -> moduleManager.getAllModules().stream()
                        .map(Module::getName)
                        .filter(name -> moduleManager.isEnabled(name))
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
                case "list", "all" -> List.of("hooks").stream()
                        .filter(s -> s.startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
                default -> List.of();
            };
        }

        return List.of();
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kmcore.modules")) {
            sender.sendMessage(MM.deserialize(msgNoPermission));
            return true;
        }

        if (args.length == 0) { sendUsage(sender); return true; }

        if (args[0].equalsIgnoreCase("reload")) return cmdReload(sender);

        if (!args[0].equalsIgnoreCase("modules")) { sendUsage(sender); return true; }

        if (args.length == 1) { sendUsage(sender); return true; }

        return switch (args[1].toLowerCase()) {
            case "list"    -> cmdList(sender, args);
            case "all"     -> cmdAll(sender, args);
            case "enable"  -> cmdEnable(sender, args);
            case "disable" -> cmdDisable(sender, args);
            default        -> { sendUsage(sender); yield true; }
        };
    }


    private boolean cmdReload(CommandSender sender) {
        configManager.reload();
        sender.sendMessage(MM.deserialize(msgReloaded));
        return true;
    }

    private boolean cmdList(CommandSender sender, String[] args) {
        boolean showHooks = args.length >= 3 && args[2].equalsIgnoreCase("hooks");

        Collection<Module> enabled = moduleManager.getAllModules().stream()
                .filter(m -> moduleManager.isEnabled(m.getName()))
                .toList();

        sender.sendMessage(header("Enabled Modules (" + enabled.size() + ")"));
        if (enabled.isEmpty()) { sender.sendMessage(MM.deserialize(msgNone)); return true; }
        for (Module m : enabled)
            sender.sendMessage(moduleRow(m.getName(), true, showHooks ? moduleManager.getHookTypes(m) : null));
        return true;
    }

    private boolean cmdAll(CommandSender sender, String[] args) {
        boolean showHooks = args.length >= 3 && args[2].equalsIgnoreCase("hooks");

        Collection<Module> all = moduleManager.getAllModules();
        sender.sendMessage(header("All Modules (" + all.size() + ")"));
        if (all.isEmpty()) { sender.sendMessage(MM.deserialize(msgNone)); return true; }
        for (Module m : all) {
            boolean enabled = moduleManager.isEnabled(m.getName());
            sender.sendMessage(moduleRow(m.getName(), enabled, showHooks ? moduleManager.getHookTypes(m) : null));
        }
        return true;
    }

    private boolean cmdEnable(CommandSender sender, String[] args) {
        if (args.length < 3) { sender.sendMessage(MM.deserialize("<dark_gray>Usage: /kmcore modules enable <name>")); return true; }
        String name = args[2];
        if (moduleManager.isEnabled(name)) {
            sender.sendMessage(MM.deserialize(msgAlreadyEnabled.replace("{name}", name)));
            return true;
        }
        if (!moduleManager.enableModule(name)) {
            sender.sendMessage(MM.deserialize(msgUnknownModule.replace("{name}", name)));
            return true;
        }
        sender.sendMessage(MM.deserialize(msgEnabled.replace("{name}", name)));
        return true;
    }

    private boolean cmdDisable(CommandSender sender, String[] args) {
        if (args.length < 3) { sender.sendMessage(MM.deserialize("<dark_gray>Usage: /kmcore modules disable <name>")); return true; }
        String name = args[2];
        if (name.equalsIgnoreCase(this.getName())) {
            sender.sendMessage(MM.deserialize(msgCannotDisableSelf));
            return true;
        }
        if (!moduleManager.isEnabled(name)) {
            sender.sendMessage(MM.deserialize(msgAlreadyDisabled.replace("{name}", name)));
            return true;
        }
        if (!moduleManager.disableModule(name)) {
            sender.sendMessage(MM.deserialize(msgUnknownModule.replace("{name}", name)));
            return true;
        }
        sender.sendMessage(MM.deserialize(msgDisabled.replace("{name}", name)));
        return true;
    }


    private void sendUsage(CommandSender sender) {
        sender.sendMessage(header("kmcore — Usage"));
        sender.sendMessage(usageLine("/kmcore reload",                "Reload config from disk"));
        sender.sendMessage(usageLine("/kmcore modules list [hooks]",  "List enabled modules"));
        sender.sendMessage(usageLine("/kmcore modules all [hooks]",   "List all modules with status"));
        sender.sendMessage(usageLine("/kmcore modules enable <n>",    "Enable a module"));
        sender.sendMessage(usageLine("/kmcore modules disable <n>",   "Disable a module"));
    }

    private Component header(String title) {
        return MM.deserialize(formatHeader.replace("{title}", title));
    }

    private Component usageLine(String cmd, String desc) {
        return MM.deserialize(formatUsageLine
                .replace("{cmd}", cmd)
                .replace("{desc}", desc));
    }

    private Component moduleRow(String name, boolean enabled, Set<Class<? extends Hook>> hooks) {
        String hookSuffix = "";
        if (hooks != null && !hooks.isEmpty()) {
            String hookNames = hooks.stream()
                    .map(Class::getSimpleName)
                    .sorted()
                    .collect(Collectors.joining(", "));
            hookSuffix = formatHooksSuffix.replace("{hooks}", hookNames);
        }

        String template = enabled ? formatRowEnabled : formatRowDisabled;
        return MM.deserialize(template
                .replace("{name}", name)
                .replace("{hooks}", hookSuffix));
    }
}