package dev.Ox6b6d.kmCore.event.hook.player;

import dev.Ox6b6d.kmCore.event.hook.Hook;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Hook fired when a player dies.
 * <p>
 * Used by {@code DownedModule} to intercept deaths caused by bleedout expiry
 * and apply per-slot random item-drop logic instead of dropping the full inventory.
 */
public interface PlayerDeathHook extends Hook {

    /**
     * Called when a player dies.
     *
     * @param event the Bukkit {@link PlayerDeathEvent}; the drops list may be modified
     */
    void onPlayerDeath(PlayerDeathEvent event);
}