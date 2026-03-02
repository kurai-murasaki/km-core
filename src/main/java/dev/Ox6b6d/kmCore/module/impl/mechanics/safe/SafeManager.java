package dev.Ox6b6d.kmCore.module.impl.mechanics.safe;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SafeManager {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    final NamespacedKey isSafeKey;
    final NamespacedKey safeIdKey;
    final NamespacedKey isCrowbarKey;

    private final JavaPlugin plugin;
    private final Map<UUID, SafeData> safes = new HashMap<>();

    private final Map<UUID, UUID> openSafeViews    = new HashMap<>();
    private final Set<UUID>       openCrowbarViews = new HashSet<>();

    public SafeManager(JavaPlugin plugin) {
        this.plugin       = plugin;
        this.isSafeKey    = new NamespacedKey(plugin, "is_safe");
        this.safeIdKey    = new NamespacedKey(plugin, "safe_id");
        this.isCrowbarKey = new NamespacedKey(plugin, "is_crowbar");
    }


    public ItemStack createBlankSafeItem(String displayName, String loreLine) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MM.deserialize(displayName));
        meta.lore(List.of(MM.deserialize(loreLine)));
        meta.getPersistentDataContainer().set(isSafeKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createSafeItem(SafeData data, String displayName, String ownerLore, String idLore) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MM.deserialize(displayName));

        String ownerName = Optional.ofNullable(Bukkit.getOfflinePlayer(data.getOwnerUuid()).getName())
                .orElse("Unknown");
        String idShort = data.getSafeId().toString().substring(0, 8);

        meta.lore(List.of(
                MM.deserialize(ownerLore.replace("{owner}", ownerName)),
                MM.deserialize(idLore.replace("{id}", idShort))
        ));

        var pdc = meta.getPersistentDataContainer();
        pdc.set(isSafeKey,  PersistentDataType.BYTE,   (byte) 1);
        pdc.set(safeIdKey,  PersistentDataType.STRING,  data.getSafeId().toString());
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createCrowbarItem(String displayName, String loreLine, String consumedLore) {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(MM.deserialize(displayName));
        meta.lore(List.of(
                MM.deserialize(loreLine),
                MM.deserialize(consumedLore)
        ));
        meta.getPersistentDataContainer().set(isCrowbarKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }


    public boolean isSafeItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(isSafeKey);
    }

    public boolean isBlankSafe(ItemStack item) {
        if (!isSafeItem(item)) return false;
        return !item.getItemMeta().getPersistentDataContainer().has(safeIdKey);
    }

    public UUID getSafeIdFromItem(ItemStack item) {
        if (!isSafeItem(item)) return null;
        String raw = item.getItemMeta().getPersistentDataContainer()
                .get(safeIdKey, PersistentDataType.STRING);
        if (raw == null) return null;
        try { return UUID.fromString(raw); }
        catch (IllegalArgumentException e) { return null; }
    }

    public boolean isCrowbarItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(isCrowbarKey);
    }


    public SafeData createSafe(UUID ownerUuid) {
        UUID     safeId = UUID.randomUUID();
        SafeData data   = new SafeData(safeId, ownerUuid);
        safes.put(safeId, data);
        return data;
    }

    public SafeData getSafe(UUID safeId) {
        return safes.get(safeId);
    }


    public void openSafe(Player player, SafeData data, String titleFormat) {
        String ownerName = Optional.ofNullable(Bukkit.getOfflinePlayer(data.getOwnerUuid()).getName())
                .orElse("Unknown");

        Component title = MM.deserialize(titleFormat.replace("{owner}", ownerName));

        Inventory inv = Bukkit.createInventory(null, 54, title);
        inv.setContents(data.getContents());

        openSafeViews.put(player.getUniqueId(), data.getSafeId());
        player.openInventory(inv);
    }

    public void openCrowbar(Player player, String title) {
        Inventory inv = Bukkit.createInventory(null, 9, MM.deserialize(title));
        openCrowbarViews.add(player.getUniqueId());
        player.openInventory(inv);
    }

    public boolean hasOpenSafe(UUID playerUUID)      { return openSafeViews.containsKey(playerUUID); }
    public UUID    getOpenSafeId(UUID playerUUID)    { return openSafeViews.get(playerUUID); }
    public void    clearOpenSafe(UUID playerUUID)    { openSafeViews.remove(playerUUID); }

    public boolean hasOpenCrowbar(UUID playerUUID)   { return openCrowbarViews.contains(playerUUID); }
    public void    clearOpenCrowbar(UUID playerUUID) { openCrowbarViews.remove(playerUUID); }


    public void load(File dataFolder) {
        File file = new File(dataFolder, "safes.yml");
        if (!file.exists()) return;

        YamlConfiguration    yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = yaml.getConfigurationSection("safes");
        if (root == null) return;

        int loaded = 0;
        for (String key : root.getKeys(false)) {
            try {
                UUID   safeId   = UUID.fromString(key);
                String ownerStr = root.getString(key + ".owner");
                if (ownerStr == null) continue;
                UUID ownerUuid = UUID.fromString(ownerStr);

                SafeData data = new SafeData(safeId, ownerUuid);

                ConfigurationSection contents = root.getConfigurationSection(key + ".contents");
                if (contents != null) {
                    ItemStack[] arr = new ItemStack[54];
                    for (String slotStr : contents.getKeys(false)) {
                        int    slot    = Integer.parseInt(slotStr);
                        String encoded = contents.getString(slotStr);
                        if (encoded != null)
                            arr[slot] = ItemStack.deserializeBytes(Base64.getDecoder().decode(encoded));
                    }
                    data.setContents(arr);
                }

                safes.put(safeId, data);
                loaded++;
            } catch (Exception e) {
                plugin.getLogger().warning("[kmCore][SafeManager] Failed to load safe '" + key + "': " + e.getMessage());
            }
        }

        plugin.getLogger().info("[kmCore][SafeManager] Loaded " + loaded + " safe(s).");
    }

    public void save(File dataFolder) {
        dataFolder.mkdirs();
        File              file = new File(dataFolder, "safes.yml");
        YamlConfiguration yaml = new YamlConfiguration();

        for (SafeData data : safes.values()) {
            String base = "safes." + data.getSafeId();
            yaml.set(base + ".owner", data.getOwnerUuid().toString());

            ItemStack[] contents = data.getContents();
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null && contents[i].getType() != Material.AIR) {
                    yaml.set(base + ".contents." + i,
                            Base64.getEncoder().encodeToString(contents[i].serializeAsBytes()));
                }
            }
        }

        try {
            yaml.save(file);
            plugin.getLogger().info("[kmCore][SafeManager] Saved " + safes.size() + " safe(s).");
        } catch (IOException e) {
            plugin.getLogger().severe("[kmCore][SafeManager] Failed to save safes: " + e.getMessage());
        }
    }
}