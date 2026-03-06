package dev.Ox6b6d.kmCore.event.hook.player;

import dev.Ox6b6d.kmCore.event.hook.Hook;
import org.bukkit.event.player.PlayerInteractEntityEvent;

/**
 * Hook fired when a player right-clicks an entity.
 * <p>
 * Used by {@code DownedModule} to detect right-clicking a downed player
 * in order to begin carrying them.
 */
public interface PlayerInteractEntityHook extends Hook {

    /**
     * Called when a player right-clicks an entity.
     *
     * @param event the Bukkit {@link PlayerInteractEntityEvent}; may be cancelled
     */
    void onPlayerInteractEntity(PlayerInteractEntityEvent event);
}