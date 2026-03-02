package dev.Ox6b6d.kmCore.module.impl.mechanics.safe;

import dev.Ox6b6d.kmCore.KmCore;
import dev.Ox6b6d.kmCore.config.Configurable;
import dev.Ox6b6d.kmCore.config.ConfigValue;
import dev.Ox6b6d.kmCore.event.hook.command.CommandHook;
import dev.Ox6b6d.kmCore.event.hook.player.InventoryClickHook;
import dev.Ox6b6d.kmCore.event.hook.player.InventoryCloseHook;
import dev.Ox6b6d.kmCore.event.hook.player.PlayerInteractHook;
import dev.Ox6b6d.kmCore.event.hook.player.PlayerQuitHook;
import dev.Ox6b6d.kmCore.module.Module;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SafeModule implements Module, Configurable, CommandHook,
        PlayerInteractHook, InventoryCloseHook, InventoryClickHook, PlayerQuitHook {

    private static final MiniMessage MM = MiniMessage.miniMessage();


    @ConfigValue(key = "allow-crowbar",                comment = "Whether players can use a crowbar to claim other safes")
    private boolean allowCrowbar = true;

    @ConfigValue(key = "crowbar-requires-permission",  comment = "If true, only players with kmcore.safe.crowbar can use crowbars")
    private boolean crowbarRequiresPermission = false;

    @ConfigValue(key = "announce-claim",               comment = "Broadcast a message to the server when a safe is claimed")
    private boolean announceClaim = false;

    @ConfigValue(key = "item.safe-name",               comment = "Display name of the safe item. MiniMessage format.")
    private String itemSafeName = "<light_purple>⚙ Safe";

    @ConfigValue(key = "item.safe-blank-lore",         comment = "Lore on an unclaimed safe.")
    private String itemSafeBlankLore = "<dark_purple>Right-click to claim.";

    @ConfigValue(key = "item.safe-owner-lore",         comment = "Lore showing owner. Use {owner}.")
    private String itemSafeOwnerLore = "<dark_gray>Owner: <gray>{owner}";

    @ConfigValue(key = "item.safe-id-lore",            comment = "Lore showing safe ID. Use {id}.")
    private String itemSafeIdLore = "<dark_gray>ID: <gray>{id}";

    @ConfigValue(key = "item.crowbar-name",            comment = "Display name of the crowbar item. MiniMessage format.")
    private String itemCrowbarName = "<light_purple>Crowbar";

    @ConfigValue(key = "item.crowbar-lore",            comment = "First lore line on the crowbar.")
    private String itemCrowbarLore = "<dark_gray>Right-click to crack a safe.";

    @ConfigValue(key = "item.crowbar-consumed-lore",   comment = "Second lore line on the crowbar (consumed note).")
    private String itemCrowbarConsumedLore = "<dark_purple>Consumed on use.";

    // -------------------------------------------------------------------------
    // Inventory title config
    // -------------------------------------------------------------------------

    @ConfigValue(key = "ui.safe-title",                comment = "Title of the safe inventory. Use {owner}.")
    private String uiSafeTitle = "<light_purple>Safe <dark_gray>— <gray>{owner}";

    @ConfigValue(key = "ui.crowbar-title",             comment = "Title of the crowbar inventory.")
    private String uiCrowbarTitle = "<light_purple>Crowbar <dark_gray>— <gray>Insert Safe";

    // -------------------------------------------------------------------------
    // Message config
    // -------------------------------------------------------------------------

    @ConfigValue(key = "msg.no-permission",            comment = "Sent when a player lacks permission.")
    private String msgNoPermission = "<red>You don't have permission.";

    @ConfigValue(key = "msg.player-not-found",         comment = "Sent when a target player is offline. Use {player}.")
    private String msgPlayerNotFound = "<dark_gray>Player not found: <gray>{player}";

    @ConfigValue(key = "msg.given-safe",               comment = "Sent to command sender after giving a safe. Use {player}.")
    private String msgGivenSafe = "<dark_gray>Given safe to <gray>{player}";

    @ConfigValue(key = "msg.given-crowbar",            comment = "Sent to command sender after giving a crowbar. Use {player}.")
    private String msgGivenCrowbar = "<dark_gray>Given crowbar to <gray>{player}";

    @ConfigValue(key = "msg.safe-claimed",             comment = "Sent to player when they claim a safe.")
    private String msgSafeClaimed = "<light_purple>Safe claimed!";

    @ConfigValue(key = "msg.safe-claimed-announce",    comment = "Broadcast when a safe is claimed (if announce-claim is true). Use {player}.")
    private String msgSafeClaimedAnnounce = "<dark_gray>[<light_purple>+<dark_gray>] <gray>{player}<dark_gray> claimed a safe.";

    @ConfigValue(key = "msg.not-owner",                comment = "Sent when a non-owner tries to open a safe.")
    private String msgNotOwner = "<dark_gray>This safe doesn't belong to you.";

    @ConfigValue(key = "msg.no-server-data",           comment = "Sent when a safe item has no server-side data.")
    private String msgNoServerData = "<red>This safe has no data on the server.";

    @ConfigValue(key = "msg.no-safe-in-safe",          comment = "Sent when a player tries to put a safe inside a safe.")
    private String msgNoSafeInSafe = "<dark_gray>You cannot put a safe inside a safe.";

    @ConfigValue(key = "msg.safe-cracked",             comment = "Sent after cracking a safe with a crowbar.")
    private String msgSafeCracked = "<light_purple>Safe cracked! <dark_gray>You are now the owner.";

    @ConfigValue(key = "msg.crowbars-disabled",        comment = "Sent when crowbars are disabled on the server.")
    private String msgCrowbarsDisabled = "<dark_gray>Crowbars are disabled on this server.";

    @ConfigValue(key = "msg.crowbar-no-permission",    comment = "Sent when player lacks kmcore.safe.crowbar.")
    private String msgCrowbarNoPermission = "<red>You don't have permission to use a crowbar.";


    private final KmCore      plugin;
    private final SafeManager safeManager;

    private NamespacedKey safeRecipeKey;
    private NamespacedKey crowbarRecipeKey;

    public SafeModule(KmCore plugin) {
        this.plugin      = plugin;
        this.safeManager = new SafeManager(plugin);
    }

    @Override public String getName()          { return "SafeModule"; }
    @Override public String getCommand()       { return "safe"; }
    @Override public String getConfigSection() { return "safe"; }


    @Override
    public void enable() {
        safeManager.load(plugin.getDataFolder());
        registerRecipes();
    }

    @Override
    public void disable() {
        safeManager.save(plugin.getDataFolder());
        Bukkit.removeRecipe(safeRecipeKey);
        Bukkit.removeRecipe(crowbarRecipeKey);
    }

    private void registerRecipes() {
        safeRecipeKey = new NamespacedKey(plugin, "safe_recipe");
        ShapedRecipe safeRecipe = new ShapedRecipe(safeRecipeKey, blankSafeItem());
        safeRecipe.shape("III", "ICI", "III");
        safeRecipe.setIngredient('I', Material.IRON_INGOT);
        safeRecipe.setIngredient('C', Material.CHEST);
        Bukkit.addRecipe(safeRecipe);

        crowbarRecipeKey = new NamespacedKey(plugin, "crowbar_recipe");
        ShapedRecipe crowbarRecipe = new ShapedRecipe(crowbarRecipeKey, crowbarItem());
        crowbarRecipe.shape("  I", " I ", "I  ");
        crowbarRecipe.setIngredient('I', Material.IRON_INGOT);
        Bukkit.addRecipe(crowbarRecipe);
    }

    private ItemStack blankSafeItem() {
        return safeManager.createBlankSafeItem(itemSafeName, itemSafeBlankLore);
    }

    private ItemStack safeItem(SafeData data) {
        return safeManager.createSafeItem(data, itemSafeName, itemSafeOwnerLore, itemSafeIdLore);
    }

    private ItemStack crowbarItem() {
        return safeManager.createCrowbarItem(itemCrowbarName, itemCrowbarLore, itemCrowbarConsumedLore);
    }


    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendUsage(sender);
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "give"    -> cmdGive(sender, args[1]);
            case "crowbar" -> cmdCrowbar(sender, args[1]);
            default        -> { sendUsage(sender); yield true; }
        };
    }

    @Override
    public Collection<String> suggest(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("give", "crowbar").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("crowbar"))) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(p -> p.getName())
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private boolean cmdGive(CommandSender sender, String targetName) {
        if (!sender.hasPermission("kmcore.safe.give")) {
            sender.sendMessage(MM.deserialize(msgNoPermission));
            return true;
        }
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(MM.deserialize(msgPlayerNotFound.replace("{player}", targetName)));
            return true;
        }
        target.getInventory().addItem(blankSafeItem());
        sender.sendMessage(MM.deserialize(msgGivenSafe.replace("{player}", target.getName())));
        return true;
    }

    private boolean cmdCrowbar(CommandSender sender, String targetName) {
        if (!sender.hasPermission("kmcore.safe.give")) {
            sender.sendMessage(MM.deserialize(msgNoPermission));
            return true;
        }
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(MM.deserialize(msgPlayerNotFound.replace("{player}", targetName)));
            return true;
        }
        target.getInventory().addItem(crowbarItem());
        sender.sendMessage(MM.deserialize(msgGivenCrowbar.replace("{player}", target.getName())));
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(MM.deserialize("<dark_gray>— <light_purple><bold>safe — Usage</bold> <dark_gray>—"));
        sender.sendMessage(MM.deserialize("<gray>  /safe give <player> <dark_gray>— give a safe"));
        sender.sendMessage(MM.deserialize("<gray>  /safe crowbar <player> <dark_gray>— give a crowbar"));
    }


    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;

        if (safeManager.isSafeItem(item) || safeManager.isCrowbarItem(item)) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK ||
                    event.getAction() == Action.RIGHT_CLICK_AIR) {
                event.setCancelled(true);
            }
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();

        if (safeManager.isSafeItem(item)) {
            handleSafeInteract(player, item);
            return;
        }

        if (safeManager.isCrowbarItem(item)) {
            if (!allowCrowbar) {
                player.sendMessage(MM.deserialize(msgCrowbarsDisabled));
                return;
            }
            if (crowbarRequiresPermission && !player.hasPermission("kmcore.safe.crowbar")) {
                player.sendMessage(MM.deserialize(msgCrowbarNoPermission));
                return;
            }
            consumeOne(player, item);
            safeManager.openCrowbar(player, uiCrowbarTitle);
        }
    }

    private void handleSafeInteract(Player player, ItemStack item) {
        if (safeManager.isBlankSafe(item)) {
            SafeData  data    = safeManager.createSafe(player.getUniqueId());
            ItemStack claimed = safeItem(data);

            if (item.getAmount() <= 1) {
                player.getInventory().setItemInMainHand(claimed);
            } else {
                item.setAmount(item.getAmount() - 1);
                player.getInventory().addItem(claimed);
            }

            if (announceClaim) {
                Bukkit.broadcast(MM.deserialize(msgSafeClaimedAnnounce.replace("{player}", player.getName())));
            } else {
                player.sendMessage(MM.deserialize(msgSafeClaimed));
            }

            safeManager.openSafe(player, data, uiSafeTitle);
            return;
        }

        UUID safeId = safeManager.getSafeIdFromItem(item);
        if (safeId == null) return;

        SafeData data = safeManager.getSafe(safeId);
        if (data == null) {
            player.sendMessage(MM.deserialize(msgNoServerData));
            return;
        }

        if (!data.getOwnerUuid().equals(player.getUniqueId())) {
            player.sendMessage(MM.deserialize(msgNotOwner));
            return;
        }

        safeManager.openSafe(player, data, uiSafeTitle);
    }

    private void consumeOne(Player player, ItemStack item) {
        if (item.getAmount() <= 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            item.setAmount(item.getAmount() - 1);
        }
    }

    // -------------------------------------------------------------------------
    // Inventory Click
    // -------------------------------------------------------------------------

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        UUID playerUUID = player.getUniqueId();

        if (!safeManager.hasOpenSafe(playerUUID) && !safeManager.hasOpenCrowbar(playerUUID)) return;

        ItemStack cursor  = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (safeManager.hasOpenSafe(playerUUID)) {
            if (safeManager.isSafeItem(cursor) || safeManager.isSafeItem(current)) {
                event.setCancelled(true);
                player.sendMessage(MM.deserialize(msgNoSafeInSafe));
            }
        }
    }


    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        UUID playerUUID = player.getUniqueId();

        if (safeManager.hasOpenSafe(playerUUID)) {
            UUID     safeId = safeManager.getOpenSafeId(playerUUID);
            SafeData data   = safeManager.getSafe(safeId);
            if (data != null) data.setContents(event.getInventory().getContents());
            safeManager.clearOpenSafe(playerUUID);
            return;
        }

        if (safeManager.hasOpenCrowbar(playerUUID)) {
            safeManager.clearOpenCrowbar(playerUUID);

            for (ItemStack slot : event.getInventory().getContents()) {
                if (slot == null) continue;

                UUID safeId = safeManager.getSafeIdFromItem(slot);
                if (safeId != null) {
                    SafeData data = safeManager.getSafe(safeId);
                    if (data != null) {
                        data.setOwnerUuid(playerUUID);
                        player.getInventory().addItem(safeItem(data));
                        player.sendMessage(MM.deserialize(msgSafeCracked));
                        continue;
                    }
                }
                player.getInventory().addItem(slot);
            }
            event.getInventory().clear();
        }
    }


    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        safeManager.clearOpenSafe(playerUUID);
        safeManager.clearOpenCrowbar(playerUUID);
    }
}