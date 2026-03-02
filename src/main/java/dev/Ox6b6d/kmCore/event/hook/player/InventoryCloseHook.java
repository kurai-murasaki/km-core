package dev.Ox6b6d.kmCore.event.hook.player;

import dev.Ox6b6d.kmCore.event.hook.Hook;
import org.bukkit.event.inventory.InventoryCloseEvent;

public interface InventoryCloseHook extends Hook {
    void onInventoryClose(InventoryCloseEvent event);
}