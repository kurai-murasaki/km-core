package dev.Ox6b6d.kmCore.module.impl.mechanics;

import dev.Ox6b6d.kmCore.config.Configurable;
import dev.Ox6b6d.kmCore.config.ConfigManager;
import dev.Ox6b6d.kmCore.config.ConfigValue;
import dev.Ox6b6d.kmCore.event.hook.command.CommandHook;
import dev.Ox6b6d.kmCore.event.hook.player.PlayerItemHeldHook;
import dev.Ox6b6d.kmCore.event.hook.player.PlayerJoinHook;
import dev.Ox6b6d.kmCore.module.Module;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Overrides {@code GENERIC_ATTACK_SPEED} for players based on the weapon type
 * they are holding. Every tracked weapon category has a configurable target speed
 * that can be adjusted live with {@code /combat set <weapon> <speed>}.
 *
 * <h3>How the math works</h3>
 * <ol>
 *   <li>Vanilla base value of {@code GENERIC_ATTACK_SPEED} is {@code 4.0}.</li>
 *   <li>Weapon items carry built-in {@code ADD_NUMBER} modifiers
 *       (e.g. swords apply {@code -2.4} → effective {@code 1.6}).</li>
 *   <li>We schedule a 1-tick delayed task so those built-in modifiers are applied
 *       before we read {@link AttributeInstance#getValue()}.</li>
 *   <li>We inject {@code targetSpeed - currentEffectiveSpeed} as another
 *       {@code ADD_NUMBER} modifier, making the final value equal
 *       {@code targetSpeed}.</li>
 * </ol>
 *
 * <h3>Commands</h3>
 * <pre>
 *   /combat list               — list all current weapon speeds
 *   /combat set &lt;weapon&gt; &lt;speed&gt; — override a weapon category's speed
 *   /combat reset &lt;weapon&gt;    — reset a weapon category to its default
 *   /combat reset all          — reset every weapon category to defaults
 * </pre>
 *
 * <p>Requires permission {@code kmcore.combat}.
 */
public class CombatModule implements Module, Configurable, CommandHook,
        PlayerJoinHook, PlayerItemHeldHook {

    private static final MiniMessage MM             = MiniMessage.miniMessage();
    private static final String      MODIFIER_KEY   = "attack_speed_override";

    // -------------------------------------------------------------------------
    // Config — behaviour
    // -------------------------------------------------------------------------

    @ConfigValue(
            key     = "enabled",
            comment = "Master toggle. Set false to disable all attack-speed overrides."
    )
    private boolean enabled = true;

    // -------------------------------------------------------------------------
    // Config — weapon speeds
    // -------------------------------------------------------------------------

    @ConfigValue(
            key     = "sword-attack-speed",
            comment = "Attack speed for all sword types. Vanilla: 1.6 | Hand: 4.0"
    )
    private double swordAttackSpeed = 4.0;

    @ConfigValue(
            key     = "axe-attack-speed",
            comment = "Attack speed for all axe types. Vanilla: 0.8–1.0 depending on tier."
    )
    private double axeAttackSpeed = 1.0;

    @ConfigValue(
            key     = "shovel-attack-speed",
            comment = "Attack speed for all shovel types. Vanilla: 1.0"
    )
    private double shovelAttackSpeed = 1.0;

    @ConfigValue(
            key     = "pickaxe-attack-speed",
            comment = "Attack speed for all pickaxe types. Vanilla: 1.2"
    )
    private double pickaxeAttackSpeed = 1.2;

    @ConfigValue(
            key     = "hoe-attack-speed",
            comment = "Attack speed for all hoe types. Vanilla: 1.0–4.0 depending on tier."
    )
    private double hoeAttackSpeed = 4.0;

    @ConfigValue(
            key     = "trident-attack-speed",
            comment = "Attack speed for the trident. Vanilla: 1.1"
    )
    private double tridentAttackSpeed = 1.1;

    @ConfigValue(
            key     = "mace-attack-speed",
            comment = "Attack speed for the mace. Vanilla: 0.6"
    )
    private double maceAttackSpeed = 0.6;

    // -------------------------------------------------------------------------
    // Config — messages
    // -------------------------------------------------------------------------

    @ConfigValue(
            key     = "msg.no-permission",
            comment = "Sent when the sender lacks kmcore.combat."
    )
    private String msgNoPermission = "<red>You don't have permission to use this command.";

    @ConfigValue(
            key     = "msg.set-success",
            comment = "Sent on a successful /combat set. Use {weapon} and {speed}."
    )
    private String msgSetSuccess = "<dark_gray>[<green>✔<dark_gray>] <gray>{weapon} <dark_gray>attack speed set to <gray>{speed}<dark_gray>.";

    @ConfigValue(
            key     = "msg.reset-one",
            comment = "Sent on a successful /combat reset <weapon>. Use {weapon} and {speed}."
    )
    private String msgResetOne = "<dark_gray>[<green>✔<dark_gray>] <gray>{weapon} <dark_gray>reset to default (<gray>{speed}<dark_gray>).";

    @ConfigValue(
            key     = "msg.reset-all",
            comment = "Sent after /combat reset all."
    )
    private String msgResetAll = "<dark_gray>[<green>✔<dark_gray>] <gray>All weapon speeds reset to defaults.";

    @ConfigValue(
            key     = "msg.invalid-speed",
            comment = "Sent when the speed argument is not a valid number. Use {input}."
    )
    private String msgInvalidSpeed = "<red>Invalid speed: <gray>{input}<red>. Must be a positive number.";

    @ConfigValue(
            key     = "msg.unknown-weapon",
            comment = "Sent when the weapon argument does not match a known category. Use {input}."
    )
    private String msgUnknownWeapon = "<red>Unknown weapon type: <gray>{input}<red>. Try: sword, axe, shovel, pickaxe, hoe, trident, mace.";

    @ConfigValue(
            key     = "format.header",
            comment = "Header line for /combat list. Use {title}."
    )
    private String formatHeader = "<dark_gray>— <light_purple><bold>{title}</bold> <dark_gray>—";

    @ConfigValue(
            key     = "format.row",
            comment = "A single weapon row in /combat list. Use {weapon} and {speed}."
    )
    private String formatRow = "<dark_gray> <gray>{weapon} <dark_gray>→ <light_purple>{speed}";

    // -------------------------------------------------------------------------
    // Default speed constants — used by /combat reset
    // -------------------------------------------------------------------------

    private static final Map<String, Double> DEFAULTS = Map.of(
            "sword",   4.0,
            "axe",     1.0,
            "shovel",  1.0,
            "pickaxe", 1.2,
            "hoe",     4.0,
            "trident", 1.1,
            "mace",    0.6
    );

    /** Ordered list of weapon names for tab-completion and list display. */
    private static final List<String> WEAPON_NAMES =
            List.of("sword", "axe", "shovel", "pickaxe", "hoe", "trident", "mace");

    // -------------------------------------------------------------------------
    // Internal state
    // -------------------------------------------------------------------------

    private final JavaPlugin   plugin;
    private final ConfigManager configManager;
    private NamespacedKey modifierKey;

    /**
     * @param plugin        the owning plugin instance — used for scheduling and
     *                      the {@link NamespacedKey}
     * @param configManager the plugin's {@link ConfigManager} — used to persist
     *                      in-game speed changes to disk
     */
    public CombatModule(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin        = plugin;
        this.configManager = configManager;
    }

    // -------------------------------------------------------------------------
    // Module lifecycle
    // -------------------------------------------------------------------------

    @Override public String getName()          { return "CombatModule"; }
    @Override public String getConfigSection() { return "combat"; }
    @Override public String getCommand()       { return "combat"; }

    @Override
    public void enable() {
        modifierKey = new NamespacedKey(plugin, MODIFIER_KEY);
        for (Player p : Bukkit.getOnlinePlayers()) {
            scheduleApply(p);
        }
    }

    @Override
    public void disable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            removeModifier(p);
        }
    }

    // -------------------------------------------------------------------------
    // Event hooks
    // -------------------------------------------------------------------------

    /**
     * Applies the correct speed override 1 tick after the player joins,
     * once their held item's built-in modifiers are active.
     */
    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        scheduleApply(event.getPlayer());
    }

    /**
     * Re-evaluates the override whenever the player changes their held item.
     * The 1-tick delay lets the game apply the new item's built-in modifiers first.
     */
    @Override
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        scheduleApply(event.getPlayer());
    }

    // -------------------------------------------------------------------------
    // Command handling
    // -------------------------------------------------------------------------

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kmcore.combat")) {
            sender.sendMessage(MM.deserialize(msgNoPermission));
            return true;
        }

        if (args.length == 0) { sendUsage(sender); return true; }

        return switch (args[0].toLowerCase()) {
            case "list"  -> cmdList(sender);
            case "set"   -> cmdSet(sender, args);
            case "reset" -> cmdReset(sender, args);
            default      -> { sendUsage(sender); yield true; }
        };
    }

    @Override
    public Collection<String> suggest(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kmcore.combat")) return List.of();

        if (args.length == 1) {
            return List.of("list", "set", "reset").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("set") || sub.equals("reset")) {
                List<String> weapons = sub.equals("reset")
                        ? concatList(WEAPON_NAMES, "all")
                        : WEAPON_NAMES;
                return weapons.stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            // Suggest the current speed as a starting value for the user to edit.
            Double current = getSpeedForWeapon(args[1].toLowerCase());
            if (current != null) return List.of(formatSpeed(current));
        }

        return List.of();
    }

    // -------------------------------------------------------------------------
    // Sub-commands
    // -------------------------------------------------------------------------

    /**
     * Lists every weapon category alongside its currently configured attack speed.
     */
    private boolean cmdList(CommandSender sender) {
        sender.sendMessage(header("Combat — Attack Speeds"));
        for (String weapon : WEAPON_NAMES) {
            double speed = getSpeedForWeapon(weapon);
            sender.sendMessage(row(weapon, speed));
        }
        return true;
    }

    /**
     * Sets the attack speed for a weapon category, persists it to disk, and
     * immediately re-applies modifiers to all online players.
     *
     * <p>Usage: {@code /combat set <weapon> <speed>}
     */
    private boolean cmdSet(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(MM.deserialize("<dark_gray>Usage: /combat set <weapon> <speed>"));
            return true;
        }

        String weapon = args[1].toLowerCase();
        String raw    = args[2];

        if (!WEAPON_NAMES.contains(weapon)) {
            sender.sendMessage(MM.deserialize(msgUnknownWeapon.replace("{input}", weapon)));
            return true;
        }

        double speed;
        try {
            speed = Double.parseDouble(raw);
            if (speed <= 0) throw new NumberFormatException("non-positive");
        } catch (NumberFormatException e) {
            sender.sendMessage(MM.deserialize(msgInvalidSpeed.replace("{input}", raw)));
            return true;
        }

        applySpeedConfig(weapon, speed);
        configManager.set(this, weaponToKey(weapon), speed);

        // Re-apply to all online players so the change takes effect immediately.
        for (Player p : Bukkit.getOnlinePlayers()) {
            scheduleApply(p);
        }

        sender.sendMessage(MM.deserialize(
                msgSetSuccess
                        .replace("{weapon}", weapon)
                        .replace("{speed}", formatSpeed(speed))
        ));
        return true;
    }

    /**
     * Resets one or all weapon categories to the module's default speeds,
     * persists each change to disk, and re-applies modifiers to all online players.
     *
     * <p>Usage: {@code /combat reset <weapon|all>}
     */
    private boolean cmdReset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(MM.deserialize("<dark_gray>Usage: /combat reset <weapon|all>"));
            return true;
        }

        String target = args[1].toLowerCase();

        if (target.equals("all")) {
            for (Map.Entry<String, Double> entry : DEFAULTS.entrySet()) {
                applySpeedConfig(entry.getKey(), entry.getValue());
                configManager.set(this, weaponToKey(entry.getKey()), entry.getValue());
            }
            for (Player p : Bukkit.getOnlinePlayers()) scheduleApply(p);
            sender.sendMessage(MM.deserialize(msgResetAll));
            return true;
        }

        if (!DEFAULTS.containsKey(target)) {
            sender.sendMessage(MM.deserialize(msgUnknownWeapon.replace("{input}", target)));
            return true;
        }

        double defaultSpeed = DEFAULTS.get(target);
        applySpeedConfig(target, defaultSpeed);
        configManager.set(this, weaponToKey(target), defaultSpeed);

        for (Player p : Bukkit.getOnlinePlayers()) scheduleApply(p);

        sender.sendMessage(MM.deserialize(
                msgResetOne
                        .replace("{weapon}", target)
                        .replace("{speed}", formatSpeed(defaultSpeed))
        ));
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(header("combat — Usage"));
        sender.sendMessage(usageLine("/combat list",                  "Show all weapon speeds"));
        sender.sendMessage(usageLine("/combat set <weapon> <speed>",  "Override a weapon's speed"));
        sender.sendMessage(usageLine("/combat reset <weapon|all>",    "Reset to default(s)"));
    }

    // -------------------------------------------------------------------------
    // Attribute logic
    // -------------------------------------------------------------------------

    /**
     * Schedules {@link #applyModifier(Player)} for 1 tick in the future so that
     * the held item's built-in attribute modifiers are active when we read the
     * effective speed.
     *
     * @param player the player to update
     */
    private void scheduleApply(Player player) {
        if (!enabled) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> applyModifier(player), 1L);
    }

    /**
     * Reads the player's current effective attack speed, then adds or replaces
     * our custom modifier so the final value equals the configured target speed
     * for whatever weapon they are holding.
     *
     * <p>If the held item is not a tracked weapon type the modifier is removed,
     * restoring vanilla behaviour.
     *
     * @param player the player whose attribute to adjust
     */
    private void applyModifier(Player player) {
        if (!player.isOnline()) return;

        AttributeInstance ai = player.getAttribute(Attribute.ATTACK_SPEED);
        if (ai == null) return;

        removeModifier(player);

        if (!enabled) return;

        ItemStack held        = player.getInventory().getItemInMainHand();
        Double    targetSpeed = getTargetSpeed(held.getType());
        if (targetSpeed == null) return;

        double adjustment = targetSpeed - ai.getValue();

        ai.addModifier(new AttributeModifier(
                modifierKey,
                adjustment,
                AttributeModifier.Operation.ADD_NUMBER
        ));
    }

    /**
     * Removes our custom attack-speed modifier from the given player if present.
     *
     * @param player the player to clean up
     */
    private void removeModifier(Player player) {
        AttributeInstance ai = player.getAttribute(Attribute.ATTACK_SPEED);
        if (ai == null) return;

        ai.getModifiers().stream()
                .filter(m -> m.getKey().equals(modifierKey))
                .findFirst()
                .ifPresent(ai::removeModifier);
    }

    // -------------------------------------------------------------------------
    // Weapon / speed helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the configured target attack speed for the given material, or
     * {@code null} if it is not a tracked weapon type.
     *
     * <p>Detection uses the {@link Material} name suffix so all tiers
     * (WOODEN_, STONE_, IRON_, GOLDEN_, DIAMOND_, NETHERITE_) are covered
     * automatically.
     *
     * @param material the material to classify
     * @return the configured target speed, or {@code null}
     */
    private @Nullable Double getTargetSpeed(Material material) {
        if (material == Material.AIR) return null;
        String name = material.name();

        if (name.endsWith("_SWORD"))   return swordAttackSpeed;
        if (name.endsWith("_AXE"))     return axeAttackSpeed;
        if (name.endsWith("_SHOVEL"))  return shovelAttackSpeed;
        if (name.endsWith("_PICKAXE")) return pickaxeAttackSpeed;
        if (name.endsWith("_HOE"))     return hoeAttackSpeed;
        if (material == Material.TRIDENT) return tridentAttackSpeed;
        if (material == Material.MACE)    return maceAttackSpeed;

        return null;
    }

    /**
     * Returns the currently configured speed for a weapon category name
     * (e.g. {@code "sword"}), or {@code null} if the name is unrecognised.
     *
     * @param weapon lowercase weapon category name
     * @return current configured speed, or {@code null}
     */
    private @Nullable Double getSpeedForWeapon(String weapon) {
        return switch (weapon) {
            case "sword"   -> swordAttackSpeed;
            case "axe"     -> axeAttackSpeed;
            case "shovel"  -> shovelAttackSpeed;
            case "pickaxe" -> pickaxeAttackSpeed;
            case "hoe"     -> hoeAttackSpeed;
            case "trident" -> tridentAttackSpeed;
            case "mace"    -> maceAttackSpeed;
            default        -> null;
        };
    }

    /**
     * Writes a new speed value into this module's speed field for the given
     * weapon category. Called before persisting so the running instance reflects
     * the change without needing a full re-inject.
     *
     * @param weapon lowercase weapon category name
     * @param speed  the new speed value
     */
    private void applySpeedConfig(String weapon, double speed) {
        switch (weapon) {
            case "sword"   -> swordAttackSpeed   = speed;
            case "axe"     -> axeAttackSpeed     = speed;
            case "shovel"  -> shovelAttackSpeed  = speed;
            case "pickaxe" -> pickaxeAttackSpeed = speed;
            case "hoe"     -> hoeAttackSpeed     = speed;
            case "trident" -> tridentAttackSpeed = speed;
            case "mace"    -> maceAttackSpeed    = speed;
        }
    }

    /**
     * Converts a weapon category name to its config key
     * (e.g. {@code "sword"} → {@code "sword-attack-speed"}).
     *
     * @param weapon lowercase weapon category name
     * @return the corresponding config key
     */
    private String weaponToKey(String weapon) {
        return weapon + "-attack-speed";
    }

    // -------------------------------------------------------------------------
    // Formatting helpers
    // -------------------------------------------------------------------------

    private Component header(String title) {
        return MM.deserialize(formatHeader.replace("{title}", title));
    }

    private Component row(String weapon, double speed) {
        return MM.deserialize(formatRow
                .replace("{weapon}",  weapon)
                .replace("{speed}",   formatSpeed(speed)));
    }

    private Component usageLine(String cmd, String desc) {
        return MM.deserialize("<gray>  " + cmd + " <dark_gray>— " + desc);
    }

    /**
     * Formats a speed value to at most 2 decimal places, stripping trailing
     * zeros (e.g. {@code 4.0} → {@code "4.0"}, {@code 1.25} → {@code "1.25"}).
     *
     * @param speed the speed value to format
     * @return a human-readable string
     */
    private String formatSpeed(double speed) {
        // Strip unnecessary trailing zeros but always keep at least one decimal.
        String s = String.format("%.2f", speed);
        s = s.replaceAll("0+$", "");
        if (s.endsWith(".")) s += "0";
        return s;
    }

    /** Convenience: return a new list that is {@code base} plus {@code extra}. */
    private List<String> concatList(List<String> base, String extra) {
        return java.util.stream.Stream.concat(base.stream(), java.util.stream.Stream.of(extra))
                .collect(Collectors.toList());
    }
}