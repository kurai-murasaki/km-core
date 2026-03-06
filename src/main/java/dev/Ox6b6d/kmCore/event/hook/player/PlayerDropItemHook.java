package dev.Ox6b6d.kmCore.event.hook.player;

import dev.Ox6b6d.kmCore.event.hook.Hook;
import org.bukkit.event.player.PlayerDropItemEvent;

/**
 * Hook fired when a player drops an item from their inventory.
 * <p>
 * Used by {@code DownedModule} to prevent downed players from throwing items
 * on the ground.
 */
public interface PlayerDropItemHook extends Hook {

    /**
     * Called when a player drops an item.
     *
     * @param event the Bukkit {@link PlayerDropItemEvent}; may be cancelled
     */
    void onPlayerDropItem(PlayerDropItemEvent event);
}