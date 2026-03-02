package dev.Ox6b6d.kmCore.event.hook.player;

import dev.Ox6b6d.kmCore.event.hook.Hook;
import org.bukkit.event.inventory.InventoryClickEvent;

public interface InventoryClickHook extends Hook {
    void onInventoryClick(InventoryClickEvent event);
}